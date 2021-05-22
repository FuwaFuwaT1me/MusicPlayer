package com.example.musicplayer.activities.playlist;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.musicplayer.app.App;
import com.example.musicplayer.color.AppColor;
import com.example.musicplayer.player.Player;
import com.example.musicplayer.R;
import com.example.musicplayer.Services.NotificationService;
import com.example.musicplayer.activities.playlistview.PlaylistViewActivity;
import com.example.musicplayer.activities.songcontroller.SongActivity;
import com.example.musicplayer.activities.selectingSongs.CreatingPlaylistActivity;
import com.example.musicplayer.database.AppDatabase;
import com.example.musicplayer.database.playlist.Playlist;
import com.example.musicplayer.database.track.Track;
import com.example.musicplayer.notification.CreateNotification;
import com.example.musicplayer.notification.Playable;

import java.util.List;

public class PlaylistActivity extends AppCompatActivity implements Playable {
    Button back, add;
    ListView playlists;
    PlaylistAdapter adapter;
    AppDatabase db;
    int selectedPlaylist;
    Button play, prev, next;
    TextView title;
    NotificationManager notificationManager;
    Player player;
    boolean running = false;
    AppColor appColor;
    RelativeLayout layout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist);

        player = App.getApp().getPlayer();
        if (!running) {
            running = true;
            startTitleThread();
        }

        init();

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
        back = findViewById(R.id.playlistBackButton);
        add = findViewById(R.id.addPlaylistButton);
        playlists = findViewById(R.id.playlists);
        play = findViewById(R.id.playBottom);
        title = findViewById(R.id.songName);
        prev = findViewById(R.id.previous);
        next = findViewById(R.id.next);
        layout = findViewById(R.id.bottomLayout);
        title.setSelected(true);

        db = App.getApp().getDb();

        appColor = App.getApp().getAppColor();
        back.setBackgroundResource(appColor.getBackColor());
        add.setBackgroundResource(appColor.getAddColor());

        adapter = new PlaylistAdapter();
        adapter.setData(db.playlistDao().getAll());
        playlists.setAdapter(adapter);
        registerForContextMenu(playlists);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PlaylistActivity.this, CreatingPlaylistActivity.class);
                startActivity(intent);
            }
        });
        playlists.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                player.setPlaylistToView(player.getPlaylistIndexById(position));
                Intent intent = new Intent(PlaylistActivity.this, PlaylistViewActivity.class);
                startActivity(intent);
            }
        });
        playlists.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                selectedPlaylist = position;
                return false;
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
                        App.getApp().createRadioNotification(R.drawable.ic_play_red);
                    }

                    player.setIsPlaying(false);
                    play.setBackgroundResource(R.drawable.ic_play_red);
                    stopService(App.getApp().getPlayerService());
                } else {
                    if (player.getMediaPlayer() == null) player.setCurrentQueueTrack(0);
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
                else if (player.getCurrentRadio() +1 < player.getRadioListSize()) {
                    moveRadio(1);
                    App.getApp().createRadioNotification(R.drawable.ic_pause_red);
                }
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

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.context_menu, menu);

    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.deleteContextMenu) {
            player.clearPlaylistIndexes();
            List<Playlist> list = db.playlistDao().getAll();
            for (Playlist playlist : list) {
                player.addPlaylistIndex(playlist.getId());
            }

            if (player.getPlaylistIndexById(selectedPlaylist) == player.getCurrentPlaylist()) {
                stopService(App.getApp().getPlayerService());
                player.clearQueue();
                for (Track track : db.trackDao().getAll()) player.addToQueue(track);
                startService(App.getApp().getPlayerService());
                App.getApp().createTrackNotification(R.drawable.ic_pause_red);
            }

            db.playlistDao().delete(player.getPlaylistIndexById(selectedPlaylist));
            player.removePlaylistIndex(selectedPlaylist);
            adapter.setData(db.playlistDao().getAll());
            adapter.notifyDataSetChanged();

            if (!db.playlistDao().ifExist()) {
                TextView noPlaylists = findViewById(R.id.noPlaylists);
                playlists.setVisibility(View.GONE);
                noPlaylists.setVisibility(View.VISIBLE);
            }
        }
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();

        App.getApp().recreateNotification();

        adapter.setData(db.playlistDao().getAll());
        adapter.notifyDataSetChanged();

        layout.setBackgroundResource(appColor.getBgColor());

        if (player.isPlaying()) {
            play.setBackgroundResource(appColor.getPauseColor());
        } else {
            play.setBackgroundResource(appColor.getPlayColor());
        }

        player.clearSelected();

        App.getApp().setCurrentActivity(this);


        if (!db.playlistDao().ifExist()) {
            TextView noPlaylists = findViewById(R.id.noPlaylists);
            playlists.setVisibility(View.GONE);
            noPlaylists.setVisibility(View.VISIBLE);
        }
        else {
            TextView noPlaylists = findViewById(R.id.noPlaylists);
            playlists.setVisibility(View.VISIBLE);
            noPlaylists.setVisibility(View.GONE);
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

    void changePlayButton() {
        if (player.isPlaying()) play.setBackgroundResource(appColor.getPauseColor());
        else play.setBackgroundResource(appColor.getPlayColor());
    }
}
