package com.sleepyduck.pixelate4crafting.view.pattern;

import com.sleepyduck.pixelate4crafting.R;
import com.sleepyduck.pixelate4crafting.data.Constants;
import com.sleepyduck.pixelate4crafting.data.Constants.MENU_STATE;
import com.sleepyduck.pixelate4crafting.data.Patterns.Pattern;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.LinearLayout;

public class PatternMenuView extends LinearLayout {
	private Constants.MENU_STATE mState = MENU_STATE.STATE_COLLAPSED;

	public PatternMenuView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		setup();
	}

	public PatternMenuView(Context context, AttributeSet attrs) {
		super(context, attrs);
		setup();
	}

	public PatternMenuView(Context context, Pattern pattern) {
		super(context);
		setup();
	}

	private void setup() {
		setBackgroundColor(getResources().getColor(R.color.primary_100));
	}

	public MENU_STATE getState() {
		return mState;
	}

	public void setState(MENU_STATE state) {
		if (mState != state) {
			mState = state;
			requestLayout();
		}
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		if (mState == MENU_STATE.STATE_COLLAPSED) {
			mState = MENU_STATE.STATE_EXPANDED;
			requestLayout();
			return true;
		}
		return super.onInterceptTouchEvent(ev);
	}

}
