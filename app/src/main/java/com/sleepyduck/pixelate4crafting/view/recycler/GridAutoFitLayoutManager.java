package com.sleepyduck.pixelate4crafting.view.recycler;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

import com.sleepyduck.pixelate4crafting.R;

public class GridAutoFitLayoutManager extends GridLayoutManager {
    private static final int DEFAULT_SPAN = 1;
    private int columnSpan = DEFAULT_SPAN;

    public GridAutoFitLayoutManager(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.RecyclerView,
                defStyleAttr, defStyleRes);
        columnSpan = (int) a.getDimension(R.styleable.RecyclerView_span, DEFAULT_SPAN);
        a.recycle();
    }

    public GridAutoFitLayoutManager(Context context, int spanCount) {
        super(context, spanCount);
    }

    public GridAutoFitLayoutManager(Context context, int spanCount, int orientation, boolean reverseLayout) {
        super(context, spanCount, orientation, reverseLayout);
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state)
    {
        int width = getWidth();
        int height = getHeight();

        int totalSpace;
        if (getOrientation() == VERTICAL) {
            totalSpace = width - getPaddingRight() - getPaddingLeft();
        } else {
            totalSpace = height - getPaddingTop() - getPaddingBottom();
        }

        int spanCount = totalSpace / columnSpan;
        if (spanCount > 0 && getSpanCount() != spanCount) {
            setSpanCount(spanCount);
        }
        super.onLayoutChildren(recycler, state);
    }
}
