package com.sleepyduck.pixelate4crafting.view.pattern;

import java.util.Arrays;

import com.sleepyduck.pixelate4crafting.BetterLog;
import com.sleepyduck.pixelate4crafting.data.BitmapHandler;
import com.sleepyduck.pixelate4crafting.data.Constants;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.widget.ImageView;

public class PatternCanvasView extends ImageView {
	public enum ColorSelectionModel {
		Average, Median
	}

	private static int UPDATE_FREQUENCE = 5;

	private int mPixelsWidth;
	private int mPixelsHeight;
	private Bitmap mImageBitmap;
	private Bitmap mPixelBitmap;
	//private Bitmap mDrawBitmap;
	private BitmapAsyncTask mBitmapAsyncTask = new BitmapAsyncTask();
	private ColorSelectionModel mColorSelectionModel = ColorSelectionModel.Median;

	public PatternCanvasView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public PatternCanvasView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public PatternCanvasView(Context context) {
		super(context);
	}

	@Override
	protected void onDetachedFromWindow() {
		mBitmapAsyncTask.cancel(true);
		super.onDetachedFromWindow();
	}

	@Override
	protected Parcelable onSaveInstanceState() {
		Parcelable superState = super.onSaveInstanceState();
		Bundle bundle = new Bundle();
		bundle.putParcelable("super", superState);
		bundle.putParcelable("image_bitmap", mImageBitmap);
		bundle.putParcelable("pixel_bitmap", mPixelBitmap);
		return bundle;
	}

	@Override
	protected void onRestoreInstanceState(Parcelable state) {
		if (state instanceof Bundle) {
			Bundle bundle = (Bundle) state;
			mPixelBitmap = (Bitmap) bundle.get("pixel_bitmap");
			setImageBitmap((Bitmap) bundle.get("image_bitmap"));
			super.onRestoreInstanceState(bundle.getParcelable("super"));
		}
	}

	@Override
	public void setImageBitmap(Bitmap bm) {
		BetterLog.d(this);
		mImageBitmap = bm;
		mPixelsWidth = Constants.NUM_PIXELS;
		mPixelsHeight = Constants.NUM_PIXELS * bm.getHeight() / bm.getWidth();
		executeRedraw();
	}

	private void executeRedraw() {
		mBitmapAsyncTask.cancel(true);
		mBitmapAsyncTask = new BitmapAsyncTask();
		mBitmapAsyncTask.execute(mImageBitmap);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
	}

	public void setColorSelectionModel(ColorSelectionModel model) {
		if (mColorSelectionModel != model) {
			BetterLog.d(this);
			mColorSelectionModel = model;
			executeRedraw();
		}
	}

	private final class BitmapAsyncTask extends AsyncTask<Bitmap, Bitmap, Bitmap> {
		int[] pixels;

		@Override
		protected Bitmap doInBackground(Bitmap... params) {
			BetterLog.d(this);
			Bitmap bitmap = params[0];
			if (bitmap == null) {
				return null;
			}
			if (mPixelBitmap == null || mPixelBitmap.getWidth() != bitmap.getWidth()
					|| mPixelBitmap.getHeight() != bitmap.getHeight()) {
				mPixelBitmap = bitmap.copy(bitmap.getConfig(), true);
				//mDrawBitmap = bitmap.copy(bitmap.getConfig(), true);
			}
			int pixelWidth = bitmap.getWidth() / mPixelsWidth;
			int pixelHeight = bitmap.getHeight() / mPixelsHeight;
			int tmpPixelsWidth = bitmap.getWidth() / pixelWidth;
			int tmpPixelsHeight = bitmap.getHeight() / pixelHeight;
			pixels = new int[pixelWidth * pixelHeight];
			BetterLog.d(this, "bitmapWidth = " + bitmap.getWidth() + ", bitmapHeight = " + bitmap.getHeight()
					+ ", pixelWidth = " + pixelWidth + ", pixelHeight = " + pixelHeight + ", mPixelsWidth = "
					+ mPixelsWidth + ", mPixelsHeight = " + mPixelsHeight + "");
			int updateCounter = 0;
			for (int y = 0; y < tmpPixelsHeight; ++y) {
				for (int x = 0; x < tmpPixelsWidth; ++x) {
					int color = getColor(bitmap, x * pixelWidth, y * pixelHeight, pixelWidth, pixelHeight);
					Arrays.fill(pixels, color);
					BitmapHandler.setPixels(mPixelBitmap, pixels, x * pixelWidth, y * pixelHeight, pixelWidth,
							pixelHeight);
				}
				Arrays.fill(pixels, Color.WHITE);
				BitmapHandler.setPixels(mPixelBitmap, pixels, tmpPixelsWidth * pixelWidth, tmpPixelsHeight
						* pixelHeight, mPixelBitmap.getWidth() - tmpPixelsWidth * pixelWidth - 1,
						mPixelBitmap.getHeight() - tmpPixelsHeight * pixelHeight - 1);
				if (isCancelled()) {
					return null;
				} else if (updateCounter++ >= UPDATE_FREQUENCE) {
					publishProgress(mPixelBitmap);//.copy(bitmap.getConfig(), false));
					updateCounter = 0;
				}
			}
			return mPixelBitmap;//.copy(bitmap.getConfig(), false);
		}

		private int getColor(Bitmap bitmap, int x, int y, int pixelWidth, int pixelHeight) {
			switch (mColorSelectionModel) {
				case Median:
					return bitmap.getPixel(x + pixelWidth / 2, y + pixelHeight / 2);
				case Average:
					BitmapHandler.getPixels(bitmap, pixels, x, y, pixelWidth, pixelHeight);
					return getAverageValue(pixels);
			}
			return Color.TRANSPARENT;
		}

		private int getAverageValue(int[] pixels) {
			long A = 0, R = 0, G = 0, B = 0;
			for (int color : pixels) {
				A += Color.alpha(color);
				R += Color.red(color);
				G += Color.green(color);
				B += Color.blue(color);
			}
			int length = pixels.length;
			A /= length;
			R /= length;
			G /= length;
			B /= length;
			return Color.argb((int) A, (int) R, (int) G, (int) B);
		}

		@Override
		protected void onProgressUpdate(Bitmap... values) {
			//mDrawBitmap.recycle();
			//mDrawBitmap = values[0];
			PatternCanvasView.super.setImageBitmap(values[0]);
		}

		@Override
		protected void onPostExecute(Bitmap bitmap) {
			if (bitmap != null) {
				//mDrawBitmap.recycle();
				//mDrawBitmap = bitmap;
				PatternCanvasView.super.setImageBitmap(bitmap);
			}
		}

	}

	public int getPixelWidth() {
		return mPixelsWidth;
	}

	public int getPixelHeight() {
		return mPixelsHeight;
	}

	public void setPixelWidth(int newVal) {
		mPixelsWidth = newVal;
		executeRedraw();
	}

	public void setPixelHeight(int newVal) {
		mPixelsHeight = newVal;
		executeRedraw();
	}

}
