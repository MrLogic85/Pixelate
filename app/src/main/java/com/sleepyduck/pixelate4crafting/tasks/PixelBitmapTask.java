package com.sleepyduck.pixelate4crafting.tasks;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;

import com.sleepyduck.pixelate4crafting.model.Pattern;

import java.util.Arrays;

/**
 * Created by fredrikmetcalf on 26/01/17.
 */

public class PixelBitmapTask extends AsyncTask<Object, Object, Bitmap> {
    public static final int PIXEL_SIZE = 11; // 10 per pixel plus 1 for grid
    private final int[] pixelsSquare = new int[PIXEL_SIZE * PIXEL_SIZE];

    @Override
    protected Bitmap doInBackground(Object... params) {
        Pattern pattern = (Pattern) params[0];
        int pixelsWidth = pattern.getPixelWidth();
        int pixelsHeight = pattern.getPixelHeight();
        int[][] pixels = pattern.getPixels(new int[pixelsWidth][pixelsHeight]);

        int resultWidth = (pixelsWidth + 1) * PIXEL_SIZE;
        int resultHeight = (pixelsHeight + 1) * PIXEL_SIZE;

        Bitmap pixelBitmap = Bitmap.createBitmap(resultWidth, resultHeight, Bitmap.Config.ARGB_8888);
        // Draw colors
        for (int y = 0; y < pixelsHeight; ++y) {
            for (int x = 0; x < pixelsWidth; ++x) {
                if (pattern.hasChangedPixelAt(x, y)) {
                    setColor(pixelBitmap, pattern.getChangedPixelAt(x, y), x, y);
                } else {
                    setColor(pixelBitmap, pixels[x][y], x, y);
                }
            }
            if (isCancelled()) {
                return null;
            }
        }

        if (!isCancelled()) {
            drawGrid(pixelBitmap);
        }

        return pixelBitmap;
    }

    private void drawGrid(Bitmap bitmap) {
        int pixelsWidth = bitmap.getWidth() / PIXEL_SIZE;
        int pixelsHeight = bitmap.getHeight() / PIXEL_SIZE;

        int bitmapWidth = bitmap.getWidth();
        int bitmapHeight = bitmap.getHeight();

        int[] line = new int[bitmapHeight];
        Arrays.fill(line, Color.BLACK);
        for (int i = 0; i < pixelsWidth; ++i) {
            if (i % 10 == 0) {
                bitmap.setPixels(line, 0, 1, (i + 1) * PIXEL_SIZE - 1, 0, 1, bitmapHeight);
            } else {
                bitmap.setPixels(line, 0, 1, (i + 1) * PIXEL_SIZE - 1, PIXEL_SIZE, 1, bitmapHeight - PIXEL_SIZE);
            }
        }

        line = new int[bitmapWidth];
        Arrays.fill(line, Color.BLACK);
        for (int i = 0; i < pixelsHeight; ++i) {
            if (i % 10 == 0) {
                bitmap.setPixels(line, 0, bitmapWidth, 0, i * PIXEL_SIZE + PIXEL_SIZE - 1, bitmapWidth, 1);
            } else {
                bitmap.setPixels(line, 0, bitmapWidth - PIXEL_SIZE, PIXEL_SIZE, (i + 1) * PIXEL_SIZE - 1, bitmapWidth - PIXEL_SIZE, 1);
            }
        }
    }

    private void setColor(Bitmap bitmap, int color, int x, int y) {
        // Add one to x and y due to extended grid
        x++;
        y++;
        Arrays.fill(pixelsSquare, color);
        bitmap.setPixels(pixelsSquare, 0, PIXEL_SIZE, x * PIXEL_SIZE, y * PIXEL_SIZE, PIXEL_SIZE, PIXEL_SIZE);
    }
}