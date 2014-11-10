package com.sleepyduck.pixelate4crafting;

import com.sleepyduck.pixelate4crafting.data.ColourPalettes;

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
    }
    
    public void onPatternsClicked(View view) {
    }
    
    public void onColourPalettesClicked(View view) {
    	startActivity(new Intent(this, ColourPalettesActivity.class));
    }
}
