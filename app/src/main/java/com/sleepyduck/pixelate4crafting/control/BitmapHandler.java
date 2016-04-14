package com.sleepyduck.pixelate4crafting.control;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.provider.OpenableColumns;

import com.sleepyduck.pixelate4crafting.R;
import com.sleepyduck.pixelate4crafting.control.util.BetterLog;
import com.sleepyduck.pixelate4crafting.control.util.ColorUtil;

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
            //--- Store image ---
            // Get image and create a compressed copy
            InputStream is = context.getContentResolver().openInputStream(uri);
            Bitmap orig = BitmapFactory.decodeStream(is);
            is.close();
            Bitmap image = Bitmap.createBitmap(orig);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            image.compress(CompressFormat.JPEG, 0, bos);
            byte[] bitmapdata = bos.toByteArray();
            image.recycle();
            bos.close();

            // Save the compressed image
            File file = new File(context.getFilesDir(), fileName);
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(bitmapdata, 0, bitmapdata.length);
            fos.close();

            // Read the image back up again
            image = getFromFileName(context, fileName);

            // Copy transparent pixels to the jpeg
            for (int x = 0; x < orig.getWidth(); ++x) {
                for (int y = 0; y < orig.getHeight(); ++y) {
                    // TODO Recycled!
                    int pixel = orig.getPixel(x, y);
                    if ((pixel & ColorUtil.ALPHA_CHANNEL) != ColorUtil.ALPHA_CHANNEL) {
                        image.setPixel(x, y, Color.TRANSPARENT);
                    }
                }
            }
            orig.recycle();
            bos = new ByteArrayOutputStream();
            image.compress(CompressFormat.PNG, 0, bos);
            bitmapdata = bos.toByteArray();
            image.recycle();
            bos.close();

            // Save png with transparent pixels
            file = new File(context.getFilesDir(), fileName);
            fos = new FileOutputStream(file);
            fos.write(bitmapdata, 0, bitmapdata.length);
            fos.close();

			//--- Store thumbnail ---
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
