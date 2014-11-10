package com.sleepyduck.pixelate4crafting;

import com.sleepyduck.pixelate4crafting.data.ColourPalettes;
import com.sleepyduck.pixelate4crafting.data.Patterns;
import com.sleepyduck.pixelate4crafting.view.colour.ColourPalettesActivity;
import com.sleepyduck.pixelate4crafting.view.pattern.PatternsActivity;

import android.view.View;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;


public class MenuActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        ColourPalettes.Load();
        Patterns.Load();
    }
    
    public void onPatternsClicked(View view) {
    	startActivity(new Intent(this, PatternsActivity.class));
    }
    
    public void onColourPalettesClicked(View view) {
    	startActivity(new Intent(this, ColourPalettesActivity.class));
    }
}
