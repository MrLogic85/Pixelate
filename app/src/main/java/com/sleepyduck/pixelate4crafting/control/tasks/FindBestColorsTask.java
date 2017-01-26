package com.sleepyduck.pixelate4crafting.control.tasks;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;

import com.sleepyduck.pixelate4crafting.control.BitmapHandler;
import com.sleepyduck.pixelate4crafting.control.util.MMCQ;
import com.sleepyduck.pixelate4crafting.model.Pattern;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by fredrik.metcalf on 2016-04-12.
 */
public class FindBestColorsTask extends AsyncTask<Object, Integer, Map<Integer, Float>> {

    @Override
    protected Map<Integer, Float> doInBackground(Object... params) {
        if (params.length > 0) {
            Context context = (Context) params[0];
            String fileName = (String) params[1];
            int numColors = (int) params[2];
            Bitmap mBitmap = BitmapHandler.getFromFileName(context, fileName);

            // MMCQ2
            /*int[][] colors = ColorThief.getPalette(mBitmap, mNumColors);
            for (int i = 0; i < colors.length; ++i) {
                colorsCounted.put(Color.rgb(colors[i][0], colors[i][1], colors[i][2]), 1f);
            }
            mPattern.setColors(colorsCounted);*/

            Map<Integer, Float> colorsCounted = new HashMap<>();

            // MMCQ
            try {
                MMCQ.CMap colors = MMCQ.computeMap(mBitmap, numColors);
                for (int[] color : colors.palette()) {
                    colorsCounted.put(Color.rgb(color[0], color[1], color[2]), 0f);
                }
                colorsCounted.put(Color.WHITE, 0f);
                return colorsCounted;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

}
