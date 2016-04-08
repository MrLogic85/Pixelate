package com.sleepyduck.pixelate4crafting.util;

import android.util.Log;

public class BetterLog {
	public static boolean DEBUG = true;

	public static void d(Class<?> source, String text) {
		if (!DEBUG)
			return;

		StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
		for (StackTraceElement element : stackTrace) {
			if (element.getClassName().equals(source.getName())) {
				Log.d(source.getSimpleName(), "" + element.getMethodName() + ":" + element.getLineNumber()
						+ (text != null ? ", " + text : ""));
				return;
			}
		}
	}

	public static void d(Object source, String text) {
		d(source.getClass(), text);
	}

	public static void d(Object source) {
		d(source, null);
	}
}
