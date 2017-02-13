package com.sleepyduck.pixelate4crafting.util;

import android.graphics.Color;
import android.support.v4.graphics.ColorUtils;
import android.util.SparseArray;

import java.util.Arrays;
import java.util.Comparator;

/**
 * Created by fredrikmetcalf on 13/04/16.
 */
public class ColorUtil {
    public static final int ALPHA_CHANNEL = 0xff000000;

    private static final double[] LabX = new double[3];
    private static final double[] LabY = new double[3];
    private static final SparseArray<SparseArray<Double>> DiffMap = new SparseArray<>();
    private static int oldId = -1;

    synchronized public static double Diff(final int id, final int x, final int y) {
        if (oldId != id) {
            DiffMap.clear();
            oldId = id;
        }
        final int _x = Math.max(x, y);
        final int _y = Math.min(x, y);
        SparseArray<Double> map = DiffMap.get(_x);
        if (map != null) {
            Double diff = map.get(_y);
            if (diff != null) {
                return diff;
            }
        }
        ColorUtils.colorToLAB(_x, LabX);
        ColorUtils.colorToLAB(_y, LabY);
        final double diff = ColorUtils.distanceEuclidean(LabX, LabY);
        if (map == null) {
            map = new SparseArray<>();
            DiffMap.put(_x, map);
        }
        map.put(_y, diff);
        return diff;
    }

    public static int getBestColorFor(final int id, final int pixel, final int[] colors) {
        double diff, minDiff = Integer.MAX_VALUE;
        int bestColor = 0;
        for (int color : colors) {
            diff = ColorUtil.Diff(id, color, pixel);
            if (diff < minDiff) {
                minDiff = diff;
                bestColor = color;
            }
        }
        return bestColor;
    }

    public static int[] splitColor(final int pixel) {
        return new int[] {Color.red(pixel), Color.green(pixel), Color.blue(pixel)};
    }

    public static void Sort(final int id, final Integer[] colors) {
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
            public int compare(final Integer x, final Integer y) {
                return (int) (diff(y) - diff(x));
            }

            private double diff(final Integer color) {
                final double[] diffs = new double[sortBy.length];
                double smallestDiff = Double.MAX_VALUE;
                int sortByI = -1;
                for (int i = 0; i < sortBy.length; ++i) {
                    diffs[i] = ColorUtil.Diff(id, color, sortBy[i]);
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
