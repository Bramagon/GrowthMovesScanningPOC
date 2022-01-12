package com.growthmoves.bluetoothmanager.Logic;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.growthmoves.bluetoothmanager.BluetoothPlugin;


public class PermissionsActivity extends Activity {


    private static final int ANDROID_PERMISSION_BLUETOOTH_ADVERTISE = 255;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case ANDROID_PERMISSION_BLUETOOTH_ADVERTISE: {
                for (int result : grantResults) {
                    if (result == PackageManager.PERMISSION_GRANTED) {
                        System.out.println(result + " Permission to advertise granted.");
                    } else {
                        System.out.println(result + " Permission to advertise not granted.");
                    }
                }
                finish();


            }

        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        System.out.println("Oncreate permissions activity");
    }

    @Override
    protected void onStart() {
        super.onStart();
        System.out.println("Started permissions activity");

        requestPermissions(new String[]{"android.permission.BLUETOOTH_ADVERTISE"}, ANDROID_PERMISSION_BLUETOOTH_ADVERTISE);

        if (!BluetoothPlugin.getInstance().getBluetoothEnabled()) {
            startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), 1);
            Toast.makeText(BluetoothPlugin.getAppContext(), "Bluetooth Turned ON", Toast.LENGTH_SHORT).show();
        }

        if (!BluetoothPlugin.manager.getAdapter().isDiscovering()) {
            startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE), 1);
            Toast.makeText(BluetoothPlugin.getAppContext(), "Making Device Discoverable", Toast.LENGTH_SHORT).show();
        }

    }
}
