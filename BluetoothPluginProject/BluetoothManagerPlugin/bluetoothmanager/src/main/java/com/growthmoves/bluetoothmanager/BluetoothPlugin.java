package com.growthmoves.bluetoothmanager;
import android.app.Activity;
import android.app.Application;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.StrictMode;
import android.util.Log;
import androidx.annotation.Keep;
import com.growthmoves.bluetoothmanager.Logic.BluetoothLogic;
import com.growthmoves.bluetoothmanager.Logic.PermissionsActivity;

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

        Intent intent = new Intent(context, PermissionsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);

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

    public String getDiscoveredBluetoothDeviceByAddress(String address) {
        if (logic == null) logic = new BluetoothLogic();
        return logic.getDeviceByAddress(address);
    }

    public static Context getAppContext() {
        return BluetoothPlugin.context;
    }



}
