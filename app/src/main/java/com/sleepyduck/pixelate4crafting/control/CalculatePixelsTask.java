package com.sleepyduck.pixelate4crafting.control;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;

import com.sleepyduck.pixelate4crafting.model.Pattern;

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
            mPattern.setColorMatrix(colorMatrix);
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
            publishProgress(x * 100 / width);
            for (int y = 0; y < height; ++y) {
                dx = pixelSize * (float) x;
                dy = pixelSize * (float) y;
                colorMatrix[x][y] = checkColorsFor(dx, dy, pixelSize);
            }
        }
        return colorMatrix;
    }

    private int checkColorsFor(float dx, float dy, float pixelSize) {
        int x = Math.round(dx);
        int y = Math.round(dy);
        int width = Math.round(dx+pixelSize) - x;
        int height = Math.round(dy+pixelSize) - y;

        int minColorCount = Integer.MAX_VALUE;
        Map.Entry<Integer, Integer> bestColor = null;
        for (int i = 0; i < width; ++i) {
            for (int j = 0; j < height; ++j) {
                int pixel = mBitmap.getPixel(i+x, j+y);
                Map.Entry<Integer, Integer> color = getBestColorFor(pixel);
                if (color.getValue() <= minColorCount) {
                    minColorCount = color.getValue();
                    bestColor = color;
                }
            }
        }
        return bestColor.getKey();
    }

    private Map.Entry<Integer, Integer> getBestColorFor(int pixel) {
        int diff, minDiff = Integer.MAX_VALUE;
        Map.Entry<Integer, Integer> bestColor = null;
        for (Map.Entry<Integer, Integer> color : mPattern.getColors().entrySet()) {
            diff = checkColorDiff(color.getKey(), pixel);
            if (diff < minDiff) {
                minDiff = diff;
                bestColor = color;
            }
        }
        return bestColor;
    }

    private int checkColorDiff(int left, int right) {
        return Math.abs(Color.red(left) - Color.red(right))
                + Math.abs(Color.green(left) - Color.green(right))
                + Math.abs(Color.blue(left) - Color.blue(right));
    }
}
