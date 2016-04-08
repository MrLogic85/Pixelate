package com.sleepyduck.pixelate4crafting.old.view.pattern;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.ImageView.ScaleType;
import android.widget.NumberPicker;
import android.widget.NumberPicker.OnValueChangeListener;
import android.widget.ToggleButton;

import com.sleepyduck.pixelate4crafting.R;
import com.sleepyduck.pixelate4crafting.control.BitmapHandler;
import com.sleepyduck.pixelate4crafting.model.Pattern;
import com.sleepyduck.pixelate4crafting.model.Patterns;
import com.sleepyduck.pixelate4crafting.old.Constants;
import com.sleepyduck.pixelate4crafting.old.Constants.MENU_STATE;
import com.sleepyduck.pixelate4crafting.old.view.pattern.PatternCanvasView.ColorSelectionModel;
import com.sleepyduck.pixelate4crafting.util.BetterLog;

public class PatternActivity extends Activity {
	private static final int REQUEST_IMAGE = 1;
	private static final int CANVAS_VIEW_ID = 0x25746874;
	private Pattern mPattern;
	private PatternView mView;
	private NumberPicker mWidthPicker;
	private NumberPicker mHeightPicker;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mView = new PatternView(this, mPattern);
		setContentView(mView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

		int id = getIntent().getIntExtra(Patterns.INTENT_EXTRA_ID, -1);
		mPattern = Patterns.GetPattern(id);

		mView.getCanvasView().setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mView.getMenu().setState(MENU_STATE.STATE_COLLAPSED);
				mView.getMenuSize().setState(MENU_STATE.STATE_COLLAPSED);
				mView.requestLayout();
			}
		});
		mView.getCanvasView().setId(CANVAS_VIEW_ID);
		mView.getCanvasView().setScaleType(ScaleType.FIT_CENTER);
		mView.getCanvasView().setPixelWidth(mPattern.getPixelWidth());
		mView.getCanvasView().setPixelHeight(mPattern.getPixelHeight());

		getActionBar().setTitle(mPattern.getTitle());
		getActionBar().setHomeButtonEnabled(true);

		mWidthPicker = (NumberPicker) findViewById(R.id.width_number);
		mWidthPicker.setMaxValue(Constants.MAX_PIXELS);
		mWidthPicker.setMinValue(1);
		mWidthPicker.setValue(mPattern.getPixelWidth());
		mWidthPicker.setOnValueChangedListener(new OnValueChangeListener() {
			@Override
			public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
				PatternCanvasView canvasView = mView.getCanvasView();
				if (canvasView.getBitmapHeight() > -1) {
					int newHeight = newVal * canvasView.getBitmapHeight() / canvasView.getBitmapWidth();
					newHeight = Math.min(mHeightPicker.getMaxValue(), newHeight);
					newHeight = Math.max(mHeightPicker.getMinValue(), newHeight);
					mHeightPicker.setValue(newHeight);
					canvasView.setPixelWidth(newVal);
					canvasView.setPixelHeight(newHeight);
					mPattern.setPixelWidth(newVal);
					mPattern.setPixelHeight(newHeight);
					Patterns.Save(PatternActivity.this);
				}
			}
		});
		mHeightPicker = (NumberPicker) findViewById(R.id.height_number);
		mHeightPicker.setMaxValue(Constants.MAX_PIXELS);
		mHeightPicker.setMinValue(1);
		mHeightPicker.setValue(mPattern.getPixelHeight());
		mHeightPicker.setOnValueChangedListener(new OnValueChangeListener() {
			@Override
			public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
				PatternCanvasView canvasView = mView.getCanvasView();
				if (canvasView.getBitmapWidth() > -1) {
					int newWidth = newVal * canvasView.getBitmapWidth() / canvasView.getBitmapHeight();
					newWidth = Math.min(mWidthPicker.getMaxValue(), newWidth);
					newWidth = Math.max(mWidthPicker.getMinValue(), newWidth);
					mWidthPicker.setValue(newWidth);
					canvasView.setPixelHeight(newVal);
					canvasView.setPixelWidth(newWidth);
					mPattern.setPixelWidth(newWidth);
					mPattern.setPixelHeight(newVal);
					Patterns.Save(PatternActivity.this);
				}
			}
		});

		mView.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				mView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
				updateFile(mPattern.getFileName());
			}
		});
	}

	@Override
	protected void onResume() {
		mView.getCanvasView().executeRedraw();
		super.onResume();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		finish();
		return true;
	}

	public void onChooseImageClicked(View view) {
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		intent.addCategory(Intent.CATEGORY_OPENABLE);
		intent.setType("image/*");
		startActivityForResult(intent, REQUEST_IMAGE);
	}

	public void onColorSelectionModelClicked(View view) {
		if (view instanceof ToggleButton) {
			ToggleButton tButton = (ToggleButton) view;
			if (tButton.isChecked()) {
				mView.getCanvasView().setColorSelectionModel(ColorSelectionModel.Average);
			} else {
				mView.getCanvasView().setColorSelectionModel(ColorSelectionModel.Median);
			}
			mView.getMenu().setState(MENU_STATE.STATE_COLLAPSED);
		}
	}

	public void onChangeSizeClicked(View view) {
		mView.getMenu().setState(MENU_STATE.STATE_COLLAPSED);
		mView.getMenuSize().setState(MENU_STATE.STATE_EXPANDED);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode != RESULT_OK) {
			return;
		}

		if (requestCode == REQUEST_IMAGE) {
			Uri imageUri = data.getData();
			try {
				final int takeFlags = data.getFlags() & (Intent.FLAG_GRANT_READ_URI_PERMISSION);
				getContentResolver().takePersistableUriPermission(imageUri, takeFlags);
			} catch (Exception e) {
			}
			if ("content".equals(imageUri.getScheme())) {
				String fileName = BitmapHandler.getFileName(this, imageUri);
				BitmapHandler.storeLocally(this, imageUri, fileName);
				mPattern.setFileName(fileName);
				mPattern.setTitle(Pattern.createTitleFromFileName(fileName));
				getActionBar().setTitle(mPattern.getTitle());
				Patterns.Save(this);
				updateFile(fileName);
			}
		}

		mView.getMenu().setState(MENU_STATE.STATE_COLLAPSED);
		mView.getMenuSize().setState(MENU_STATE.STATE_COLLAPSED);
	}

	private void updateFile(String fileName) {
		if (fileName != null) {
			Bitmap bitmap = BitmapHandler.getFromFileName(this, fileName);
			if (bitmap != null) {
				mView.getCanvasView().setImageBitmap(bitmap);
				int height = mWidthPicker.getValue() * bitmap.getHeight() / bitmap.getWidth();
				mView.getCanvasView().setPixelHeight(height);
				mHeightPicker.setValue(height);
				mPattern.setPixelHeight(height);
				Patterns.Save(this);
			} else {
				BetterLog.d(this, "Found no bitmap");
			}
		} else {
			BetterLog.d(this, "No file name");
		}
	}
}
