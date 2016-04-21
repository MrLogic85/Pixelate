package com.sleepyduck.pixelate4crafting.control;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.sleepyduck.pixelate4crafting.R;
import com.sleepyduck.pixelate4crafting.control.configuration.ConfigurationPixelsActivity;
import com.sleepyduck.pixelate4crafting.model.Pattern;
import com.sleepyduck.pixelate4crafting.model.Patterns;
import com.sleepyduck.pixelate4crafting.view.PatternImageView;

public class PatternActivity extends AppCompatActivity {
    private static final int REQUEST_CHANGE_PARAMETERS = 1;
    private static final int REQUEST_NEW_PIXELS = 2;
    private Pattern mPattern;
	private PatternImageView mCanvas;

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
		
		mCanvas = (PatternImageView) findViewById(R.id.canvas);
		mCanvas.setPattern(mPattern);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.pattern_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_item_change_parameters) {
            Intent intent = new Intent(this, ChangeParametersActivity.class);
            intent.putExtra(Patterns.INTENT_EXTRA_ID, mPattern.Id);
            startActivityForResult(intent, REQUEST_CHANGE_PARAMETERS);
            return true;
        }
		return super.onOptionsItemSelected(item);
	}

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CHANGE_PARAMETERS) {
            if (mPattern.needsRecalculation()) {
                Intent intent = new Intent(this, ConfigurationPixelsActivity.class);
                intent.putExtra(Patterns.INTENT_EXTRA_ID, mPattern.Id);
                startActivityForResult(intent, REQUEST_NEW_PIXELS);
            }
        } else if (requestCode == REQUEST_NEW_PIXELS) {
            if (mPattern.needsRecalculation()) {
                finish();
            }
        }
    }
}
