package com.sleepyduck.pixelate4crafting;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.sleepyduck.pixelate4crafting.model.Patterns;
import com.sleepyduck.pixelate4crafting.util.BetterLog;
import com.sleepyduck.pixelate4crafting.view.adapter.RecyclerAdapter;
import com.sleepyduck.pixelate4crafting.view.adapter.RecyclerItemClickListener;

/**
 * Created by fredrik.metcalf on 2016-04-08.
 */
public class MainActivity extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Patterns.Load(this);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);

        RecyclerAdapter adapter = new RecyclerAdapter(this);
        adapter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BetterLog.d(this, "On view clicked");
            }
        });
        recyclerView.setAdapter(adapter);

        recyclerView.addOnItemTouchListener(new RecyclerItemClickListener(this,
                new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        // TODO Check if it works
                    }
                }));
    }
}
