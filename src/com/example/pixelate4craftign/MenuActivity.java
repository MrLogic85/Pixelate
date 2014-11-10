package com.example.pixelate4craftign;

import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.content.Intent;
import android.os.Bundle;


public class MenuActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
    }
    
    public void onPatternsClicked(View view) {
    	startActivity(new Intent(this, ColourPalettesActivity.class));
    }
}
