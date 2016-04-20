package com.sleepyduck.pixelate4crafting.control.tasks;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;

import com.sleepyduck.pixelate4crafting.control.BitmapHandler;
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
        int height = mPattern.getPixelHeight();

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
        int stepX = 1;
        int stepY = 1;

        int checkXPixels = 4;
        if (width > checkXPixels) {
            //BetterLog.d(this, "Width > 6, " + width);
            stepX = width / checkXPixels;
        }
        if (height > checkXPixels) {
            stepY = height / checkXPixels;
        }

        Map<Integer, Float> countPixelColors = new HashMap<>();
        float resInv = 1f / (float)(width*height);
        for (int i = 0; i < width; i += stepX) {
            for (int j = 0; j < height; j += stepY) {
                int pixel = mBitmap.getPixel(i+x, j+y);
                if ((pixel & ColorUtil.ALPHA_CHANNEL) != ColorUtil.ALPHA_CHANNEL) {
                    continue;
                }
                Integer color = ColorUtil.getBestColorFor(pixel, mPattern.getColors()).getKey();
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
}
