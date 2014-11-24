package com.sleepyduck.pixelate4crafting.data;

import java.io.IOException;
import java.io.InputStream;

import com.sleepyduck.pixelate4crafting.BetterLog;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.OpenableColumns;

public class BitmapHandler {
	public static Bitmap getFromUri(Context context, Uri uri) throws IOException {
		BetterLog.d(BitmapHandler.class, uri.toString());
		InputStream is = context.getContentResolver().openInputStream(uri);
		Bitmap image = BitmapFactory.decodeStream(is);
		return image;
	}

	public static String getFileName(Context context, Uri uri) {
		Cursor cursor = context.getContentResolver().query(uri, null, null, null, null, null);
		try {
			if (cursor != null && cursor.moveToFirst()) {
				String displayName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
				if (displayName != null && displayName.length() > 0) {
					return displayName;
				}
			}
		} finally {
			cursor.close();
		}
		return null;
	}

	public static void getPixels(Bitmap bitmap, int[] pixels, int startX, int startY, int width, int height) {
		int endX = startX + width;
		int endY = startY + height;
		int counter = 0;
		for (int y = startY; y < endY; ++y) {
			for (int x = startX; x < endX; ++x) {
				pixels[counter++] = bitmap.getPixel(x, y);
			}
		}
	}

	public static void setPixels(Bitmap bitmap, int[] pixels, int startX, int startY, int width, int height) {
		int endX = startX + width;
		int endY = startY + height;
		int counter = 0;
		for (int y = startY; y < endY; ++y) {
			for (int x = startX; x < endX; ++x) {
				bitmap.setPixel(x, y, pixels[counter++]);
			}
		}
	}
}
