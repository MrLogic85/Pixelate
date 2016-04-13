package com.sleepyduck.pixelate4crafting.control;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.sleepyduck.pixelate4crafting.R;
import com.sleepyduck.pixelate4crafting.model.Pattern;
import com.sleepyduck.pixelate4crafting.model.Patterns;
import com.sleepyduck.pixelate4crafting.view.PatternCanvasView;
import com.sleepyduck.pixelate4crafting.control.util.BetterLog;

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
		mCanvas.setPattern(mPattern);

		/*mCanvas.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				mCanvas.getViewTreeObserver().removeOnGlobalLayoutListener(this);
				updateFile(mPattern.getFileName());
			}
		});*/
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
				//mCanvas.setPixelHeight(height);
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