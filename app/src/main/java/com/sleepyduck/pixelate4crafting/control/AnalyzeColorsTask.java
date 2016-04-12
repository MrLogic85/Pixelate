package com.sleepyduck.pixelate4crafting.control;

import android.content.Context;
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
import java.util.List;
import java.util.Map;

/**
 * Created by fredrik.metcalf on 2016-04-12.
 */
public class AnalyzeColorsTask extends AsyncTask<Object, Integer, Integer> {
    private Pattern mPattern;
    private Context mContext;
    private int mNumColors;
    private Bitmap mBitmap;

    private int[] colors = new int[10];
    private int colorsSize = 0;

    @Override
    protected Integer doInBackground(Object... params) {
        publishProgress(0);
        if (params.length > 0) {
            mContext = (Context) params[0];
            mPattern = (Pattern) params[1];
            mNumColors = (int) params[2];
            mBitmap = BitmapHandler.getFromFileName(mContext, mPattern.getFileName());
            publishProgress(1);

            boolean isFewColors = coundColorsIfFew();
            if (!isFewColors) {
                selectColors();
            }
            mPattern.setColors(Arrays.copyOf(colors, colorsSize));
        }
        return colorsSize;
    }

    private boolean coundColorsIfFew() {
        for (int x = 0; x < mBitmap.getWidth(); ++x) {
            publishProgress(1 + x * 5 / mBitmap.getWidth()); // 1%-6%
            for (int y = 0; y < mBitmap.getHeight(); ++y) {
                addUniqueColor(mBitmap.getPixel(x, y));
            }
            if (colorsSize > mNumColors) {
                // Too many colors, this is not a picture with a few select colors
                break;
            }
        }

        if (colorsSize <= mNumColors) {
            publishProgress(100);
            return true;
        }
        return false;
    }

    private void selectColors() {
        colorsSize = 0;
        int color;
        for (int x = 0; x < mBitmap.getWidth(); ++x) {
            if (isCancelled()) {
                return;
            }
            publishProgress(6 + x * 74 / mBitmap.getWidth()); // 6%-80%
            for (int y = 0; y < mBitmap.getHeight(); ++y) {
                color = mBitmap.getPixel(x, y);
                checkColor(color, Constants.COLOR_SELECT_THRESHOLD);
            }
        }
        int[] filteredColors = Arrays.copyOf(colors, colorsSize);
        Arrays.sort(filteredColors);

        // If too many colors, rerun with higher threshold
        final int maxThresh = 255*3;
        int step = maxThresh;
        int currentThresh = Constants.COLOR_SELECT_THRESHOLD;
        while (colorsSize != mNumColors && !isCancelled()) {
            step /= 2;
            if (colorsSize > mNumColors) {
                currentThresh += step;
            } else {
                currentThresh -= step;
            }

            colorsSize = 0;
            for (int i = 0; i < filteredColors.length; ++i) {
                checkColor(filteredColors[i], currentThresh);
            }
            BetterLog.d(this, "currentThresh = " + currentThresh + ", colorCount = " + colorsSize);
            publishProgress(80 + 20 / (Math.abs(colorsSize - mNumColors) + 1)); // 80%-90%
            if (step <= 1 || colorsSize == mNumColors) {
                break;
            }
        }
        BetterLog.d(this, "Colors " + colors);
        publishProgress(100);
    }

    private void checkColor(int color, int threshold) {
        if (Arrays.binarySearch(colors, 0, colorsSize, color) >= 0) {
            return;
        }
        for (int i = 0; i < colorsSize; ++i) {
            if (checkColorDiff(color, colors[i]) < threshold) {
                return;
            }
        }
        addColor(color);
        //BetterLog.d(this, String.format("Found color %x, size %d", color, colorsSize));
    }

    private int checkColorDiff(int left, int right) {
        return Math.abs(Color.red(left) - Color.red(right))
                + Math.abs(Color.green(left) - Color.green(right))
                + Math.abs(Color.blue(left) - Color.blue(right));
    }

    private void addColor(int color) {
        if (colors.length == colorsSize+1) {
            colors = Arrays.copyOf(colors, colors.length*2);
        }
        colors[colorsSize++] = color;
        Arrays.sort(colors, 0, colorsSize);
    }

    private void addUniqueColor(int color) {
        if (Arrays.binarySearch(colors, 0, colorsSize, color) >= 0) {
            return;
        }
        addColor(color);
    }
}
