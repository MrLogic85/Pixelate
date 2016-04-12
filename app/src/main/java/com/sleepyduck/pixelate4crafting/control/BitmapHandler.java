package com.sleepyduck.pixelate4crafting.control;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.provider.OpenableColumns;

import com.sleepyduck.pixelate4crafting.R;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;

public class BitmapHandler {

	public static Bitmap getFromFileName(Context context, String fileName) {
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
			FileOutputStream fos = new FileOutputStream(file);
			byte[] buffer = new byte[256];
			int read = 0;
			do {
				read = is.read(buffer);
				fos.write(buffer, 0, read);
			} while (read == buffer.length);
			is.close();
			fos.close();

			// Store thumbnail
			int thumbSize = (int) context.getResources().getDimension(R.dimen.small_picture_size);
			Bitmap thumb = ThumbnailUtils.extractThumbnail(getFromFileName(context, fileName), thumbSize, thumbSize);
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			thumb.compress(CompressFormat.PNG, 0 /* ignored for PNG */, bos);
			byte[] bitmapdata = bos.toByteArray();
			file = new File(context.getFilesDir(), fileName + Constants.FILE_THUMBNAIL);
			fos = new FileOutputStream(file);
			fos.write(bitmapdata, 0, bitmapdata.length);
			bos.close();
			fos.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
