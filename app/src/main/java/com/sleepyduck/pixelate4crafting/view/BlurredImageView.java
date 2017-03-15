package com.sleepyduck.pixelate4crafting.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.util.AttributeSet;

/**
 * Image view that draws a blurred zoomed version of the image behind the orig image
 */
public class BlurredImageView extends android.support.v7.widget.AppCompatImageView {
    private Matrix zoom;
    private Bitmap scaledBitmap;
    private Paint paint = new Paint();

    public BlurredImageView(Context context) {
        this(context, null);
    }

    public BlurredImageView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BlurredImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        paint.setAlpha(128);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Drawable drawable = getDrawable();
        if (drawable == null) {
            drawable = getBackground();
        }
        Bitmap bitmap = null;
        if (drawable != null && drawable instanceof BitmapDrawable) {
            bitmap = ((BitmapDrawable) drawable).getBitmap();
        }
        if (bitmap != null) {
            if (zoom == null) {
                performZoom(bitmap);
            }
            canvas.drawBitmap(scaledBitmap, zoom, paint);
        }
        super.onDraw(canvas);
    }

    private void performZoom(Bitmap bitmap) {
        scaledBitmap = Bitmap.createScaledBitmap(bitmap, bitmap.getWidth() / 8, bitmap.getHeight() / 8, true);
        scaledBitmap = Bitmap.createScaledBitmap(scaledBitmap, bitmap.getWidth(), bitmap.getHeight(), true);
        zoom = new Matrix();
        RectF drawableRect = new RectF(0, 0, bitmap.getWidth(), bitmap.getHeight());
        RectF viewRect = new RectF(-getWidth(), -getHeight(), 2 * getWidth(), 2 * getHeight());
        zoom.setRectToRect(drawableRect, viewRect, Matrix.ScaleToFit.CENTER);
    }
}
