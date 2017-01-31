package com.sleepyduck.pixelate4crafting.configuration;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.sleepyduck.pixelate4crafting.R;
import com.sleepyduck.pixelate4crafting.model.DatabaseManager;
import com.sleepyduck.pixelate4crafting.model.Pattern;
import com.sleepyduck.pixelate4crafting.model.Patterns;

/**
 * Created by fredrik.metcalf on 2016-04-13.
 */
public class ConfigurationNameActivity extends Activity {

    private Pattern mPattern;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configure_name);

        mPattern = DatabaseManager.getPattern(this
                , getIntent().getIntExtra(Patterns.INTENT_EXTRA_ID, 0));
        EditText text = (EditText) findViewById(R.id.edit_text);
        text.setText(mPattern.getTitle());

        setFinishOnTouchOutside(false);
    }

    public void onDoneClicked(View view) {
        EditText text = (EditText) findViewById(R.id.edit_text);
        if (text.getText().length() > 0) {
            mPattern.edit()
                    .setTitle(text.getText().toString())
                    .apply(false);
        }

        Intent result = new Intent();
        result.putExtra(Patterns.INTENT_EXTRA_ID, mPattern.Id);
        setResult(RESULT_OK, result);
        finish();
    }
}
