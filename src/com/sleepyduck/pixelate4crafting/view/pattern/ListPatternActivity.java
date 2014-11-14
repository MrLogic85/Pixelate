package com.sleepyduck.pixelate4crafting.view.pattern;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.sleepyduck.pixelate4crafting.BetterLog;
import com.sleepyduck.pixelate4crafting.R;
import com.sleepyduck.pixelate4crafting.data.Patterns;
import com.sleepyduck.pixelate4crafting.data.Patterns.Pattern;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;

public class ListPatternActivity extends Activity implements OnClickListener {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_list_patterns);
		getActionBar().setTitle(R.string.patterns);
		getActionBar().setHomeButtonEnabled(true);

		ListPatternView listPattern = (ListPatternView) findViewById(R.id.patterns_list);
		List<Pattern> patterns = new ArrayList<Pattern>(Patterns.GetPatterns());
		Collections.sort(patterns);
		ListPatternItemView item;

		for (Pattern pattern : patterns) {
			item = (ListPatternItemView) View.inflate(this, R.layout.list_pattern_item_view, null);
			item.setOnClickListener(this);
			item.setPattern(pattern);
			listPattern.addPattern(item);
		}
	}

	@Override
	public void onClick(View view) {
		if (view instanceof ListPatternItemView) {
			Intent intent = new Intent(this, PatternActivity.class);
			intent.putExtra(Patterns.INTENT_EXTRA_ID, ((ListPatternItemView) view).getPattern().Id);
			startActivity(intent);
		}
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		BetterLog.d(this, "" + item + ", " + item.getOrder());
		finish();
		return true;
	}

}
