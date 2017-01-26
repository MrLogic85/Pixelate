package com.sleepyduck.pixelate4crafting.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import com.sleepyduck.pixelate4crafting.R;

public class LineProgressBar extends View {

    private final Paint paintBackground = new Paint();
    private final Paint paintProgress = new Paint();
    private final int margin;
    private final Rect clipBounds = new Rect();

    private int progress;

    public LineProgressBar(Context context) {
        super(context);
        paintProgress.setColor(Color.BLACK);
        paintBackground.setColor(Color.BLACK);
        margin = 0;
    }

    public LineProgressBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LineProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.LineProgressBar,
                defStyleAttr,
                0);

        int colorBackground = a.getColor(R.styleable.LineProgressBar_progressBackground, Color.BLACK);
        paintBackground.setColor(colorBackground);

        int colorProgress = a.getColor(R.styleable.LineProgressBar_progressColor, Color.BLACK);
        paintProgress.setColor(colorProgress);

        margin = (int) a.getDimension(R.styleable.LineProgressBar_progressMargin, 0);

        if (isInEditMode()) {
            progress = a.getInteger(R.styleable.LineProgressBar_progressToolsProgress, 30);
        }

        a.recycle();
    }

    public void setProgress(int newProgress) {
        if (progress != newProgress) {
            progress = newProgress;
            invalidate(new Rect(getLeft(), getTop(), getRight(), getBottom()));
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.getClipBounds(clipBounds);
        int width = getMeasuredWidth(), height = getMeasuredHeight();
        int left = Math.max(0, clipBounds.left);
        int top = Math.max(0, clipBounds.top);
        int right = Math.min(width, clipBounds.right);
        int rightProgress = Math.min((width - 2 * margin) * progress / 100 + margin, clipBounds.right);
        int bottom = Math.min(height, clipBounds.bottom);
        canvas.drawRect(left, top, right, bottom, paintBackground);
        canvas.drawRect(left + margin, top + margin, rightProgress, bottom - margin, paintProgress);
    }
}
