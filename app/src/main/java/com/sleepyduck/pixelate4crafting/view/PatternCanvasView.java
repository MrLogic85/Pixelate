package com.sleepyduck.pixelate4crafting.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.ImageView;

import com.sleepyduck.pixelate4crafting.control.util.BetterLog;
import com.sleepyduck.pixelate4crafting.model.Pattern;

import java.util.Arrays;

public class PatternCanvasView extends ImageView implements View.OnTouchListener {
    private float MAX_SCALE = 10.0f;
    private float MIN_SCALE = 0.1f;
    private Bitmap mImageBitmap;
	private BitmapAsyncTask mBitmapAsyncTask = new BitmapAsyncTask();
	private Pattern mPattern;
    private Matrix mMatrix;
    float mScale, mX, mY;

    private GestureDetector mGestureDetector = new GestureDetector(getContext(), new GestureDetector.OnGestureListener() {
        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public void onShowPress(MotionEvent e) {

        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            return false;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            if (!mScaleDetector.isInProgress()) {
                mMatrix.postTranslate(-distanceX, -distanceY);
                setImageMatrix(mMatrix);
                //checkMatrixBounds();
                return true;
            }
            return false;
        }

        @Override
        public void onLongPress(MotionEvent e) {

        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            return false;
        }
    });

    private ScaleGestureDetector mScaleDetector = new ScaleGestureDetector(getContext(), new ScaleGestureDetector.OnScaleGestureListener() {
        float lastX, lastY;

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            // Scale
            float scale = detector.getScaleFactor();
            mMatrix.postScale(scale, scale, detector.getFocusX(), detector.getFocusY());

            // Move
            mMatrix.postTranslate(detector.getFocusX()- lastX, detector.getFocusY()- lastY);
            lastX = detector.getFocusX();
            lastY = detector.getFocusY();
            setImageMatrix(mMatrix);
            return true;
        }

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            lastX = detector.getFocusX();
            lastY = detector.getFocusY();
            return true;
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {
        }
    });

    public PatternCanvasView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
        setOnTouchListener(this);
	}

	public PatternCanvasView(Context context, AttributeSet attrs) {
		super(context, attrs);
        setOnTouchListener(this);
	}

	public PatternCanvasView(Context context) {
		super(context);
        setOnTouchListener(this);
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
        if (mMatrix != null) {
            float[] values = new float[9];
            mMatrix.getValues(values);
            bundle.putFloatArray("image_matrix", values);
        }
		return bundle;
	}

	@Override
	protected void onRestoreInstanceState(Parcelable state) {
		if (state instanceof Bundle) {
			Bundle bundle = (Bundle) state;
            mImageBitmap = bundle.getParcelable("image_bitmap");
			setImageBitmap(mImageBitmap);
            if (bundle.containsKey("image_matrix")) {
                mMatrix = new Matrix();
                mMatrix.setValues(bundle.getFloatArray("image_matrix"));
                setImageMatrix(mMatrix);
                BetterLog.d(this, "Found image matrix " + mMatrix);
            }
			super.onRestoreInstanceState(bundle.getParcelable("super"));
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
        if (mImageBitmap == null) {
            executeRedraw();
        }
	}

    @Override
    public void setImageBitmap(Bitmap bm) {
        super.setImageBitmap(bm);
        BetterLog.d(this, "Bitmap redrawn");

        if (mMatrix == null) {
            scaletoFit();
        }
    }

    private void scaletoFit() {
        mMatrix = getImageMatrix();
        RectF drawableRect = new RectF(0, 0, mImageBitmap.getWidth(), mImageBitmap.getHeight());
        RectF viewRect = new RectF(0, 0, getWidth(), getHeight());
        mMatrix.setRectToRect(drawableRect, viewRect, Matrix.ScaleToFit.CENTER);
        setImageMatrix(mMatrix);
        BetterLog.d(this, "Scaling pattern to fit screen with " + mMatrix);
    }

    private void checkMatrixBounds() {
        float[] values = new float[9];
        mMatrix.getValues(values);
        BetterLog.d(this, "Check Matrix, before " + Arrays.toString(values));
        double scale = Math.sqrt(values[0]*values[0] + values[1]*values[1]);
        if (scale > MAX_SCALE) {
            mMatrix.setScale(MAX_SCALE, MAX_SCALE, values[1], values[4]);
        } else if (scale < MIN_SCALE) {
            mMatrix.setScale(MIN_SCALE, MIN_SCALE, values[1], values[4]);
        }
        mMatrix.getValues(values);
        BetterLog.d(this, "Check Matrix, after " + Arrays.toString(values));
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        mScaleDetector.onTouchEvent(event);
        mGestureDetector.onTouchEvent(event);
        return true;
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

			pixelSize = 11; // 10 per pixel plus 1 for grid
			int resultWidth = (pixelsWidth + 1) * pixelSize;
			int resultHeight = (pixelsHeight + 1) * pixelSize;

			pixelBitmap = Bitmap.createBitmap(resultWidth, resultHeight, Config.ARGB_8888);
			// Draw colors
			for (int y = 0; y < pixelsHeight; ++y) {
				for (int x = 0; x < pixelsWidth; ++x) {
					setColor(pixelBitmap, pixels[x][y], x, y);
				}
				if (isCancelled()) {
					return null;
				}
			}
			// Draw grid
			for (int y = 0; y <= pixelsHeight; ++y) {
				for (int x = 0; x <= pixelsWidth; ++x) {
					drawGrid(pixelBitmap, x, y);
				}
				if (isCancelled()) {
					return null;
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

		private void setColor(Bitmap bitmap, int color, int x, int y) {
			// Add one to x and y due to extended grid
			x++;
			y++;
			for (int Y = y * pixelSize; Y < (y + 1) * pixelSize; ++Y) {
				for (int X = x * pixelSize; X < (x + 1) * pixelSize; ++X) {
					bitmap.setPixel(X, Y, color);
				}
			}
		}

		@Override
		protected void onPostExecute(Bitmap bitmap) {
			if (bitmap != null) {
                mImageBitmap = bitmap;
				setImageBitmap(bitmap);
			}
		}
	}
}
