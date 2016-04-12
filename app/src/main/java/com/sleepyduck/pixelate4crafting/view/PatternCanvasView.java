package com.sleepyduck.pixelate4crafting.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.sleepyduck.pixelate4crafting.control.Constants;
import com.sleepyduck.pixelate4crafting.model.Pattern;

public class PatternCanvasView extends ImageView {
	private Bitmap mImageBitmap;
	private BitmapAsyncTask mBitmapAsyncTask = new BitmapAsyncTask();
	private Pattern mPattern;

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
		return bundle;
	}

	@Override
	protected void onRestoreInstanceState(Parcelable state) {
		if (state instanceof Bundle) {
			Bundle bundle = (Bundle) state;
			setImageBitmap((Bitmap) bundle.get("image_bitmap"));
			super.onRestoreInstanceState(bundle.getParcelable("super"));
		}
	}

	@Override
	public void setImageBitmap(Bitmap bm) {
		if (bm != null) {
			mImageBitmap = bm;
			executeRedraw();
		}
	}

	public void executeRedraw() {
		mBitmapAsyncTask.cancel(true);
		mBitmapAsyncTask = new BitmapAsyncTask();
		mBitmapAsyncTask.execute(mImageBitmap);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
	}

	public void setPattern(Pattern pattern) {
		mPattern = pattern;
	}

	private final class BitmapAsyncTask extends AsyncTask<Bitmap, Bitmap, Bitmap> {
		int pixelSize;
		private int[][] pixels;
		private Bitmap pixelBitmap;
		int pixelsWidth, pixelsHeight;

		@Override
		protected Bitmap doInBackground(Bitmap... params) {
			pixelsWidth = mPattern.getPixelWidth();
			pixelsHeight = mPattern.getPixelHeight();
			pixels = mPattern.getColorMatrix();

			boolean drawGrid = true;
			pixelSize = 10;
			if (drawGrid) {
				pixelSize++;
			}
			int resultWidth = pixelsWidth * pixelSize;
			int resultHeight = pixelsHeight * pixelSize;
			if (drawGrid) {
				resultWidth += pixelSize;
				resultHeight += pixelSize;
			}
			pixelBitmap = Bitmap.createBitmap(resultWidth, resultHeight, Config.ARGB_8888);
			for (int y = 0; y < pixelsHeight; ++y) {
				for (int x = 0; x < pixelsWidth; ++x) {
					setColor(pixelBitmap, pixels[x][y], x, y, drawGrid);
				}
				if (isCancelled()) {
					return null;
				}
			}
			if (drawGrid) {
				for (int y = 0; y <= pixelsHeight; ++y) {
					for (int x = 0; x <= pixelsWidth; ++x) {
						drawGrid(pixelBitmap, x, y);
					}
					if (isCancelled()) {
						return null;
					}
				}
			}

			return pixelBitmap;
		}

		private void drawGrid(Bitmap bitmap, int x, int y) {
			boolean isTenthY = (y % 10 == 0);
			boolean isTenthX = (x % 10 == 0);
			for (int Y = y * pixelSize; Y < (y + 1) * pixelSize; ++Y) {
				int X = (x + 1) * pixelSize - 1;
				if (isTenthX || y > 0) {
					bitmap.setPixel(X, Y, Color.BLACK);
				}
				if (isTenthX) {
					bitmap.setPixel(X - 1, Y, Color.BLACK);
				}
			}
			for (int X = x * pixelSize; X < (x + 1) * pixelSize; ++X) {
				int Y = (y + 1) * pixelSize - 1;
				if (isTenthY || x > 0) {
					bitmap.setPixel(X, Y, Color.BLACK);
				}
				if (isTenthY) {
					bitmap.setPixel(X, Y - 1, Color.BLACK);
				}
			}
		}

		private void setColor(Bitmap bitmap, int color, int x, int y, boolean drawGrid) {
			if (drawGrid) {
				x++;
				y++;
			}
			for (int Y = y * pixelSize; Y < (y + 1) * pixelSize; ++Y) {
				for (int X = x * pixelSize; X < (x + 1) * pixelSize; ++X) {
					bitmap.setPixel(X, Y, color);
				}
			}
		}

		@Override
		protected void onPostExecute(Bitmap bitmap) {
			if (bitmap != null) {
				PatternCanvasView.super.setImageBitmap(bitmap);
			}
		}

	}

	public int getBitmapWidth() {
		if (mImageBitmap != null) {
			return mImageBitmap.getWidth();
		} else {
			return -1;
		}
	}

	public int getBitmapHeight() {
		if (mImageBitmap != null) {
			return mImageBitmap.getHeight();
		} else {
			return -1;
		}
	}

}
