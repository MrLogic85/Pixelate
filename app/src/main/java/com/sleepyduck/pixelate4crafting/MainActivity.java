package com.sleepyduck.pixelate4crafting;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.sleepyduck.pixelate4crafting.model.Pattern;
import com.sleepyduck.pixelate4crafting.model.Patterns;
import com.sleepyduck.pixelate4crafting.view.adapter.RecyclerAdapter;

import static com.sleepyduck.pixelate4crafting.model.Pattern.State.ACTIVE;
import static com.sleepyduck.pixelate4crafting.model.Pattern.State.COMPLETED;

/**
 * Created by fredrik.metcalf on 2016-04-08.
 */
public class MainActivity extends AppCompatActivity {
    private RecyclerAdapter mAdapter;
    private RecyclerView mRecyclerView;

    private View.OnClickListener mOnItemClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Object tag = v.getTag();
            if (tag != null && tag instanceof Pattern) {
                Pattern pattern = (Pattern) tag;
                Patterns.MakeLatest(pattern);
                Patterns.Save(MainActivity.this);
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
        //mRecyclerView.removeAllViews();
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
        Intent intent = new Intent(this, PatternActivity.class);
        intent.putExtra(Patterns.INTENT_EXTRA_ID, patternId);
        startActivity(intent);
    }

    private void createPattern() {
        Intent intent = new Intent(this, ConfigurationActivity.class);
        startActivity(intent);
    }
}
