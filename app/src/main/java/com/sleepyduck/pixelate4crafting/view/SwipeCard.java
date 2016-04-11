package com.sleepyduck.pixelate4crafting.view;

import android.content.Context;
import android.graphics.Rect;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.ImageButton;

import com.sleepyduck.pixelate4crafting.R;
import com.sleepyduck.pixelate4crafting.util.OnItemSwipeListener;
import com.sleepyduck.pixelate4crafting.util.BetterLog;

/**
 * Created by fredrik.metcalf on 2016-04-08.
 */
public class SwipeCard extends CardView {
    private final ViewConfiguration mViewConfig;

    private static final int[] ButtonIds = {R.id.button1, R.id.button2, R.id.button3};
    private ImageButton[] mButtons = new ImageButton[3];
    private View mContentView;

    private float mStartDragPos;

    public SwipeCard(Context context) {
        super(context);
        mViewConfig = ViewConfiguration.get(getContext());
    }

    public SwipeCard(Context context, AttributeSet attrs) {
        super(context, attrs);
        mViewConfig = ViewConfiguration.get(getContext());
    }

    public SwipeCard(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mViewConfig = ViewConfiguration.get(getContext());
    }

    private ImageButton getButton(int i) {
        if (mButtons[i] == null) {
            mButtons[i] = (ImageButton) findViewById(ButtonIds[i]);
        }
        return mButtons[i];
    }

    public void setOnClickListener(OnClickListener l, int i) {
        getButton(i).setOnClickListener(l);
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

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mStartDragPos = ev.getX() - getContentView().getTranslationX();
                return false;
            case MotionEvent.ACTION_MOVE:
                float distance = Math.abs(ev.getX() - getContentView().getTranslationX() - mStartDragPos);
                if (distance >= mViewConfig.getScaledTouchSlop()) {
                    requestDisallowInterceptTouchEvent(true);
                    return true;
                }
                return false;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                break;
        }
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        float distance = ev.getX() - mStartDragPos;
        int width = getButton(0).getWidth();
        switch (ev.getAction()) {
            case MotionEvent.ACTION_MOVE:
                // TODO handle multiple buttons
                if (distance <= width && distance >= -width) {
                    getContentView().setTranslationX(distance);
                } else if (distance > width) {
                    getContentView().setTranslationX(width);
                } else {
                    getContentView().setTranslationX(-width);
                }
                break;
            case MotionEvent.ACTION_UP:
                if (distance <= width / 2 && distance >= -width / 2) {
                    getContentView().animate().translationX(0).start();
                } else if (distance > width / 2) {
                    getContentView().animate().translationX(width).start();
                } else {
                    getContentView().animate().translationX(-width).start();
                }
                break;
            case MotionEvent.ACTION_CANCEL:
                getContentView().animate().translationX(0).start();
                break;
        }
        return true;
    }

    public void restore() {
        getContentView().setTranslationX(0);
    }
}
