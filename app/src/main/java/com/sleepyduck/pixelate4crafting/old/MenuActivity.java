package com.sleepyduck.pixelate4crafting.old;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.sleepyduck.pixelate4crafting.R;
import com.sleepyduck.pixelate4crafting.model.Patterns;
import com.sleepyduck.pixelate4crafting.old.view.pattern.ListPatternActivity;

public class MenuActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        Patterns.Load(this);
    }
    
    public void onPatternsClicked(View view) {
    	startActivity(new Intent(this, ListPatternActivity.class));
    }
    
    public void onShortCutClicked(View view) {
    }
}
