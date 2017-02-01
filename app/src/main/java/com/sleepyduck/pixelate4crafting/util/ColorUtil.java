package com.sleepyduck.pixelate4crafting.util;

import android.graphics.Color;
import android.support.v4.graphics.ColorUtils;
import android.util.SparseArray;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by fredrikmetcalf on 13/04/16.
 */
public class ColorUtil {
    public static final int ALPHA_CHANNEL = 0xff000000;

    private static double[] LabLeft = new double[3];
    private static double[] LabRight = new double[3];
    private static SparseArray<SparseArray<Double>> DiffMap = new SparseArray<>();

    synchronized public static double Diff(int left, int right) {
        int _left = Math.max(left, right);
        int _right = Math.min(left, right);
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

    public static int getBestColorFor(int pixel, int[] colors) {
        double diff, minDiff = Integer.MAX_VALUE;
        int bestColor = 0;
        for (int color : colors) {
            diff = ColorUtil.Diff(color, pixel);
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

    public static void Sort(Integer[] colors) {
        final Integer[] sortBy = {
                Color.BLACK,
                Color.WHITE,
                Color.RED,
                Color.BLUE,
                Color.GREEN,
                Color.YELLOW
        };
        Arrays.sort(colors, new Comparator<Integer>() {
            @Override
            public int compare(Integer left, Integer right) {
                return (int) (diff(right) - diff(left));
            }

            private double diff(Integer color) {
                double[] diffs = new double[sortBy.length];
                double smallestDiff = Double.MAX_VALUE;
                int sortByI = -1;
                for (int i = 0; i < sortBy.length; ++i) {
                    diffs[i] = ColorUtil.Diff(color, sortBy[i]);
                    if (diffs[i] < smallestDiff) {
                        smallestDiff = diffs[i];
                        sortByI = i;
                    }
                }
                return diffs[sortByI] * Math.pow(100., sortByI);
            }
        });
    }
}
