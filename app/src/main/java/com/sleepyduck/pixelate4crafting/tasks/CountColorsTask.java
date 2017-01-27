package com.sleepyduck.pixelate4crafting.tasks;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.renderscript.Allocation;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsic;
import android.renderscript.Type;

import com.sleepyduck.pixelate4crafting.control.BitmapHandler;
import com.sleepyduck.pixelate4crafting.util.BetterLog;
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

            if (pattern.getColors() != null && pattern.getColors().size() > 0) {
                // Clear the color map
                for (Map.Entry<Integer, Float> color : pattern.getColors().entrySet()) {
                    color.setValue(0f);
                }

                // Count the colors
                countColors(context, bitmap, pattern.getColors());
            }

            return pattern.getColors();
        }
        return null;
    }

    private void countColors(final Context context, final Bitmap mBitmap, final Map<Integer, Float> colorMap) {
        long timeStart = SystemClock.currentThreadTimeMillis();
        long timeGetPixel = 0, timeGetPixelStart, timeDiff = 0, timeDiffStart;
        double diff, bestDiff;
        final float resInv = 1f / (float) (mBitmap.getWidth() * mBitmap.getHeight());
        int bestColor, i, pixel;
        final int size = colorMap.size();
        final int[] colors = new int[size];
        final float[] weights = new float[size];
        timeGetPixelStart = SystemClock.currentThreadTimeMillis();
        final int pixelCount = mBitmap.getWidth() * mBitmap.getHeight();
        final int[] pixels = new int[pixelCount];
        mBitmap.getPixels(pixels, 0, mBitmap.getWidth(), 0, 0, mBitmap.getWidth(), mBitmap.getHeight());
        timeGetPixel += SystemClock.currentThreadTimeMillis() - timeGetPixelStart;
        for (int p = 0; p < pixelCount; ++p) {
            if (isCancelled()) {
                return;
            }
            if ((p * 100) % pixelCount == 0) {
                publishProgress(p * 100 / pixelCount);
            }
            bestColor = -1;
            if ((pixels[p] & ColorUtil.ALPHA_CHANNEL) != ColorUtil.ALPHA_CHANNEL) {
                // Transparent
                continue;
            }
            bestDiff = Integer.MAX_VALUE;
            timeDiffStart = SystemClock.currentThreadTimeMillis();
            for (i = 0; i < size; ++i) {
                diff = ColorUtil.DiffMap(pixels[p], colors[i]);
                if (diff < bestDiff) {
                    bestDiff = diff;
                    bestColor = i;
                }
            }
            timeDiff += SystemClock.currentThreadTimeMillis() - timeDiffStart;
            if (bestColor >= 0) {
                weights[bestColor] += resInv;
            }
        }
        for (i = 0; i < size; ++i) {
            colorMap.put(colors[i], weights[i]);
        }
        BetterLog.d(CountColorsTask.class, "Count colors time: %d (Get: %d, Diff: %d)", SystemClock.currentThreadTimeMillis() - timeStart, timeGetPixel, timeDiff);
    }
}
