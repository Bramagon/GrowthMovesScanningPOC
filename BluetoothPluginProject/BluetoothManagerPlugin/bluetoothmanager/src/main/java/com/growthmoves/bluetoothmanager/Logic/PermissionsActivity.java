package com.growthmoves.bluetoothmanager.Logic;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;


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

    }
}
