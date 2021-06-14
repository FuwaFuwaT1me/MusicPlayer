package com.example.musicplayer.activities.selectingSongs;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.musicplayer.app.App;
import com.example.musicplayer.color.AppColor;
import com.example.musicplayer.player.Player;
import com.example.musicplayer.R;
import com.example.musicplayer.services.NotificationService;
import com.example.musicplayer.activities.songcontroller.SongActivity;
import com.example.musicplayer.database.AppDatabase;
import com.example.musicplayer.database.entities.Track;
import com.example.musicplayer.database.entities.TrackPlaylist;
import com.example.musicplayer.notification.CreateNotification;
import com.example.musicplayer.notification.Playable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AddSongsActivity extends AppCompatActivity implements Playable {
    Player player;
    Button add, back;
    RecyclerView tracks;
    EditText playlistName;
    AppDatabase db;
    TrackAdapterSelect adapter;
    Button play, prev, next;
    TextView title;
    NotificationManager notificationManager;
    boolean running = false;
    AppColor appColor;
    RelativeLayout layout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_songs);
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

        if (!player.getSource().equals(".") && player.getCurrentRadio() != -1) {
            title.setText(player.getCurrentRadioTrack().getName());
        }
        else if (player.getSource().equals(".") && player.getCurrentQueueTrack() != -1) {
            title.setText(player.getCurrentTitle());
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannel();
            registerReceiver(broadcastReceiver, new IntentFilter("TRACKS_TRACKS"));
            startService(new Intent(getBaseContext(), NotificationService.class));
        }
    }

    void init() {
        App.getApp().setLastCondition(1);

        add = findViewById(R.id.addSongsToExistingPlaylist);
        back = findViewById(R.id.addingBack);
        tracks = findViewById(R.id.addingTracks);
        play = findViewById(R.id.playBottom);
        title = findViewById(R.id.songName);
        prev = findViewById(R.id.previous);
        next = findViewById(R.id.next);
        layout = findViewById(R.id.bottomLayout);
        title.setSelected(true);

        player.clearSelected();

        db = App.getApp().getDb();

        appColor = App.getApp().getAppColor();

        layout.setBackgroundResource(appColor.getBgColor());
        back.setBackgroundResource(appColor.getBackColor());
        add.setBackgroundResource(appColor.getBgColor());

        adapter = new TrackAdapterSelect(this);

        List<Integer> inPlaylistTracks = getPlaylistTrackIds(player.getPlaylistToView());
        List<Track> adapterTracks = new ArrayList<>();

        for (Track track : db.trackDao().getAll()) {
            if (!inPlaylistTracks.contains(track.getId())) {
                adapterTracks.add(track);
            }
        }

        adapter.setData(adapterTracks);
        tracks.setAdapter(adapter);
        tracks.setLayoutManager(new LinearLayoutManager(this));
        tracks.setHasFixedSize(true);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<Track> tracks = new ArrayList<>();

                Collections.sort(player.getSelected());

                for (int i = 0; i < player.getSelected().size(); i++) {
                    tracks.add(db.trackDao().getById(player.getSelectedIndex(i)));
                }

                player.clearSelected();

                for (Track track : tracks) {
                    db.trackPlaylistDao().insert(
                            new TrackPlaylist(track.getId(), player.getPlaylistToView())
                    );
                }

                finish();
            }
        });
        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (player.getMediaPlayer() == null) return;
                if (player.isPlaying()) {
                    if (player.getSource().equals(".")) {
                        App.getApp().createTrackNotification(R.drawable.ic_play_red);
                    }
                    else {
                        App.getApp().createLoadingDialog(App.getApp().getCurrentActivity());
                        App.getApp().getLoadingDialog().startLoadingAnimation();
                        App.getApp().createRadioNotification(R.drawable.ic_play_red);
                    }

                    player.setIsPlaying(false);
                    play.setBackgroundResource(R.drawable.ic_play_red);
                    stopService(App.getApp().getPlayerService());
                } else {
                    if (player.getMediaPlayer() == null) return;
                    if (player.getSource().equals(".")) {
                        App.getApp().createTrackNotification(R.drawable.ic_pause_red);
                    }
                    else {
                        App.getApp().createRadioNotification(R.drawable.ic_pause_red);
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
                    App.getApp().createTrackNotification(R.drawable.ic_pause_red);
                }
                else if (!player.getSource().equals(".") && player.getCurrentRadio() - 1 >= 0) {
                    moveRadio(-1);
                    App.getApp().createRadioNotification(R.drawable.ic_pause_red);
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
                    App.getApp().createTrackNotification(R.drawable.ic_pause_red);
                }
                else if (!player.getSource().equals(".") && player.getCurrentRadio() +1 < player.getRadioListSize()) {
                    moveRadio(1);
                    App.getApp().createRadioNotification(R.drawable.ic_pause_red);
                }
                changePlaying();
            }
        });
        title.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!player.getSource().equals(".")) return;
                if (player.getCurrentPath().equals("")) return;
                if (player.isPlaying()) player.setMediaPlayerCurrentPosition(player.getMediaPlayer().getCurrentPosition());
                Intent intent = new Intent(getApplicationContext(), SongActivity.class);
                startActivity(intent);
            }
        });
    }

    List<Integer> getPlaylistTrackIds(int playlistId) {
        List<Integer> trackList = new ArrayList<>();
        List<TrackPlaylist> temp = db.trackPlaylistDao().getAllByPlaylistId(playlistId);

        for (TrackPlaylist item : temp) {
            trackList.add(item.getTrackId());
        }

        return trackList;
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
        if (!player.isPlaying()) return;
        if (player.getSource().equals(".") && !title.getText().equals(player.getCurrentTitle())) {
            title.setText(player.getCurrentTitle());
        }
        else if (player.getCurrentRadio() != -1 && !player.getSource().equals(".") && !title.getText().equals(player.getCurrentRadioTrack().getName())) title.setText(player.getCurrentRadioTrack().getName());
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
                            changePlayButton();
                            updateTitle();
                            if (player.wasSongSwitched()) {
                                changePlaying();
                                player.setWasSongSwitched(false);
                            }
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
            List<Integer> inPlaylistTracks = getPlaylistTrackIds(player.getPlaylistToView());
            List<Track> adapterTracks = new ArrayList<>();

            for (Track track : db.trackDao().getAll()) {
                db.trackDao().updatePlaying(track.getId(), false);
                if (track.getId() == player.getCurrentTrack().getId())
                    db.trackDao().updatePlaying(track.getId(), true);
            }

            for (Track track : db.trackDao().getAll()) {
                if (!inPlaylistTracks.contains(track.getId())) adapterTracks.add(track);
            }

            adapter.setData(adapterTracks);
            adapter.notifyDataSetChanged();
        }
    }

    void changePlayButton() {
        if (player.isPlaying()) play.setBackgroundResource(appColor.getPauseColor());
        else play.setBackgroundResource(appColor.getPlayColor());
    }

    @Override
    protected void onResume() {
        super.onResume();

        App.getApp().recreateNotification();
    }
}
