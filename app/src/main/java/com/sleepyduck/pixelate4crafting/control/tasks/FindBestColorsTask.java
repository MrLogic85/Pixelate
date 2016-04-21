package com.sleepyduck.pixelate4crafting.control.tasks;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;

import com.sleepyduck.pixelate4crafting.control.BitmapHandler;
import com.sleepyduck.pixelate4crafting.control.Constants;
import com.sleepyduck.pixelate4crafting.control.util.ColorUtil;
import com.sleepyduck.pixelate4crafting.control.util.MMCQ;
import com.sleepyduck.pixelate4crafting.model.Pattern;
import com.sleepyduck.pixelate4crafting.control.util.BetterLog;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by fredrik.metcalf on 2016-04-12.
 */
public class FindBestColorsTask extends AsyncTask<Object, Integer, Integer> {
    private Pattern mPattern;
    private Context mContext;
    private int mNumColors;
    private Bitmap mBitmap;

    Map<Integer, Float> colorsCounted = new HashMap<>();

    @Override
    protected Integer doInBackground(Object... params) {
        if (params.length > 0) {
            mContext = (Context) params[0];
            mPattern = (Pattern) params[1];
            mNumColors = (int) params[2];
            mBitmap = BitmapHandler.getFromFileName(mContext, mPattern.getFileName());

            // MMCQ2
            /*int[][] colors = ColorThief.getPalette(mBitmap, mNumColors);
            for (int i = 0; i < colors.length; ++i) {
                colorsCounted.put(Color.rgb(colors[i][0], colors[i][1], colors[i][2]), 1f);
            }
            mPattern.setColors(colorsCounted);*/

            // MMCQ
            try {
                MMCQ.CMap colors = MMCQ.computeMap(mBitmap, mNumColors);
                for (int[] color : colors.palette()) {
                    colorsCounted.put(Color.rgb(color[0], color[1], color[2]), 0f);
                }
                colorsCounted.put(Color.WHITE, 0f);
                mPattern.setColors(colorsCounted);
            } catch (IOException e) {
                e.printStackTrace();
            }
            /*
            countColors();
            if (colorsCounted.size() > mNumColors) {
                selectColors();
            }
            if (!isCancelled()) {
                mPattern.setColors(colorsCounted);
            }*/
        }
        return colorsCounted.size();
    }

    private void countColors() {
        for (int x = 0; x < mBitmap.getWidth(); ++x) {
            if (isCancelled()) {
                return;
            }
            publishProgress(x * 50 / mBitmap.getWidth()); // 0%-50%
            for (int y = 0; y < mBitmap.getHeight(); ++y) {
                int pixel = mBitmap.getPixel(x, y);
                if ((pixel & ColorUtil.ALPHA_CHANNEL) != ColorUtil.ALPHA_CHANNEL) {
                    pixel = Color.WHITE;
                }
                if (colorsCounted.containsKey(pixel)) {
                    colorsCounted.put(pixel, colorsCounted.get(pixel) + 1f);
                } else {
                    colorsCounted.put(pixel, 1f);
                }
            }
        }
    }

    private void selectColors() {
        ArrayList<Map.Entry<Integer, Float>> sortedColors = new ArrayList<>(colorsCounted.entrySet());
        Collections.sort(sortedColors, new Comparator<Map.Entry<Integer, Float>>() {
            @Override
            public int compare(Map.Entry<Integer, Float> lhs, Map.Entry<Integer, Float> rhs) {
                return (int) (rhs.getValue() - lhs.getValue());
            }
        });
        BetterLog.d(FindBestColorsTask.this, "Filtered out colors size " + sortedColors.size());

        // If too many colors, rerun with higher threshold
        double step = 256*3;//*3*4;
        double currentThresh = Constants.COLOR_SELECT_THRESHOLD;
        while (colorsCounted.size() != mNumColors && !isCancelled()) {
            publishProgress(50 + mNumColors * 50 / (Math.abs(colorsCounted.size() - mNumColors) + 1)); // 0%-50%
            step /= 2;
            if (colorsCounted.size() > mNumColors) {
                currentThresh += step;
            } else {
                currentThresh -= step;
            }

            colorsCounted.clear();
            for (Map.Entry<Integer, Float> color : sortedColors) {
                checkColor(color, currentThresh);
            }
            BetterLog.d(FindBestColorsTask.this, "currentThresh = " + currentThresh + ", colorCount = " + colorsCounted.size());
            if (step <= 0.1 || colorsCounted.size() == mNumColors) {
                break;
            }
        }
        BetterLog.d(this, "Colors " + colorsCounted);
    }

    private void checkColor(Map.Entry<Integer, Float> color, double threshold) {
        if (colorsCounted.containsKey(color.getKey())) {
            return;
        }
        for (int existingColor : colorsCounted.keySet()) {
            if (ColorUtil.Diff(color.getKey(), existingColor) < threshold) {
                return;
            }
        }
        colorsCounted.put(color.getKey(), color.getValue());
    }
}
