package com.sleepyduck.pixelate4crafting.view.pattern;

import com.sleepyduck.pixelate4crafting.BetterLog;
import com.sleepyduck.pixelate4crafting.data.Patterns;
import com.sleepyduck.pixelate4crafting.data.Patterns.Pattern;

import android.app.Activity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.ViewGroup.LayoutParams;

public class PatternActivity extends Activity {
	private Pattern mPattern;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		int id = getIntent().getIntExtra(Patterns.INTENT_EXTRA_ID, -1);
		mPattern = Patterns.GetPattern(id);
		setContentView(new PatternView(this, mPattern), new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT));
		
		getActionBar().setTitle(mPattern.Title);
		getActionBar().setHomeButtonEnabled(true);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		BetterLog.d(this, "" + item + ", " + item.getOrder());
		finish();
		return true;
	}
}
