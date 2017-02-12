package com.sleepyduck.pixelate4crafting.tasks;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
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
public abstract class CalculatePixelsTask extends CancellableProcess<Object, Integer, int[][]> {
    private Map<Integer, Float> mColorsMap;
    private Bitmap mBitmap;
    private int mWidth;
    private int mHeight;
    private int[] mColors;

    @Override
    public int[][] execute(Object... params) {
        onPublishProgress(0);
        if (params.length > 0) {
            Context context = (Context) params[0];
            Pattern pattern = (Pattern) params[1];
            mBitmap = BitmapHandler.getFromFileName(context, pattern.getFileName());
            mColorsMap = new HashMap<>();
            for (int color : pattern.getColors(new int[pattern.getColorCount()])) {
                mColorsMap.put(color, pattern.getColorWeight(color));
            }
            mWidth = pattern.getPixelWidth();
            mHeight = pattern.getPixelHeight();

            if (mColorsMap.size() == 0) {
                mColorsMap.put(Color.BLACK, 1f);
                mColorsMap.put(Color.WHITE, 1f);
            }

            mColors = new int[mColorsMap.size()];
            final Object[] colorObjects = mColorsMap.keySet().toArray();
            for (int i = 0; i < mColors.length; ++i) {
                mColors[i] = (int) colorObjects[i];
            }

            return calculatePixels(pattern);
        }
        return null;
    }

    private int[][] calculatePixels(Pattern pattern) {
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
            onPublishProgress(x * 100 / width);
            for (int y = 0; y < height; ++y) {
                dx = pixelSize * (float) x;
                dy = pixelSize * (float) y;
                pixels[x][y] = findColorForPixel(pattern.Id, dx, dy, pixelSize);
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
    private int findColorForPixel(int id, float dx, float dy, float pixelSize) {
        long initializeStart = SystemClock.currentThreadTimeMillis();
        int x = Math.round(dx);
        int y = Math.round(dy);
        int width = Math.round(dx+pixelSize) - x;
        int height = Math.round(dy+pixelSize) - y;

        SparseArray<Float> countPixelColors = new SparseArray<>();
        float resInv = 1f / (float)(width*height);
        initializeTime += SystemClock.currentThreadTimeMillis() - initializeStart;
        long countStart = SystemClock.currentThreadTimeMillis();
        for (int i = 0; i <= width; i += 1) {
            for (int j = 0; j <= height; j += 1) {
                if (i+x < mBitmap.getWidth() && j+y < mBitmap.getHeight()) {
                    long getPixelStart = SystemClock.currentThreadTimeMillis();
                    int pixel = mBitmap.getPixel(i + x, j + y);
                    getPixelTime += SystemClock.currentThreadTimeMillis() - getPixelStart;
                    if ((pixel & ColorUtil.ALPHA_CHANNEL) != ColorUtil.ALPHA_CHANNEL) {
                        continue;
                    }
                    Integer color = ColorUtil.getBestColorFor(id, pixel, mColors);
                    Float weight = countPixelColors.get(color, 0f);
                    countPixelColors.put(color, weight + resInv);
                } else {
                    break;
                }
            }
        }
        countTime += SystemClock.currentThreadTimeMillis() - countStart;
        long findBestStart = SystemClock.currentThreadTimeMillis();
        Map<Integer, Float> colorMap = mColorsMap;
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
