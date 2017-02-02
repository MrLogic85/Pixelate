package com.sleepyduck.pixelate4crafting.activity;

import android.content.ClipData;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.sleepyduck.pixelate4crafting.BuildConfig;
import com.sleepyduck.pixelate4crafting.R;
import com.sleepyduck.pixelate4crafting.firebase.FirebaseLogger;
import com.sleepyduck.pixelate4crafting.model.DatabaseContract;
import com.sleepyduck.pixelate4crafting.model.DatabaseManager;
import com.sleepyduck.pixelate4crafting.model.Pattern;
import com.sleepyduck.pixelate4crafting.service.AddNewPatternService;
import com.sleepyduck.pixelate4crafting.service.CalculateService;
import com.sleepyduck.pixelate4crafting.util.BetterLog;
import com.sleepyduck.pixelate4crafting.util.ItemAnimator;
import com.sleepyduck.pixelate4crafting.view.recycler.PatternLoader;
import com.sleepyduck.pixelate4crafting.view.recycler.SwipeCardAdapter;

import static com.sleepyduck.pixelate4crafting.model.DatabaseContract.PatternColumns.FLAG_COMPLETE;
import static com.sleepyduck.pixelate4crafting.model.DatabaseContract.PatternColumns.FLAG_IMAGE_STORED;
import static com.sleepyduck.pixelate4crafting.model.DatabaseContract.PatternColumns.FLAG_STORING_IMAGE;

/**
 * Created by fredrik.metcalf on 2016-04-08.
 */
public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_NEW_PATTERN = 1;
    private SwipeCardAdapter mAdapter;
    private FirebaseLogger mFirebaseLogger;

    private final View.OnClickListener mOnItemClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Object tag = v.getTag();
            if (tag != null && tag instanceof Pattern) {
                Pattern pattern = (Pattern) tag;
                launch(pattern.Id, v.findViewById(R.id.icon));
            }
        }
    };

    private final View.OnClickListener mOnRightButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Object tag = v.getTag();
            if (tag != null && tag instanceof Pattern) {
                Pattern pattern = (Pattern) tag;
                switch (pattern.getState()) {
                    case DatabaseContract.PatternColumns.STATE_LATEST:
                    case DatabaseContract.PatternColumns.STATE_ACTIVE:
                        pattern.edit()
                                .setState(DatabaseContract.PatternColumns.STATE_COMPLETED)
                                .apply(false);
                        break;
                    case DatabaseContract.PatternColumns.STATE_COMPLETED:
                        pattern.edit()
                                .setState(DatabaseContract.PatternColumns.STATE_ACTIVE)
                                .apply(false);
                        break;
                }
                mAdapter.notifyDataSetChanged();
            }
        }
    };

    private final View.OnClickListener mOnLeftButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Object tag = v.getTag();
            if (tag != null && tag instanceof Pattern) {
                Pattern pattern = (Pattern) tag;
                pattern.delete(MainActivity.this);
                mFirebaseLogger.patternDeleted();
            }
        }
    };

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {

        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mFirebaseLogger = new FirebaseLogger(FirebaseAnalytics.getInstance(this));

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        bindService(new Intent(this, CalculateService.class), serviceConnection, BIND_AUTO_CREATE);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler);

        if (recyclerView.getAdapter() == null) {
            mAdapter = new SwipeCardAdapter(this);
            mAdapter.setOnItemClickListener(mOnItemClickListener);
            mAdapter.setOnRightButtonClickListener(mOnRightButtonClickListener);
            mAdapter.setOnLeftButtonClickListener(mOnLeftButtonClickListener);
            recyclerView.setAdapter(mAdapter);
            recyclerView.setItemAnimator(new ItemAnimator());
            new PatternLoader(this, mAdapter);
        }

        AdView adView = (AdView) findViewById(R.id.adView);
        AdRequest.Builder adRequestBuilder = new AdRequest.Builder();
        if (BuildConfig.DEBUG) {
            adRequestBuilder.addTestDevice("C39E64851CA596B020F5A5C95550CBDA");
        }
        AdRequest adRequest = adRequestBuilder.build();
        adView.loadAd(adRequest);

        onNewIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        if (intent.getClipData() != null) {
            mFirebaseLogger.logShareReceived(intent.getClipData().getItemCount());
            ClipData clipData = intent.getClipData();
            for (int i = 0; i < clipData.getItemCount(); ++i) {
                Intent serviceIntent = new Intent(this, AddNewPatternService.class);
                serviceIntent.setData(clipData.getItemAt(i).getUri());
                startService(serviceIntent);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mAdapter.notifyDataSetChanged();
    }

    public void onAddClicked(View view) {
        createPattern();
    }

    private void launch(int patternId, View transitionView) {
        Pattern pattern = DatabaseManager.getPattern(this, patternId);
        switch (pattern.getFlag()) {
            case FLAG_STORING_IMAGE:
            case FLAG_IMAGE_STORED: {
                Toast.makeText(this, "Image processing, please stand by...", Toast.LENGTH_LONG).show();
            }
            break;
            case FLAG_COMPLETE: {
                mFirebaseLogger.patternOpened();
                ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                        this, transitionView, getString(R.string.transitionImage));
                Intent intent = new Intent(this, PatternActivity.class);
                intent.putExtra(Pattern.INTENT_EXTRA_ID, patternId);
                ActivityCompat.startActivity(this, intent, options.toBundle());
            }
            break;
            default: {
                Intent intent = new Intent(this, ChangeParametersActivity.class);
                intent.putExtra(Pattern.INTENT_EXTRA_ID, patternId);
                startActivity(intent);
            }
            break;
        }
    }

    @Override
    protected void onDestroy() {
        unbindService(serviceConnection);
        super.onDestroy();
    }

    private void createPattern() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_NEW_PATTERN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        BetterLog.d(this, "onActivityResult(%d, %d, data)", requestCode, resultCode);

        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_NEW_PATTERN:
                    Intent intent = new Intent(this, AddNewPatternService.class);
                    intent.setData(data.getData());
                    startService(intent);
                    break;
                default:
                    launch(data.getIntExtra(Pattern.INTENT_EXTRA_ID, 0), null);
                    break;
            }
        }
    }
}