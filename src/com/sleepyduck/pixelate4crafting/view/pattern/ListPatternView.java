package com.sleepyduck.pixelate4crafting.view.pattern;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;

public class ListPatternView extends ViewGroup {
	private List<ListPatternItemView> mPatternViews = new ArrayList<ListPatternItemView>();

	public ListPatternView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public ListPatternView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public ListPatternView(Context context) {
		super(context);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		for (ListPatternItemView patternView : mPatternViews) {
			patternView.measure(widthMeasureSpec, heightMeasureSpec);
		}
		int height = 0;
		for (ListPatternItemView patternView : mPatternViews) {
			height += patternView.getMeasuredHeight();
		}
		setMeasuredDimension(widthMeasureSpec, MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		int y = 0;
		for (ListPatternItemView patternView : mPatternViews) {
			patternView.layout(0, y, patternView.getMeasuredWidth(), patternView.getMeasuredHeight() + y);
			y += patternView.getMeasuredHeight();
		}
	}

	public void addPattern(ListPatternItemView item) {
		mPatternViews.add(item);
		addView(item);
	}

}
