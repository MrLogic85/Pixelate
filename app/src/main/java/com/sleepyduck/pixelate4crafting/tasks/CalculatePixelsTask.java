package com.sleepyduck.pixelate4crafting.tasks;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.util.SparseArray;

import com.sleepyduck.pixelate4crafting.control.BitmapHandler;
import com.sleepyduck.pixelate4crafting.model.Pattern;
import com.sleepyduck.pixelate4crafting.util.BetterLog;
import com.sleepyduck.pixelate4crafting.util.ColorUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by fredrik.metcalf on 2016-04-12.
 */
public class CalculatePixelsTask extends AsyncTask<Object, Integer, int[][]> {
    private Map<Integer, Float> mColors;
    private Bitmap mBitmap;
    private int mWidth;
    private int mHeight;

    @Override
    protected int[][] doInBackground(Object... params) {
        publishProgress(0);
        if (params.length > 0) {
            Context context = (Context) params[0];
            Pattern pattern = (Pattern) params[1];
            mBitmap = BitmapHandler.getFromFileName(context, pattern.getFileName());
            mColors = new HashMap<>();
            for (int color : pattern.getColors(new int[pattern.getColorCount()])) {
                mColors.put(color, pattern.getColorWeight(color));
            }
            mWidth = pattern.getPixelWidth();
            mHeight = pattern.getPixelHeight();

            if (mColors.size() == 0) {
                mColors.put(Color.BLACK, 1f);
                mColors.put(Color.WHITE, 1f);
            }

            return calculatePixels();
        }
        return null;
    }

    private int[][] calculatePixels() {
        long timeStart = SystemClock.currentThreadTimeMillis();
        float pixelSize = (float) mBitmap.getWidth() / (float) mWidth;
        int width = mWidth;
        int height = mHeight;

        int[][] pixels = new int[width][height];
        float dx, dy;
        for (int x = 0; x < width; ++x) {
            if (isCancelled()) {
                return null;
            }
            publishProgress(x * 100 / width);
            for (int y = 0; y < height; ++y) {
                dx = pixelSize * (float) x;
                dy = pixelSize * (float) y;
                pixels[x][y] = findColorForPixel(dx, dy, pixelSize);
            }
        }
        BetterLog.d(
                this,
                "Calculate pixels: %d (Init %d, Count %d, Find %d, Get pixel %d)",
                SystemClock.currentThreadTimeMillis() - timeStart,
                initializeTime,
                countTime,
                findBestTime,
                getPixelTime);
        return pixels;
    }

    private long initializeTime = 0;
    private long countTime = 0;
    private long findBestTime = 0;
    private long getPixelTime = 0;

    /**
     * Find the dominant color in the pixel, comparing the color weight in the pixel compared to
     * the entire image
     */
    private int findColorForPixel(float dx, float dy, float pixelSize) {
        long initializeStart = SystemClock.currentThreadTimeMillis();
        int x = Math.round(dx);
        int y = Math.round(dy);
        int width = Math.round(dx+pixelSize) - x;
        int height = Math.round(dy+pixelSize) - y;
        int[] colors = new int[mColors.size()];
        final Object[] pixelObjects = mColors.keySet().toArray();
        for (int i = 0; i < colors.length; ++i) {
            colors[i] = (int) pixelObjects[i];
        }

        SparseArray<Float> countPixelColors = new SparseArray<>();
        float resInv = 1f / (float)(width*height);
        initializeTime += SystemClock.currentThreadTimeMillis() - initializeStart;
        long countStart = SystemClock.currentThreadTimeMillis();
        for (int i = 0; i < width; i += 1) {
            for (int j = 0; j < height; j += 1) {
                if (i+x < mBitmap.getWidth() && j+y < mBitmap.getHeight()) {
                    long getPixelStart = SystemClock.currentThreadTimeMillis();
                    int pixel = mBitmap.getPixel(i + x, j + y);
                    getPixelTime += SystemClock.currentThreadTimeMillis() - getPixelStart;
                    if ((pixel & ColorUtil.ALPHA_CHANNEL) != ColorUtil.ALPHA_CHANNEL) {
                        continue;
                    }
                    Integer color = ColorUtil.getBestColorFor(pixel, colors);
                    Float weight = countPixelColors.get(color, 0f);
                    countPixelColors.put(color, weight + resInv);
                } else {
                    break;
                }
            }
        }
        countTime += SystemClock.currentThreadTimeMillis() - countStart;
        long findBestStart = SystemClock.currentThreadTimeMillis();
        Map<Integer, Float> colorMap = mColors;
        if (countPixelColors.size() > 0) {
            int bestColor = Color.TRANSPARENT;
            float bestWeight = Float.MIN_VALUE, weight;
            for(int i = 0; i < countPixelColors.size(); i++) {
                int color = countPixelColors.keyAt(i);
                weight = countPixelColors.valueAt(i) / colorMap.get(color);
                if (weight > bestWeight) {
                    bestWeight = weight;
                    bestColor = color;
                }
            }
            return bestColor;
        }
        findBestTime += SystemClock.currentThreadTimeMillis() - findBestStart;
        return Color.TRANSPARENT;
    }
}
