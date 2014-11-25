package com.sleepyduck.pixelate4crafting.data;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
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

	public static Bitmap getFromFileName(Context context, String fileName) {
		BetterLog.d(BitmapHandler.class, fileName);
		InputStream is;
		try {
			is = context.openFileInput(fileName);
			return BitmapFactory.decodeStream(is);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return null;
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

	public static void storeLocally(Context context, Uri uri, String fileName) {
		try {
			InputStream is = context.getContentResolver().openInputStream(uri);
			File file = new File(context.getFilesDir(), fileName);
			FileOutputStream os = new FileOutputStream(file);
			byte[] buffer = new byte[256];
			int read = 0;
			do {
				read = is.read(buffer);
				os.write(buffer, 0, read);
			} while (read == buffer.length);
			os.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
