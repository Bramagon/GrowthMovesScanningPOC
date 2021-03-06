package com.growthmoves.bluetoothmanager.Logic;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Parcel;
import android.os.ParcelUuid;
import android.util.ArrayMap;
import android.util.Log;

import com.growthmoves.bluetoothmanager.BluetoothPlugin;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class BluetoothLogic {

    private final BluetoothManager manager;
    private final Integer defaultTxPowerValue = -65;
    private int pingCounter = 0;
    private String uniqueID = null;
    private static final Map<Integer, Integer> txPowerMap = new ArrayMap<>();
    public static final Map<String, DeviceContainer> btDevices = new ArrayMap<>();

    private Runnable updateRateResetter;
    private final SignalStrength signalStrength = new SignalStrength();
    private final AdvertiseCallback advertiseCallback = new AdvertiseCallback() {

        @Override
        public void onStartSuccess(AdvertiseSettings settingsInEffect) {
            super.onStartSuccess(settingsInEffect);
            System.out.println("Started advertising");
        }

        @Override
        public void onStartFailure(int errorCode) {
            Log.e( "BLE", "Advertising onStartFailure: " + errorCode );
            super.onStartFailure(errorCode);
        }
    };


    public String getDeviceByID(String id) {
        if (!manager.getAdapter().isDiscovering()) {
            discoverDevices();
        }

        StringBuilder connectionsString = new StringBuilder();

        DeviceContainer container = null;

        for (DeviceContainer c : btDevices.values()) {
            System.out.println("SEARCHINGDEVICE: " + c.device.getName() + " , UUIDS: " + c.uuids);
            List<ParcelUuid> uuids = c.uuids;
            if (uuids != null) {
                for (ParcelUuid uuid : uuids) {
                    if (uuid.getUuid() != null && uuid.getUuid().toString().equals(id)) {
                        container = c;
                    }
                }
            }
        }

        String deviceName = null;
        if (container != null) {
            deviceName = container.device.getName();
        } else {
            return "{}";
        }

        if (deviceName == null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                deviceName = container.device.getAlias();
            }
        }
        Sendable sendable = new Sendable(deviceName, container.uuids.get(0).toString(), container);

        Double value = round(signalStrength.CalculateAverageDistance(sendable.container), 3);
        connectionsString.append("{\"name\":\"").append(sendable.name).append("\", \"address\": \"").append(sendable.container.device.getAddress()).append("\", \"distance\": \"").append(value).append("\", \"accurate\": \"").append(sendable.container.accurate).append("\", \"updateRate\": \"").append(1/sendable.container.updateRate).append("\"}");
        return connectionsString.toString();
    }

    static class DeviceContainer {
        public List<Double> distanceMeasurements = new ArrayList<>();
        public BluetoothDevice device;

        public int previousUpdates = 0;
        public float updateRate = 0;
        public boolean accurate = true;
        public List<ParcelUuid> uuids;
    }

    static class Sendable {
        public String name;
        public DeviceContainer container;
        public String uuid;

        public Sendable(String deviceName, String uuidValue, DeviceContainer containerObject) {
            name = deviceName;
            container = containerObject;
            uuid = uuidValue;
        }
    }

    public BluetoothLogic() {
        manager = BluetoothPlugin.manager;
        preInitializeTxPowerMap();

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        BluetoothPlugin.getAppContext().registerReceiver(receiver, filter);
    }

    public String getDiscoveredDevices() {
        if (!manager.getAdapter().isDiscovering()) {
            discoverDevices();
        }
        startAdvertising();
        return constructConnectionsJsonString();
    }

    public boolean getBluetoothState() {
        return manager.getAdapter().getState() == BluetoothAdapter.STATE_ON;
    }

    private String constructConnectionsJsonString() {
        StringBuilder connectionsString = new StringBuilder();

        connectionsString.append("{\"").append("connections\": [");

        List<Sendable> toSend = new ArrayList<>();

        for (Map.Entry<String, DeviceContainer> entry : btDevices.entrySet()) {
            String deviceName = entry.getValue().device.getName();
            String uuid = entry.getValue().uuids.get(0).toString();
            if (deviceName == null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    deviceName = entry.getValue().device.getAlias();
                }
                if (deviceName == null) continue;
            }
            toSend.add(new Sendable(deviceName, uuid, entry.getValue()));
        }

        int count = 0;
        for (Sendable sendable : toSend) {
            count++;

            Double value = round(signalStrength.CalculateAverageDistance(sendable.container), 3);
            connectionsString.append("{\"name\":\"").append(sendable.name).append("\", \"address\": \"").append(sendable.container.device.getAddress()).append("\", \"distance\": \"").append(value).append("\", \"accurate\": \"").append(sendable.container.accurate).append("\", \"updateRate\": \"").append(1/sendable.container.updateRate).append("\", \"uuid\": \"").append(sendable.uuid).append("\"}");
            if (count != toSend.size()) connectionsString.append(",");

        }

        connectionsString.append("]}");

        System.out.println(connectionsString);

        return connectionsString.toString();
    }

    private void preInitializeTxPowerMap() {
        int [][] initializer =
                {
                        {-30, -115}, {-20, -84}, {-16, -81}, {-12, -77}, {-8, -72}, {-4, -69}, {0, -65},
                        {4, -59}, {0, -115}, {1, -84}, {2, -81}, {3, -77}, {4, -72}, {5, -69}, {6, -65}, {7, -59}
                };

        for (int[] ints : initializer) {
            txPowerMap.put(ints[0], ints[1]);
        }
    }

    private static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();
        if (value == Double.MIN_VALUE || Double.isNaN(value)) {
            return Double.NaN;
        }
        System.out.println("Rounding value: " + value);
            BigDecimal bd = BigDecimal.valueOf(value);
            bd = bd.setScale(places, RoundingMode.HALF_UP);
            return bd.doubleValue();
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

    private final ScanCallback leReceiver = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);

            BluetoothDevice device = result.getDevice();
            String deviceName = device.getName();
            List<ParcelUuid> Uuids = result.getScanRecord().getServiceUuids();

            int rssi = result.getRssi();
            boolean accurate = true;
            Integer txPower = tryGetTxValue(result);

            if (txPower == null) {
                txPower = defaultTxPowerValue;
                accurate = false;
            }

            double distance = signalStrength.CalcDistance(rssi, txPower);

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
                deviceContainer.updateRate++;

                deviceContainer.uuids = Uuids;

                if (deviceContainer.distanceMeasurements.size() > 10) {
                    deviceContainer.distanceMeasurements.subList(0, deviceContainer.distanceMeasurements.size() - 10).clear();
                }

                btDevices.put(device.getAddress(), deviceContainer);
            }
            System.out.println(deviceName + " " + device.getAddress() + " distance: " + distance + " RSSI: " + rssi + " txPower " + txPower + " Accurate " + accurate);
            System.out.println("scanRecordTXPOWER: " + result.getScanRecord().getTxPowerLevel());
            System.out.println("normalTXPOWER:" + result.getTxPower());
            startAdvertising();
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);

            System.out.println("BLE Scan failed! " + errorCode);
        }

    };

    private Integer tryGetTxValue(ScanResult result) {

        Integer txPower = result.getTxPower();

        if (txPower == ScanResult.TX_POWER_NOT_PRESENT) {
            int transmissionPower = result.getScanRecord().getTxPowerLevel();

            if (transmissionPower != Integer.MIN_VALUE) {
                txPower = txPowerMap.get(transmissionPower);
            } else {
                System.out.println(result.getScanRecord().getManufacturerSpecificData());
                txPower = null;
            }
        }

        return txPower;
    }

    private Integer tryReadTxValueFromByteArray(byte[] byteArray) {
        Integer txPower = null;
        txPower = signalStrength.getTxPowerFromByteArray(byteArray);
        return txPower;
    }

    private void discoverDevices() {

        manager.getAdapter().startDiscovery();


        List<ScanFilter> filters = new ArrayList<>();
        ScanFilter.Builder scanFilterBuilder = new ScanFilter.Builder();
        filters.add(scanFilterBuilder.build());

        ScanSettings.Builder settingsBuilder = new ScanSettings.Builder();
        settingsBuilder.setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY);
        settingsBuilder.setMatchMode(ScanSettings.MATCH_MODE_STICKY);
        settingsBuilder.setReportDelay(0);

        manager.getAdapter().getBluetoothLeScanner().startScan(filters, settingsBuilder.build(), leReceiver);


        //StartUpdateTracking();
    }

    public String startAdvertising() {

        if (uniqueID == null) {
            uniqueID = UUID.randomUUID().toString();
        }

        System.out.println("Started advertising with UUID: " + uniqueID);

        AdvertiseData.Builder advertiseBuilder = new AdvertiseData.Builder();
        advertiseBuilder.setIncludeTxPowerLevel(true);
        advertiseBuilder.setIncludeDeviceName(true);

        ParcelUuid pUuid = new ParcelUuid(UUID.fromString(uniqueID));

        advertiseBuilder.addServiceUuid(pUuid);
        advertiseBuilder.addServiceData( pUuid, "Data".getBytes(StandardCharsets.UTF_8) );

        AdvertiseSettings.Builder advertiseSettingsBuilder = new AdvertiseSettings.Builder();
        advertiseSettingsBuilder.setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY);
        advertiseSettingsBuilder.setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH);

        advertiseSettingsBuilder.setConnectable(false);

        System.out.println("BluetoothLE advertising support: " +  manager.getAdapter().isMultipleAdvertisementSupported());
        manager.getAdapter().getBluetoothLeAdvertiser().startAdvertising(advertiseSettingsBuilder.build(), advertiseBuilder.build(), advertiseCallback);

        return uniqueID;
    }

    private void StartUpdateTracking() {
        if (updateRateResetter == null) {

            int period = 1;
            BluetoothPinging pinging = new BluetoothPinging();
            updateRateResetter = () -> {

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
            };

            ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
            executor.scheduleAtFixedRate(updateRateResetter, 0, period, TimeUnit.SECONDS);
        }
    }
}
