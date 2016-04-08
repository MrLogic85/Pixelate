package com.sleepyduck.pixelate4crafting.old.view.pattern;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.sleepyduck.pixelate4crafting.BetterLog;
import com.sleepyduck.pixelate4crafting.R;
import com.sleepyduck.pixelate4crafting.old.Patterns;
import com.sleepyduck.pixelate4crafting.old.Patterns.Pattern;
import com.sleepyduck.pixelate4crafting.old.view.LinearLayoutFling;
import com.sleepyduck.pixelate4crafting.old.view.OnItemFlungListener;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;

public class ListPatternActivity extends Activity implements OnClickListener, OnItemFlungListener {
	private LinearLayoutFling mListPattern;

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
		mListPattern = (LinearLayoutFling) findViewById(R.id.patterns_list);
		mListPattern.setOnItemFlungListener(this);
		mListPattern.removeAllViews();

		List<Pattern> patterns = new ArrayList<Pattern>(Patterns.GetPatterns());
		Collections.sort(patterns);
		ListPatternItemView item;
		for (Pattern pattern : patterns) {
			item = (ListPatternItemView) View.inflate(this, R.layout.list_pattern_item_view, null);
			item.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
			item.setOnClickListener(this);
			item.setPattern(pattern);
			mListPattern.addView(item);
		}
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

	@Override
	public boolean onItemFlung(View view) {
		if (view instanceof ListPatternItemView) {
			ListPatternItemView item = (ListPatternItemView) view;
			Patterns.Remove(item.getPattern());
			Patterns.Save(this);
			mListPattern.removeView(view);
			return true;
		}
		return false;
	}

	public void onNewClicked(View view) {
		Pattern pattern = new Pattern("New Pattern");
		pattern.setPaletteId(1);
		Patterns.Add(pattern);
		launch(pattern.Id);
	}

}
