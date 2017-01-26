package com.sleepyduck.pixelate4crafting.tasks;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;

import com.sleepyduck.pixelate4crafting.control.BitmapHandler;
import com.sleepyduck.pixelate4crafting.util.ColorUtil;
import com.sleepyduck.pixelate4crafting.util.MMCQ;
import com.sleepyduck.pixelate4crafting.model.Pattern;

import java.util.Map;

/**
 * Created by fredrik.metcalf on 2016-04-12.
 */
public class CountColorsTask extends AsyncTask<Object, Integer, Map<Integer, Float>> {

    @Override
    protected Map<Integer, Float> doInBackground(Object... params) {
        publishProgress(0);
        if (params.length > 0) {
            Context context = (Context) params[0];
            Pattern pattern = (Pattern) params[1];
            Bitmap bitmap = BitmapHandler.getFromFileName(context, pattern.getFileName());

            // Clear the color map
            for (Map.Entry<Integer, Float> color : pattern.getColors().entrySet()) {
                color.setValue(0f);
            }

            // Count the colors
            if (pattern.getColors() != null && pattern.getColors().size() > 0) {
                countColors(bitmap, pattern.getColors());
            }

            return pattern.getColors();
        }
        return null;
    }

    // TODO remove as many allocations as possible
    private void countColors(Bitmap mBitmap, Map<Integer, Float> colors) {
        double diff, bestDiff;
        float resInv = 1f / (float) (mBitmap.getWidth() * mBitmap.getHeight());
        MMCQ.CMap CMap = null;// = mPattern.getCMap();
        int[] colorRGB = new int[3];
        Map.Entry<Integer, Float> bestColor = null;
        for (int x = 0; x < mBitmap.getWidth(); ++x) {
            if(isCancelled()) {
                clearCount(colors);
                return;
            }
            publishProgress(x * 100 / mBitmap.getWidth());
            for (int y = 0; y < mBitmap.getHeight(); ++y) {
                int pixel = mBitmap.getPixel(x, y);
                if ((pixel & ColorUtil.ALPHA_CHANNEL) != ColorUtil.ALPHA_CHANNEL) {
                    // Transparent
                    continue;
                }
                if (CMap != null) {
                    colorRGB[0] = Color.red(pixel);
                    colorRGB[1] = Color.green(pixel);
                    colorRGB[2] = Color.blue(pixel);
                    int[] mapColor = CMap.map(new int[]{Color.red(pixel), Color.green(pixel), Color.blue(pixel)});
                    int rgb = Color.rgb(mapColor[0], mapColor[1], mapColor[2]);
                    colors.put(rgb, colors.get(rgb) + resInv);
                } else {
                    bestDiff = Integer.MAX_VALUE;
                    for (Map.Entry<Integer, Float> color : colors.entrySet()) {
                        diff = ColorUtil.Diff(pixel, color.getKey());
                        if (diff < bestDiff) {
                            bestDiff = diff;
                            bestColor = color;
                        }
                    }
                    if (bestColor != null) {
                        bestColor.setValue(bestColor.getValue() + resInv);
                    }
                }
            }
        }
    }

    private void clearCount(Map<Integer, Float> colors) {
        for (Map.Entry<Integer, Float> entry : colors.entrySet()) {
            entry.setValue(0f);
        }
    }
}
