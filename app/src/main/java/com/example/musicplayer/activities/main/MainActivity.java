package com.example.musicplayer.activities.main;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.accessibility.AccessibilityManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.musicplayer.color.AppColor;
import com.example.musicplayer.player.Player;
import com.example.musicplayer.R;
import com.example.musicplayer.Services.BackgroundMusicService;
import com.example.musicplayer.Services.NotificationService;
import com.example.musicplayer.activities.liked.LikeTrackAdapter;
import com.example.musicplayer.activities.settings.SettingsActivity;
import com.example.musicplayer.app.App;
import com.example.musicplayer.database.AppDatabase;
import com.example.musicplayer.database.radio.Radio;
import com.example.musicplayer.database.track.Track;
import com.example.musicplayer.activities.liked.LikedActivity;
import com.example.musicplayer.activities.playlist.PlaylistActivity;
import com.example.musicplayer.activities.radio.RadioActivity;
import com.example.musicplayer.activities.songcontroller.SongActivity;
import com.example.musicplayer.notification.CreateNotification;
import com.example.musicplayer.notification.Playable;

import java.io.File;

public class MainActivity extends AppCompatActivity implements Playable {
    private static final int SPLASH_TIME_OUT = 4000;
    private static final String[] PERMISSIONS = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO};
    private static final int REQUEST_PERMISSIONS = 12345;
    private static final int PERMISSIONS_COUNT = 2;
    private final int REQUEST_CODE_PERMISSION_WRITE_STORAGE = 1;
    Button prev, play, next;
    TextView songName;
    NotificationManager notificationManager;
    private ListView mDrawerList;
    private DrawerLayout mDrawerLayout;
    private ArrayAdapter<String> mAdapter;
    private ActionBarDrawerToggle mDrawerToggle;
    Intent intentNotification;
    boolean running = false;
    AppDatabase db;
    int fileIndex = 0;
    Player player;
    LikeTrackAdapter trackAdapter;
    RecyclerView listView;
    AppColor appColor;
    RelativeLayout layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();

        if (App.getApp().getPlayerService() == null) App.getApp().setPlayerService(new Intent(this, BackgroundMusicService.class));
        if (!arePermissionsDenied()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                createChannel();
                registerReceiver(broadcastReceiver, new IntentFilter("TRACKS_TRACKS"));
                intentNotification = new Intent(getBaseContext(), NotificationService.class);
                startService(intentNotification);
            }

            if (App.getApp().getPlayerService() == null) App.getApp().setPlayerService(new Intent(this, BackgroundMusicService.class));
            //onResume();
        } else {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO},
                    REQUEST_CODE_PERMISSION_WRITE_STORAGE);
        }
    }

    void init() {
        db = App.getApp().getDb();
        player = App.getApp().getPlayer();

        mDrawerList = findViewById(R.id.navList);
        mDrawerLayout = findViewById(R.id.drawer_layout);

        appColor = App.getApp().getAppColor();

        addDrawerItems();
        setupDrawer();

        if( getSupportActionBar() != null ) {
            getSupportActionBar().setTitle("EarFeeder");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
            setActionBarColor();
        }

        layout = findViewById(R.id.bottomLayout);
        play = findViewById(R.id.playBottom);
        prev = findViewById(R.id.previous);
        next = findViewById(R.id.next);
        listView = findViewById(R.id.listView);
        songName = findViewById(R.id.songName);
        songName.setSelected(true);

        setColor();

        trackAdapter = new LikeTrackAdapter(this);
        trackAdapter.setData(db.trackDao().getAll());
        listView.setAdapter(trackAdapter);
        listView.setLayoutManager(new LinearLayoutManager(this));
        listView.setHasFixedSize(true);

        if (!App.getApp().isMusicPlayerInit()) {
            for (Track track : db.trackDao().getAll()) {
                db.trackDao().updatePlaying(track.getId(), false);
            }

            for (Radio radio : db.radioDao().getAll()) {
                db.radioDao().updatePlaying(radio.getId(), false);
            }
        }

        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (player.getCurrentPath().equals("") && player.getSource().equals(".")) return;
                player.setIsAnotherSong(false);
                if (player.isPlaying()) {
                    onTrackPause();
                } else {
                    onTrackPlay();
                }
            }
        });
        prev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (player.getCurrentPath().equals("") && player.getSource().equals(".")) return;

                onTrackPrevious();
                changePlaying();
            }
        });
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (player.getCurrentPath().equals("") && player.getSource().equals(".")) return;

                onTrackNext();
                changePlaying();
            }
        });
        songName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!player.getSource().equals(".")) return;
                if (player.getCurrentPath().equals("")) return;
                if (player.isPlaying()) player.setMediaPlayerCurrentPosition(player.getMediaPlayerCurrentPosition());
                Intent intent = new Intent(MainActivity.this, SongActivity.class);
                startActivity(intent);
            }
        });
    }

    private void addDrawerItems() {
        String[] osArray = { getString(R.string.radio), getString(R.string.playlists), getString(R.string.liked), getString(R.string.settings) };
        mAdapter = new ArrayAdapter<String>(this, R.layout.left_menu_textview, osArray);
        mDrawerList.setAdapter(mAdapter);

        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        Intent intentRadio = new Intent(MainActivity.this, RadioActivity.class);
                        startActivity(intentRadio);
                        break;
                    case 1:
                        Intent intentPlaylist = new Intent(MainActivity.this, PlaylistActivity.class);
                        startActivity(intentPlaylist);
                        break;
                    case 2:
                        Intent intentLiked = new Intent(MainActivity.this, LikedActivity.class);
                        startActivity(intentLiked);
                        break;
                    case 3:
                        Intent intentSettings = new Intent(MainActivity.this, SettingsActivity.class);
                        startActivity(intentSettings);
                        break;
                }
            }
        });
    }

    private void setupDrawer() {
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.drawer_open, R.string.drawer_close);

        mDrawerToggle.setDrawerIndicatorEnabled(true);
        mDrawerLayout.addDrawerListener(mDrawerToggle);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressLint("NewApi")
    private boolean arePermissionsDenied() {
        for (int i = 0; i < PERMISSIONS_COUNT; i++) {
            if (checkSelfPermission(PERMISSIONS[i]) != PackageManager.PERMISSION_GRANTED) {
                return true;
            }
        }
        return false;
    }

    @SuppressLint("NewApi")
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_PERMISSION_WRITE_STORAGE:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    onResume();
                } else {
                    Toast.makeText(this, "NO", Toast.LENGTH_SHORT).show();
                }
        }
    }

    private void addMusicFilesFrom(String dirPath) {
        final File musicDir = new File(dirPath);
        if (!musicDir.exists()) {
            musicDir.mkdir();
            return;
        }
        final File[] files = musicDir.listFiles();
        for (File file : files) {
            final String path = file.getAbsolutePath();
            if (path.endsWith(".mp3") || path.endsWith(".wav")) {
                db.trackDao().insert(new Track(fileIndex++, player.updateTitle(file.getName()), path));
                player.addToQueue(new Track(player.updateTitle(file.getName()), path));
            }
        }
    }

    @SuppressLint("NewApi")
    private void fillMusicList() {
        addMusicFilesFrom(String.valueOf(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS)));
        addMusicFilesFrom(String.valueOf(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_MUSIC)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && arePermissionsDenied()) {
            requestPermissions(PERMISSIONS, REQUEST_PERMISSIONS);
            return;
        }

        App.getApp().setLastActivity(this);

        player = App.getApp().getPlayer();

        if (!App.getApp().isMusicPlayerInit()) {
            for (Track track : db.trackDao().getAll()) {
                db.trackDao().updatePlaying(track.getId(), false);
            }
            fillMusicList();
            App.getApp().setMusicPlayerInit(true);
        }
        else {
            updateTitle();
            changePlayButton();
            changePlaying();
        }

        App.getApp().recreateNotification();

        App.getApp().setCurrentActivity(this);

        setColor();
        setActionBarColor();

        if (!running) {
            running = true;
            startTitleThread();
        }

        if (db.trackDao().getAll().isEmpty()) {
            TextView noSongs = findViewById(R.id.noSongs);
            listView.setVisibility(View.GONE);
            noSongs.setVisibility(View.VISIBLE);
        }
    }

    private void createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CreateNotification.CHANNEL_ID,
                    "KOD DEV", NotificationManager.IMPORTANCE_LOW);

            notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getExtras().getString("actionName");

            switch (action) {
                case CreateNotification.ACTION_PREVIOUS:
                    onTrackPrevious();
                    break;
                case CreateNotification.ACTION_PLAY:
                    if (player.isPlaying()) onTrackPause();
                    else onTrackPlay();
                    break;
                case CreateNotification.ACTION_NEXT:
                    onTrackNext();
                    break;
            }
        }
    };

    @Override
    public void onTrackPrevious() {
        if (player.getMediaPlayer() == null) return;
        if (player.getSource().equals(".") && player.getCurrentQueueTrack() - 1 >= 0) {
            moveTrack(-1);
            App.getApp().createTrackNotification(appColor.getPauseColor());
        }
        else if (!player.getSource().equals(".") && player.getCurrentRadio() - 1 >= 0) {
            moveRadio(-1);
            App.getApp().createRadioNotification(appColor.getPauseColor());
        }
        changePlaying();
        updateTitle();
        Log.d("testing", ""+player.getCurrentQueueTrack());
    }

    @Override
    public void onTrackPlay() {
        if (player.getMediaPlayer() == null) return;

        if (player.getSource().equals(".")) {
            App.getApp().createTrackNotification(appColor.getPauseColor());
        }
        else {
            App.getApp().createLoadingDialog(App.getApp().getCurrentActivity());
            App.getApp().getLoadingDialog().startLoadingAnimation();
            App.getApp().createRadioNotification(appColor.getPauseColor());
        }

        player.setIsPlaying(true);
        player.setIsAnotherSong(false);
        play.setBackgroundResource(appColor.getPauseColor());
        startService(App.getApp().getPlayerService());
    }

    @Override
    public void onTrackPause() {
        if (player.getMediaPlayer() == null) return;
        if (player.getSource().equals(".")) {
            App.getApp().createTrackNotification(appColor.getPlayColor());
        }
        else {
            App.getApp().createRadioNotification(appColor.getPlayColor());
        }

        player.setIsPlaying(false);
        player.setIsAnotherSong(false);
        play.setBackgroundResource(appColor.getPlayColor());
        stopService(App.getApp().getPlayerService());
    }

    @Override
    public void onTrackNext() {
        if (player.getMediaPlayer() == null) return;
        if (player.getSource().equals(".") && player.getCurrentQueueTrack() + 1 < player.getQueueSize()) {
            moveTrack(1);
            App.getApp().createTrackNotification(appColor.getPauseColor());
        }
        else if (!player.getSource().equals(".") && player.getCurrentRadio() + 1 < player.getRadioListSize()) {
            moveRadio(1);
            App.getApp().createRadioNotification(appColor.getPauseColor());
        }
        changePlaying();
        updateTitle();
    }

    void moveTrack(int direction) {
        player.setWasSongSwitched(true);
        player.setCurrentQueueTrack(player.getCurrentQueueTrack() + direction);
        stopService(App.getApp().getPlayerService());
        player.setIsAnotherSong(true);
        updateTitle();
        startService(App.getApp().getPlayerService());
    }

    void moveRadio(int direction) {
        App.getApp().createLoadingDialog(App.getApp().getCurrentActivity());
        App.getApp().getLoadingDialog().startLoadingAnimation();

        stopService(App.getApp().getPlayerService());
        player.setCurrentRadio(player.getCurrentRadio() + direction);
        player.setSource(player.getCurrentRadioTrack().getPath());
        player.setIsAnotherSong(true);
        player.setWasSongSwitched(true);
        updateTitle();
        startService(App.getApp().getPlayerService());
    }

    void updateTitle() {
        if (player.getSource().equals(".") && !songName.getText().equals(player.getCurrentTitle())) {
            songName.setText(player.getCurrentTitle());
        }
        else if (!player.getSource().equals(".") && !songName.getText().equals(player.getCurrentRadioTrack().getName())) songName.setText(player.getCurrentRadioTrack().getName());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        running = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.cancelAll();
        }
        unregisterReceiver(broadcastReceiver);
    }

    private void startTitleThread() {
        Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            public void run() {
                while (running) {
                    try {
                        Thread.sleep(1000);
                    }
                    catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    handler.post(new Runnable(){
                        public void run() {
                            if (player.getMediaPlayer() == null) {
                                play.setBackgroundResource(appColor.getPlayColor());
                                return;
                            }
                            updateTitle();
                            changePlayButton();
                            if (player.wasSongSwitched()) {
                                changePlaying();
                                player.setWasSongSwitched(false);
                            }
                            if (App.getApp().getLastActivity() == MainActivity.this) {
                                App.getApp().recreateNotification();
                                App.getApp().setLastActivity(null);
                            }
                        }
                    });
                }
            }
        };
        new Thread(runnable).start();
    }

    void changePlayButton() {
        if (player.isPlaying()) play.setBackgroundResource(appColor.getPauseColor());
        else play.setBackgroundResource(appColor.getPlayColor());
    }

    void changePlaying() {
        Log.d("testing", "aaa");
        if (player.getSource().equals(".")) {
            for (Track track : db.trackDao().getAll()) {
                db.trackDao().updatePlaying(track.getId(), false);
                if (track.getId() == player.getCurrentTrack().getId())
                    db.trackDao().updatePlaying(track.getId(), true);
            }
            trackAdapter.setData(db.trackDao().getAll());
            trackAdapter.notifyDataSetChanged();
        }
    }

    void setColor() {
        layout.setBackgroundResource(appColor.getBgColor());
        mDrawerList.setBackgroundColor(Color.parseColor(appColor.getColor()));
    }

    void setActionBarColor() {
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor(appColor.getColor())));
    }
}