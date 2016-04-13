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
public class CountColorsTask extends AsyncTask<Object, Integer, Void> {
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

            countColors(mBitmap, mPattern.getColors());
        }
        return null;
    }

    private void countColors(Bitmap mBitmap, Map<Integer, Integer> colors) {
        int diff, bestDiff, bestColor = 0;
        for (int x = 0; x < mBitmap.getWidth(); ++x) {
            publishProgress(x * 100 / mBitmap.getWidth());
            for (int y = 0; y < mBitmap.getHeight(); ++y) {
                int pixel = mBitmap.getPixel(x, y);
                bestDiff = Integer.MAX_VALUE;
                for (int color : colors.keySet()) {
                    diff = checkColorDiff(pixel, color);
                    if (diff < bestDiff) {
                        bestDiff = diff;
                        bestColor = color;
                    }
                }
                colors.put(bestColor, colors.get(bestColor) + 1);
            }
        }
    }

    private int checkColorDiff(int left, int right) {
        return Math.abs(Color.red(left) - Color.red(right))
                + Math.abs(Color.green(left) - Color.green(right))
                + Math.abs(Color.blue(left) - Color.blue(right));
    }
}