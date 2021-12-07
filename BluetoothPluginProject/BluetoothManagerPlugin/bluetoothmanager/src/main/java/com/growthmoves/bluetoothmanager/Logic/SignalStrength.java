package com.growthmoves.bluetoothmanager.Logic;

import java.util.List;

public class SignalStrength {

    public double CalcDistance(int rssi, int txPower) {
        return Math.pow(10d, ((double) txPower - rssi) / (10 * 2));
    }

    public double CalculateAverageDistance(BluetoothLogic.DeviceContainer previousMeasurements) {
        if (previousMeasurements != null) {
            List<Float> allPreviousMeasurements = previousMeasurements.distanceMeasurements;

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
}
