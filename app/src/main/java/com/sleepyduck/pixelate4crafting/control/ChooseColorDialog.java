package com.sleepyduck.pixelate4crafting.control;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.GridLayout;

import com.sleepyduck.pixelate4crafting.R;

/**
 * Created by fredrikmetcalf on 21/04/16.
 */
public class ChooseColorDialog extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chose_color);
        GridLayout layout = (GridLayout) findViewById(R.id.color_grid);

        int[] pixels = getIntent().getIntArrayExtra("pixels");
        for (int i = 0; i < layout.getChildCount(); ++i) {
            if (i < pixels.length) {
                layout.getChildAt(i).setBackgroundColor(pixels[i]);
            } else {
                layout.getChildAt(i).setVisibility(View.GONE);
            }
        }
    }

    public void onColorClicked(View view) {
        Intent intent = new Intent();
        intent.putExtra("pixel", ((ColorDrawable)view.getBackground()).getColor());
        setResult(RESULT_OK, intent);
        finish();
    }
}
