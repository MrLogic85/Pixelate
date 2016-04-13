package com.sleepyduck.pixelate4crafting.control;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Toast;

import com.sleepyduck.pixelate4crafting.R;
import com.sleepyduck.pixelate4crafting.control.configuration.ConfigurationColorsActivity;
import com.sleepyduck.pixelate4crafting.control.configuration.ConfigurationPixelsActivity;
import com.sleepyduck.pixelate4crafting.model.Pattern;
import com.sleepyduck.pixelate4crafting.model.Patterns;
import com.sleepyduck.pixelate4crafting.view.adapter.RecyclerAdapter;
import com.sleepyduck.pixelate4crafting.control.configuration.ConfigurationActivity;

import static com.sleepyduck.pixelate4crafting.model.Pattern.State.ACTIVE;
import static com.sleepyduck.pixelate4crafting.model.Pattern.State.COMPLETED;

/**
 * Created by fredrik.metcalf on 2016-04-08.
 */
public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_NEW_PATTERN = 1;
    private static final int REQUEST_REDO_COLORS = 2;
    private static final int REQUEST_REDO_PIXELS = 3;
    private RecyclerAdapter mAdapter;
    private RecyclerView mRecyclerView;

    private View.OnClickListener mOnItemClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Object tag = v.getTag();
            if (tag != null && tag instanceof Pattern) {
                Pattern pattern = (Pattern) tag;
                launch(pattern.Id);
            }
        }
    };

    private View.OnClickListener mOnRightButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Object tag = v.getTag();
            if (tag != null && tag instanceof Pattern) {
                Pattern pattern = (Pattern) tag;
                switch (pattern.getState()) {
                    case LATEST:
                    case ACTIVE:
                        pattern.setState(COMPLETED);
                        break;
                    case COMPLETED:
                        pattern.setState(ACTIVE);
                        break;
                }
                Patterns.Save(MainActivity.this);
                mAdapter.notifyDataSetChanged();
            }
        }
    };

    private View.OnClickListener mOnLeftButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Object tag = v.getTag();
            if (tag != null && tag instanceof Pattern) {
                Pattern pattern = (Pattern) tag;
                Patterns.Remove(pattern);
                pattern.destroy(MainActivity.this);
                Patterns.Save(MainActivity.this);
                mAdapter.notifyDataSetChanged();
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Patterns.Load(this);

        mRecyclerView = (RecyclerView) findViewById(R.id.recycler);
        if (mRecyclerView.getLayoutManager() == null) {
            LinearLayoutManager layoutManager = new LinearLayoutManager(this);
            mRecyclerView.setLayoutManager(layoutManager);
        }

        if (mRecyclerView.getAdapter() == null) {
            mAdapter = new RecyclerAdapter(this);
            mAdapter.setOnItemClickListener(mOnItemClickListener);
            mAdapter.setOnRightButtonClickListener(mOnRightButtonClickListener);
            mAdapter.setOnLeftButtonClickListener(mOnLeftButtonClickListener);
            mRecyclerView.setAdapter(mAdapter);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mAdapter.notifyDataSetChanged();
    }

    public void onAddClicked(View view) {
        createPattern();
    }

    private void launch(int patternId) {
        Pattern pattern = Patterns.GetPattern(patternId);
        if (BitmapHandler.getFromFileName(this, pattern.getFileName()) == null) {
            Toast.makeText(this, "Image not found! I am truly sorry, this pattern is broken :(", Toast.LENGTH_LONG).show();
        } else if (pattern.getColors() == null) {
            Toast.makeText(this, "No color scheme in pattern, please redo the color choosing process", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(this, ConfigurationColorsActivity.class);
            intent.putExtra(Patterns.INTENT_EXTRA_ID, patternId);
            startActivityForResult(intent, REQUEST_REDO_COLORS);
        } else if (pattern.getColorMatrix() == null) {
            Toast.makeText(this, "Pixels haven't been created yet, ", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(this, ConfigurationPixelsActivity.class);
            intent.putExtra(Patterns.INTENT_EXTRA_ID, patternId);
            startActivityForResult(intent, REQUEST_REDO_PIXELS);
        } else {
            if (pattern.getState() == ACTIVE) {
                Patterns.MakeLatest(pattern);
                Patterns.Save(MainActivity.this);
            }
            Intent intent = new Intent(this, PatternActivity.class);
            intent.putExtra(Patterns.INTENT_EXTRA_ID, patternId);
            startActivity(intent);
        }
    }

    private void createPattern() {
        Intent intent = new Intent(this, ConfigurationActivity.class);
        startActivityForResult(intent, REQUEST_NEW_PATTERN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_NEW_PATTERN: {
                    int patternId = data.getIntExtra(Patterns.INTENT_EXTRA_ID, 0);
                    Pattern pattern = Patterns.GetPattern(patternId);
                    Patterns.MakeLatest(pattern);

                    Intent intent = new Intent(this, PatternActivity.class);
                    intent.putExtra(Patterns.INTENT_EXTRA_ID, patternId);
                    startActivity(intent);
                }
                case REQUEST_REDO_COLORS:
                case REQUEST_REDO_PIXELS: {
                    launch(data.getIntExtra(Patterns.INTENT_EXTRA_ID, 0));
                }
            }
        }

        // Save in case something changed
        Patterns.Save(this);
    }
}