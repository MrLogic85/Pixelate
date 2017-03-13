package com.sleepyduck.pixelate4crafting.control;

import android.content.Context;

import com.sleepyduck.pixelate4crafting.util.BetterLog;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Created by fredrik.metcalf on 2016-04-13.
 */
public class DataManager {
    private static final String PIXEL_FILE = "PIXELS";

    public static String SavePixels(Context context, int patternId, String data) {
        return SaveData(context, patternId, PIXEL_FILE, data);
    }

    public static String LoadPixels(Context context, int patternId) {
        try {
            return LoadData(context, patternId, PIXEL_FILE);
        } catch (ClassCastException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean DestroyPixels(Context context, int patternId) {
        return DestroyDataFile(context, patternId, PIXEL_FILE);
    }

    private static String SaveData(Context context, int patternId, String prefix, String data) {
        File file = new File(context.getFilesDir(), prefix + String.format("%8x", patternId));
        try (PrintWriter writer = new PrintWriter(file)) {
            writer.print(data);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return file.getPath();
    }

    private static String LoadData(Context context, int patternId, String prefix) {
        File file = new File(context.getFilesDir(), prefix + String.format("%8x", patternId));
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            return reader.readLine();
        } catch (IOException e) {
            //e.printStackTrace();
        }
        return "";
    }

    private static boolean DestroyDataFile(Context context, int patternId, String prefix) {
        File file = new File(context.getFilesDir(), prefix + String.format("%8x", patternId));
        BetterLog.d(DataManager.class, "Destroying data file " + prefix + String.format("%8x", patternId));
        return file.delete();
    }
}
