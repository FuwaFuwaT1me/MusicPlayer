package com.example.musicplayer.music;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PorterDuff;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.musicplayer.App;
import com.example.musicplayer.Player;
import com.example.musicplayer.R;
import com.example.musicplayer.Services.OnClearFromRecentService;
import com.example.musicplayer.database.AppDatabase;
import com.example.musicplayer.database.Track;
import com.example.musicplayer.notification.CreateNotification;
import com.example.musicplayer.notification.Playable;
import com.gauravk.audiovisualizer.visualizer.BarVisualizer;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SongActivity extends AppCompatActivity implements Runnable, Playable {
    Player player;

    Button back, shuffle, prev, play, next, repeat, fastForward, fastBack;
    TextView songNameView, startTiming, endTiming;
    SeekBar seekBar;
    Thread seekBarThread = new Thread(this);
    BarVisualizer visualizer;
    boolean running = true;
    NotificationManager notificationManager;
    AppDatabase db;

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song);
        player = App.getApp().getPlayer();
        init();
    }

    @SuppressLint("UseCompatLoadingForColorStateLists")
    private void init() {
        db = App.getApp().getDb();

        play = findViewById(R.id.play2);
        prev = findViewById(R.id.previous2);
        next = findViewById(R.id.next2);
        back = findViewById(R.id.back);
        fastForward = findViewById(R.id.fastForward);
        fastBack = findViewById(R.id.fastBack);
        seekBar = findViewById(R.id.seekBar);
        songNameView = findViewById(R.id.songName2);
        updateTitle();
        songNameView.setSelected(true);
        startTiming = findViewById(R.id.startTiming);
        startTiming.setText(createTime(player.getMediaPlayerCurrentPosition()));
        endTiming = findViewById(R.id.endTiming);
        endTiming.setText(createTime(player.getCurrentDuration()));
        visualizer = findViewById(R.id.bar);
        repeat = findViewById(R.id.repeat);
        shuffle = findViewById(R.id.shuffle);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannel();
            registerReceiver(broadcastReceiver, new IntentFilter("TRACKS_TRACKS"));
            startService(new Intent(getBaseContext(), OnClearFromRecentService.class));
        }

        int audioSessionId = player.getPlayerId();
        if (audioSessionId != -1) {
            visualizer.setAudioSessionId(audioSessionId);
        }

        if (player.isPlaying()) {
            play.setBackgroundResource(R.drawable.ic_pause);
        } else {
            play.setBackgroundResource(R.drawable.ic_play);
        }

        if (!player.isRepeated()) {
            repeat.setBackgroundTintList((getResources().getColorStateList(R.color.white)));
        }
        else {
            repeat.setBackgroundTintList((getResources().getColorStateList(R.color.button_tint_color)));
        }

        if (!player.isShuffled()) {
            shuffle.setBackgroundTintList((getResources().getColorStateList(R.color.white)));
        }
        else {
            shuffle.setBackgroundTintList((getResources().getColorStateList(R.color.button_tint_color)));
        }

        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                player.setIsAnotherSong(false);
                if (player.isPlaying()) {
                    createTrackNotification();

                    player.setIsPlaying(false);
                    play.setBackgroundResource(R.drawable.ic_play);
                    stopService(App.getApp().getPlayerService());
                } else {
                    createTrackNotification();

                    player.setIsPlaying(true);
                    play.setBackgroundResource(R.drawable.ic_pause);
                    startService(App.getApp().getPlayerService());
                }
            }
        });

        prev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (player.getCurrentSong() - 1 >= 0) {
                    moveTrack(-1);
                }
                else {
                    if (!player.isPlaying()) startService(App.getApp().getPlayerService());

                    player.getMediaPlayer().seekTo(0);
                }
                play.setBackgroundResource(R.drawable.ic_pause);
                createTrackNotification();
            }
        });
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playNext();
            }
        });
        fastForward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (player.isPlaying()) {
                    player.setIsAnotherSong(false);
                    player.getMediaPlayer().seekTo(player.getMediaPlayer().getCurrentPosition()+10000);
                }
            }
        });
        fastBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (player.isPlaying()) {
                    player.setIsAnotherSong(false);
                    player.getMediaPlayer().seekTo(player.getCurrentPosition()-10000);
                }
            }
        });
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                player.setIsAnotherSong(false);
                player.getMediaPlayer().seekTo(seekBar.getProgress());
            }
        });
        repeat.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("UseCompatLoadingForColorStateLists")
            @Override
            public void onClick(View v) {
                if (!player.isRepeated()) {
                    player.setIsRepeated(true);
                    repeat.setBackgroundTintList((getResources().getColorStateList(R.color.button_tint_color)));
                }
                else {
                    player.setIsRepeated(false);
                    player.setLooping(false);
                    repeat.setBackgroundTintList((getResources().getColorStateList(R.color.white)));
                }
                setRepeat();
            }
        });
        shuffle.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("UseCompatLoadingForColorStateLists")
            @Override
            public void onClick(View v) {
                if (!player.isShuffled()) {
                    player.setIsShuffled(true);
                    shuffle.setBackgroundTintList((getResources().getColorStateList(R.color.button_tint_color)));
                    shuffleQueue();
                }
                else {
                    player.setIsShuffled(false);
                    shuffle.setBackgroundTintList((getResources().getColorStateList(R.color.white)));
                    returnQueueToNormal();
                }
            }
        });

        seekBar.setMax(player.getCurrentDuration());
        seekBar.setProgress(player.getMediaPlayerCurrentPosition());
        seekBarThread.start();
        seekBar.getProgressDrawable().setColorFilter(getResources().getColor(R.color.primeColor), PorterDuff.Mode.MULTIPLY);
        seekBar.getThumb().setColorFilter(getResources().getColor(R.color.primeColor), PorterDuff.Mode.SRC_IN);
    }

    private void returnQueueToNormal() {
        player.clearQueue();
        for (Track track : db.trackDao().getAll()) player.addToQueue(track);
    }

    private void shuffleQueue() {
        List<Track> temp = new ArrayList<>();
        Track currTrack = player.getCurrentTrack();
        int currIndex = player.getCurrentSong();
        for (int i = 0; i < player.getQueueSize(); i++) {
            temp.add(player.getTrackFromQueue(i));
        }
        player.clearQueue();
        Random rnd = new Random();

        for (int i = 0; i < temp.size(); i++) {
            if (i == currIndex) {
                player.addToQueue(currTrack);
                temp.remove(currTrack);
                continue;
            }
            int index = rnd.nextInt(temp.size());
            while (index == currIndex) index = rnd.nextInt(temp.size());

            player.addToQueue(
                    temp.get(index)
            );
            temp.remove(index);
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

    public void run() {
        while (running) {
            try {
                if (player.getMediaPlayer() != null) {
                    setRepeat();
                    int audioSessionId = player.getAudioSessionId();
                    if (audioSessionId != -1) {
                        visualizer.setAudioSessionId(audioSessionId);
                    }
                    Thread.sleep(1000);
                    int total = player.getDuration();
                    int current = player.getCurrentPosition();
                    startTiming.setText(createTime(current));
                    endTiming.setText(createTime(total));
                    seekBar.setMax(total);
                    seekBar.setProgress(current);
                }
            } catch (Exception e) {
            }
        }
    }

    @Override
    protected void onDestroy() {
        if (visualizer != null) {
            visualizer.release();
        }
        super.onDestroy();
        running = false;
    }

    public String createTime(int duration) {
        String time = "";
        int min = duration / 1000 / 60;
        int sec = duration / 1000 % 60;

        time += min + ":";
        if (sec < 10) time += "0";
        time += sec;
        return time;
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

    void createTrackNotification() {
        CreateNotification.createNotification(getApplicationContext(),
                player.getCurrentTrack(),
                R.drawable.ic_pause,
                player.getCurrentSong(),
                player.getQueueSize()-1);
    }

    void moveTrack(int direction) {
        player.setWasSongSwitched(true);
        player.setCurrentSong(player.getCurrentSong() + direction);
        stopService(App.getApp().getPlayerService());
        player.setIsAnotherSong(true);
        updateTitle();
        startService(App.getApp().getPlayerService());
    }

    void updateTitle() {
        songNameView.setText(player.getCurrentTitle());
    }

    void playNext() {
        if (player.getCurrentSong() + 1 < player.getQueueSize()) {
            moveTrack(1);
        }
        else {
            player.getMediaPlayer().seekTo(player.getDuration());
            seekBar.setProgress(player.getDuration());
            startTiming.setText(createTime(player.getDuration()));

            stopService(App.getApp().getPlayerService());
        }

        play.setBackgroundResource(R.drawable.ic_pause);
        createTrackNotification();
    }

    private void setRepeat() {
        if (player.getMediaPlayer() == null) return;
        if (!player.isRepeated()) {
            player.getMediaPlayer().setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    player.setLooping(false);
                    playNext();
                }
            });
        }
        else {
            player.getMediaPlayer().setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    startService(App.getApp().getPlayerService());
                    player.setLooping(true);
                }
            });
        }
    }
}
