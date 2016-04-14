package com.sleepyduck.pixelate4crafting.control;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;

import com.sleepyduck.pixelate4crafting.control.util.ColorUtil;
import com.sleepyduck.pixelate4crafting.model.Pattern;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by fredrik.metcalf on 2016-04-12.
 */
public class CalculatePixelsTask extends AsyncTask<Object, Integer, Void> {
    private Pattern mPattern;
    private Context mContext;
    private Bitmap mBitmap;

    @Override
    protected Void doInBackground(Object... params) {
        publishProgress(0);
        if (params.length > 0) {
            mContext = (Context) params[0];
            mPattern = (Pattern) params[1];
            mBitmap = BitmapHandler.getFromFileName(mContext, mPattern.getFileName());

            int[][] colorMatrix = calculatePixels();
            if (colorMatrix != null) {
                mPattern.setColorMatrix(colorMatrix);
            }
        }
        return null;
    }

    private int[][] calculatePixels() {
        float pixelSize = (float) mBitmap.getWidth() / (float) mPattern.getPixelWidth();
        int width = mPattern.getPixelWidth();
        int height = (int) (mBitmap.getHeight() / pixelSize);
        mPattern.setPixelHeight(height);

        int[][] colorMatrix = new int[width][height];
        float dx, dy;
        for (int x = 0; x < width; ++x) {
            if (isCancelled()) {
                return null;
            }
            publishProgress(x * 100 / width);
            for (int y = 0; y < height; ++y) {
                dx = pixelSize * (float) x;
                dy = pixelSize * (float) y;
                colorMatrix[x][y] = checkColorsFor(dx, dy, pixelSize);
            }
        }
        return colorMatrix;
    }

    /**
     * Find the dominant color in the pixel, comparing the color weight in the pixel compared to
     * the entire image
     * @param dx
     * @param dy
     * @param pixelSize
     * @return
     */
    private int checkColorsFor(float dx, float dy, float pixelSize) {
        int x = Math.round(dx);
        int y = Math.round(dy);
        int width = Math.round(dx+pixelSize) - x;
        int height = Math.round(dy+pixelSize) - y;

        Map<Integer, Float> countPixelColors = new HashMap<>();
        float resInv = 1f / (float)(width*height);
        for (int i = 0; i < width; ++i) {
            for (int j = 0; j < height; ++j) {
                int pixel = mBitmap.getPixel(i+x, j+y);
                if (Color.alpha(pixel) < 0xff) {
                    pixel = Color.WHITE;
                }
                Integer color = getBestColorFor(pixel).getKey();
                if (countPixelColors.containsKey(color)) {
                    countPixelColors.put(color, (countPixelColors.get(color) + resInv));
                } else {
                    countPixelColors.put(color, resInv);
                }
            }
        }
        Map<Integer, Float> colors = mPattern.getColors();
        if (countPixelColors.size() > 0) {
            int bestColor = Color.TRANSPARENT;
            float bestWeight = Float.MIN_VALUE, weight;
            for (Map.Entry<Integer, Float> entry : countPixelColors.entrySet()) {
                weight = entry.getValue() / colors.get(entry.getKey());
                if (weight > bestWeight) {
                    bestWeight = weight;
                    bestColor = entry.getKey();
                }
            }
            return bestColor;
        }
        return Color.TRANSPARENT;
    }

    private Map.Entry<Integer, Float> getBestColorFor(int pixel) {
        double diff, minDiff = Integer.MAX_VALUE;
        Map.Entry<Integer, Float> bestColor = null;
        for (Map.Entry<Integer, Float> color : mPattern.getColors().entrySet()) {
            diff = ColorUtil.Diff(color.getKey(), pixel);
            if (diff < minDiff) {
                minDiff = diff;
                bestColor = color;
            }
        }
        return bestColor;
    }
}
