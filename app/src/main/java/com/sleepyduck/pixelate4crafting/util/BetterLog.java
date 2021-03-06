package com.sleepyduck.pixelate4crafting.util;

import android.util.Log;

public class BetterLog {
    public static final boolean DEBUG = true;

    public static <T> void d(Class<T> source, String text) {
        if (!DEBUG)
            return;

        String simpleName = source.getSimpleName();
        if (simpleName.length() == 0) {
            simpleName = source.getName();
        }

        if (text == null) {
            text = simpleName;
        }

        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        for (StackTraceElement element : stackTrace) {
            if (element.getClassName().equals(source.getName())) {

                Log.d("BetterLog: " + element.getMethodName() + " (" + element.getFileName() + ":" + element.getLineNumber() + ")", text);
                return;
            }
        }

        Log.d("BetterLog", simpleName + ": " + text);
    }

    public static void d(Object source, String text) {
        d(source.getClass(), text);
    }

    public static void d(Object source) {
        d(source, null);
    }

    public static void d(Object source, String text, Object... args) {
        d(source, String.format(text, args));
    }

    public static void e(Class<?> source, Exception e, String text) {
        if (!DEBUG)
            return;

        String simpleName = source.getSimpleName();
        if (simpleName.length() == 0) {
            simpleName = source.getName();
        }

        if (text == null) {
            text = simpleName;
        }

        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        for (StackTraceElement element : stackTrace) {
            if (element.getClassName().equals(source.getName())) {

                Log.d(element.getMethodName() + " (" + element.getFileName() + ":" + element.getLineNumber() + ")", text);
                return;
            }
        }

        Log.e(simpleName, text, e);
    }

    public static void e(Object source, Exception e, String text) {
        e(source.getClass(), e, text);
    }

    public static void e(Object source, Exception e) {
        e(source, e, null);
    }

    public static void e(Object source, Exception e, String text, Object... args) {
        e(source, e, String.format(text, args));
    }
}
