package com.sleepyduck.pixelate4crafting.view.pattern;

import com.sleepyduck.pixelate4crafting.R;
import com.sleepyduck.pixelate4crafting.data.Patterns.Pattern;

import android.animation.LayoutTransition;
import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.TextView;

public class ModifyPatternView extends ViewGroup {
	private LayoutTransition mLayoutTransition;

	private ModifyPatternMenuView mMenuView;
	private ModifyPatternCanvasView mCanvasView;

	private Pattern mPattern;

	private TextView mTitleView;

	public ModifyPatternView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		setup();
	}

	public ModifyPatternView(Context context, AttributeSet attrs) {
		super(context, attrs);
		setup();
	}

	public ModifyPatternView(Context context, Pattern pattern) {
		super(context);
		mPattern = pattern;
		setup();
	}

	private void setup() {
		mLayoutTransition = new LayoutTransition();
		mLayoutTransition.enableTransitionType(LayoutTransition.CHANGING);
		setLayoutTransition(mLayoutTransition);

		mTitleView = (TextView) inflate(getContext(), R.layout.text_view_title, null);
		mTitleView.setText(mPattern.Title);
		addView(mTitleView);

		mCanvasView = new ModifyPatternCanvasView(getContext());
		mCanvasView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		addView(mCanvasView);

		mMenuView = (ModifyPatternMenuView) inflate(getContext(), R.layout.pattern_menu_view, null);
		mMenuView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				mMenuView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
				((TextView) findViewById(R.id.title)).setText(mPattern.Title);
			}
		});
		// ((TextView) findViewById(R.id.title)).setText(mPattern.Title);
		addView(mMenuView);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		int width = MeasureSpec.getSize(widthMeasureSpec);
		int height = MeasureSpec.getSize(heightMeasureSpec);
		int widthMeasured = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY);
		int widthMeasuredAtMost = MeasureSpec.makeMeasureSpec(width, MeasureSpec.AT_MOST);
		int heightMeasuredAtMost = MeasureSpec.makeMeasureSpec(height, MeasureSpec.AT_MOST);
		mTitleView.measure(widthMeasured, heightMeasuredAtMost);
		int heightMeasured = MeasureSpec.makeMeasureSpec(height - mTitleView.getMeasuredHeight(), MeasureSpec.EXACTLY);
		mMenuView.measure(widthMeasuredAtMost, heightMeasured);
		mCanvasView.measure(widthMeasured, heightMeasureSpec);
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		mTitleView.layout(0, 0, mTitleView.getMeasuredWidth(), mTitleView.getMeasuredHeight());
		mCanvasView.layout(0, mTitleView.getMeasuredHeight(), mCanvasView.getMeasuredWidth(),
				mTitleView.getMeasuredHeight() + mCanvasView.getMeasuredHeight());
		switch (mMenuView.getState()) {
			case ModifyPatternMenuView.STATE_COLLAPSED:
				int visibleMenu = (int) getResources().getDimension(R.dimen.pattern_menu_collapsed);
				mMenuView.layout(visibleMenu - mMenuView.getMeasuredWidth(), mTitleView.getMeasuredHeight(),
						visibleMenu, mTitleView.getMeasuredHeight() + b - t);
				break;
			case ModifyPatternMenuView.STATE_EXPANDED:
				mMenuView.layout(0, mTitleView.getMeasuredHeight(), mMenuView.getMeasuredWidth(),
						mTitleView.getMeasuredHeight() + mMenuView.getMeasuredHeight());
				break;
			default:
				break;
		}
	}

}
