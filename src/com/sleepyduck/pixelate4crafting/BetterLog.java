package com.sleepyduck.pixelate4crafting;

import android.util.Log;

public class BetterLog {
	public static boolean DEBUG = true;

	public static void d(Object source, String text) {
		if (!DEBUG)
			return;

		StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
		for (StackTraceElement element : stackTrace) {
			if (element.getClassName().equals(source.getClass().getName())) {
				Log.d(source.getClass().getSimpleName(),
						"" + element.getMethodName() + ":"
								+ element.getLineNumber() + ", " + text);
				return;
			}
		}
	}
}
