package com.robotemplates.webviewapp.utility;

/**
 * Created by Manda on 11/08/2017.
 */

public class DistanceUtil {
    public static double calculateDistance(double levelInDb, double freqInMHz) {
        return Math.pow(10.0d, ((27.55d - (Math.log10(freqInMHz) * 20.0d)) + Math.abs(levelInDb)) / 20.0d);
    }
}
