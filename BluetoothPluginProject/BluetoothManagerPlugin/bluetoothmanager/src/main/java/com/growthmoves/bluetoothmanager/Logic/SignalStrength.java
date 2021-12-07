package com.growthmoves.bluetoothmanager.Logic;

import android.bluetooth.BluetoothDevice;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class SignalStrength {

    public double CalcDistance(int rssi, int txPower) {
        return Math.pow(10d, ((double) txPower - rssi) / (10 * 2));
    }

    public double CalculateAverageDistance(BluetoothLogic.DeviceContainer previousMeasurements) {
        if (previousMeasurements != null) {
            List<Double> allPreviousMeasurements = previousMeasurements.distanceMeasurements;

            double totalValue = 0;
            int maxAveragingAmount = 10;

            int count = allPreviousMeasurements.size()-1;
            if (count > maxAveragingAmount) count = maxAveragingAmount;

            for (int i = count; i > 0; i--) {
                totalValue += allPreviousMeasurements.get(i);
            }

            return totalValue/(count);

        } else return 0;
    }

    public Integer getTxPowerFromByteArray(final byte[] scanData) {
        Integer txPower = null;


            System.out.println("iBeacon Packet: " + bytesToHexString(scanData));
            UUID uuid = getGuidFromByteArray(Arrays.copyOfRange(scanData, 9, 25));
            int major = (scanData[25] & 0xff) * 0x100 + (scanData[26] & 0xff);
            int minor = (scanData[27] & 0xff) * 0x100 + (scanData[28] & 0xff);
            byte txpw = scanData[29];
            System.out.println(" - iBeacon Major = " + major + " | Minor = " + minor + " TxPw " + (int)txpw + " | UUID = " + uuid.toString());

            txPower = (int)txpw;


        return txPower;
    }


public static String bytesToHexString(byte[] bytes) {
        StringBuilder buffer = new StringBuilder();
        for(int i=0; i<bytes.length; i++) {
        buffer.append(String.format("%02x", bytes[i]));
        }
        return buffer.toString();
        }
public static UUID getGuidFromByteArray(byte[] bytes)
        {
        ByteBuffer bb = ByteBuffer.wrap(bytes);
        UUID uuid = new UUID(bb.getLong(), bb.getLong());
        return uuid;
        }
}
