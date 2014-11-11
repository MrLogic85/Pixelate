package com.sleepyduck.pixelate4crafting.view.pattern;

import com.sleepyduck.pixelate4crafting.BetterLog;
import com.sleepyduck.pixelate4crafting.R;
import com.sleepyduck.pixelate4crafting.data.ColourPalettes;
import com.sleepyduck.pixelate4crafting.data.Patterns;
import com.sleepyduck.pixelate4crafting.data.Patterns.Pattern;

import android.animation.LayoutTransition;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class PatternItemView extends ViewGroup implements Comparable<PatternItemView>, OnClickListener {
	public static final int STATE_NORMAL = 0x1;
	public static final int STATE_FOCUSED = 0x2;
	private int mState = STATE_NORMAL;

	private LayoutTransition mLayoutTransition;
	private final Pattern mPattern;
	private TextView mTitleView;
	private ImageView mPicture;
	private View mDivider;
	private LinearLayout mPaletteLayout;
	private int mMargins;

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
		mMargins = (int) getResources().getDimension(R.dimen.padding_small);

		mLayoutTransition = new LayoutTransition();
		mLayoutTransition.enableTransitionType(LayoutTransition.CHANGING);
		mLayoutTransition.enableTransitionType(LayoutTransition.CHANGE_APPEARING);
		mLayoutTransition.enableTransitionType(LayoutTransition.CHANGE_DISAPPEARING);
		setLayoutTransition(mLayoutTransition);

		mTitleView = new TextView(getContext());
		mTitleView.setText(mPattern.Title);
		mTitleView.setGravity(Gravity.CENTER_VERTICAL);
		addView(mTitleView);

		mPicture = new ImageView(getContext());
		mPicture.setBackgroundColor(Color.GRAY);
		mPicture.setOnClickListener(this);
		addView(mPicture);

		mDivider = new View(getContext());
		mDivider.setBackgroundResource(R.drawable.divider);
		addView(mDivider);

		mPaletteLayout = new LinearLayout(getContext());
		mPaletteLayout.setOrientation(LinearLayout.HORIZONTAL);
		mPaletteLayout.setDividerPadding(mMargins);
		if (mPattern.getPaletteId() > -1) {
			ColourPalettes.Palette palette = ColourPalettes.Get(mPattern.getPaletteId());
			int colourSize = (int) getResources().getDimension(R.dimen.colour_size);
			View view;
			LayoutParams lp = new LayoutParams(colourSize, colourSize);
			for (int colour : palette.getColours()) {
				view = new View(getContext());
				view.setLayoutParams(lp);
				view.setBackgroundColor(colour);
				mPaletteLayout.addView(view);
			}
		}
		addView(mPaletteLayout);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int width = MeasureSpec.getSize(widthMeasureSpec);
		int widthMeasured = MeasureSpec.makeMeasureSpec(width - mMargins * 2, MeasureSpec.EXACTLY);
		int widthMeasuredFull = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY);
		int itemSize = (int) getResources().getDimension(R.dimen.item_size);
		int itemSizeMeasured = MeasureSpec.makeMeasureSpec(itemSize, MeasureSpec.EXACTLY);
		int dividerHeight = (int) getResources().getDimension(R.dimen.divider);
		int dividerHeightMeasured = MeasureSpec.makeMeasureSpec(dividerHeight, MeasureSpec.EXACTLY);
		int paletteHeight = (int) getResources().getDimension(R.dimen.colour_size);
		int paletteHeightMeasured = MeasureSpec.makeMeasureSpec(paletteHeight, MeasureSpec.EXACTLY);
		int titleHeightMeasured = MeasureSpec.makeMeasureSpec(itemSize, MeasureSpec.AT_MOST);
		mDivider.measure(widthMeasuredFull, dividerHeightMeasured);
		int totalHeightMeasured;
		switch (mState) {
			case STATE_NORMAL:
				int titleWidthMeasured = MeasureSpec.makeMeasureSpec(width - itemSize - mMargins * 3,
						MeasureSpec.EXACTLY);
				mPicture.measure(itemSizeMeasured, itemSizeMeasured);
				mTitleView.measure(titleWidthMeasured, titleHeightMeasured);
				mPaletteLayout.measure(titleWidthMeasured, paletteHeightMeasured);
				totalHeightMeasured = MeasureSpec
						.makeMeasureSpec(mPicture.getMeasuredHeight() + mDivider.getMeasuredHeight() + mMargins * 2,
								MeasureSpec.EXACTLY);
				setMeasuredDimension(widthMeasureSpec, totalHeightMeasured);
				break;
			case STATE_FOCUSED:
				int pictureSize = (int) getResources().getDimension(R.dimen.picture_size);
				int pictureSizeMeasured = MeasureSpec.makeMeasureSpec(pictureSize, MeasureSpec.EXACTLY);
				mTitleView.measure(widthMeasured, titleHeightMeasured);
				mPicture.measure(pictureSizeMeasured, pictureSizeMeasured);
				mPaletteLayout.measure(widthMeasured, paletteHeightMeasured);
				totalHeightMeasured = MeasureSpec.makeMeasureSpec(
						mTitleView.getMeasuredHeight() + mPaletteLayout.getMeasuredHeight()
								+ mPicture.getMeasuredHeight() + mDivider.getMeasuredHeight() + mMargins * 4,
						MeasureSpec.EXACTLY);
				setMeasuredDimension(widthMeasureSpec, totalHeightMeasured);
				break;
		}
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		switch (mState) {
			case STATE_NORMAL:
				mPicture.layout(mMargins, mMargins, mPicture.getMeasuredWidth() + mMargins,
						mPicture.getMeasuredHeight() + mMargins);
				mTitleView.layout(mPicture.getMeasuredWidth() + mMargins * 2, mMargins * 2, mPicture.getMeasuredWidth()
						+ mTitleView.getMeasuredWidth() + mMargins * 2, mTitleView.getMeasuredHeight() + mMargins * 2);
				mPaletteLayout.layout(mPicture.getMeasuredWidth() + mMargins * 2, mPicture.getMeasuredHeight()
						- mPaletteLayout.getMeasuredHeight(),
						mPicture.getMeasuredWidth() + mPaletteLayout.getMeasuredWidth() + mMargins * 3,
						mPicture.getMeasuredHeight());
				mDivider.layout(0, mPicture.getMeasuredHeight() + mMargins * 2, mDivider.getMeasuredWidth(),
						mPicture.getMeasuredHeight() + mDivider.getMeasuredHeight() + mMargins * 2);
				break;
			case STATE_FOCUSED:
				mTitleView.layout(mMargins, mMargins, mTitleView.getMeasuredWidth() + mMargins,
						mTitleView.getMeasuredHeight() + mMargins);
				mPaletteLayout.layout(mMargins, mTitleView.getMeasuredHeight() + mMargins * 2,
						mPaletteLayout.getMeasuredWidth() + mMargins,
						mTitleView.getMeasuredHeight() + mPaletteLayout.getMeasuredHeight() + mMargins * 2);
				int dxPicture = ((r - l) - mPicture.getMeasuredWidth()) / 2;
				mPicture.layout(dxPicture, mTitleView.getMeasuredHeight() + mPaletteLayout.getMeasuredHeight()
						+ mMargins * 3, dxPicture + mPicture.getMeasuredWidth(), mTitleView.getMeasuredHeight()
						+ mPaletteLayout.getMeasuredHeight() + mPicture.getMeasuredHeight() + mMargins * 3);
				mDivider.layout(
						0,
						mTitleView.getMeasuredHeight() + mPaletteLayout.getMeasuredHeight()
								+ mPicture.getMeasuredHeight() + mMargins * 4,
						mDivider.getMeasuredWidth(),
						mTitleView.getMeasuredHeight() + mPaletteLayout.getMeasuredHeight()
								+ mPicture.getMeasuredHeight() + mDivider.getMeasuredHeight() + mMargins * 4);
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
		switch (state) {
			case STATE_NORMAL:
				setBackground(null);
				break;
			case STATE_FOCUSED:
				setBackgroundColor(getResources().getColor(R.color.background_blue));
			default:
				break;
		}
	}

	@Override
	public void onClick(View view) {
		if (view instanceof ImageView) {
			Intent intent = new Intent(getContext(), ModifyPatternActivity.class);
			intent.putExtra(Patterns.INTENT_EXTRA_ID, mPattern.Id);
			getContext().startActivity(intent);
		}
	}

}
