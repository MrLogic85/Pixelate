package com.sleepyduck.pixelate4crafting.view.pattern;

import java.util.Arrays;

import com.sleepyduck.pixelate4crafting.data.Constants;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.widget.ImageView;

public class PatternCanvasView extends ImageView {
	public enum ColorSelectionModel {
		Average, Median
	}

	private int mPixelsWidth = Constants.NUM_PIXELS;
	private int mPixelsHeight = Constants.NUM_PIXELS;
	private Bitmap mImageBitmap;
	private Bitmap mPixelBitmap;
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
		bundle.putInt("pixel_width", mPixelsWidth);
		bundle.putInt("pixel_height", mPixelsHeight);
		bundle.putSerializable("color_model", mColorSelectionModel);
		bundle.putParcelable("image_bitmap", mImageBitmap);
		bundle.putParcelable("pixel_bitmap", mPixelBitmap);
		return bundle;
	}

	@Override
	protected void onRestoreInstanceState(Parcelable state) {
		if (state instanceof Bundle) {
			Bundle bundle = (Bundle) state;
			mPixelsWidth = bundle.getInt("pixel_width");
			mPixelsHeight = bundle.getInt("pixel_height");
			mColorSelectionModel = (ColorSelectionModel) bundle.getSerializable("color_model");
			mPixelBitmap = (Bitmap) bundle.get("pixel_bitmap");
			setImageBitmap((Bitmap) bundle.get("image_bitmap"));
			super.setImageBitmap(mPixelBitmap);
			super.onRestoreInstanceState(bundle.getParcelable("super"));
		}
	}

	@Override
	public void setImageBitmap(Bitmap bm) {
		if (bm != null) {
			mImageBitmap = bm;
			setPixelHeight(mPixelsWidth * bm.getHeight() / bm.getWidth());
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

	public void setColorSelectionModel(ColorSelectionModel model) {
		if (mColorSelectionModel != model) {
			mColorSelectionModel = model;
			executeRedraw();
		}
	}

	private final class BitmapAsyncTask extends AsyncTask<Bitmap, Bitmap, Bitmap> {
		int mPixelSize;
		private int[][] mPixels;

		@Override
		protected Bitmap doInBackground(Bitmap... params) {
			Bitmap bitmap = params[0];
			if (bitmap == null || getWidth() == 0) {
				return null;
			}

			mPixels = new int[mPixelsWidth][mPixelsHeight];
			float pixelWidth = (float) bitmap.getWidth() / (float) mPixelsWidth;
			float pixelHeight = (float) bitmap.getHeight() / (float) mPixelsHeight;
			for (int y = 0; y < mPixelsHeight; ++y) {
				for (int x = 0; x < mPixelsWidth; ++x) {
					int color = getColor(bitmap, (float) x * pixelWidth, (float) y * pixelHeight, pixelWidth,
							pixelHeight);
					mPixels[x][y] = color;
				}
				if (isCancelled()) {
					return null;
				}
			}

			boolean drawGrid = true;
			mPixelSize = Math.min(getWidth() / mPixelsWidth, getHeight() / mPixelsHeight);
			if (drawGrid) {
				mPixelSize++;
			}
			int resultWidth = mPixelsWidth * mPixelSize;
			int resultHeight = mPixelsHeight * mPixelSize;
			if (drawGrid) {
				resultWidth += mPixelSize;
				resultHeight += mPixelSize;
			}
			mPixelBitmap = Bitmap.createBitmap(resultWidth, resultHeight, Config.ARGB_8888);
			for (int y = 0; y < mPixelsHeight; ++y) {
				for (int x = 0; x < mPixelsWidth; ++x) {
					setColor(mPixelBitmap, mPixels[x][y], x, y, drawGrid);
				}
				if (isCancelled()) {
					return null;
				}
			}
			if (drawGrid) {
				for (int y = 0; y <= mPixelsHeight; ++y) {
					for (int x = 0; x <= mPixelsWidth; ++x) {
						drawGrid(mPixelBitmap, x, y);
					}
					if (isCancelled()) {
						return null;
					}
				}
			}

			return mPixelBitmap;
		}

		private void drawGrid(Bitmap bitmap, int x, int y) {
			boolean isTenthY = (y % 10 == 0);
			boolean isTenthX = (x % 10 == 0);
			for (int Y = y * mPixelSize; Y < (y + 1) * mPixelSize; ++Y) {
				int X = (x + 1) * mPixelSize - 1;
				if (isTenthX || y > 0) {
					bitmap.setPixel(X, Y, Color.BLACK);
				}
				if (isTenthX) {
					bitmap.setPixel(X - 1, Y, Color.BLACK);
				}
			}
			for (int X = x * mPixelSize; X < (x + 1) * mPixelSize; ++X) {
				int Y = (y + 1) * mPixelSize - 1;
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
			for (int Y = y * mPixelSize; Y < (y + 1) * mPixelSize; ++Y) {
				for (int X = x * mPixelSize; X < (x + 1) * mPixelSize; ++X) {
					bitmap.setPixel(X, Y, color);
				}
			}
		}

		private int getColor(Bitmap bitmap, float x, float y, float width, float height) {
			switch (mColorSelectionModel) {
				case Median:
					return bitmap.getPixel((int) (x + width / 2.0), (int) (y + height / 2.0));
				case Average:
					int intX = (int) x;
					int intY = (int) y;
					int intW = (int) Math.ceil(x + width) - intX;
					int intH = (int) Math.ceil(y + height) - intY;
					float weightMinX = (float) (intX + 1) - x;
					float weightMaxX = x + width - (float) (intW - 1) - intX;
					float weightMinY = (float) (intY + 1) - y;
					float weightMaxY = y + height - (float) (intH - 1) - intY;
					double A = 0,
					R = 0,
					G = 0,
					B = 0;
					int color;
					double weight,
					totalWeight = 0;
					for (int X = intX; X < intX + intW; ++X) {
						for (int Y = intY; Y < intY + intH; ++Y) {
							// Due to rounding errors we need to check if we are
							// within the bitmap
							if (X >= bitmap.getWidth() || Y >= bitmap.getHeight()) {
								color = 0;
								weight = 0;
							} else {
								color = bitmap.getPixel(X, Y);
								weight = 1;
								if (X == intX) {
									weight *= weightMinX;
								} else if (X == intX + intW - 1) {
									weight *= weightMaxX;
								}
								if (Y == intY) {
									weight *= weightMinY;
								} else if (Y == intY + intH - 1) {
									weight *= weightMaxY;
								}
							}
							totalWeight += weight;
							A += Color.alpha(color) * weight;
							R += Color.red(color) * weight;
							G += Color.green(color) * weight;
							B += Color.blue(color) * weight;
						}
					}
					A /= totalWeight;
					R /= totalWeight;
					G /= totalWeight;
					B /= totalWeight;
					return Color.argb((int) A, (int) R, (int) G, (int) B);
			}
			return Color.TRANSPARENT;
		}

		@Override
		protected void onPostExecute(Bitmap bitmap) {
			if (bitmap != null) {
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

	public int getBitmapWidth() {
		return mImageBitmap.getWidth();
	}

	public int getBitmapHeight() {
		return mImageBitmap.getHeight();
	}

}
