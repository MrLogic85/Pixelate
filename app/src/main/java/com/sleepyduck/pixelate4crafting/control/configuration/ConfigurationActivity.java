package com.sleepyduck.pixelate4crafting.control.configuration;

import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;

import com.sleepyduck.pixelate4crafting.control.ChangeParametersActivity;
import com.sleepyduck.pixelate4crafting.model.Patterns;

public class ConfigurationActivity extends Activity {
    public static final int REQUEST_CONFIGURE_IMAGE = 1;
    public static final int REQUEST_CONFIGURE_WIDTH = 2;
    public static final int REQUEST_CONFIGURE_COLORS = 3;
    public static final int REQUEST_CONFIGURE_PIXELS= 4;
    private static final int REQUEST_CHANGE_PARAMETERS = 5;
    private int mPatternId = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setFinishOnTouchOutside(false);
        startActivityForResult(getIntent(REQUEST_CONFIGURE_IMAGE), REQUEST_CONFIGURE_IMAGE);
    }

    public Intent getIntent(int request) {
        switch (request) {
            case REQUEST_CONFIGURE_IMAGE: {
                return new Intent(this, ConfigurationImageActivity.class);
            }
            case REQUEST_CONFIGURE_WIDTH: {
                Intent intent = new Intent(this, ConfigurationWidthActivity.class);
                intent.putExtra(Patterns.INTENT_EXTRA_ID, mPatternId);
                return intent;
            }
            case REQUEST_CONFIGURE_COLORS: {
                Intent intent = new Intent(this, ConfigurationColorsActivity.class);
                intent.putExtra(Patterns.INTENT_EXTRA_ID, mPatternId);
                return intent;
            }
            case REQUEST_CONFIGURE_PIXELS: {
                Intent intent = new Intent(this, ConfigurationPixelsActivity.class);
                intent.putExtra(Patterns.INTENT_EXTRA_ID, mPatternId);
                return intent;
            }
            case REQUEST_CHANGE_PARAMETERS: {
                Intent intent = new Intent(this, ChangeParametersActivity.class);
                intent.putExtra(Patterns.INTENT_EXTRA_ID, mPatternId);
                return intent;
            }
        }
        return null;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) {
            finish();
            return;
        }

        switch (requestCode) {
            case REQUEST_CONFIGURE_IMAGE: {
                mPatternId = data.getIntExtra(Patterns.INTENT_EXTRA_ID, 0);
                startActivityForResult(getIntent(REQUEST_CONFIGURE_WIDTH), REQUEST_CONFIGURE_WIDTH);
                return;
            }
            case REQUEST_CONFIGURE_WIDTH: {
                //startActivityForResult(getIntent(REQUEST_CONFIGURE_COLORS), REQUEST_CONFIGURE_COLORS);
                startActivityForResult(getIntent(REQUEST_CHANGE_PARAMETERS), REQUEST_CHANGE_PARAMETERS);
                return;
            }
            case REQUEST_CONFIGURE_COLORS: {
                startActivityForResult(getIntent(REQUEST_CONFIGURE_PIXELS), REQUEST_CONFIGURE_PIXELS);
                return;
            }
            case REQUEST_CONFIGURE_PIXELS: {
                Intent result = new Intent();
                result.putExtra(Patterns.INTENT_EXTRA_ID, mPatternId);
                setResult(RESULT_OK, result);
                finish();
                return;
            }
        }

        setResult(RESULT_CANCELED);
        finish();
    }
}
