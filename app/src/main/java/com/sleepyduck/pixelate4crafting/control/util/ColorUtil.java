package com.sleepyduck.pixelate4crafting.control.util;

import android.support.v4.graphics.ColorUtils;

import java.util.Map;

/**
 * Created by fredrikmetcalf on 13/04/16.
 */
public class ColorUtil {
    public static final int ALPHA_CHANNEL = 0xff000000;

    private static double[] LabLeft = new double[3];
    private static double[] LabRight = new double[3];

    public static double Diff(int left, int right) {
        ColorUtils.colorToLAB(left, LabLeft);
        ColorUtils.colorToLAB(right, LabRight);
        return ColorUtils.distanceEuclidean(LabLeft, LabRight);
    }

    public static Map.Entry<Integer, Float> getBestColorFor(int pixel, Map<Integer, Float> colors) {
        double diff, minDiff = Integer.MAX_VALUE;
        Map.Entry<Integer, Float> bestColor = null;
        for (Map.Entry<Integer, Float> color : colors.entrySet()) {
            diff = ColorUtil.Diff(color.getKey(), pixel);
            if (diff < minDiff) {
                minDiff = diff;
                bestColor = color;
            }
        }
        return bestColor;
    }
}
