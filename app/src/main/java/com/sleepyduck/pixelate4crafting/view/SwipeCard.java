package com.sleepyduck.pixelate4crafting.view;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

import com.sleepyduck.pixelate4crafting.R;

/**
 * Created by fredrik.metcalf on 2016-04-08.
 */
public class SwipeCard extends CardView {
    private static final int[] ButtonIds = {R.id.imageLeft, R.id.imageRight1, R.id.imageRight2};
    private final ImageView[] mButtons = new ImageView[3];
    private View mContentView;

    public SwipeCard(Context context) {
        this(context, null);
    }

    public SwipeCard(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SwipeCard(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private ImageView getButton(int i) {
        if (mButtons[i] == null) {
            mButtons[i] = (ImageView) findViewById(ButtonIds[i]);
        }
        return mButtons[i];
    }

    @Override
    public void setOnClickListener(OnClickListener l) {
        getContentView().setClickable(true);
        getContentView().setOnClickListener(l);
    }

    public View getContentView() {
        if (mContentView == null) {
            mContentView = findViewById(R.id.content);
        }
        return mContentView;
    }

    @Override
    public void setTag(final Object tag) {
        getContentView().setTag(tag);
        for (int i = 0; i < mButtons.length; ++i) {
            getButton(i).setTag(tag);
        }
    }
}
