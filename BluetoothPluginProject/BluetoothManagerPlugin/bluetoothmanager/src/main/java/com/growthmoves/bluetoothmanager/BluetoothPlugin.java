package com.growthmoves.bluetoothmanager;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import androidx.annotation.Keep;

import com.growthmoves.bluetoothmanager.Logic.BluetoothLogic;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

@Keep
public class BluetoothPlugin extends Application {
    private static BluetoothPlugin instance = new BluetoothPlugin();
    private static final String LOGTAG = "Growthmoves";
    private static BluetoothLogic logic;
    private static Context context;

    public static BluetoothPlugin getInstance() { return instance; }

    private long startTime;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(LOGTAG, "Created BluetoothPlugin");

        BluetoothPlugin.context = getApplicationContext();
    }


    public String getDiscoveredBluetoothDevices() throws JSONException {
        if (logic == null) logic = new BluetoothLogic();

        return logic.getDiscoveredDevices();
    }

    public double getElapsedTime() {
        if (logic == null) logic = new BluetoothLogic();
        return (System.currentTimeMillis()-startTime)/1000f;
    }

    public boolean getBluetoothEnabled() {
        if (logic == null) logic = new BluetoothLogic();
        return logic.getBluetoothState();
    }

    public static Context getAppContext() {
        return BluetoothPlugin.context;
    }



}
