package com.sleepyduck.pixelate4crafting.tasks;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.SystemClock;

import com.sleepyduck.pixelate4crafting.control.BitmapHandler;
import com.sleepyduck.pixelate4crafting.model.Pattern;
import com.sleepyduck.pixelate4crafting.util.BetterLog;
import com.sleepyduck.pixelate4crafting.util.ColorUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by fredrik.metcalf on 2016-04-12.
 */
public abstract class CountColorsTask extends CancellableProcess<Object, Integer, Map<Integer, Float>> {

    @Override
    public Map<Integer, Float> execute(Object... params) {
        onPublishProgress(0);
        if (params.length > 0) {
            Context context = (Context) params[0];
            Pattern pattern = (Pattern) params[1];
            Bitmap bitmap = BitmapHandler.getFromFileName(context, pattern.getFileName());

            if (bitmap == null) {
                cancel();
                return null;
            }

            Map<Integer, Float> colors = new HashMap<>();
            for (int color : pattern.getColors(new int[pattern.getColorCount()])) {
                colors.put(color, 0f);
            }
            if (colors.size() == 0) {
                colors = new HashMap<>();
                colors.put(Color.BLACK, 0f);
                colors.put(Color.WHITE, 0f);
            }

            for (Map.Entry<Integer, Float> color : colors.entrySet()) {
                color.setValue(0f);
            }

            // Count the colors
            countColors(bitmap, colors);

            return colors;
        }
        return null;
    }

    private void countColors(final Bitmap mBitmap, final Map<Integer, Float> colorMap) {
        long timeStart = SystemClock.currentThreadTimeMillis();
        long timeGetPixel = 0, timeGetPixelStart, timeDiff = 0, timeDiffStart;
        double diff, bestDiff;
        final float resInv = 1f / (float) (mBitmap.getWidth() * mBitmap.getHeight());
        int bestColor, i;
        final int size = colorMap.size();
        final int[] colors = new int[size];
        Object[] colorObject = colorMap.keySet().toArray();
        for (i = 0; i < size; ++i) {
            colors[i] = (Integer) colorObject[i];
        }
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
                onPublishProgress(p * 100 / pixelCount);
            }
            bestColor = -1;
            if ((pixels[p] & ColorUtil.ALPHA_CHANNEL) != ColorUtil.ALPHA_CHANNEL) {
                // Transparent
                continue;
            }
            bestDiff = Integer.MAX_VALUE;
            timeDiffStart = SystemClock.currentThreadTimeMillis();
            for (i = 0; i < size; ++i) {
                diff = ColorUtil.Diff(pixels[p], colors[i]);
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
            Float oldWeight = colorMap.put(colors[i], weights[i]);
            assert oldWeight != null;
        }
        BetterLog.d(this, "Count colors time: %d (Get: %d, Diff: %d)", SystemClock.currentThreadTimeMillis() - timeStart, timeGetPixel, timeDiff);
    }
}
