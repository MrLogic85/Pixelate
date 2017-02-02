package com.sleepyduck.pixelate4crafting.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

import com.sleepyduck.pixelate4crafting.util.BetterLog;

public class InteractiveImageView extends ImageView implements View.OnTouchListener {
    private Matrix mMatrix;
    protected Bitmap mImageBitmap;
    private OnImageClickListener mImageListener;

    private final GestureDetector mGestureDetector = new GestureDetector(getContext(), new GestureDetector.OnGestureListener() {
        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public void onShowPress(MotionEvent e) {
        }

        @Override
        public boolean onSingleTapUp(MotionEvent ev) {
            if (mImageListener != null) {
                Matrix inv = new Matrix();
                mMatrix.invert(inv);
                float[] point = {ev.getX(), ev.getY()};
                inv.mapPoints(point);
                mImageListener.onImageClicked(mImageBitmap, (int) point[0], (int) point[1], ev.getX(), ev.getY());
            }
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

    private final ScaleGestureDetector mScaleDetector = new ScaleGestureDetector(getContext(), new ScaleGestureDetector.OnScaleGestureListener() {
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
            Matrix inv = new Matrix();
            mMatrix.invert(inv);
            float x = getWidth() > getHeight() ? (getWidth()-getHeight())/2 : 0;
            float y = getWidth() < getHeight() ? (getHeight()-getWidth())/2 : 0;
            float size = getWidth() > getHeight() ? getHeight() : getWidth();
            float[] square = {x, y, size, size};
            inv.mapPoints(square);
            bundle.putFloatArray("square", square);
        }
        return bundle;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle) {
            Bundle bundle = (Bundle) state;
            if (bundle.containsKey("square")) {
                final float[] square = bundle.getFloatArray("square");
                getViewTreeObserver().addOnGlobalLayoutListener(
                        new ViewTreeObserver.OnGlobalLayoutListener() {
                            @Override
                            public void onGlobalLayout() {
                                getViewTreeObserver().removeOnGlobalLayoutListener(this);
                                mMatrix = new Matrix(getImageMatrix());
                                RectF from = new RectF(square[0], square[1], square[2], square[3]);
                                float x = getWidth() > getHeight() ? (getWidth()-getHeight())/2 : 0;
                                float y = getWidth() < getHeight() ? (getHeight()-getWidth())/2 : 0;
                                float size = getWidth() > getHeight() ? getHeight() : getWidth();
                                RectF to = new RectF(x, y, size+x, size+y);
                                mMatrix.setRectToRect(from, to, Matrix.ScaleToFit.START);
                                setImageMatrix(mMatrix);
                                BetterLog.d(this, "Found image matrix " + mMatrix);
                            }
                        });
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
                    if (mMatrix == null) {
                        scaleToFit();
                    }
                }
            });
        }
    }

    public void scaleToFit() {
        if (mImageBitmap != null
                && getWidth() > 0
                && getHeight() > 0) {
            mMatrix = new Matrix(getImageMatrix());
            RectF drawableRect = new RectF(0, 0, mImageBitmap.getWidth(), mImageBitmap.getHeight());
            RectF viewRect = new RectF(0, 0, getWidth(), getHeight());
            mMatrix.setRectToRect(drawableRect, viewRect, Matrix.ScaleToFit.CENTER);
            setImageMatrix(mMatrix);
            BetterLog.d(this, "Scaling mPattern to fit screen (" + getWidth() + ", " + getHeight() + ") with " + mMatrix);
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        mScaleDetector.onTouchEvent(event);
        mGestureDetector.onTouchEvent(event);
        return true;
    }

    public void setOnImageClickListener(OnImageClickListener listener) {
        mImageListener = listener;
    }

    public interface OnImageClickListener {
        void onImageClicked(Bitmap bitmap, int x, int y, float rawX, float rawY);
    }
}
