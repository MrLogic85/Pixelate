package com.sleepyduck.pixelate4crafting.view.recycler;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created by fredrik.metcalf on 2017-01-27.
 */

public class UntouchableRecyclerView extends RecyclerView {
    public UntouchableRecyclerView(Context context) {
        super(context);
    }

    public UntouchableRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public UntouchableRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        return false;
    }
}
