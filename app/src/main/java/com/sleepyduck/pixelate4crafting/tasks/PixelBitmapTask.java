package com.sleepyduck.pixelate4crafting.tasks;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;

import com.sleepyduck.pixelate4crafting.model.Pattern;

/**
 * Created by fredrikmetcalf on 26/01/17.
 */

public class PixelBitmapTask extends AsyncTask<Object, Object, Bitmap> {
    private static final int pixelSize = 11; // 10 per pixel plus 1 for grid

    @Override
    protected Bitmap doInBackground(Object... params) {
        Context context = (Context) params[0];
        Pattern pattern = (Pattern) params[1];
        int pixelsWidth = pattern.getPixelWidth();
        int pixelsHeight = pattern.getPixelHeight();
        int[][] pixels = pattern.getPixels();

        int resultWidth = (pixelsWidth + 1) * pixelSize;
        int resultHeight = (pixelsHeight + 1) * pixelSize;

        Bitmap pixelBitmap = Bitmap.createBitmap(resultWidth, resultHeight, Bitmap.Config.ARGB_8888);
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
}