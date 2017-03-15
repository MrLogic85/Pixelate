package com.sleepyduck.pixelate4crafting.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.AttributeSet;

import com.sleepyduck.pixelate4crafting.R;

/**
 * Created by fredrikmetcalf on 07/02/17.
 */

public class PatternInteractiveView extends InteractiveImageView {
    private float[] topLeft = new float[2], bottomRight = new float[2];
    private float[] canvasTopLeft = new float[2], canvasBottomRight = new float[2];
    private Matrix invertMatrix = new Matrix();
    private Paint paint = new Paint();
    private int mMarkerX = 0;
    private int mMarkerY = 0;
    private int mMarkerColor = 0xFFFFFF;
    private boolean mDrawLines = false;

    {
        paint.setAntiAlias(false);
        paint.setFilterBitmap(false);
    }

    private final float lineThickness;

    private static final int LAYER_BEVEL = 0;
    private static final int LAYER_GRID = 1;
    private boolean[] drawLayer = {false, true};
    private Paint[] layerPaint = {new Paint(), new Paint()};

    {
        layerPaint[LAYER_BEVEL].setStyle(Paint.Style.STROKE);
        layerPaint[LAYER_GRID].setStyle(Paint.Style.STROKE);

        layerPaint[LAYER_BEVEL].setColor(Color.WHITE);
        layerPaint[LAYER_GRID].setColor(Color.BLACK);

    }

    private float[] layerLineThicknessScale = {2f, 1f};
    private float[] layerLineThicknessScaleExtra = {3f, 2f};

    public PatternInteractiveView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        if (isInEditMode()) {
            lineThickness = 1;
        } else {
            lineThickness = context.getResources().getDimension(R.dimen.pattern_line_thickness);
            mMarkerColor = getResources().getColor(R.color.accent_a200);
        }
    }

    public PatternInteractiveView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PatternInteractiveView(Context context) {
        this(context, null);
    }

    public void setPixel(int x, int y, int color) {
        if (mImageBitmap != null) {
            mImageBitmap.setPixel(++x, ++y, color);
            invalidate();
        }
    }

    public void setMarker(int markerX, int markerY) {
        mMarkerX = ++markerX;
        mMarkerY = ++markerY;
        invalidate();
    }

    private synchronized void drawLines(Canvas canvas) {
        if (mImageBitmap == null) {
            return;
        }

        topLeft[0] = 0;
        topLeft[1] = 0;
        bottomRight[0] = canvas.getWidth();
        bottomRight[1] = canvas.getHeight();

        getImageMatrix().invert(invertMatrix);
        invertMatrix.mapPoints(topLeft);
        invertMatrix.mapPoints(bottomRight);

        int startX = Math.max((int) topLeft[0], 1);
        int endX = Math.min((int) Math.ceil(bottomRight[0]), mImageBitmap.getWidth());
        int startY = Math.max((int) topLeft[1], 1);
        int endY = Math.min((int) Math.ceil(bottomRight[1]), mImageBitmap.getHeight());
        float lineThicknessScale = Math.min(1f, 30f / Math.max(bottomRight[0] - topLeft[0], bottomRight[1] - topLeft[1]));

        // Draw grid
        for (int i = LAYER_BEVEL; i <= LAYER_GRID; ++i) {
            layerPaint[i].setAlpha(getImageAlpha());
            if (drawLayer[i]) {
                for (int x = startX; x <= endX; ++x) {
                    canvasTopLeft[0] = x;
                    if (startY == 1 && x % 10 == 1) {
                        canvasTopLeft[1] = 0;
                    } else {
                        canvasTopLeft[1] = startY;
                    }
                    canvasBottomRight[0] = x;
                    canvasBottomRight[1] = endY;
                    getImageMatrix().mapPoints(canvasTopLeft);
                    getImageMatrix().mapPoints(canvasBottomRight);

                    float extraThickness = x % 10 == 1 ? layerLineThicknessScaleExtra[i] : layerLineThicknessScale[i];
                    layerPaint[i].setStrokeWidth(lineThickness * lineThicknessScale * extraThickness);
                    canvas.drawLine(canvasTopLeft[0], canvasTopLeft[1], canvasBottomRight[0], canvasBottomRight[1], layerPaint[i]);
                }
                for (int y = startY; y <= endY; ++y) {
                    if (startX == 1 && y % 10 == 1) {
                        canvasTopLeft[0] = 0;
                    } else {
                        canvasTopLeft[0] = startX;
                    }
                    canvasTopLeft[1] = y;
                    canvasBottomRight[0] = endX;
                    canvasBottomRight[1] = y;
                    getImageMatrix().mapPoints(canvasTopLeft);
                    getImageMatrix().mapPoints(canvasBottomRight);

                    float extraThickness = y % 10 == 1 ? layerLineThicknessScaleExtra[i] : layerLineThicknessScale[i];
                    layerPaint[i].setStrokeWidth(lineThickness * lineThicknessScale * extraThickness);
                    canvas.drawLine(canvasTopLeft[0], canvasTopLeft[1], canvasBottomRight[0], canvasBottomRight[1], layerPaint[i]);
                }
            }
        }

        // Draw marker
        if (drawLayer[LAYER_GRID]) {
            layerPaint[LAYER_GRID].setStrokeWidth(lineThickness * lineThicknessScale * layerLineThicknessScaleExtra[LAYER_GRID]);
            layerPaint[LAYER_GRID].setColor(mMarkerColor);

            canvasTopLeft[0] = mMarkerX;
            canvasTopLeft[1] = 0;
            canvasBottomRight[0] = mMarkerX;
            canvasBottomRight[1] = endY;
            getImageMatrix().mapPoints(canvasTopLeft);
            getImageMatrix().mapPoints(canvasBottomRight);
            canvas.drawLine(canvasTopLeft[0], canvasTopLeft[1], canvasBottomRight[0], canvasBottomRight[1], layerPaint[LAYER_GRID]);

            canvasTopLeft[0] = 0;
            canvasTopLeft[1] = mMarkerY;
            canvasBottomRight[0] = endX;
            canvasBottomRight[1] = mMarkerY;
            getImageMatrix().mapPoints(canvasTopLeft);
            getImageMatrix().mapPoints(canvasBottomRight);
            canvas.drawLine(canvasTopLeft[0], canvasTopLeft[1], canvasBottomRight[0], canvasBottomRight[1], layerPaint[LAYER_GRID]);

            layerPaint[LAYER_GRID].setColor(Color.BLACK);
        }
    }

    @Override
    protected synchronized void onDraw(Canvas canvas) {
        paint.setAlpha(getImageAlpha());
        if (mImageBitmap != null) {
            canvas.drawBitmap(mImageBitmap, getImageMatrix(), paint);
        }
        if (mDrawLines) {
            drawLines(canvas);
        }
    }

    public void enableGridLines(boolean enable) {
        mDrawLines = enable;
    }
}
