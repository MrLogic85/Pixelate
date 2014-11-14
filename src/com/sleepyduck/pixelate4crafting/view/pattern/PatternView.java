package com.sleepyduck.pixelate4crafting.view.pattern;

import com.sleepyduck.pixelate4crafting.R;
import com.sleepyduck.pixelate4crafting.data.Patterns.Pattern;

import android.animation.LayoutTransition;
import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;

public class PatternView extends ViewGroup {
	private LayoutTransition mLayoutTransition;

	private PatternMenuView mMenuView;
	private PatternCanvasView mCanvasView;

	private Pattern mPattern;

	public PatternView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		setup();
	}

	public PatternView(Context context, AttributeSet attrs) {
		super(context, attrs);
		setup();
	}

	public PatternView(Context context, Pattern pattern) {
		super(context);
		mPattern = pattern;
		setup();
	}

	private void setup() {
		mLayoutTransition = new LayoutTransition();
		mLayoutTransition.enableTransitionType(LayoutTransition.CHANGING);
		setLayoutTransition(mLayoutTransition);

		mCanvasView = new PatternCanvasView(getContext());
		mCanvasView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		addView(mCanvasView);

		mMenuView = (PatternMenuView) inflate(getContext(), R.layout.pattern_menu_view, null);
		mMenuView.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT));
		addView(mMenuView);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		int width = MeasureSpec.getSize(widthMeasureSpec);
		int height = MeasureSpec.getSize(heightMeasureSpec);
		int widthMeasured = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY);
		int widthMeasuredMenu = MeasureSpec.makeMeasureSpec((int) (width * 0.8), MeasureSpec.AT_MOST);
		int heightMeasured = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);
		mMenuView.measure(widthMeasuredMenu, heightMeasured);
		mCanvasView.measure(widthMeasured, heightMeasureSpec);
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		mCanvasView.layout(0, 0, mCanvasView.getMeasuredWidth(), mCanvasView.getMeasuredHeight());
		switch (mMenuView.getState()) {
			case PatternMenuView.STATE_COLLAPSED:
				int visibleMenu = (int) getResources().getDimension(R.dimen.pattern_menu_collapsed);
				mMenuView.layout(visibleMenu - mMenuView.getMeasuredWidth(), 0, visibleMenu, b - t);
				break;
			case PatternMenuView.STATE_EXPANDED:
				mMenuView.layout(0, 0, mMenuView.getMeasuredWidth(), mMenuView.getMeasuredHeight());
				break;
			default:
				break;
		}
	}

}
