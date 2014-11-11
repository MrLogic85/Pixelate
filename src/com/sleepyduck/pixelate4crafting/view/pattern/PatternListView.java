package com.sleepyduck.pixelate4crafting.view.pattern;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.animation.LayoutTransition;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import com.sleepyduck.pixelate4crafting.BetterLog;
import com.sleepyduck.pixelate4crafting.data.Patterns;
import com.sleepyduck.pixelate4crafting.data.Patterns.Pattern;

public class PatternListView extends ViewGroup implements OnClickListener {
	private List<PatternItemView> mPatterns = new ArrayList<PatternItemView>();
	private PatternItemView mSelectedPattern = null;
	private LayoutTransition mLayoutTransition;

	public PatternListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		setup();
	}

	public PatternListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		setup();
	}

	public PatternListView(Context context) {
		super(context);
		setup();
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		for (PatternItemView patternView : mPatterns) {
			patternView.measure(widthMeasureSpec, heightMeasureSpec);
		}
		int height = 0;
		for (PatternItemView patternView : mPatterns) {
			height += patternView.getMeasuredHeight();
		}
		setMeasuredDimension(widthMeasureSpec, MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		int y = 0;
		for (PatternItemView patternView : mPatterns) {
			patternView.layout(0, y, patternView.getMeasuredWidth(), patternView.getMeasuredHeight() + y);
			y += patternView.getMeasuredHeight();
		}
	}

	private void setup() {
		BetterLog.d(this);

		mLayoutTransition = new LayoutTransition();
		mLayoutTransition.enableTransitionType(LayoutTransition.CHANGING);
		setLayoutTransition(mLayoutTransition);
		
		for (Pattern pattern : Patterns.GetPatterns()) {
			BetterLog.d(this, "new PatternItemView " + pattern.Title);
			mPatterns.add(new PatternItemView(getContext(), pattern));
			Collections.sort(mPatterns);
		}

		for (PatternItemView view : mPatterns) {
			addView(view);
			view.setOnClickListener(this);
		}

		setOnClickListener(this);
		setWillNotDraw(false);
	}

	@Override
	public void onClick(View view) {
		if (view instanceof PatternItemView) {
			if (mSelectedPattern != null) {
				mSelectedPattern.setState(PatternItemView.STATE_NORMAL);
			}
			if (mSelectedPattern == view) {
				mSelectedPattern = null;
			} else {
				mSelectedPattern = (PatternItemView) view;
				mSelectedPattern.setState(PatternItemView.STATE_FOCUSED);
			}
		} else if (view instanceof PatternListView) {
			if (mSelectedPattern != null) {
				mSelectedPattern.setState(PatternItemView.STATE_NORMAL);
			}
			mSelectedPattern = null;
		}
	}

}
