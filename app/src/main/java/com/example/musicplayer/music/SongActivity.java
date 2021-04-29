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
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.musicplayer.App;
import com.example.musicplayer.R;
import com.example.musicplayer.Services.OnClearFromRecentService;
import com.example.musicplayer.notification.CreateNotification;
import com.example.musicplayer.notification.Playable;
import com.gauravk.audiovisualizer.visualizer.BarVisualizer;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SongActivity extends AppCompatActivity implements Runnable, Playable {
    Button back, shuffle, prev, play, next, repeat, fastForward, fastBack;
    TextView songNameView, startTiming, endTiming;
    SeekBar seekBar;
    Thread seekBarThread = new Thread(this);
    BarVisualizer visualizer;
    boolean running = true;
    NotificationManager notificationManager;

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

        init();
    }

    @SuppressLint("UseCompatLoadingForColorStateLists")
    private void init() {
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
        startTiming.setText(createTime(App.getMediaPlayerCurrentPosition()));
        endTiming = findViewById(R.id.endTiming);
        endTiming.setText(createTime(App.getCurrentDuration()));
        visualizer = findViewById(R.id.bar);
        repeat = findViewById(R.id.repeat);
        shuffle = findViewById(R.id.shuffle);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannel();
            registerReceiver(broadcastReceiver, new IntentFilter("TRACKS_TRACKS"));
            startService(new Intent(getBaseContext(), OnClearFromRecentService.class));
        }

        int audioSessionId = App.getPlayerId();
        if (audioSessionId != -1) {
            visualizer.setAudioSessionId(audioSessionId);
        }

        if (App.isPlaying()) {
            play.setBackgroundResource(R.drawable.ic_pause);
        } else {
            play.setBackgroundResource(R.drawable.ic_play);
        }

        if (!App.isRepeated()) {
            repeat.setBackgroundTintList((getResources().getColorStateList(R.color.white)));
        }
        else {
            repeat.setBackgroundTintList((getResources().getColorStateList(R.color.button_tint_color)));
        }

        if (!App.isShuffled()) {
            shuffle.setBackgroundTintList((getResources().getColorStateList(R.color.white)));
        }
        else {
            shuffle.setBackgroundTintList((getResources().getColorStateList(R.color.button_tint_color)));
        }

        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                App.setIsAnotherSong(false);
                if (App.isPlaying()) {
                    createTrackNotification();

                    App.setIsPlaying(false);
                    play.setBackgroundResource(R.drawable.ic_play);
                    stopService(App.getPlayerService());
                } else {
                    createTrackNotification();

                    App.setIsPlaying(true);
                    play.setBackgroundResource(R.drawable.ic_pause);
                    startService(App.getPlayerService());
                }
            }
        });

        prev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (App.getCurrentSong() - 1 >= 0) {
                    moveTrack(-1);
                }
                else {
                    if (!App.isPlaying()) startService(App.getPlayerService());

                    App.getPlayer().seekTo(0);
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
                if (App.isPlaying()) {
                    App.setIsAnotherSong(false);
                    App.getPlayer().seekTo(App.getPlayer().getCurrentPosition()+10000);
                }
            }
        });
        fastBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (App.isPlaying()) {
                    App.setIsAnotherSong(false);
                    App.getPlayer().seekTo(App.getPlayer().getCurrentPosition()-10000);
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
                App.setIsAnotherSong(false);
                App.getPlayer().seekTo(seekBar.getProgress());
            }
        });
        repeat.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("UseCompatLoadingForColorStateLists")
            @Override
            public void onClick(View v) {
                if (!App.isRepeated()) {
                    App.setIsRepeated(true);
                    repeat.setBackgroundTintList((getResources().getColorStateList(R.color.button_tint_color)));
                }
                else {
                    App.setIsRepeated(false);
                    App.getPlayer().setLooping(false);
                    repeat.setBackgroundTintList((getResources().getColorStateList(R.color.white)));
                }
                setRepeat();
            }
        });
        shuffle.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("UseCompatLoadingForColorStateLists")
            @Override
            public void onClick(View v) {
                if (!App.isShuffled()) {
                    App.setIsShuffled(true);
                    shuffle.setBackgroundTintList((getResources().getColorStateList(R.color.button_tint_color)));
                    shuffleQueue();
                }
                else {
                    App.setIsShuffled(false);
                    shuffle.setBackgroundTintList((getResources().getColorStateList(R.color.white)));
                    returnQueueToNormal();
                }
            }
        });

        seekBar.setMax(App.getCurrentDuration());
        seekBar.setProgress(App.getMediaPlayerCurrentPosition());
        seekBarThread.start();
        seekBar.getProgressDrawable().setColorFilter(getResources().getColor(R.color.primeColor), PorterDuff.Mode.MULTIPLY);
        seekBar.getThumb().setColorFilter(getResources().getColor(R.color.primeColor), PorterDuff.Mode.SRC_IN);
    }

    private void returnQueueToNormal() {
        App.clearQueue();
        for (int i = 0; i < App.getTrackListSize(); i++) {
            App.addToQueue(App.getTrack(i));
        }
    }

    private void shuffleQueue() {
        List<Track> temp = new ArrayList<>();
        Track currTrack = App.getCurrentTrack();
        int currIndex = App.getCurrentSong();
        for (int i = 0; i < App.getQueueSize(); i++) {
            temp.add(App.getTrackFromQueue(i));
        }
        App.clearQueue();
        Random rnd = new Random();

        for (int i = 0; i < temp.size(); i++) {
            if (i == currIndex) {
                App.addToQueue(currTrack);
                temp.remove(currTrack);
                continue;
            }
            int index = rnd.nextInt(temp.size());
            while (index == currIndex) index = rnd.nextInt(temp.size());

            App.addToQueue(
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
                if (App.getPlayer() != null) {
                    setRepeat();
                    int audioSessionId = App.getPlayer().getAudioSessionId();
                    if (audioSessionId != -1) {
                        visualizer.setAudioSessionId(audioSessionId);
                    }
                    Thread.sleep(1000);
                    int total = App.getPlayer().getDuration();
                    int current = App.getPlayer().getCurrentPosition();
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
                    if (!App.isPlaying()) onTrackPause();
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
                App.getCurrentTrack(),
                R.drawable.ic_pause,
                App.getCurrentSong(),
                App.getQueueSize()-1);
    }

    void moveTrack(int direction) {
        App.setWasSongSwitched(true);
        App.setCurrentSong(App.getCurrentSong() + direction);
        stopService(App.getPlayerService());
        App.setIsAnotherSong(true);
        updateTitle();
        startService(App.getPlayerService());
    }

    void updateTitle() {
        songNameView.setText(App.getCurrentTitle());
    }

    void playNext() {
        if (App.getCurrentSong() + 1 < App.getQueueSize()) {
            moveTrack(1);
        }
        else {
            App.getPlayer().seekTo(App.getPlayer().getDuration());
            seekBar.setProgress(App.getPlayer().getDuration());
            startTiming.setText(createTime(App.getPlayer().getDuration()));

            stopService(App.getPlayerService());
        }

        play.setBackgroundResource(R.drawable.ic_pause);
        createTrackNotification();
    }

    private void setRepeat() {
        if (App.getPlayer() == null) return;
        if (!App.isRepeated()) {
            App.getPlayer().setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    App.getPlayer().setLooping(false);
                    playNext();
                }
            });
        }
        else {
            App.getPlayer().setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    startService(App.getPlayerService());
                    App.getPlayer().setLooping(true);
                }
            });
        }
    }
}
