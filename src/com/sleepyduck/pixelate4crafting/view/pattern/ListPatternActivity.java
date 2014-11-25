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
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;

public class ListPatternActivity extends Activity implements OnClickListener {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_list_patterns);
		getActionBar().setTitle(R.string.patterns);
		getActionBar().setHomeButtonEnabled(true);
	}

	@Override
	protected void onResume() {
		super.onResume();
		setContentView(R.layout.activity_list_patterns);
		getActionBar().setTitle(R.string.patterns);
		getActionBar().setHomeButtonEnabled(true);

		LinearLayout listPattern = (LinearLayout) findViewById(R.id.patterns_list);
		listPattern.removeAllViews();
		List<Pattern> patterns = new ArrayList<Pattern>(Patterns.GetPatterns());
		Collections.sort(patterns);
		ListPatternItemView item;

		for (Pattern pattern : patterns) {
			item = (ListPatternItemView) View.inflate(this, R.layout.list_pattern_item_view, null);
			item.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
			item.setOnClickListener(this);
			item.setPattern(pattern);
			listPattern.addView(item);
		}

		Button button = new Button(this);
		button.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		int padding = (int) getResources().getDimension(R.dimen.padding);
		button.setPadding(padding, padding, padding, padding);
		button.setText(R.string.new_pattern);
		listPattern.addView(button, button.getLayoutParams());
		button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Pattern pattern = new Pattern("New Pattern");
				pattern.setPaletteId(1);
				Patterns.Add(pattern);
				launch(pattern.Id);
			}
		});
	}

	@Override
	public void onClick(View view) {
		if (view instanceof ListPatternItemView) {
			launch(((ListPatternItemView) view).getPattern().Id);
		}
	}

	private void launch(int patternId) {
		Intent intent = new Intent(this, PatternActivity.class);
		intent.putExtra(Patterns.INTENT_EXTRA_ID, patternId);
		startActivity(intent);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		BetterLog.d(this, "" + item + ", " + item.getOrder());
		finish();
		return true;
	}

}
