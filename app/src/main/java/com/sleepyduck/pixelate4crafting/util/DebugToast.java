package com.sleepyduck.pixelate4crafting.util;

import android.content.Context;
import android.widget.Toast;

import com.sleepyduck.pixelate4crafting.BuildConfig;

/**
 * Created by fredrikmetcalf on 07/02/17.
 */

public class DebugToast {
    public static void makeText(Context context, String text, Object... params) {
        if (BuildConfig.DEBUG) {
            Toast.makeText(context, String.format(text, params), Toast.LENGTH_SHORT).show();
        }
    }
}
