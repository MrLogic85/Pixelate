package com.sleepyduck.pixelate4crafting;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.ImageView.ScaleType;
import android.widget.NumberPicker;
import android.widget.NumberPicker.OnValueChangeListener;
import android.widget.ToggleButton;

import com.sleepyduck.pixelate4crafting.control.BitmapHandler;
import com.sleepyduck.pixelate4crafting.model.Pattern;
import com.sleepyduck.pixelate4crafting.model.Patterns;
import com.sleepyduck.pixelate4crafting.old.Constants;
import com.sleepyduck.pixelate4crafting.old.Constants.MENU_STATE;
import com.sleepyduck.pixelate4crafting.old.view.pattern.PatternView;
import com.sleepyduck.pixelate4crafting.view.PatternCanvasView;
import com.sleepyduck.pixelate4crafting.view.PatternCanvasView.ColorSelectionModel;
import com.sleepyduck.pixelate4crafting.util.BetterLog;

public class PatternActivity extends AppCompatActivity {
	private static final int CANVAS_VIEW_ID = 0x25746874;
	private Pattern mPattern;
	private PatternCanvasView mCanvas;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_pattern);

		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		ActionBar ab = getSupportActionBar();
		ab.setDisplayHomeAsUpEnabled(true);

		int id = getIntent().getIntExtra(Patterns.INTENT_EXTRA_ID, -1);
		mPattern = Patterns.GetPattern(id);
		
		mCanvas = (PatternCanvasView) findViewById(R.id.canvas);
		mCanvas.setId(CANVAS_VIEW_ID);
		mCanvas.setPixelWidth(mPattern.getPixelWidth());
		mCanvas.setPixelHeight(mPattern.getPixelHeight());

		mCanvas.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				mCanvas.getViewTreeObserver().removeOnGlobalLayoutListener(this);
				updateFile(mPattern.getFileName());
			}
		});
	}

	@Override
	protected void onResume() {
		mCanvas.executeRedraw();
		super.onResume();
	}

	private void updateFile(String fileName) {
		if (fileName != null) {
			Bitmap bitmap = BitmapHandler.getFromFileName(this, fileName);
			if (bitmap != null) {
				mCanvas.setImageBitmap(bitmap);
				int height = mPattern.getPixelWidth() * bitmap.getHeight() / bitmap.getWidth();
				mCanvas.setPixelHeight(height);
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
