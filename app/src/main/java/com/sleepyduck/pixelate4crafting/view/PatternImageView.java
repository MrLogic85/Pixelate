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
import com.sleepyduck.pixelate4crafting.util.ColorUtil;

import static com.sleepyduck.pixelate4crafting.view.PatternImageView.Style.Full;

public class PatternImageView extends InteractiveImageView {
    private Bitmap mImageBitmap;
    private AsyncTask<Object, Object, Bitmap> mBitmapAsyncTask;
	private Pattern mPattern;
    private Bitmap mOrigBitmap;
    private Style mStyle;
    private boolean mScaleToFitNewImage = false;

    public enum Style {
        Simple,
        Full
    }

    public PatternImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public PatternImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public PatternImageView(Context context) {
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

	public void executeRedraw(Style style) {
        mStyle = style;

        if (style == Full
                && mPattern.getPatternFileName() != null
                && mPattern.getPatternFileName().length() > 0) {
            if (mImageBitmap != null) {
                mImageBitmap.recycle();
            }
            Bitmap bitmap =  BitmapHandler.getFromFileName(getContext(), mPattern.getPatternFileName());
            setImageBitmap(bitmap);
            return;
        }

        if (mBitmapAsyncTask != null) {
            mBitmapAsyncTask.cancel(true);
            mBitmapAsyncTask = null;
        }
        switch (style) {
            case Simple:
                mBitmapAsyncTask = new BitmapAsyncTaskSimple();
                break;
            case Full:
                mBitmapAsyncTask = new BitmapAsyncTask();
                break;
        }
        mBitmapAsyncTask.execute();
        setImageAlpha(0xff / 2);
	}

	public void setPattern(Pattern pattern) {
        setPattern(pattern, Full);
	}

    public void setPattern(Pattern pattern, Style style) {
        mPattern = pattern;
        mOrigBitmap = BitmapHandler.getFromFileName(getContext(), mPattern.getFileName());
        if (mOrigBitmap != null) {
            executeRedraw(style);
        } else {
            Toast.makeText(getContext(), "Failed to retrieve original image, pattern may be broken", Toast.LENGTH_LONG).show();
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
                mColors = new int[mPattern.getColors().size()];
                final Object[] pixelObjects = mPattern.getColors().keySet().toArray();
                for (int i = 0; i < mColors.length; ++i) {
                    mColors[i] = (int) pixelObjects[i];
                }
            } else {
                mColors = new int[] { Color.WHITE, Color.BLACK };
            }
            float dRes = (float)mOrigBitmap.getWidth() / (float) pixelsWidth;
            int pixelSize = Math.max(1, Math.min(PIXEL_SIZE_MAX, 300/pixelsWidth));

            pixelBitmap = Bitmap.createBitmap(pixelsWidth*pixelSize, pixelsHeight*pixelSize, Config.ARGB_8888);
            // Draw colors
            for (int x = 0; x < pixelsWidth; ++x) {
                for (int y = 0; y < pixelsHeight; ++y) {
                    int pixel = mOrigBitmap.getPixel((int) (dRes * (x + .5f)), (int) (dRes * (y + .5f)));
                    if ((pixel & ColorUtil.ALPHA_CHANNEL) != ColorUtil.ALPHA_CHANNEL) {
                        continue;
                    }
                    pixel = ColorUtil.getBestColorFor(pixel, mColors);
                    for (int ix = x * pixelSize; ix < (x + 1) * pixelSize; ++ix) {
                        for (int iy = y * pixelSize; iy < (y + 1) * pixelSize; ++iy) {
                            pixelBitmap.setPixel(ix, iy, pixel);
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

    private final class BitmapAsyncTask extends AsyncTask<Object, Object, Bitmap> {
		int pixelSize;
		private int[][] pixels;
		private Bitmap pixelBitmap;
		int pixelsWidth, pixelsHeight;

		@Override
		protected Bitmap doInBackground(Object... params) {
			pixelsWidth = mPattern.getPixelWidth();
			pixelsHeight = mPattern.getPixelHeight();
			pixels = mPattern.getPixels();

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
                if (mImageBitmap != null) {
                    mImageBitmap.recycle();
                }
                mImageBitmap = bitmap;
                if (mStyle == Full) {
                    String patternName = BitmapHandler.storePattern(getContext(), mImageBitmap, mPattern.getFileName());
                    mPattern.edit()
                            .setFilePattern(patternName)
                            .apply();
                }
                mScaleToFitNewImage = true;
				setImageBitmap(bitmap);
            }
		}
	}
}
