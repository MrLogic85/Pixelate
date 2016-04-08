package com.sleepyduck.pixelate4crafting.old;

import com.sleepyduck.pixelate4crafting.R;
import com.sleepyduck.pixelate4crafting.old.ColorPalettes;
import com.sleepyduck.pixelate4crafting.old.Patterns;
import com.sleepyduck.pixelate4crafting.old.view.color.ColorPalettesActivity;
import com.sleepyduck.pixelate4crafting.old.view.pattern.ListPatternActivity;
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
