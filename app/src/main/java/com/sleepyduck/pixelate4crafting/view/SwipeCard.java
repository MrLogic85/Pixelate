package com.sleepyduck.pixelate4crafting.view;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.sleepyduck.pixelate4crafting.R;
import com.sleepyduck.pixelate4crafting.util.BetterLog;

/**
 * Created by fredrik.metcalf on 2016-04-08.
 */
public class SwipeCard extends CardView {
    private ImageButton mLeftButton;
    private ImageButton mRightButton;
    private View mContentView;

    public SwipeCard(Context context) {
        super(context);
    }

    public SwipeCard(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SwipeCard(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private ImageButton getLeftButton() {
        if (mLeftButton == null) {
            mLeftButton = (ImageButton) findViewById(R.id.left_button);
        }
        return mLeftButton;
    }

    private ImageButton getRightButton() {
        if (mRightButton == null) {
            mRightButton = (ImageButton) findViewById(R.id.right_button);
        }
        return mRightButton;
    }

    public void setOnClickListenerLeft(OnClickListener l) {
        getLeftButton().setOnClickListener(l);
    }

    public void setOnClickListenerRight(OnClickListener l) {
        mRightButton.setOnClickListener(l);
    }

    @Override
    public void setOnClickListener(OnClickListener l) {
        BetterLog.d(this, "Set on click");
        getContentView().setClickable(true);
        getContentView().setOnClickListener(l);
        getContentView().findViewById(R.id.icon).setClickable(true);
        getContentView().findViewById(R.id.icon).setOnClickListener(l);
    }

    public View getContentView() {
        if (mContentView == null) {
            mContentView = findViewById(R.id.content);
        }
        return mContentView;
    }
}
