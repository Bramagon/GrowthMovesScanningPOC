package com.growthmoves.bluetoothmanager.Logic;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.R.string;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.material.snackbar.Snackbar;
import com.growthmoves.bluetoothmanager.R;

public class PermissionsActivity extends Activity {


    private static final int ANDROID_PERMISSION_BLUETOOTH_ADVERTISE = 255;
    private View mLayout;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case ANDROID_PERMISSION_BLUETOOTH_ADVERTISE: {
                for (int result : grantResults) {
                    if (result == PackageManager.PERMISSION_GRANTED) {
                        System.out.println(result + " Permission to advertise granted.");
                    }
                    else
                    {
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
        mLayout = findViewById(R.id.layout);
        System.out.println("Started permissions activity");
        shouldShowRequestPermissionRationale(Manifest.permission.BLUETOOTH_ADMIN);

        Snackbar.make(mLayout, R.string.appbar_scrolling_view_behavior,
                Snackbar.LENGTH_INDEFINITE).setAction("OK", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Request the permission
                ActivityCompat.requestPermissions(PermissionsActivity.this, new String[] { Manifest.permission.BLUETOOTH, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.BLUETOOTH_ADVERTISE}, ANDROID_PERMISSION_BLUETOOTH_ADVERTISE);

            }
        }).show();

    }
}
