package com.sleepyduck.pixelate4crafting.tasks;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;

import com.sleepyduck.pixelate4crafting.control.BitmapHandler;
import com.sleepyduck.pixelate4crafting.util.MMCQ;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by fredrik.metcalf on 2016-04-12.
 */
public abstract class FindBestColorsTask extends CancellableProcess<Object, Integer, Map<Integer, Float>> {

    @Override
    public Map<Integer, Float> execute(Object... params) {
        if (params.length > 0) {
            Context context = (Context) params[0];
            String fileName = (String) params[1];
            int numColors = (int) params[2];
            Bitmap mBitmap = BitmapHandler.getFromFileName(context, fileName);

            if (mBitmap == null) {
                return null;
            }

            Map<Integer, Float> colorsCounted = new HashMap<>();

            MMCQ.CMap colors = MMCQ.computeMap(mBitmap, numColors);
            for (int[] color : colors.palette()) {
                colorsCounted.put(Color.rgb(color[0], color[1], color[2]), 0f);
            }
            colorsCounted.put(Color.WHITE, 0f);
            return colorsCounted;
        }
        return null;
    }

}
