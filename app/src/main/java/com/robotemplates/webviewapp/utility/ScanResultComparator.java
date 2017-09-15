package com.robotemplates.webviewapp.utility;

import android.net.wifi.ScanResult;

import java.util.Comparator;

import static com.robotemplates.webviewapp.utility.DistanceUtil.calculateDistance;


/**
 * Created by Manda on 11/08/2017.
 */

public class ScanResultComparator implements Comparator<ScanResult> {
    public int compare(ScanResult lhs, ScanResult rhs) {
        return Double.valueOf(calculateDistance((double) lhs.level, (double) lhs.frequency))
                .compareTo(Double.valueOf(calculateDistance((double) rhs.level, (double) rhs.frequency)));
    }

//    @Override
//    public Comparator<ScanResult> reversed() {
//        return null;
//    }
}
