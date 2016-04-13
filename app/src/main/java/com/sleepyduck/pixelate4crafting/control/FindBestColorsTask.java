package com.sleepyduck.pixelate4crafting.control;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;

import com.sleepyduck.pixelate4crafting.control.util.ColorUtil;
import com.sleepyduck.pixelate4crafting.model.Pattern;
import com.sleepyduck.pixelate4crafting.control.util.BetterLog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by fredrik.metcalf on 2016-04-12.
 */
public class FindBestColorsTask extends AsyncTask<Object, Integer, Integer> {
    private static final int ALPHA_CHANNEL = 0xff000000;
    private Pattern mPattern;
    private Context mContext;
    private int mNumColors;
    private Bitmap mBitmap;

    Map<Integer, Integer> colorsCounted = new HashMap<>();

    @Override
    protected Integer doInBackground(Object... params) {
        if (params.length > 0) {
            mContext = (Context) params[0];
            mPattern = (Pattern) params[1];
            mNumColors = (int) params[2];
            mBitmap = BitmapHandler.getFromFileName(mContext, mPattern.getFileName());

            countColors();
            if (colorsCounted.size() > mNumColors) {
                selectColors();
            }
            if (!isCancelled()) {
                mPattern.setColors(colorsCounted);
            }
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
                if (colorsCounted.containsKey(pixel)) {
                    colorsCounted.put(pixel, colorsCounted.get(pixel) + 1);
                } else {
                    colorsCounted.put(pixel, 1);
                }
            }
        }
    }

    private void selectColors() {
        List<Map.Entry<Integer, Integer>> sortedColors = new ArrayList<>(colorsCounted.entrySet());
        Collections.sort(sortedColors, new Comparator<Map.Entry<Integer, Integer>>() {
            @Override
            public int compare(Map.Entry<Integer, Integer> lhs, Map.Entry<Integer, Integer> rhs) {
                return rhs.getValue() - lhs.getValue();
            }
        });

        // If too many colors, rerun with higher threshold
        int step = 256*3;//*3*4;
        int currentThresh = Constants.COLOR_SELECT_THRESHOLD;
        while (colorsCounted.size() != mNumColors && !isCancelled()) {
            publishProgress(50 + mNumColors * 50 / (Math.abs(colorsCounted.size() - mNumColors) + 1)); // 0%-50%
            step /= 2;
            if (colorsCounted.size() > mNumColors) {
                currentThresh += step;
            } else {
                currentThresh -= step;
            }

            colorsCounted.clear();
            for (Map.Entry<Integer, Integer> color : sortedColors) {
                checkColor(color, currentThresh);
            }
            BetterLog.d(FindBestColorsTask.this, "currentThresh = " + currentThresh + ", colorCount = " + colorsCounted.size());
            if (step <= 1 || colorsCounted.size() == mNumColors) {
                break;
            }
        }
        BetterLog.d(this, "Colors " + colorsCounted);
    }

    private void checkColor(Map.Entry<Integer, Integer> color, int threshold) {
        if (colorsCounted.containsKey(color.getKey())) {
            return;
        }
        for (int existingColor : colorsCounted.values()) {
            if (ColorUtil.Diff(color.getValue(), existingColor) < threshold) {
                return;
            }
        }
        colorsCounted.put(color.getKey(), color.getValue());
    }
}
