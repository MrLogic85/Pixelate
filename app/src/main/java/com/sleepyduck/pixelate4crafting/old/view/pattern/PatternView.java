package com.sleepyduck.pixelate4crafting.old.view.pattern;

import android.animation.LayoutTransition;
import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;

import com.sleepyduck.pixelate4crafting.R;
import com.sleepyduck.pixelate4crafting.model.Pattern;
import com.sleepyduck.pixelate4crafting.old.Constants.MENU_STATE;
import com.sleepyduck.pixelate4crafting.view.PatternCanvasView;

public class PatternView extends ViewGroup {
	private LayoutTransition mLayoutTransition;

	private PatternMenuView mMenuView;
	private PatternCanvasView mCanvasView;
	private PatternMenuSizeView mMenuSizeView;

	private int mPadding;
	private int mVisibleMenu;

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
		//mPattern = pattern;
		setup();
	}

	private void setup() {
		mLayoutTransition = new LayoutTransition();
		mLayoutTransition.enableTransitionType(LayoutTransition.CHANGING);
		setLayoutTransition(mLayoutTransition);

		mPadding = (int) getResources().getDimension(R.dimen.padding);
		mVisibleMenu = (int) getResources().getDimension(R.dimen.pattern_menu_collapsed);

		mCanvasView = new PatternCanvasView(getContext());
		mCanvasView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		addView(mCanvasView);

		mMenuView = (PatternMenuView) inflate(getContext(), R.layout.pattern_menu_view, null);
		mMenuView.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT));
		addView(mMenuView);

		mMenuSizeView = (PatternMenuSizeView) inflate(getContext(), R.layout.pattern_menu_size_view, null);
		mMenuSizeView.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT));
		addView(mMenuSizeView);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);

		int width = MeasureSpec.getSize(widthMeasureSpec);
		int height = MeasureSpec.getSize(heightMeasureSpec);

		int widthMeasuredMenuSize = MeasureSpec.makeMeasureSpec((int) (width * 0.25), MeasureSpec.EXACTLY);
		int heightMeasuredMenuSize = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);
		mMenuSizeView.measure(widthMeasuredMenuSize, heightMeasuredMenuSize);

		int widthMeasuredMenu = MeasureSpec.makeMeasureSpec((int) (width * 0.8), MeasureSpec.EXACTLY);
		int heightMeasuredMenu = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);
		mMenuView.measure(widthMeasuredMenu, heightMeasuredMenu);

		int widthOffset = (mMenuSizeView.getState() == MENU_STATE.STATE_EXPANDED ? mMenuSizeView.getMeasuredWidth() : 0);
		widthOffset = Math.max(widthOffset, mVisibleMenu);
		int widthMeasuredCanvas = MeasureSpec.makeMeasureSpec(width - mPadding * 2 - widthOffset, MeasureSpec.EXACTLY);
		int heightMeasuredCanvas = MeasureSpec.makeMeasureSpec(height - mPadding * 2, MeasureSpec.EXACTLY);
		mCanvasView.measure(widthMeasuredCanvas, heightMeasuredCanvas);
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		switch (mMenuSizeView.getState()) {
			case STATE_COLLAPSED:
				mMenuSizeView.layout(0 - mMenuSizeView.getMeasuredWidth(), 0, 0, mMenuSizeView.getMeasuredHeight());
				break;
			case STATE_EXPANDED:
				mMenuSizeView.layout(0, 0, mMenuSizeView.getMeasuredWidth(), mMenuSizeView.getMeasuredHeight());
				break;
			default:
				break;
		}
		switch (mMenuView.getState()) {
			case STATE_COLLAPSED:
				mMenuView.layout(mVisibleMenu - mMenuView.getMeasuredWidth(), 0, mVisibleMenu,
						mMenuView.getMeasuredHeight());
				break;
			case STATE_EXPANDED:
				mMenuView.layout(0, 0, mMenuView.getMeasuredWidth(), mMenuView.getMeasuredHeight());
				break;
			default:
				break;
		}
		int widthOffset = (mMenuSizeView.getState() == MENU_STATE.STATE_EXPANDED ? mMenuSizeView.getMeasuredWidth() : 0);
		widthOffset = Math.max(widthOffset, mVisibleMenu);
		mCanvasView.layout(mPadding + widthOffset, mPadding, mCanvasView.getMeasuredWidth() + mPadding + widthOffset,
				mCanvasView.getMeasuredHeight() + mPadding);
	}

	public PatternCanvasView getCanvasView() {
		return mCanvasView;
	}

	public PatternMenuView getMenu() {
		return mMenuView;
	}

	public PatternMenuSizeView getMenuSize() {
		return mMenuSizeView;
	}

}
