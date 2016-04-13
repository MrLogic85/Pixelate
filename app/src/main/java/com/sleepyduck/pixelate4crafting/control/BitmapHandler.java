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
import com.sleepyduck.pixelate4crafting.control.util.BetterLog;

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
            //Store image
            InputStream is = context.getContentResolver().openInputStream(uri);
            int newWidth = (int) context.getResources().getDimension(R.dimen.picture_size);
            int newHeight;
            Bitmap orig = BitmapFactory.decodeStream(is);
            if (newWidth >= orig.getWidth()) {
                newWidth = orig.getWidth();
                newHeight = orig.getHeight();
                BetterLog.d(BitmapHandler.class, "Not scaling down from " + orig.getWidth());
            } else {
                BetterLog.d(BitmapHandler.class, "Scaling down from " + orig.getWidth() + " to " + newWidth);
                newHeight = newWidth * orig.getHeight() / orig.getWidth();
            }
            Bitmap image = ThumbnailUtils.extractThumbnail(orig, newWidth, newHeight);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            image.compress(CompressFormat.JPEG, 0, bos);
            byte[] bitmapdata = bos.toByteArray();
            File file = new File(context.getFilesDir(), fileName);
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(bitmapdata, 0, bitmapdata.length);
            bos.close();
            fos.close();
            is.close();

			// Store thumbnail
            is = context.getContentResolver().openInputStream(uri);
			int thumbSize = (int) context.getResources().getDimension(R.dimen.small_picture_size);
			Bitmap thumb = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeStream(is), thumbSize, thumbSize);
			bos = new ByteArrayOutputStream();
			thumb.compress(CompressFormat.PNG, 100, bos);
			bitmapdata = bos.toByteArray();
			file = new File(context.getFilesDir(), fileName + Constants.FILE_THUMBNAIL);
			fos = new FileOutputStream(file);
			fos.write(bitmapdata, 0, bitmapdata.length);
			bos.close();
			fos.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void removeFileOfName(Context context, String fileName) {
		File file = new File(context.getFilesDir(), fileName);
		if (file != null) {
			file.delete();
		}
	}
}
