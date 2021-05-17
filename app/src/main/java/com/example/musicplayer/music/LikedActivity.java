package com.example.musicplayer.music;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.musicplayer.App;
import com.example.musicplayer.AppColor;
import com.example.musicplayer.Player;
import com.example.musicplayer.R;
import com.example.musicplayer.RecyclerItemClickListener;
import com.example.musicplayer.Services.OnClearFromRecentService;
import com.example.musicplayer.adapter.TrackAdapter;
import com.example.musicplayer.database.AppDatabase;
import com.example.musicplayer.database.Radio;
import com.example.musicplayer.database.Track;
import com.example.musicplayer.notification.CreateNotification;
import com.example.musicplayer.notification.Playable;

import java.util.ArrayList;
import java.util.List;

public class LikedActivity extends AppCompatActivity implements Playable {
    Player player;
    TextView playlistName;
    RecyclerView tracks;
    Button back;
    TrackAdapter adapter;
    AppDatabase db;
    Button play, prev, next;
    TextView title;
    NotificationManager notificationManager;
    boolean running = false;
    AppColor appColor;
    RelativeLayout layout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_liked);

        player = App.getApp().getPlayer();

        if (!running) {
            running = true;
            startTitleThread();
        }

        init();

        App.getApp().setCurrentActivity(this);

        if (player.isPlaying()) {
            play.setBackgroundResource(appColor.getPauseColor());
        } else {
            play.setBackgroundResource(appColor.getPlayColor());
        }

        updateTitle();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannel();
            registerReceiver(broadcastReceiver, new IntentFilter("TRACKS_TRACKS"));
            startService(new Intent(getBaseContext(), OnClearFromRecentService.class));
        }

        ifNoLiked();
    }

    void init() {
        playlistName = findViewById(R.id.playlistName);
        tracks = findViewById(R.id.likedSongs);
        back = findViewById(R.id.playlistTracksBack);
        play = findViewById(R.id.playBottom);
        title = findViewById(R.id.songName);
        prev = findViewById(R.id.previous);
        next = findViewById(R.id.next);
        layout = findViewById(R.id.bottomLayout);
        title.setSelected(true);

        db = App.getApp().getDb();

        appColor = App.getApp().getAppColor();

        back.setBackgroundResource(appColor.getBackColor());
        layout.setBackgroundResource(appColor.getBgColor());

        adapter = new TrackAdapter();
        adapter.setData(getLikedTracks());
        tracks.setAdapter(adapter);
        tracks.setLayoutManager(new LinearLayoutManager(this));
        tracks.setHasFixedSize(true);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (player.isPlaying()) {
                    if (player.getSource().equals(".")) {
                        createTrackNotification(R.drawable.ic_play_red);
                    }
                    else {
                        createRadioNotification(R.drawable.ic_play_red);
                    }

                    player.setIsPlaying(false);
                    play.setBackgroundResource(R.drawable.ic_play_red);
                    stopService(App.getApp().getPlayerService());
                } else {
                    if (player.getMediaPlayer() == null) player.setCurrentQueueTrack(0);
                    if (player.getSource().equals(".")) {
                        createTrackNotification(R.drawable.ic_pause_red);
                    }
                    else {
                        App.getApp().createLoadingDialog(App.getApp().getCurrentActivity());
                        App.getApp().getLoadingDialog().startLoadingAnimation();
                        createRadioNotification(R.drawable.ic_pause_red);
                    }

                    player.setIsPlaying(true);
                    play.setBackgroundResource(R.drawable.ic_pause_red);
                    startService(App.getApp().getPlayerService());
                }
                player.setIsAnotherSong(false);
            }
        });
        prev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (player.getMediaPlayer() == null) return;

                if (player.getSource().equals(".") && player.getCurrentQueueTrack() - 1 >= 0) {
                    moveTrack(-1);
                    createTrackNotification(R.drawable.ic_pause_red);
                }
                else if (!player.getSource().equals(".") && player.getCurrentRadio() - 1 >= 0) {
                    moveRadio(-1);
                    createRadioNotification(R.drawable.ic_pause_red);
                }
                changePlaying();
            }
        });
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (player.getMediaPlayer() == null) return;

                if (player.getSource().equals(".") && player.getCurrentQueueTrack() + 1 < player.getQueueSize()) {
                    moveTrack(1);
                    createTrackNotification(R.drawable.ic_pause_red);
                }
                else if (!player.getSource().equals(".") && player.getCurrentRadio() +1 < player.getRadioListSize()) {
                    moveRadio(1);
                    createRadioNotification(R.drawable.ic_pause_red);
                }
                changePlaying();
            }
        });
        title.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!player.getSource().equals(".")) return;
                if (player.getCurrentPath().equals("")) return;
                if (player.isPlaying()) player.setMediaPlayerCurrentPosition(player.getCurrentPosition());
                Intent intent = new Intent(getApplicationContext(), SongActivity.class);
                startActivity(intent);
            }
        });
        tracks.addOnItemTouchListener(
                new RecyclerItemClickListener(this, tracks, new RecyclerItemClickListener.OnItemClickListener() {
                    @Override public void onItemClick(View view, int position) {
                        stopService(App.getApp().getPlayerService());
                        player.clearQueue();
                        for (Track track : getLikedTracks()) player.addToQueue(track);
                        player.setCurrentQueueTrack(position);
                        player.setIsPlaying(true);
                        player.setIsAnotherSong(true);
                        player.setSource(".");
                        startService(App.getApp().getPlayerService());
                        createTrackNotification(R.drawable.ic_pause_red);
                        play.setBackgroundResource(R.drawable.ic_pause_red);
                        player.setCurrentPlaylist(player.getPlaylistToView());

                        for (Track track : db.trackDao().getAll()) db.trackDao().updatePlaying(track.getId(), false);

                        List<Track> updatedTracks = getLikedTracks();

                        for (Track track : updatedTracks) {
                            if (updatedTracks.get(position).getId() == track.getId()) {
                                db.trackDao().updatePlaying(track.getId(), true);
                                track.setPlaying(true);
                            }
                        }

                        for (Radio radio : db.radioDao().getAll()) db.radioDao().updatePlaying(radio.getId(), false);

                        adapter.setData(updatedTracks);
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onLongItemClick(View view, int position) {}
                })
        );
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

    List<Track> getLikedTracks() {
        List<Track> trackList = new ArrayList<>();

        for (Track track : db.trackDao().getAll()) {
            if (track.isLiked()) {
                trackList.add(track);
            }
        }

        return trackList;
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
        if (player.getSource().equals(".") && !title.getText().equals(player.getCurrentTitle())) {
            title.setText(player.getCurrentTitle());
        }
        else if (!player.getSource().equals(".") && !title.getText().equals(player.getCurrentRadioTrack().getName())) title.setText(player.getCurrentRadioTrack().getName());
    }

    void createTrackNotification(int index) {
        CreateNotification.createNotification(getApplicationContext(),
                player.getCurrentTrack(),
                index,
                player.getCurrentQueueTrack(),
                player.getQueueSize()-1);
    }

    void createRadioNotification(int index) {
        CreateNotification.createNotification(getApplicationContext(),
                new Track(player.getCurrentRadioTrack().getName(), player.getCurrentRadioTrack().getPath()),
                index,
                player.getCurrentRadio(),
                player.getRadioListSize() - 1);
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
                    if (!player.isPlaying()) onTrackPause();
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
        changePlaying();
        updateTitle();
        play.setBackgroundResource(R.drawable.ic_pause_red);
    }

    @Override
    public void onTrackPlay() {
        play.setBackgroundResource(R.drawable.ic_pause_red);
    }

    @Override
    public void onTrackPause() {
        play.setBackgroundResource(R.drawable.ic_play_red);
    }

    @Override
    public void onTrackNext() {
        changePlaying();
        updateTitle();
        play.setBackgroundResource(R.drawable.ic_pause_red);
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
                            if (player.getMediaPlayer() == null) return;
                            updateTitle();
                            changePlayButton();
                        }
                    });
                }
            }
        };
        new Thread(runnable).start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        running = false;
    }

    void changePlaying() {
        if (player.getSource().equals(".")) {
            for (Track track : db.trackDao().getAll()) {
                db.trackDao().updatePlaying(track.getId(), false);
                if (track.getId() == player.getCurrentTrack().getId())
                    db.trackDao().updatePlaying(track.getId(), true);
            }
            adapter.setData(getLikedTracks());
            adapter.notifyDataSetChanged();
        }
    }

    void changePlayButton() {
        if (player.isPlaying()) play.setBackgroundResource(appColor.getPauseColor());
        else play.setBackgroundResource(appColor.getPlayColor());
    }

    void ifNoLiked() {
        int count = 0;
        for (Track track : db.trackDao().getAll()) {
            if (track.isLiked()) count++;
        }

        TextView noLiked = findViewById(R.id.noLiked);
        if (count == 0) {
            tracks.setVisibility(View.GONE);
            noLiked.setVisibility(View.VISIBLE);
        }
        else {
            noLiked.setVisibility(View.GONE);
            tracks.setVisibility(View.VISIBLE);
        }
    }
}
