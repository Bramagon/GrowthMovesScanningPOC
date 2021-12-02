package com.growthmoves.bluetoothmanager.Logic;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.ArrayMap;

import androidx.annotation.Nullable;

import com.growthmoves.bluetoothmanager.BluetoothPlugin;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Map;


public class BluetoothLogic {

    private final BluetoothManager manager;

    private final Map<String, Short> btDevices = new ArrayMap<>();

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)){
                short rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI,Short.MIN_VALUE);
                String name = intent.getStringExtra(BluetoothDevice.EXTRA_NAME);

                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                System.out.println("BluetoothDevice: " + device.getName() + ", " + device.getUuids() + ", " + device.getBluetoothClass() );
                btDevices.put(name, rssi);
                System.out.println("Device: " + name + " was found!");

            }
        }
    };

    public BluetoothLogic() {
        manager = (BluetoothManager) BluetoothPlugin.getAppContext().getSystemService(Context.BLUETOOTH_SERVICE);
    }

    public boolean getBluetoothState() {
        return manager.getAdapter().getState() == BluetoothAdapter.STATE_ON;
    }

    private void discoverDevices() {
        manager.getAdapter().startDiscovery();
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        BluetoothPlugin.getAppContext().registerReceiver(receiver, filter);

    }



    public String getDiscoveredDevices() {
        if (!manager.getAdapter().isDiscovering()) discoverDevices();

        StringBuilder connectionsString = new StringBuilder();

        connectionsString.append("{\"").append("connections\": [");

        for (Map.Entry<String, Short> entry : btDevices.entrySet()) {
            String key = entry.getKey();
            Short value = entry.getValue();

            connectionsString.append("{\"name\":\"").append(key).append("\", \"rssi\": \"").append(value).append("\"}");
        }

        connectionsString.append("]}");


        System.out.println(connectionsString);


        return connectionsString.toString();
    }


}
