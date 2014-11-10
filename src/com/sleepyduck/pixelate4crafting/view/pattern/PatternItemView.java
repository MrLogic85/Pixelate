package com.sleepyduck.pixelate4crafting.view.pattern;

import com.sleepyduck.pixelate4crafting.BetterLog;
import com.sleepyduck.pixelate4crafting.R;
import com.sleepyduck.pixelate4crafting.data.Patterns.Pattern;

import android.animation.LayoutTransition;
import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class PatternItemView extends ViewGroup implements
		Comparable<PatternItemView> {
	public static final int STATE_NORMAL = 0x1;
	public static final int STATE_FOCUSED = 0x2;
	private int mState = STATE_NORMAL;

	private LayoutTransition mLayoutTransition;
	private final Pattern mPattern;
	private TextView mTitleView;
	private ImageView mPicture;
	private View mDivider;

	public PatternItemView(Context context, Pattern pattern) {
		super(context);
		mPattern = pattern;
		setup();
	}

	public PatternItemView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mPattern = null;
		setup();
	}

	public PatternItemView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mPattern = null;
		setup();
	}

	private void setup() {
		BetterLog.d(this);
		mLayoutTransition = new LayoutTransition();
		mLayoutTransition.enableTransitionType(LayoutTransition.CHANGING);
		setLayoutTransition(mLayoutTransition);
		setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT));

		mTitleView = new TextView(getContext());
		mTitleView.setText(mPattern.Title);
		mTitleView.setGravity(Gravity.CENTER_VERTICAL);
		mTitleView.setPadding(
				(int) getResources().getDimension(R.dimen.padding_small), 0, 0,
				0);
		addView(mTitleView);

		mPicture = new ImageView(getContext());
		mPicture.setBackgroundColor(Color.GRAY);
		addView(mPicture);

		mDivider = new View(getContext());
		mDivider.setBackgroundColor(getResources().getColor(R.color.divider));
		addView(mDivider);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int widthMeasured = MeasureSpec.getSize(widthMeasureSpec);
		int itemSize = (int) getResources().getDimension(R.dimen.item_size);
		int itemSizeMeasured = MeasureSpec.makeMeasureSpec(itemSize,
				MeasureSpec.EXACTLY);
		int dividerHeight = (int) getResources().getDimension(R.dimen.divider);
		int dividerHeightMeasured = MeasureSpec.makeMeasureSpec(dividerHeight,
				MeasureSpec.EXACTLY);
		switch (mState) {
			case STATE_NORMAL:
				int titleWidthMeasured = MeasureSpec.makeMeasureSpec(
						widthMeasured - itemSize, MeasureSpec.EXACTLY);
				mTitleView.measure(titleWidthMeasured, itemSizeMeasured);
				mPicture.measure(itemSizeMeasured, itemSizeMeasured);
				mDivider.measure(widthMeasured, dividerHeightMeasured);
				int totalHeightMeasured = MeasureSpec.makeMeasureSpec(itemSize
						+ dividerHeight, MeasureSpec.EXACTLY);
				setMeasuredDimension(widthMeasureSpec, totalHeightMeasured);
				break;
			case STATE_FOCUSED:
				int pictureSize = (int) getResources().getDimension(
						R.dimen.picture_size);
				int pictureSizeMeasured = MeasureSpec.makeMeasureSpec(
						pictureSize, MeasureSpec.EXACTLY);
				totalHeightMeasured = MeasureSpec.makeMeasureSpec(itemSize
						+ pictureSize + dividerHeight, MeasureSpec.EXACTLY);
				mTitleView.measure(widthMeasured, itemSizeMeasured);
				mPicture.measure(pictureSizeMeasured, pictureSizeMeasured);
				mDivider.measure(widthMeasured, dividerHeightMeasured);
				setMeasuredDimension(widthMeasureSpec, totalHeightMeasured);
				break;
		}
		BetterLog.d(this, "Measured: " + getMeasuredWidth() + ", "
				+ getMeasuredHeight());
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		BetterLog.d(this, "" + l + ", " + t + ", " + r + ", " + b);
		switch (mState) {
			case STATE_NORMAL:
				mTitleView.layout(mPicture.getMeasuredWidth(), 0, r - l,
						mTitleView.getMeasuredHeight());
				mPicture.layout(0, 0, mPicture.getMeasuredWidth(),
						mPicture.getMeasuredHeight());
				mDivider.layout(
						0,
						mPicture.getMeasuredHeight(),
						r - l,
						mPicture.getMeasuredHeight()
								+ mDivider.getMeasuredHeight());
				break;
			case STATE_FOCUSED:
				mTitleView.layout(0, 0, r - l, mTitleView.getMeasuredHeight());
				int dxPicture = ((r - l) - mPicture.getMeasuredWidth()) / 2;
				mPicture.layout(
						dxPicture,
						mTitleView.getMeasuredHeight(),
						dxPicture + mPicture.getMeasuredWidth(),
						mTitleView.getMeasuredHeight()
								+ mPicture.getMeasuredHeight());
				mDivider.layout(
						0,
						mTitleView.getMeasuredHeight()
								+ mPicture.getMeasuredHeight(),
						r - l,
						mTitleView.getMeasuredHeight()
								+ mPicture.getMeasuredHeight()
								+ mDivider.getMeasuredHeight());
				break;
		}
	}

	public Pattern getPattern() {
		return mPattern;
	}

	@Override
	public int compareTo(PatternItemView another) {
		return this.mPattern.Title.compareTo(another.mPattern.Title);
	}

	public void setState(int state) {
		mState = state;
		requestLayout();
	}

}
