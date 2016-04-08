package com.sleepyduck.pixelate4crafting.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

/**
 * Created by fredrik.metcalf on 2016-04-08.
 */
public class SwipeCard extends RelativeLayout {
    public SwipeCard(Context context) {
        super(context);
    }

    public SwipeCard(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SwipeCard(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public SwipeCard(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }
}
