package com.sleepyduck.pixelate4crafting;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ListView;

public class PatternsActivity extends Activity {

	private ListView mPatternList;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mPatternList = (ListView) findViewById(R.id.patterns_list);
		mPatternList.setAdapter(new PatternListAdapter());
	}

}
