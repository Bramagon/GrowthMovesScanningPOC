package com.growthmoves.bluetoothmanager;

import android.app.Application;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.util.Log;
import androidx.annotation.Keep;
import com.growthmoves.bluetoothmanager.Logic.BluetoothLogic;

@Keep
public class BluetoothPlugin extends Application {
    private static BluetoothPlugin instance = new BluetoothPlugin();
    private static final String LOGTAG = "Growthmoves";
    private static BluetoothLogic logic;
    private static Context context;
    private long startTime;



    public static BluetoothManager manager;
    public static BluetoothPlugin getInstance() { return instance; }

    public BluetoothPlugin() {
        startTime = System.currentTimeMillis();

    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(LOGTAG, "Created BluetoothPlugin");

        BluetoothPlugin.context = getApplicationContext();
        BluetoothPlugin.manager = (BluetoothManager) BluetoothPlugin.getAppContext().getSystemService(Context.BLUETOOTH_SERVICE);
    }


    public String getDiscoveredBluetoothDevices() {
        if (logic == null) logic = new BluetoothLogic();
        return logic.getDiscoveredDevices();
    }

    public int getElapsedTime() {
        return Math.round((System.currentTimeMillis()-startTime)/1000f);
    }

    public boolean getBluetoothEnabled() {
        if (logic == null) logic = new BluetoothLogic();
        return logic.getBluetoothState();
    }

    public static Context getAppContext() {
        return BluetoothPlugin.context;
    }



}
