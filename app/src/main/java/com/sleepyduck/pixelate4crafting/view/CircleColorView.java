package com.sleepyduck.pixelate4crafting.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.sleepyduck.pixelate4crafting.R;

import java.util.Random;

/**
 * Created by fredrikmetcalf on 30/01/17.
 */

public class CircleColorView extends View {
    private static final float DROP_SHADOW = 8;

    private final float innerRadius;
    private final Paint paint;
    private float colorRadius;
    private int[] colors = {};
    private float[][] colorPos;

    private final ObjectAnimator showAnimator = ObjectAnimator.ofFloat(this, "scaleAnim", 0f, 1f);
    private final ObjectAnimator hideAnimator = ObjectAnimator.ofFloat(this, "scaleAnim", 1f, 0f);
    private float scaleAnim = 1;
    private Integer selectColor = null;

    public CircleColorView(Context context) {
        this(context, null);
    }

    public CircleColorView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CircleColorView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.CircleColorView,
                defStyleAttr,
                0);

        int innerCircleColorCount;
        try {
            innerCircleColorCount = a.getInt(R.styleable.CircleColorView_innerCircleColorCount, 6);
        } catch (Exception ignored) {
            innerCircleColorCount = 6;
        }

        try {
            colorRadius = a.getDimension(R.styleable.CircleColorView_colorRadius, 20);
        } catch (Exception ignored) {
            colorRadius = 20;
        }

        float sweepAngle = (float) (Math.PI * 2f / (float) innerCircleColorCount);
        innerRadius = (float) ((colorRadius + DROP_SHADOW) / Math.sin(sweepAngle / 2));

        if (isInEditMode()) {
            int colorCount = 5;
            try {
                colorCount = a.getInt(R.styleable.CircleColorView_testColorCount, 5);
            } catch (Exception ignored) {
            }
            colors = new int[colorCount];
            Random random = new Random();
            for (int i = 0; i < colorCount; ++i) {
                colors[i] = Color.argb(0xFF, random.nextInt(), random.nextInt(), random.nextInt());
            }
            measure(0, 0);
            setColors(colors);
        }

        paint = new Paint();
        paint.setShadowLayer(DROP_SHADOW, 0, 0, Color.BLACK);
        setLayerType(LAYER_TYPE_SOFTWARE, paint);

        a.recycle();
    }

    public void setColors(int[] colors) {
        this.colors = colors;
        int layers = getLayer(colors.length - 1) + 1;
        int[] colorsInLayer = new int[layers];
        int colorsLeft = this.colors.length;
        for (int i = 0; i < layers - 1; ++i) {
            int colorsForLayer = getColorsInLayer(i);
            colorsInLayer[i] = colorsForLayer;
            colorsLeft -= colorsForLayer;
        }
        colorsInLayer[layers - 1] = colorsLeft;

        for (int ignored : this.colors) {
            for (int layer = colorsInLayer.length - 1; layer > 0; --layer) {
                if (colorsInLayer[layer] - colorsInLayer[layer - 1] < 0) {
                    colorsInLayer[layer]++;
                    colorsInLayer[layer - 1]--;
                }
            }
        }

        int[] layer = new int[this.colors.length];
        int colorsPlacedInLayer = 0;
        int currentLayer = 0;
        for (int i = 0; i < this.colors.length; ++i) {
            if (colorsInLayer[currentLayer] > colorsPlacedInLayer) {
                colorsPlacedInLayer++;
            } else {
                currentLayer++;
                colorsPlacedInLayer = 1;
            }
            layer[i] = currentLayer;
        }

        measure(0, 0);
        colorPos = new float[this.colors.length][2];
        for (int i = 0; i < this.colors.length; i++) {
            int colorLayer = layer[i];
            float radius = getRadius(colorLayer);
            int colorCount = colorsInLayer[colorLayer];
            int circleIndex = i;
            int actualColorCount = this.colors.length;
            for (int j = 0; j < colorLayer; ++j) {
                int count = colorsInLayer[j];
                circleIndex -= count;
                actualColorCount -= count;
            }
            actualColorCount = Math.min(actualColorCount, colorCount);
            float sweepAngle = (float) (Math.PI * 2f / (float) actualColorCount);
            float angle = circleIndex * sweepAngle;
            colorPos[i][0] = (float) Math.sin(angle) * radius;
            colorPos[i][1] = (float) Math.cos(angle) * radius;
        }
        requestLayout();
        invalidate();
    }

    private float getRadius(int layer) {
        return (colorRadius + DROP_SHADOW) * 2f * (float) layer + innerRadius;
    }

    private int getLayer(int colorNum) {
        int colorsFitted = 0;
        int layer = -1;
        while (colorsFitted < colorNum + 1) {
            layer++;
            colorsFitted += getColorsInLayer(layer);
        }
        return layer;
    }

    private int getColorsInLayer(int layer) {
        float angle = (float) (Math.asin((colorRadius + DROP_SHADOW) / getRadius(layer)) * 2f);
        return (int) (Math.PI * 2f / angle + 0.1);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int size = (int) Math.ceil((getRadius(getLayer(colors.length - 1)) + colorRadius + DROP_SHADOW) * 2);
        if (MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.AT_MOST) {
            widthMeasureSpec = MeasureSpec.makeMeasureSpec(size, MeasureSpec.EXACTLY);
        }
        if (MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.AT_MOST) {
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(size, MeasureSpec.EXACTLY);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        paint.setStyle(Paint.Style.FILL);
        for (int i = 0; i < colors.length; i++) {
            paint.setColor(colors[i]);
            float x;
            float y;
            if (scaleAnim < 1f) {
                if (selectColor != null && selectColor == i) {
                    x = getMeasuredWidth() / 2f + colorPos[i][0] * scaleAnim;
                    y = (getMeasuredHeight() / 2f - colorPos[i][1]) * scaleAnim;
                } else if (selectColor == null) {
                    x = getMeasuredWidth() / 2f + colorPos[i][0] * scaleAnim;
                    y = getMeasuredHeight() / 2f - colorPos[i][1] * scaleAnim;
                    paint.setAlpha(0xFF);
                } else {
                    x = getMeasuredWidth() / 2f + colorPos[i][0];
                    y = getMeasuredHeight() / 2f - colorPos[i][1];
                    paint.setAlpha((int) (0xFF * scaleAnim));
                }
            } else {
                x = getMeasuredWidth() / 2f + colorPos[i][0];
                y = getMeasuredHeight() / 2f - colorPos[i][1];
                paint.setAlpha(0xFF);
            }
            canvas.drawCircle(x, y, colorRadius, paint);
            paint.setAlpha(0xFF);
        }
        super.onDraw(canvas);
    }

    public void setRawPos(float rawX, float rawY) {
        int[] out = new int[2];
        getLocationInWindow(out);
        setTranslationX(rawX - getMeasuredWidth() / 2);
        setTranslationY(rawY - getMeasuredHeight() / 2);
    }

    // Used by ObjectAnimator
    @SuppressWarnings("unused")
    public void setScaleAnim(float scale) {
        scaleAnim = scale;
        invalidate();
    }

    public void show() {
        showAnimator.start();
        setVisibility(VISIBLE);
    }

    public void hide() {
        hide(null);
    }

    private void hide(final OnColorClickListener listener) {
        hideAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                setVisibility(INVISIBLE);
                if (listener != null) {
                    if (selectColor != null) {
                        listener.onColorClicked(selectColor);
                    } else {
                        listener.onCancel();
                    }
                }
                selectColor = null;
                hideAnimator.removeListener(this);
            }
        });
        hideAnimator.start();
    }

    public void setOnColorClickListener(final OnColorClickListener listener) {
        OnColorClickListener listener1 = listener;
        final float[] touchPos = new float[2];
        setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                touchPos[0] = motionEvent.getX();
                touchPos[1] = motionEvent.getY();
                if (getDistanceToNearestColor(touchPos) > colorRadius) {
                    if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                        hide(listener);
                    }
                    return true;
                }
                return false;
            }
        });
        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listener != null) {
                    if (getDistanceToNearestColor(touchPos) < colorRadius) {
                        int colorIndex = getNearestColor(touchPos);
                        selectColor = colorIndex;
                        hide(listener);
                    }
                }
            }
        });
    }

    private int getNearestColor(float[] touchPos) {
        int color = 0;
        float closestDistance = Float.MAX_VALUE;
        for (int i = 0; i < colorPos.length; ++i) {
            float dist = (float) Math.sqrt(Math.pow(getMeasuredWidth() / 2f + colorPos[i][0] - touchPos[0], 2) +
                    Math.pow(getMeasuredHeight() / 2f - colorPos[i][1] - touchPos[1], 2));
            if (dist < closestDistance) {
                closestDistance = dist;
                color = i;
            }
        }
        return color;
    }

    private float getDistanceToNearestColor(float[] touchPos) {
        float closestDistance = Float.MAX_VALUE;
        for (float[] pos : colorPos) {
            float dist = (float) Math.sqrt(Math.pow(getMeasuredWidth() / 2f + pos[0] - touchPos[0], 2) +
                    Math.pow(getMeasuredHeight() / 2f - pos[1] - touchPos[1], 2));
            closestDistance = Math.min(closestDistance, dist);
        }
        return closestDistance;
    }

    public interface OnColorClickListener {
        void onColorClicked(int colorIndex);
        void onCancel();
    }
}
