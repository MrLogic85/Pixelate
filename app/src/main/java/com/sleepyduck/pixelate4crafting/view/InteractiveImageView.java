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
import android.view.ViewTreeObserver;
import android.widget.ImageView;

import com.sleepyduck.pixelate4crafting.control.util.BetterLog;
import com.sleepyduck.pixelate4crafting.model.Pattern;

import java.util.Arrays;

public class InteractiveImageView extends ImageView implements View.OnTouchListener {
    private Matrix mMatrix;
    private Bitmap mImageBitmap;

    private GestureDetector mGestureDetector = new GestureDetector(getContext(), new GestureDetector.OnGestureListener() {
        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public void onShowPress(MotionEvent e) {}

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            return false;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            if (!mScaleDetector.isInProgress() && mMatrix != null) {
                mMatrix.postTranslate(-distanceX, -distanceY);
                setImageMatrix(mMatrix);
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
            if (mMatrix != null) {
                // Scale
                float scale = detector.getScaleFactor();
                mMatrix.postScale(scale, scale, detector.getFocusX(), detector.getFocusY());

                // Move
                mMatrix.postTranslate(detector.getFocusX() - lastX, detector.getFocusY() - lastY);
                lastX = detector.getFocusX();
                lastY = detector.getFocusY();
                setImageMatrix(mMatrix);
            }
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

    public InteractiveImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
        setOnTouchListener(this);
	}

	public InteractiveImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
        setOnTouchListener(this);
	}

	public InteractiveImageView(Context context) {
		super(context);
        setOnTouchListener(this);
	}

	@Override
	protected Parcelable onSaveInstanceState() {
		Parcelable superState = super.onSaveInstanceState();
		Bundle bundle = new Bundle();
		bundle.putParcelable("super", superState);
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
            if (bundle.containsKey("image_matrix")) {
                mMatrix = new Matrix();
                mMatrix.setValues(bundle.getFloatArray("image_matrix"));
                setImageMatrix(mMatrix);
                BetterLog.d(this, "Found image matrix " + mMatrix);
            }
			super.onRestoreInstanceState(bundle.getParcelable("super"));
		}
	}

	@Override
	public void setImageBitmap(Bitmap bm) {
        super.setImageBitmap(bm);
        mImageBitmap = bm;

        if (mMatrix == null) {
            getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    scaleToFit();
                }
            });
        }
	}

    private void scaleToFit() {
        if (mImageBitmap != null
                && getWidth() > 0
                && getHeight() > 0) {
            mMatrix = getImageMatrix();
            RectF drawableRect = new RectF(0, 0, mImageBitmap.getWidth(), mImageBitmap.getHeight());
            RectF viewRect = new RectF(0, 0, getWidth(), getHeight());
            mMatrix.setRectToRect(drawableRect, viewRect, Matrix.ScaleToFit.CENTER);
            setImageMatrix(mMatrix);
            BetterLog.d(this, "Scaling pattern to fit screen (" + getWidth() + ", " + getHeight() + ") with " + mMatrix);
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        mScaleDetector.onTouchEvent(event);
        mGestureDetector.onTouchEvent(event);
        return true;
    }
}
