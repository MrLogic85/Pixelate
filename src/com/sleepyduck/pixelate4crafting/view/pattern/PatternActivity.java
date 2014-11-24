package com.sleepyduck.pixelate4crafting.view.pattern;

import java.io.IOException;

import com.sleepyduck.pixelate4crafting.BetterLog;
import com.sleepyduck.pixelate4crafting.R;
import com.sleepyduck.pixelate4crafting.data.BitmapHandler;
import com.sleepyduck.pixelate4crafting.data.Constants;
import com.sleepyduck.pixelate4crafting.data.Patterns;
import com.sleepyduck.pixelate4crafting.data.Constants.MENU_STATE;
import com.sleepyduck.pixelate4crafting.data.Patterns.Pattern;
import com.sleepyduck.pixelate4crafting.view.pattern.PatternCanvasView.ColorSelectionModel;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.NumberPicker;
import android.widget.NumberPicker.OnValueChangeListener;
import android.widget.Toast;
import android.widget.ToggleButton;

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
				BetterLog.d(this);
				mView.getMenu().setState(MENU_STATE.STATE_COLLAPSED);
				mView.getMenuSize().setState(MENU_STATE.STATE_COLLAPSED);
				mView.requestLayout();
			}
		});
		mView.getCanvasView().setId(CANVAS_VIEW_ID);

		getActionBar().setTitle(mPattern.Title);
		getActionBar().setHomeButtonEnabled(true);

		mWidthPicker = (NumberPicker) findViewById(R.id.width_number);
		mWidthPicker.setMaxValue(1000);
		mWidthPicker.setMinValue(1);
		mWidthPicker.setValue(Constants.NUM_PIXELS);
		mWidthPicker.setOnValueChangedListener(new OnValueChangeListener() {
			@Override
			public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
				PatternCanvasView canvasView = mView.getCanvasView();
				int newHeight = newVal * canvasView.getHeight() / canvasView.getWidth();
				mHeightPicker.setValue(newHeight);
				canvasView.setPixelWidth(newVal);
				canvasView.setPixelHeight(newHeight);
			}
		});
		mHeightPicker = (NumberPicker) findViewById(R.id.height_number);
		mHeightPicker.setMaxValue(1000);
		mHeightPicker.setMinValue(1);
		mHeightPicker.setValue(Constants.NUM_PIXELS);
		mHeightPicker.setOnValueChangedListener(new OnValueChangeListener() {
			@Override
			public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
				PatternCanvasView canvasView = mView.getCanvasView();
				int newWidth = newVal * canvasView.getWidth() / canvasView.getHeight();
				mWidthPicker.setValue(newWidth);
				canvasView.setPixelHeight(newVal);
				canvasView.setPixelWidth(newWidth);
			}
		});
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		BetterLog.d(this, "" + item + ", " + item.getOrder());
		finish();
		return true;
	}

	public void onChooseImageClicked(View view) {
		BetterLog.d(this);
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		intent.addCategory(Intent.CATEGORY_OPENABLE);
		intent.setType("image/*");
		startActivityForResult(intent, REQUEST_IMAGE);
	}

	public void onColorSelectionModelClicked(View view) {
		BetterLog.d(this);
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
		BetterLog.d(this);
		mView.getMenu().setState(MENU_STATE.STATE_COLLAPSED);
		mView.getMenuSize().setState(MENU_STATE.STATE_EXPANDED);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode != RESULT_OK) {
			return;
		}

		if (requestCode == REQUEST_IMAGE) {
			BetterLog.d(this, "" + data);
			Uri imageUri = data.getData();
			try {
				final int takeFlags = data.getFlags() & (Intent.FLAG_GRANT_READ_URI_PERMISSION);
				getContentResolver().takePersistableUriPermission(imageUri, takeFlags);
			} catch (Exception e) {
			}
			if ("content".equals(imageUri.getScheme())) {
				String fileName = BitmapHandler.getFileName(this, imageUri);
				BetterLog.d(this, fileName);
				try {
					Bitmap bitmap = BitmapHandler.getFromUri(this, imageUri);
					mView.getCanvasView().setImageBitmap(bitmap);
					mWidthPicker.setMaxValue(bitmap.getWidth());
					mHeightPicker.setMaxValue(bitmap.getHeight());
					mWidthPicker.setValue(mView.getCanvasView().getPixelWidth());
					mHeightPicker.setValue(mView.getCanvasView().getPixelHeight());
				} catch (IOException e) {
					e.printStackTrace();
					Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
				}
			}
		}

		mView.getMenu().setState(MENU_STATE.STATE_COLLAPSED);
		mView.getMenuSize().setState(MENU_STATE.STATE_COLLAPSED);
	}
}
