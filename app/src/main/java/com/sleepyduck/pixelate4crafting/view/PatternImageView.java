package com.sleepyduck.pixelate4crafting.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Color;
import android.os.AsyncTask;
import android.util.AttributeSet;

import com.sleepyduck.pixelate4crafting.control.BitmapHandler;
import com.sleepyduck.pixelate4crafting.control.util.BetterLog;
import com.sleepyduck.pixelate4crafting.control.util.ColorUtil;
import com.sleepyduck.pixelate4crafting.model.Pattern;

import java.util.HashMap;
import java.util.Map;

import static com.sleepyduck.pixelate4crafting.view.PatternImageView.Style.Full;

public class PatternImageView extends InteractiveImageView {
    private Bitmap mImageBitmap;
    private AsyncTask mBitmapAsyncTask;
	private Pattern mPattern;
    private Bitmap mOrigBitmap;

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
		mBitmapAsyncTask.cancel(true);
        if (mOrigBitmap != null) {
            mOrigBitmap.recycle();
        }
		super.onDetachedFromWindow();
	}

	public void executeRedraw(Style style) {
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
	}

	public void setPattern(Pattern pattern) {
        setPattern(pattern, Full);
	}

    public void setPattern(Pattern pattern, Style style) {
        mPattern = pattern;
        mOrigBitmap = BitmapHandler.getFromFileName(getContext(), mPattern.getFileName());
        executeRedraw(style);
    }

    @Override
    public void setImageBitmap(Bitmap bm) {
        super.setImageBitmap(bm);
        BetterLog.d(this, "Bitmap redrawn");
    }

    private final class BitmapAsyncTaskSimple extends AsyncTask<Object, Object, Bitmap> {
        private static final int PIXEL_SIZE_MAX = 3;

        private Bitmap pixelBitmap;
        int pixelsWidth, pixelsHeight;
        private Map<Integer, Float> mColors;

        @Override
        protected Bitmap doInBackground(Object... params) {
            pixelsWidth = mPattern.getPixelWidth();
            pixelsHeight = mPattern.getPixelHeight();
            if (mPattern.hasColors()) {
                mColors = new HashMap<>(mPattern.getColors());
            } else {
                mColors = new HashMap<>();
                mColors.put(Color.WHITE, 0f);
                mColors.put(Color.BLACK, 0f);
            }
            float dRes = (float)mOrigBitmap.getWidth() / (float) pixelsWidth;
            int pixelSize = Math.max(1, Math.min(PIXEL_SIZE_MAX, 300/pixelsWidth));

            pixelBitmap = Bitmap.createBitmap(pixelsWidth*pixelSize, pixelsHeight*pixelSize, Config.ARGB_8888);
            // Draw colors
            for (int x = 0; x < pixelsWidth; ++x) {
            for (int y = 0; y < pixelsHeight; ++y) {
                    int pixel = mOrigBitmap.getPixel((int) (dRes*(x+.5f)), (int) (dRes*(y+.5f)));
                    for (int ix = x*pixelSize; ix < (x+1)*pixelSize; ++ix) {
                        for (int iy = y*pixelSize; iy < (y+1)*pixelSize; ++iy) {
                            pixelBitmap.setPixel(ix, iy, ColorUtil.getBestColorFor(pixel, mColors).getKey());
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
				setImageBitmap(bitmap);
			}
		}
	}
}
