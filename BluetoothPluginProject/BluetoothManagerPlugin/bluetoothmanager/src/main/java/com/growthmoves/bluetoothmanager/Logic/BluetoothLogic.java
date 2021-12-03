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
                String name = intent.getStringExtra(BluetoothDevice.EXTRA_NAME);
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                if (btDevices.containsKey(device.getAddress())) {
                    DeviceContainer deviceContainer = btDevices.get(device.getAddress());
                    if (deviceContainer != null) {
                        deviceContainer.name = name;
                        btDevices.put(device.getAddress(), deviceContainer);
                    }
                }
            }
        }
    };

    private void StartUpdateTracking() {
        if (updateRateResetter == null) {
            int period = 3;
            BluetoothPinging pinging = new BluetoothPinging();
            updateRateResetter = new Runnable() {
            public void run() {
                for (Map.Entry<String, DeviceContainer> entry : btDevices.entrySet()) {
                    entry.getValue().updateRate = (float)entry.getValue().previousUpdates/(float)period;
                    entry.getValue().previousUpdates = 0;

                    pinging.pingDevice(entry.getValue().device.getAddress());
                }
            }
        };

        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        executor.scheduleAtFixedRate(updateRateResetter, 0, period, TimeUnit.SECONDS);

        }
    }

    private final ScanCallback leReceiver = new ScanCallback() {

        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);

            BluetoothDevice device = result.getDevice();

            String deviceName = device.getName();

            if (deviceName == null) return;

            long actualTime = System.currentTimeMillis() - TimeUnit.MILLISECONDS.convert(result.getTimestampNanos(), TimeUnit.NANOSECONDS);
            int rssi = result.getRssi();
            int txPower = result.getTxPower();
            double distance = signalStrength.CalcDistance(rssi, txPower);

            DeviceContainer deviceContainer;
            if (btDevices.containsKey(device.getAddress())) {
                deviceContainer = btDevices.get(device.getAddress());

            } else {
                deviceContainer = new DeviceContainer();
                deviceContainer.distanceMeasurements.add(distance);
            }

            if (deviceContainer != null) {
                deviceContainer.distanceMeasurements.add(distance);
                deviceContainer.device = device;
                deviceContainer.name = deviceName;
                deviceContainer.previousUpdates++;

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
        /*manager.getAdapter().startDiscovery();
*/
        /*IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);


        BluetoothPlugin.getAppContext().registerReceiver(receiver, filter);*/

        List<ScanFilter> filters = new ArrayList<>();
        ScanFilter.Builder scanFilterBuilder = new ScanFilter.Builder();

        filters.add(scanFilterBuilder.build());

        ScanSettings.Builder settingsBuilder = new ScanSettings.Builder();

        settingsBuilder.setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY);
        settingsBuilder.setMatchMode(ScanSettings.MATCH_MODE_STICKY);

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

            String name = entry.getValue().name;


            Double value = signalStrength.CalculateAverageDistance(entry.getValue());

            connectionsString.append("{\"name\":\"").append(name).append("\", \"distance\": \"").append(value).append("\", \"updateRate\": \"").append(entry.getValue().updateRate).append("\"}");
            if (count != btDevices.size()) connectionsString.append(",");
        }

        connectionsString.append("]}");


        System.out.println(connectionsString);


        return connectionsString.toString();
    }

}
