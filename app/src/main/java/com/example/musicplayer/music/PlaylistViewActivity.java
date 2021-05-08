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
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.musicplayer.App;
import com.example.musicplayer.Player;
import com.example.musicplayer.R;
import com.example.musicplayer.Services.OnClearFromRecentService;
import com.example.musicplayer.adapter.TrackAdapter;
import com.example.musicplayer.database.AppDatabase;
import com.example.musicplayer.database.Track;
import com.example.musicplayer.database.TrackPlaylist;
import com.example.musicplayer.notification.CreateNotification;
import com.example.musicplayer.notification.Playable;

import java.util.ArrayList;
import java.util.List;

public class PlaylistViewActivity extends AppCompatActivity implements Playable {
    Player player;
    TextView playlistName;
    ListView tracks;
    Button back;
    TrackAdapter adapter;
    AppDatabase db;
    List<Track> trackList = new ArrayList<>();
    Button play, prev, next;
    TextView title;
    NotificationManager notificationManager;
    boolean running = false;

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
            play.setBackgroundResource(R.drawable.ic_pause);
        } else {
            play.setBackgroundResource(R.drawable.ic_play);
        }

        if (!player.getSource().equals(".") && player.getCurrentRadio() != -1) {
            title.setText(player.getCurrentRadioTrack().getName());
        }
        else if (player.getSource().equals(".") && player.getCurrentSong() != -1) {
            title.setText(player.getCurrentTitle());
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannel();
            registerReceiver(broadcastReceiver, new IntentFilter("TRACKS_TRACKS"));
            startService(new Intent(getBaseContext(), OnClearFromRecentService.class));
        }
    }

    void init() {
        playlistName = findViewById(R.id.playlistName);
        tracks = findViewById(R.id.playlistTracks);
        back = findViewById(R.id.playlistTracksBack);
        play = findViewById(R.id.play);
        title = findViewById(R.id.songName);
        prev = findViewById(R.id.previous);
        next = findViewById(R.id.next);
        title.setSelected(true);

        db = App.getApp().getDb();

        adapter = new TrackAdapter();
        adapter.setData(getPlaylistTracks(player.getPlaylistToView()));
        tracks.setAdapter(adapter);

        playlistName.setText(db.playlistDao().getById(player.getPlaylistToView()).getName());

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        tracks.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                stopService(App.getApp().getPlayerService());
                player.clearQueue();
                for (Track track : trackList) player.addToQueue(track);
                player.setCurrentSong(position);
                player.setIsPlaying(true);
                player.setIsAnotherSong(true);
                player.setSource(".");
                startService(App.getApp().getPlayerService());
                createTrackNotification(R.drawable.ic_pause);
                play.setBackgroundResource(R.drawable.ic_pause);
                player.setCurrentPlaylist(player.getPlaylistToView());
            }
        });
        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (player.isPlaying()) {
                    if (player.getSource().equals(".")) {
                        createTrackNotification(R.drawable.ic_play);
                    }
                    else {
                        createRadioNotification(R.drawable.ic_play);
                    }

                    player.setIsPlaying(false);
                    play.setBackgroundResource(R.drawable.ic_play);
                    stopService(App.getApp().getPlayerService());
                } else {
                    if (player.getMediaPlayer() == null) player.setCurrentSong(0);
                    if (player.getSource().equals(".")) {
                        createTrackNotification(R.drawable.ic_pause);
                    }
                    else {
                        createRadioNotification(R.drawable.ic_pause);
                    }

                    player.setIsPlaying(true);
                    play.setBackgroundResource(R.drawable.ic_pause);
                    startService(App.getApp().getPlayerService());
                }
                player.setIsAnotherSong(false);
            }
        });
        prev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (player.getMediaPlayer() == null) return;
                if (player.getSource().equals(".") && player.getCurrentSong() - 1 >= 0) {
                    moveTrack(-1);
                    createTrackNotification(R.drawable.ic_pause);
                }
                else if (!player.getSource().equals(".") && player.getCurrentRadio() - 1 >= 0) {
                    moveRadio(-1);
                    createRadioNotification(R.drawable.ic_pause);
                }
            }
        });
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (player.getMediaPlayer() == null) return;
                if (player.getSource().equals(".") && player.getCurrentSong() + 1 < player.getQueueSize()) {
                    moveTrack(1);
                    createTrackNotification(R.drawable.ic_pause);
                }
                else if (!player.getSource().equals(".") && player.getCurrentRadio() +1 < player.getRadioListSize()) {
                    moveRadio(1);
                    createRadioNotification(R.drawable.ic_pause);
                }
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
        List<TrackPlaylist> temp = db.trackPlaylistDao().getAllByPlaylistId(playlistId);

        for (TrackPlaylist item : temp) {
            trackList.add(db.trackDao().getById(item.getTrackId()));
        }

        return trackList;
    }

    void moveTrack(int direction) {
        player.setWasSongSwitched(true);
        player.setCurrentSong(player.getCurrentSong() + direction);
        stopService(App.getApp().getPlayerService());
        player.setIsAnotherSong(true);
        updateTitle();
        startService(App.getApp().getPlayerService());
    }

    void moveRadio(int direction) {
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
                player.getCurrentSong(),
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
        updateTitle();
        play.setBackgroundResource(R.drawable.ic_pause);
    }

    @Override
    public void onTrackPlay() {
        play.setBackgroundResource(R.drawable.ic_pause);
    }

    @Override
    public void onTrackPause() {
        play.setBackgroundResource(R.drawable.ic_play);
    }

    @Override
    public void onTrackNext() {
        updateTitle();
        play.setBackgroundResource(R.drawable.ic_pause);
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
}
