package com.sleepyduck.pixelate4crafting.view.pattern;

import com.sleepyduck.pixelate4crafting.R;
import com.sleepyduck.pixelate4crafting.data.BitmapHandler;
import com.sleepyduck.pixelate4crafting.data.ColorPalettes;
import com.sleepyduck.pixelate4crafting.data.Patterns.Pattern;

import android.content.Context;
import android.media.ThumbnailUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

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
		if (mPattern.getPaletteId() > -1) {
			for (int color : ColorPalettes.Get(mPattern.getPaletteId()).getColors()) {
				view = new View(getContext());
				view.setBackgroundColor(color);
				mPaletteLayout.addView(view, lp);
			}
		}

		if (mPattern.getFileNameThumbnail() != null && mPattern.getFileNameThumbnail().length() > 0) {
			mPicture.setImageBitmap(BitmapHandler.getFromFileName(getContext(), mPattern.getFileNameThumbnail()));
		}
	}
}
