package com.growthmoves.bluetoothmanager.Logic;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.util.ArrayMap;

import com.growthmoves.bluetoothmanager.BluetoothPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class BluetoothLogic {

    private final BluetoothManager manager;

    public static final Map<String, DeviceContainer> btDevices = new ArrayMap<>();

    private final SignalStrength signalStrength = new SignalStrength();

    private final int defaultTxPower = -65;

    private int pingCounter = 0;

    private Runnable updateRateResetter;

    static class DeviceContainer {
        public List<Double> distanceMeasurements = new ArrayList<>();
        public BluetoothDevice device;
        public String name;
        public int previousUpdates = 0;
        public float updateRate = 0;
    }

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)){
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                System.out.println("Normal scan found " + device.getName() + " Address: " + device.getAddress());
                if (btDevices.containsKey(device.getAddress())) {
                    DeviceContainer deviceContainer = btDevices.get(device.getAddress());
                    if (deviceContainer != null) {
                        deviceContainer.device = device;
                        btDevices.put(device.getAddress(), deviceContainer);
                    }
                } else {
                    DeviceContainer container = new DeviceContainer();
                    container.device = device;
                    btDevices.put(device.getAddress(), container);
                }
            }
        }

    };

    private void StartUpdateTracking() {
        if (updateRateResetter == null) {
            int period = 100;
            BluetoothPinging pinging = new BluetoothPinging();
            updateRateResetter = new Runnable() {
            public void run() {
                pingCounter++;
                for (Map.Entry<String, DeviceContainer> entry : btDevices.entrySet()) {
                    pinging.pingDevice(entry.getValue().device.getAddress());

                    if (pingCounter > 10) {
                        entry.getValue().updateRate = (float) entry.getValue().previousUpdates / (float) pingCounter;
                        if (pingCounter > 100) {
                            entry.getValue().previousUpdates = 0;
                            pingCounter = 0;
                            }
                        }
                    }

                }

        };

        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        executor.scheduleAtFixedRate(updateRateResetter, 0, period, TimeUnit.MILLISECONDS);

        }
    }

    private final ScanCallback leReceiver = new ScanCallback() {

        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);

            BluetoothDevice device = result.getDevice();

            String deviceName = device.getName();

            long actualTime = System.currentTimeMillis() - TimeUnit.MILLISECONDS.convert(result.getTimestampNanos(), TimeUnit.NANOSECONDS);
            int rssi = result.getRssi();
            int txPower = result.getTxPower() != ScanResult.TX_POWER_NOT_PRESENT ? result.getTxPower() : defaultTxPower;

            double distance = signalStrength.CalcDistance(rssi, txPower);
            System.out.println(deviceName + " distance: " + distance + " RSSI: " + rssi + " txPower " + txPower);

            DeviceContainer deviceContainer;
            if (btDevices.containsKey(device.getAddress())) {
                deviceContainer = btDevices.get(device.getAddress());

            } else {
                deviceContainer = new DeviceContainer();
                deviceContainer.device = device;
            }

            if (deviceContainer != null) {
                deviceContainer.distanceMeasurements.add(distance);

                if (deviceContainer.distanceMeasurements.size() > 10) {
                    deviceContainer.distanceMeasurements.subList(0, deviceContainer.distanceMeasurements.size() - 10).clear();
                }

                btDevices.put(device.getAddress(), deviceContainer);
            }

            System.out.println("Device: " + deviceName + " was found at: " + actualTime + "!");
        }
    };

    public BluetoothLogic() {
        manager = BluetoothPlugin.manager;
    }

    public boolean getBluetoothState() {
        return manager.getAdapter().getState() == BluetoothAdapter.STATE_ON;
    }


    private void discoverDevices() {
        manager.getAdapter().startDiscovery();

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);

        BluetoothPlugin.getAppContext().registerReceiver(receiver, filter);

        List<ScanFilter> filters = new ArrayList<>();
        ScanFilter.Builder scanFilterBuilder = new ScanFilter.Builder();

        filters.add(scanFilterBuilder.build());

        ScanSettings.Builder settingsBuilder = new ScanSettings.Builder();

        settingsBuilder.setScanMode(ScanSettings.SCAN_MODE_OPPORTUNISTIC);
        settingsBuilder.setMatchMode(ScanSettings.MATCH_MODE_AGGRESSIVE);

        settingsBuilder.setReportDelay(0);


        manager.getAdapter().getBluetoothLeScanner().startScan(filters, settingsBuilder.build(), leReceiver);


        StartUpdateTracking();
    }

    public String getDiscoveredDevices() {
        if (!manager.getAdapter().isDiscovering()) discoverDevices();

        StringBuilder connectionsString = new StringBuilder();

        connectionsString.append("{\"").append("connections\": [");

        int count = 0;

        for (Map.Entry<String, DeviceContainer> entry : btDevices.entrySet()) {
            count++;

            String deviceName = entry.getValue().name;
            BluetoothDevice device = entry.getValue().device;

            if (deviceName == null) {
                deviceName = device.getName() == null ? device.getAddress() : device.getName();

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    deviceName = device.getAlias() == null ? device.getAddress() : device.getAlias();
                }
            }
            Double value = signalStrength.CalculateAverageDistance(entry.getValue());

            connectionsString.append("{\"name\":\"").append(deviceName).append("\", \"distance\": \"").append(value).append("\", \"updateRate\": \"").append(entry.getValue().updateRate).append("\"}");
            if (count != btDevices.size()) connectionsString.append(",");
        }

        connectionsString.append("]}");


        System.out.println(connectionsString);


        return connectionsString.toString();
    }


}
