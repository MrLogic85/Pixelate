package com.sleepyduck.pixelate4crafting.util;

import android.graphics.Color;
import android.support.v4.graphics.ColorUtils;
import android.util.SparseArray;

import java.nio.DoubleBuffer;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by fredrikmetcalf on 13/04/16.
 */
public class ColorUtil {
    public static final int ALPHA_CHANNEL = 0xff000000;

    private static double[] LabLeft = new double[3];
    private static double[] LabRight = new double[3];
    private static SparseArray<SparseArray<Double>> DiffMap = new SparseArray<>();

    synchronized public static double DiffMap(int left, int right) {
        int _left = Math.max(left, right);
        int _right = Math.max(left, right);
        SparseArray<Double> map = DiffMap.get(_left);
        if (map != null) {
            Double diff = map.get(_right);
            if (diff != null) {
                return diff;
            }
        }
        ColorUtils.colorToLAB(_left, LabLeft);
        ColorUtils.colorToLAB(_right, LabRight);
        double diff = ColorUtils.distanceEuclidean(LabLeft, LabRight);
        if (map == null) {
            map = new SparseArray<>();
            DiffMap.put(_left, map);
        }
        map.put(_right, diff);
        return diff;
    }

    synchronized public static double Diff(int left, int right) {
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

    public static int[] splitColor(int pixel) {
        return new int[] {Color.red(pixel), Color.green(pixel), Color.blue(pixel)};
    }
}
