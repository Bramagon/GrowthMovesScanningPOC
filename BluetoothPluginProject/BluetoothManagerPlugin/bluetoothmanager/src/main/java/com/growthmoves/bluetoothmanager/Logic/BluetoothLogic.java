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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class BluetoothLogic {

    private final BluetoothManager manager;

    public static final Map<String, DeviceContainer> btDevices = new ArrayMap<>();
    private static final Map<Integer, Integer> txPowerMap = new ArrayMap<>();

    private final SignalStrength signalStrength = new SignalStrength();

    private int pingCounter = 0;

    private Runnable updateRateResetter;

    static class DeviceContainer {
        public List<Float> distanceMeasurements = new ArrayList<>();
        public BluetoothDevice device;
        public int previousUpdates = 0;
        public float updateRate = 0;
        public boolean accurate = true;
    }

    static class Sendable {
        public String name;
        public DeviceContainer container;

        public Sendable(String deviceName, DeviceContainer containerObject) {
            name = deviceName;
            container = containerObject;
        }
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

    private void preInitializeTxPowerMap() {
        int [][] initializer =
                {{-30, -115}, {-20, -84}, {-16, -81}, {-12, -77}, {-8, -72}, {-4, -69}, {0, -65}, {4, -59},
                        {0, -115}, {1, -84}, {2, -81}, {3, -77}, {4, -72}, {5, -69}, {6, -65}, {7, -59}};

        for (int[] ints : initializer) {
            txPowerMap.put(ints[0], ints[1]);
        }

    }

    private final ScanCallback leReceiver = new ScanCallback() {

        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);

            BluetoothDevice device = result.getDevice();

            String deviceName = device.getName();

            int rssi = result.getRssi();
            int transmissionPower = result.getScanRecord().getTxPowerLevel();
            boolean accurate = true;

            Integer defaultTxPower = null;

            if (transmissionPower != Integer.MIN_VALUE) {
                defaultTxPower = txPowerMap.getOrDefault(transmissionPower, -65);
            }

            if (defaultTxPower == null) {
                accurate = false;
                defaultTxPower = -65;
            }

            int txPower = result.getTxPower() != ScanResult.TX_POWER_NOT_PRESENT ? result.getTxPower() : defaultTxPower;


            float distance = (float)round(signalStrength.CalcDistance(rssi, txPower), 2);
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
                deviceContainer.accurate = accurate;

                if (deviceContainer.distanceMeasurements.size() > 10) {
                    deviceContainer.distanceMeasurements.subList(0, deviceContainer.distanceMeasurements.size() - 10).clear();
                }

                btDevices.put(device.getAddress(), deviceContainer);
            }

            System.out.println("scanRecordTXPOWER: " + result.getScanRecord().getTxPowerLevel());
            System.out.println("normalTXPOWER:" + result.getTxPower());
        }
    };

    public BluetoothLogic() {
        manager = BluetoothPlugin.manager;
        preInitializeTxPowerMap();
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

        List<Sendable> toSend = new ArrayList<>();

        for (Map.Entry<String, DeviceContainer> entry : btDevices.entrySet()) {
            String deviceName = entry.getValue().device.getName();
            if (deviceName == null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    deviceName = entry.getValue().device.getAlias();
                }
                if (deviceName == null) continue;
            }
            toSend.add(new Sendable(deviceName, entry.getValue()));
        }

        int count = 0;
        for (Sendable sendable : toSend) {
            count++;
            Double value = signalStrength.CalculateAverageDistance(sendable.container);
            connectionsString.append("{\"name\":\"").append(sendable.name).append("\", \"distance\": \"").append(value).append("\", \"updateRate\": \"").append(sendable.container.updateRate).append("\"}");
            if (count != toSend.size()) connectionsString.append(",");
        }

        connectionsString.append("]}");


        System.out.println(connectionsString);


        return connectionsString.toString();
    }

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    private void StartUpdateTracking() {
        if (updateRateResetter == null) {
            int period = 1;
            BluetoothPinging pinging = new BluetoothPinging();
            updateRateResetter = new Runnable() {
                public void run() {
                    pingCounter++;
                    for (Map.Entry<String, DeviceContainer> entry : btDevices.entrySet()) {
                        pinging.pingDevice(entry.getValue().device.getAddress());

                        if (pingCounter > 5) {
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
            executor.scheduleAtFixedRate(updateRateResetter, 0, period, TimeUnit.SECONDS);

        }
    }

}
