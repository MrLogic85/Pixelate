package com.sleepyduck.pixelate4crafting.old.view.pattern;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sleepyduck.pixelate4crafting.R;
import com.sleepyduck.pixelate4crafting.control.BitmapHandler;
import com.sleepyduck.pixelate4crafting.model.Pattern;

public class ListPatternItemView extends RelativeLayout {
	private Pattern mPattern;
	private TextView mTitleView;
	private ImageView mPicture;
	private LinearLayout mPaletteLayout;

	public ListPatternItemView(Context context) {
		super(context);
	}

	public ListPatternItemView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public ListPatternItemView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public Pattern getPattern() {
		return mPattern;
	}

	public void setPattern(Pattern pattern) {
		mPattern = pattern;
		mTitleView = (TextView) findViewById(R.id.titleView);
		mPaletteLayout = (LinearLayout) findViewById(R.id.palette);
		mPicture = (ImageView) findViewById(R.id.imageView);

		mTitleView.setText(mPattern.getTitle());
		int colorSize = (int) getResources().getDimension(R.dimen.color_size);
		LayoutParams lp = new LayoutParams(colorSize, colorSize);
		View view;

		if (mPattern.getFileNameThumbnail() != null && mPattern.getFileNameThumbnail().length() > 0) {
			mPicture.setImageBitmap(BitmapHandler.getFromFileName(getContext(), mPattern.getFileNameThumbnail()));
		}
	}
}
