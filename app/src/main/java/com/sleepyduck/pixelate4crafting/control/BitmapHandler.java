package com.sleepyduck.pixelate4crafting.control;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.provider.OpenableColumns;

import com.sleepyduck.pixelate4crafting.R;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class BitmapHandler {

	public static Bitmap getFromFileName(Context context, String fileName) {
		try (InputStream is = context.openFileInput(fileName)) {
			return BitmapFactory.decodeStream(is);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static void getSize(Context context, String fileName, Rect outSize) {
		try (InputStream is = context.openFileInput(fileName)) {
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inJustDecodeBounds = true;
			BitmapFactory.decodeStream(is, null, options);
			outSize.set(0, 0, options.outWidth, options.outHeight);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static Drawable getDrawableFromFileName(Context context, String fileName) {
		InputStream is;
		try {
			is = context.openFileInput(fileName);
			return BitmapDrawable.createFromStream(is, fileName);
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

	public static String storePattern(Context context, Bitmap pattern, String fileName) {
		try {
			//--- Store image ---
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			pattern.compress(CompressFormat.PNG, 0, bos);
			byte[] bitmapdata = bos.toByteArray();
			bos.close();
			File file = new File(context.getFilesDir(), fileName + Constants.FILE_PATTERN);
			FileOutputStream fos = new FileOutputStream(file);
			fos.write(bitmapdata, 0, bitmapdata.length);
			fos.close();

			return fileName + Constants.FILE_PATTERN;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static void storeLocally(Context context, Uri uri, String title, OnFileStoredListener listener) {
		try {
			String fileName = title + (int) (Math.random() * 99999999);

			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inDensity = context.getResources().getDisplayMetrics().densityDpi;
			options.inSampleSize = 1;
            //--- Calculate sample size ---
			try (InputStream is = context.getContentResolver().openInputStream(uri)) {
				options.inJustDecodeBounds = true;
				BitmapFactory.decodeStream(is, null, options);
				int pixels = options.outHeight * options.outHeight;
				int maxPixels = context.getResources().getInteger(R.integer.max_num_pixels_in_orig_image);
				while (pixels > maxPixels) {
					options.inSampleSize *= 2;
					pixels /= 4;
				}
			}
			//--- Store image ---
			try (InputStream is = context.getContentResolver().openInputStream(uri)) {
				options.inJustDecodeBounds = false;
				Bitmap orig = BitmapFactory.decodeStream(is, null, options);
				try (FileOutputStream fos = new FileOutputStream(new File(context.getFilesDir(), fileName))) {
					orig.compress(CompressFormat.PNG, 0, fos);
					orig.recycle();
				}
			}

			//--- Store thumbnail ---
			try (InputStream is = context.getContentResolver().openInputStream(uri)) {
				int thumbSize = (int) context.getResources().getDimension(R.dimen.small_picture_size);
				Bitmap thumb = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeStream(is), thumbSize, thumbSize);
				try (FileOutputStream fos = new FileOutputStream(new File(context.getFilesDir(), fileName + Constants.FILE_THUMBNAIL))) {
					thumb.compress(CompressFormat.PNG, 100, fos);
					thumb.recycle();
				}
			}

			listener.onFileStored(fileName, fileName + Constants.FILE_THUMBNAIL);
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

	public interface OnFileStoredListener {
		void onFileStored(String file, String thumbnail);
	}
}
