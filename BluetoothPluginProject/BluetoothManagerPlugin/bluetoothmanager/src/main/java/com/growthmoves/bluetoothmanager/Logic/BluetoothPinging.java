package com.growthmoves.bluetoothmanager.Logic;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Parcelable;
import android.util.ArrayMap;

import com.growthmoves.bluetoothmanager.BluetoothPlugin;

import java.util.Map;

public class BluetoothPinging {
    private Parcelable deviceExtra;

    private final Map<String, PingingObject> pings = new ArrayMap<>();

    static class PingingObject {

        public boolean isValid;
        public PingingObject(boolean validity) {

            isValid = validity;
        }
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {


            BluetoothDevice device = (BluetoothDevice) intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

            String address = device.getAddress();


            if (pings.containsKey(address)) {
                PingingObject pingObject = pings.get(address);

                if (pingObject != null && pingObject.isValid) {

                    BluetoothLogic.DeviceContainer container = BluetoothLogic.btDevices.get(address);
                    if (container != null) container.updateRate++;
                    BluetoothLogic.btDevices.put(address, container);

                    System.out.println("PINGING: " + address);
/*                    double timeTaken = time - pingObject.pingTimeNanos;
                    double distance = calcDistance(timeTaken);*/

/*                    System.out.println("TESTING, ADDRESS: " + address + " TIME: " + timeTaken + " DISTANCE: " + distance);*/


                }
            }
        }
    };

    public float calcDistance(double nanoseconds) {
        double MetersTraveledInOneNs = 0.299792458f;
        return (float) (nanoseconds * MetersTraveledInOneNs);
    }

    public BluetoothPinging() {
        String action = "android.bluetooth.device.action.UUID";
        IntentFilter filter = new IntentFilter(action);

        BluetoothPlugin.getAppContext().registerReceiver(mReceiver, filter);
    }

    public void pingDevice(String address) {
        BluetoothDevice bd = BluetoothPlugin.manager.getAdapter().getRemoteDevice(address);

/*        double pingTime = System.nanoTime();*/
        boolean isValid = bd.fetchUuidsWithSdp();

        pings.put(address, new PingingObject(isValid));

    }




    public boolean checkValidity(double nanoseconds) {
        double nanosecondsToTravelCm = 1000d; // if ping took longer than 1000 nanoseconds, distance would be over 30m, these distances are invalid.
        return !(nanoseconds > nanosecondsToTravelCm);
    }

}
