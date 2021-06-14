package com.example.musicplayer.activities.playlistview;

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
import com.example.musicplayer.listener.RecyclerItemClickListener;
import com.example.musicplayer.services.NotificationService;
import com.example.musicplayer.activities.songcontroller.SongActivity;
import com.example.musicplayer.activities.selectingSongs.AddSongsActivity;
import com.example.musicplayer.database.AppDatabase;
import com.example.musicplayer.database.entities.Radio;
import com.example.musicplayer.database.entities.Track;
import com.example.musicplayer.database.entities.TrackPlaylist;
import com.example.musicplayer.notification.CreateNotification;
import com.example.musicplayer.notification.Playable;

import java.util.ArrayList;
import java.util.List;

public class PlaylistViewActivity extends AppCompatActivity implements Playable {
    Player player;
    TextView playlistName;
    RecyclerView tracks;
    Button back, add;
    TrackAdapter adapter;
    AppDatabase db;
    List<Track> trackList = new ArrayList<>();
    Button play, prev, next;
    TextView title;
    NotificationManager notificationManager;
    boolean running = false;
    AppColor appColor;
    RelativeLayout layout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist_view);

        player = App.getApp().getPlayer();

        if (!running) {
            running = true;
            startTitleThread();
        }

        init();

        if (player.isPlaying()) {
            play.setBackgroundResource(appColor.getPauseColor());
        } else {
            play.setBackgroundResource(appColor.getPlayColor());
        }

        App.getApp().setCurrentActivity(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannel();
            registerReceiver(broadcastReceiver, new IntentFilter("TRACKS_TRACKS"));
            startService(new Intent(getBaseContext(), NotificationService.class));
        }
    }

    void init() {
        playlistName = findViewById(R.id.playlistName);
        tracks = findViewById(R.id.playlistTracks);
        back = findViewById(R.id.playlistBackButton);
        play = findViewById(R.id.playBottom);
        title = findViewById(R.id.songName);
        prev = findViewById(R.id.previous);
        next = findViewById(R.id.next);
        layout = findViewById(R.id.bottomLayout);
        add = findViewById(R.id.addSongsButton);
        title.setSelected(true);

        player.clearSelected();

        db = App.getApp().getDb();

        appColor = App.getApp().getAppColor();

        layout.setBackgroundResource(appColor.getBgColor());
        back.setBackgroundResource(appColor.getBackColor());
        add.setBackgroundResource(appColor.getAddColor());

        adapter = new TrackAdapter();

        List<Track> updatedTracks = getPlaylistTracks(player.getPlaylistToView());

        for (Track track : updatedTracks) {
            if (track.isPlaying()) {
                for (Track updatedTrack : updatedTracks) {
                    if (updatedTrack.getId() == track.getId()) {
                        db.trackDao().updatePlaying(track.getId(), true);
                        track.setPlaying(true);
                        break;
                    }
                }
            }
        }

        adapter.setData(updatedTracks);
        tracks.setAdapter(adapter);
        tracks.setLayoutManager(new LinearLayoutManager(this));
        tracks.setHasFixedSize(true);

        playlistName.setText(db.playlistDao().getById(player.getPlaylistToView()).getName());

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        tracks.addOnItemTouchListener(
                new RecyclerItemClickListener(this, tracks, new RecyclerItemClickListener.OnItemClickListener() {
                    @Override public void onItemClick(View view, int position) {
                        stopService(App.getApp().getPlayerService());
                        player.clearQueue();
                        for (Track track : trackList) player.addToQueue(track);
                        player.setCurrentQueueTrack(position);
                        player.setIsPlaying(true);
                        player.setIsAnotherSong(true);
                        player.setSource(".");
                        startService(App.getApp().getPlayerService());
                        App.getApp().createTrackNotification(appColor.getPauseColor());
                        play.setBackgroundResource(appColor.getPauseColor());
                        player.setCurrentPlaylist(player.getPlaylistToView());

                        for (Track track : db.trackDao().getAll()) db.trackDao().updatePlaying(track.getId(), false);

                        List<Track> updatedTracks = getPlaylistTracks(player.getPlaylistToView());

                        for (Track track : updatedTracks) {
                            if (updatedTracks.get(position).getId() == track.getId()) {
                                db.trackDao().updatePlaying(track.getId(), true);
                                track.setPlaying(true);
                            }
                        }

                        for (Radio radio : db.radioDao().getAll()) db.radioDao().updatePlaying(radio.getId(), false);

                        adapter.setData(updatedTracks);
                        adapter.notifyDataSetChanged();

                        player.setIsShuffled(false);
                    }
                    @Override
                    public void onLongItemClick(View view, int position) {}
                })
        );
        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (player.isPlaying()) {
                    if (player.getSource().equals(".")) {
                        App.getApp().createTrackNotification(appColor.getPlayColor());
                    }
                    else {
                        App.getApp().createRadioNotification(appColor.getPlayColor());
                    }

                    player.setIsPlaying(false);
                    play.setBackgroundResource(appColor.getPlayColor());
                    stopService(App.getApp().getPlayerService());
                } else {
                    if (player.getMediaPlayer() == null) player.setCurrentQueueTrack(0);
                    if (player.getSource().equals(".")) {
                        App.getApp().createTrackNotification(appColor.getPauseColor());
                    }
                    else {
                        App.getApp().createLoadingDialog(App.getApp().getCurrentActivity());
                        App.getApp().getLoadingDialog().startLoadingAnimation();
                        App.getApp().createRadioNotification(appColor.getPauseColor());
                    }

                    player.setIsPlaying(true);
                    play.setBackgroundResource(appColor.getPauseColor());
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
                    App.getApp().createTrackNotification(appColor.getPauseColor());
                }
                else if (!player.getSource().equals(".") && player.getCurrentRadio() - 1 >= 0) {
                    moveRadio(-1);
                    App.getApp().createRadioNotification(appColor.getPauseColor());
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
                    App.getApp().createTrackNotification(appColor.getPauseColor());
                }
                else if (!player.getSource().equals(".") && player.getCurrentRadio() +1 < player.getRadioListSize()) {
                    moveRadio(1);
                    App.getApp().createRadioNotification(appColor.getPauseColor());
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
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent addIntent = new Intent(PlaylistViewActivity.this, AddSongsActivity.class);
                startActivity(addIntent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        App.getApp().recreateNotification();

        List<Track> updatedTracks = getPlaylistTracks(player.getPlaylistToView());

        for (Track track : updatedTracks) {
            if (track.isPlaying()) {
                for (Track updatedTrack : updatedTracks) {
                    if (updatedTrack.getId() == track.getId()) {
                        db.trackDao().updatePlaying(track.getId(), true);
                        track.setPlaying(true);
                        break;
                    }
                }
            }
        }

        adapter.setData(updatedTracks);
        adapter.notifyDataSetChanged();
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

    List<Track> getPlaylistTracks(int playlistId) {
        trackList.clear();
        List<TrackPlaylist> temp = db.trackPlaylistDao().getAllByPlaylistId(playlistId);

        for (TrackPlaylist item : temp) {
            trackList.add(db.trackDao().getById(item.getTrackId()));
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
        if (!player.isPlaying()) return;
        if (player.getSource().equals(".") && !title.getText().equals(player.getCurrentTitle())) {
            title.setText(player.getCurrentTitle());
        }
        else if (!player.getSource().equals(".") && !title.getText().equals(player.getCurrentRadioTrack().getName())) title.setText(player.getCurrentRadioTrack().getName());
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
        play.setBackgroundResource(appColor.getPauseColor());
    }

    @Override
    public void onTrackPlay() {
        play.setBackgroundResource(appColor.getPauseColor());
    }

    @Override
    public void onTrackPause() {
        play.setBackgroundResource(appColor.getPlayColor());
    }

    @Override
    public void onTrackNext() {
        changePlaying();
        updateTitle();
        play.setBackgroundResource(appColor.getPauseColor());
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
            adapter.setData(getPlaylistTracks(player.getPlaylistToView()));
            adapter.notifyDataSetChanged();
        }
    }

    void changePlayButton() {
        if (player.isPlaying()) play.setBackgroundResource(appColor.getPauseColor());
        else play.setBackgroundResource(appColor.getPlayColor());
    }
}
