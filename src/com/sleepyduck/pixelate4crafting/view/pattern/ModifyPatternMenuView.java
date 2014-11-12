package com.sleepyduck.pixelate4crafting.view.pattern;

import com.sleepyduck.pixelate4crafting.BetterLog;
import com.sleepyduck.pixelate4crafting.R;
import com.sleepyduck.pixelate4crafting.data.Patterns.Pattern;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ModifyPatternMenuView extends LinearLayout implements OnClickListener {

	public static final int STATE_COLLAPSED = 0x1;
	public static final int STATE_EXPANDED = 0x2;
	private int mState = STATE_COLLAPSED;
	
	private Pattern mPattern;
	private TextView mTitleView;

	public ModifyPatternMenuView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		setup();
	}

	public ModifyPatternMenuView(Context context, AttributeSet attrs) {
		super(context, attrs);
		setup();
	}

	public ModifyPatternMenuView(Context context, Pattern pattern) {
		super(context);
		setup();
	}

	private void setup() {
		setBackgroundColor(getResources().getColor(R.color.primary_100));
		setOnClickListener(this);
	}

	public int getState() {
		return mState;
	}

	@Override
	public void onClick(View v) {
		BetterLog.d(this);
		mState = (mState == STATE_COLLAPSED ? STATE_EXPANDED : STATE_COLLAPSED);
		requestLayout();
	}

}
