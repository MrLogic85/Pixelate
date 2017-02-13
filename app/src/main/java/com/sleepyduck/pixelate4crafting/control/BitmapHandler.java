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
import com.sleepyduck.pixelate4crafting.util.BetterLog;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class BitmapHandler {

	public static Bitmap getFromFileName(Context context, String fileName) {
		try (InputStream is = context.openFileInput(fileName)) {
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inMutable = true;
			return BitmapFactory.decodeStream(is, null, options);
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
		try (InputStream is = context.openFileInput(fileName)) {
			return BitmapDrawable.createFromStream(is, fileName);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String getFileName(Context context, Uri uri) {
		if ("content".equals(uri.getScheme())) {
			try (Cursor cursor = context.getContentResolver().query(uri, null, null, null, null, null)) {
				if (cursor != null && cursor.moveToFirst()) {
					String displayName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
					if (displayName != null && displayName.length() > 0) {
						return displayName;
					}
				}
			}
		} else {
			return new File(uri.getPath()).getName();
		}
		return null;
	}

	public static String storePattern(Context context, Bitmap pattern, String fileName) {
		File file = new File(context.getFilesDir(), fileName + Constants.FILE_PATTERN);
		try (FileOutputStream fos = new FileOutputStream(file)) {
			pattern.compress(CompressFormat.PNG, 0, fos);
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
			try (InputStream is = openStream(context, uri)) {
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
			try (InputStream is = openStream(context, uri)) {
				options.inJustDecodeBounds = false;
				Bitmap orig = BitmapFactory.decodeStream(is, null, options);
				try (FileOutputStream fos = new FileOutputStream(new File(context.getFilesDir(), fileName))) {
					orig.compress(CompressFormat.PNG, 0, fos);
					orig.recycle();
				}
			}

			//--- Store thumbnail ---
			try (InputStream is = openStream(context, uri)) {
				int thumbSize = (int) context.getResources().getDimension(R.dimen.small_picture_size);
				Bitmap thumb = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeStream(is), thumbSize, thumbSize);
				try (FileOutputStream fos = new FileOutputStream(new File(context.getFilesDir(), fileName + Constants.FILE_THUMBNAIL))) {
					thumb.compress(CompressFormat.PNG, 100, fos);
					thumb.recycle();
				}
			}

			listener.onFileStored(fileName, fileName + Constants.FILE_THUMBNAIL);
		} catch (Exception e) {
			BetterLog.e(BitmapHandler.class, e);
		}
	}

	public static boolean removeFileOfName(Context context, String fileName) {
		BetterLog.d(BitmapHandler.class, "Removing file %s", fileName);
		File file = new File(context.getFilesDir(), fileName);
		return file.exists() && file.delete();
	}

	private static InputStream openStream(Context context, Uri uri) throws IOException {
		if ("content".equals(uri.getScheme()) || "file".equals(uri.getScheme())) {
			return context.getContentResolver().openInputStream(uri);
		} else {
			return context.getAssets().open(uri.getPath());
		}
	}

	public interface OnFileStoredListener {
		void onFileStored(String file, String thumbnail);
	}
}
