package com.sleepyduck.pixelate4crafting.control;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.util.Pair;

import com.sleepyduck.pixelate4crafting.model.Pattern;
import com.sleepyduck.pixelate4crafting.util.BetterLog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by fredrik.metcalf on 2016-04-12.
 */
public class FindBestColorsTask extends AsyncTask<Object, Integer, Integer> {
    private Pattern mPattern;
    private Context mContext;
    private int mNumColors;
    private Bitmap mBitmap;

    Set<Integer> colors = new HashSet<>();
    //private int[] colors = new int[10];
    //private int colors.size() = 0;

    @Override
    protected Integer doInBackground(Object... params) {
        if (params.length > 0) {
            mContext = (Context) params[0];
            mPattern = (Pattern) params[1];
            mNumColors = (int) params[2];
            mBitmap = BitmapHandler.getFromFileName(mContext, mPattern.getFileName());

            boolean isFewColors = coundColorsIfFew();
            if (!isFewColors) {
                selectColors();
            }
            Map<Integer, Integer> colorMap = new HashMap<>();
            for (int color : colors) {
                colorMap.put(color, 0);
            }
            mPattern.setColors(colorMap);
        }
        return colors.size();
    }

    private boolean coundColorsIfFew() {
        for (int x = 0; x < mBitmap.getWidth(); ++x) {
            publishProgress(x * 10 / mBitmap.getWidth()); // 0%-10%
            for (int y = 0; y < mBitmap.getHeight(); ++y) {
                colors.add(mBitmap.getPixel(x, y));
            }
            if (colors.size() > mNumColors) {
                // Too many colors, this is not a picture with a few select colors
                break;
            }
        }

        if (colors.size() <= mNumColors) {
            publishProgress(100);
            return true;
        }
        return false;
    }

    private void selectColors() {
        colors.clear();
        for (int x = 0; x < mBitmap.getWidth(); ++x) {
            if (isCancelled()) {
                return;
            }
            publishProgress(10 + x * 90 / mBitmap.getWidth()); // 10%-100%
            for (int y = 0; y < mBitmap.getHeight(); ++y) {
                int color = mBitmap.getPixel(x, y);
                if (Color.alpha(color) < 0xff) {
                    continue;
                }
                checkColor(color, Constants.COLOR_SELECT_THRESHOLD);
            }
        }
        Set<Integer> filteredColors = new HashSet<>(colors);

        // If too many colors, rerun with higher threshold
        int step = 255*3;
        int currentThresh = Constants.COLOR_SELECT_THRESHOLD;
        while (colors.size() != mNumColors && !isCancelled()) {
            step /= 2;
            if (colors.size() > mNumColors) {
                currentThresh += step;
            } else {
                currentThresh -= step;
            }

            colors.clear();
            for (int color : filteredColors) {
                checkColor(color, currentThresh);
            }
            BetterLog.d(this, "currentThresh = " + currentThresh + ", colorCount = " + colors.size());
            if (step <= 1 || colors.size() == mNumColors) {
                break;
            }
        }
        BetterLog.d(this, "Colors " + colors);
    }

    private void checkColor(int color, int threshold) {
        if (colors.contains(color)) {
            return;
        }
        for (int existingColor : colors) {
            if (checkColorDiff(color, existingColor) < threshold) {
                return;
            }
        }
        colors.add(color);
    }

    private int checkColorDiff(int left, int right) {
        return Math.abs(Color.red(left) - Color.red(right))
                + Math.abs(Color.green(left) - Color.green(right))
                + Math.abs(Color.blue(left) - Color.blue(right));
    }
}
