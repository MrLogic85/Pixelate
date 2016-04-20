package com.sleepyduck.pixelate4crafting.control;

import android.content.Context;

import com.sleepyduck.pixelate4crafting.control.util.BetterLog;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Map;

/**
 * Created by fredrik.metcalf on 2016-04-13.
 */
public class DataManager {
    private static final String COLOR_FILE = "COLORS";
    private static final String PIXEL_FILE = "PIXELS";

    public static void SavePixels(Context context, int patternId, Map<Integer, Float> colors) {
        SaveData(context, patternId, COLOR_FILE, colors);
    }

    public static void SavePixels(Context context, int patternId, int[][] pixels) {
        SaveData(context, patternId, PIXEL_FILE, pixels);
    }

    public static Map<Integer, Float> LoadColors(Context context, int patternId) {
        try {
            return (Map<Integer, Float>) LoadData(context, patternId, COLOR_FILE);
        } catch (ClassCastException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static int[][] LoadPixels(Context context, int patternId) {
        try {
            return (int[][]) LoadData(context, patternId, PIXEL_FILE);
        } catch (ClassCastException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void DestroyColors(Context context, int patternId) {
        DestroyColors(context, patternId, COLOR_FILE);
    }

    public static void DestroyPixels(Context context, int patternId) {
        DestroyColors(context, patternId, PIXEL_FILE);
    }

    private static void SaveData(Context context, int patternId, String prefix, Object data) {
        File file = new File(context.getFilesDir(), prefix + String.format("%8x", patternId));
        ObjectOutputStream oos = null;
        try {
            oos = new ObjectOutputStream(new FileOutputStream(file));
            oos.writeObject(data);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (oos != null) {
                try {
                    oos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static Object LoadData(Context context, int patternId, String prefix) {
        File file = new File(context.getFilesDir(), prefix + String.format("%8x", patternId));
        ObjectInputStream ois = null;
        Object result = null;
        try {
            ois = new ObjectInputStream(new FileInputStream(file));
            result = ois.readObject();
        } catch (IOException
                | ClassNotFoundException
                | ClassCastException e) {
            e.printStackTrace();
        } finally {
            if (ois != null) {
                try {
                    ois.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }

    private static void DestroyColors(Context context, int patternId, String prefix) {
        File file = new File(context.getFilesDir(), prefix + String.format("%8x", patternId));
        BetterLog.d(DataManager.class, "Destroying data file " + prefix + String.format("%8x", patternId));
        file.delete();
    }
}
