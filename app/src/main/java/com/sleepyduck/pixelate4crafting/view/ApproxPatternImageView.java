package com.sleepyduck.pixelate4crafting.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Color;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.widget.Toast;

import com.sleepyduck.pixelate4crafting.control.BitmapHandler;
import com.sleepyduck.pixelate4crafting.model.Pattern;
import com.sleepyduck.pixelate4crafting.tasks.PixelBitmapTask;
import com.sleepyduck.pixelate4crafting.util.ColorUtil;

import java.util.Arrays;

public class ApproxPatternImageView extends InteractiveImageView {
    private AsyncTask<Object, Object, Bitmap> mBitmapAsyncTask;
    private Pattern mPattern;
    private Bitmap mOrigBitmap;
    private boolean mScaleToFitNewImage = false;

    public ApproxPatternImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public ApproxPatternImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ApproxPatternImageView(Context context) {
        super(context);
    }

    @Override
    protected void onDetachedFromWindow() {
        if (mBitmapAsyncTask != null) {
            mBitmapAsyncTask.cancel(true);
        }
        if (mOrigBitmap != null) {
            mOrigBitmap.recycle();
        }
        if (mImageBitmap != null) {
            mImageBitmap.recycle();
        }
        super.onDetachedFromWindow();
    }

    public void executeRedraw() {
        if (mBitmapAsyncTask != null) {
            mBitmapAsyncTask.cancel(true);
            mBitmapAsyncTask = null;
        }

        mBitmapAsyncTask = new BitmapAsyncTaskSimple();
        mBitmapAsyncTask.execute();
        setImageAlpha(0xff / 2);
    }

    public void setPattern(Pattern pattern) {
        mPattern = pattern;
        mOrigBitmap = BitmapHandler.getFromFileName(getContext(), mPattern.getFileName());
        if (mOrigBitmap != null) {
            executeRedraw();
        } else {
            Toast.makeText(getContext(), "Failed to retrieve original image, pattern may be broken", Toast.LENGTH_LONG).show();
        }
    }

    public void setPixel(int x, int y, int color) {
        if (mImageBitmap != null) {
            x++;
            y++;
            int pixelSize = PixelBitmapTask.PIXEL_SIZE - 1;
            int[] colorSquare = new int[pixelSize * pixelSize];
            Arrays.fill(colorSquare, color);
            mImageBitmap.setPixels(colorSquare, 0, pixelSize, x * PixelBitmapTask.PIXEL_SIZE, y * PixelBitmapTask.PIXEL_SIZE, pixelSize, pixelSize);
            invalidate();
        }
    }

    @Override
    public void setImageBitmap(Bitmap bm) {
        super.setImageBitmap(bm);
        setImageAlpha(0xff);
        mBitmapAsyncTask = null;
        if (mScaleToFitNewImage) {
            scaleToFit();
            mScaleToFitNewImage = false;
        }
    }

    @Override
    public void scaleToFit() {
        if (mBitmapAsyncTask != null && !mBitmapAsyncTask.isCancelled()) {
            mScaleToFitNewImage = true;
        } else {
            super.scaleToFit();
        }
    }

    private final class BitmapAsyncTaskSimple extends AsyncTask<Object, Object, Bitmap> {
        private static final int PIXEL_SIZE_MAX = 3;

        private Bitmap pixelBitmap;
        int pixelsWidth, pixelsHeight;
        private int[] mColors;

        @Override
        protected Bitmap doInBackground(Object... params) {
            pixelsWidth = mPattern.getPixelWidth();
            pixelsHeight = mPattern.getPixelHeight();
            if (mPattern.hasColors()) {
                mColors = mPattern.getColors(new int[mPattern.getColorCount()]);
            } else {
                mColors = new int[]{Color.WHITE, Color.BLACK};
            }
            float dRes = (float) mOrigBitmap.getWidth() / (float) pixelsWidth;
            int pixelSize = Math.max(1, Math.min(PIXEL_SIZE_MAX, 300 / pixelsWidth));

            pixelBitmap = Bitmap.createBitmap(pixelsWidth * pixelSize, pixelsHeight * pixelSize, Config.ARGB_8888);
            // Draw colors
            for (int x = 0; x < pixelsWidth; ++x) {
                for (int y = 0; y < pixelsHeight; ++y) {
                    if ((int) (dRes * (x + .5f)) < mOrigBitmap.getWidth() && (int) (dRes * (y + .5f)) < mOrigBitmap.getHeight()) {
                        int pixel = mOrigBitmap.getPixel((int) (dRes * (x + .5f)), (int) (dRes * (y + .5f)));
                        if ((pixel & ColorUtil.ALPHA_CHANNEL) != ColorUtil.ALPHA_CHANNEL) {
                            continue;
                        }
                        pixel = ColorUtil.getBestColorFor(mPattern.Id, pixel, mColors);
                        for (int ix = x * pixelSize; ix < (x + 1) * pixelSize; ++ix) {
                            for (int iy = y * pixelSize; iy < (y + 1) * pixelSize; ++iy) {
                                pixelBitmap.setPixel(ix, iy, pixel);
                            }
                        }
                    }
                }
                if (isCancelled()) {
                    return null;
                }
            }

            return pixelBitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (bitmap != null) {
                if (mImageBitmap != null) {
                    mImageBitmap.recycle();
                }
                mImageBitmap = bitmap;
                setImageBitmap(bitmap);
            }
        }
    }
}
