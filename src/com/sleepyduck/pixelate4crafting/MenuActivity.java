package com.sleepyduck.pixelate4crafting;

import com.sleepyduck.pixelate4crafting.data.ColorPalettes;
import com.sleepyduck.pixelate4crafting.data.Patterns;
import com.sleepyduck.pixelate4crafting.view.color.ColorPalettesActivity;
import com.sleepyduck.pixelate4crafting.view.pattern.ListPatternActivity;
import com.sleepyduck.pixelate4crafting.view.pattern.PatternActivity;

import android.view.View;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;


public class MenuActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        ColorPalettes.Load(this);
        Patterns.Load(this);
    }
    
    public void onPatternsClicked(View view) {
    	startActivity(new Intent(this, ListPatternActivity.class));
    }
    
    public void onColourPalettesClicked(View view) {
    	startActivity(new Intent(this, ColorPalettesActivity.class));
    }
    
    public void onShortCutClicked(View view) {
    }
}
