package com.example.musicplayer;

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
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.musicplayer.Services.OnClearFromRecentService;
import com.gauravk.audiovisualizer.visualizer.BarVisualizer;

//import static com.example.musicplayer.Services.BackgroundMusicService.player;

public class SongActivity extends AppCompatActivity implements Runnable, Playable {
    Button back, refresh, prev, play, next, repeat, fastForward, fastBack;
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

        //Toast.makeText(this, ""+App.isPlaying(), Toast.LENGTH_SHORT).show();

        play = findViewById(R.id.play2);
        prev = findViewById(R.id.previous2);
        next = findViewById(R.id.next2);
        back = findViewById(R.id.back);
        fastForward = findViewById(R.id.fastForward);
        fastBack = findViewById(R.id.fastBack);
        seekBar = findViewById(R.id.seekBar);
        songNameView = findViewById(R.id.songName2);
        songNameView.setText(App.getCurrentTitle());
        songNameView.setSelected(true);
        startTiming = findViewById(R.id.startTiming);
        startTiming.setText(createTime(App.getMediaPlayerCurrentPosition()));
        endTiming = findViewById(R.id.endTiming);
        endTiming.setText(createTime(App.getCurrentDuration()));
        visualizer = findViewById(R.id.bar);

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

        App.getPlayer().setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                next.performClick();
            }
        });

        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                App.setIsAnotherSong(false);
                if (App.isPlaying()) {
                    onTrackPause();
                } else {
                    onTrackPlay();
                }
            }
        });
//        pause.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                stopService(MainActivity.service);
//                play.setVisibility(View.VISIBLE);
//                pause.setVisibility(View.GONE);
//            }
//        });
        prev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (App.getCurrentSong() - 1 >= 0) {
                    App.setCurrentSong(App.getCurrentSong()-1);
                    songNameView.setText(App.getCurrentTitle());
                    stopService(App.getPlayerService());
                    App.setIsAnotherSong(true);
                    startService(App.getPlayerService());
                    play.setBackgroundResource(R.drawable.ic_pause);
                    //startAnimation(imageView);
                } else {
                    if (!App.isPlaying()) {
                        startService(App.getPlayerService());
                        App.setIsAnotherSong(true);
                        play.setBackgroundResource(R.drawable.ic_pause);
                    } else {
                        App.getPlayer().seekTo(0);
                    }
                }
//                int audioSessionId = player.getAudioSessionId();
//                if (audioSessionId != -1) {
//                    visualizer.setAudioSessionId(audioSessionId);
//                }
            }
        });
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (App.getCurrentSong()+ 1 < App.getSize()) {
                    App.setCurrentSong(App.getCurrentSong()+1);
                    songNameView.setText(App.getCurrentTitle());
                    stopService(App.getPlayerService());
                    App.setIsAnotherSong(true);
                    startService(App.getPlayerService());
                    play.setBackgroundResource(R.drawable.ic_pause);
                    //startAnimation(imageView);

                } else {
                    App.getPlayer().seekTo(App.getPlayer().getDuration());
                    seekBar.setProgress(App.getPlayer().getDuration());
                    startTiming.setText(createTime(App.getPlayer().getDuration()));
                    play.setBackgroundResource(R.drawable.ic_play);
                    stopService(App.getPlayerService());
                }
//                int audioSessionId = player.getAudioSessionId();
//                if (audioSessionId != -1) {
//                    visualizer.setAudioSessionId(audioSessionId);
//                }
            }
        });
        fastForward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (App.isPlaying()) {
                    App.getPlayer().seekTo(App.getPlayer().getCurrentPosition()+10000);
                }
            }
        });
        fastBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (App.isPlaying()) {
                    App.getPlayer().seekTo(App.getPlayer().getCurrentPosition()-10000);
                }
            }
        });
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent intent = new Intent(SongActivity.this, MainActivity.class);
//                startActivity(intent);
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
//                if (player != null && player.isPlaying()) {
//                    player.seekTo(seekBar.getProgress());
//                }
                App.getPlayer().seekTo(seekBar.getProgress());
            }
        });
        seekBar.setMax(App.getCurrentDuration());
        seekBar.setProgress(App.getMediaPlayerCurrentPosition());
        seekBarThread.start();
        seekBar.getProgressDrawable().setColorFilter(getResources().getColor(R.color.primeColor), PorterDuff.Mode.MULTIPLY);
        seekBar.getThumb().setColorFilter(getResources().getColor(R.color.primeColor), PorterDuff.Mode.SRC_IN);
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
                    int audioSessionId = App.getPlayer().getAudioSessionId();
                    if (audioSessionId != -1) {
                        visualizer.setAudioSessionId(audioSessionId);
                    }
                    Thread.sleep(1000);
                    int total = App.getPlayer().getDuration();
                    int current = App.getPlayer().getCurrentPosition();
                    startTiming.setText(createTime(current));
                    seekBar.setMax(total);
                    seekBar.setProgress(current);
                }
            } catch (Exception e) {
            }
        }

//        int total = player.getDuration();
//        int current = 0;
//
//        while (current < total) {
//            try {
//                Thread.sleep(500);
//                current = player.getCurrentPosition();
//                seekBar.setProgress(current);
//            } catch (InterruptedException | IllegalStateException e) {
//                e.printStackTrace();
//            }
//        }
    }

    @Override
    protected void onDestroy() {
        if (visualizer != null) {
            visualizer.release();
        }
        super.onDestroy();
        running = false;
    }

//    public void startAnimation(View view) {
//        ObjectAnimator animator = ObjectAnimator.ofFloat(imageView, "rotation", 0f, 360f);
//        animator.setDuration(1000);
//        AnimatorSet animatorSet = new AnimatorSet();
//        animatorSet.playTogether(animator);
//        animatorSet.start();
//    }

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
            //Toast.makeText(context, ""+App.isPlaying(), Toast.LENGTH_SHORT).show();
            String action = intent.getExtras().getString("actionName");


            switch (action) {
                case CreateNotification.ACTION_PREVIOUS:
                    onTrackPrevious();
                    break;
                case CreateNotification.ACTION_PLAY:
                    //Toast.makeText(context, ""+App.isPlaying(), Toast.LENGTH_SHORT).show();
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
        App.setWasSongSwitched(true);
        App.setCurrentSong(App.getCurrentSong()-1);
        stopService(App.getPlayerService());
        App.setIsAnotherSong(true);
        startService(App.getPlayerService());

        CreateNotification.createNotification(SongActivity.this,
                App.getCurrentTrack(),
                R.drawable.ic_pause,
                App.getCurrentSong(),
                App.getSize()-1);
        songNameView.setText(App.getCurrentTitle());
    }

    @Override
    public void onTrackPlay() {
        CreateNotification.createNotification(SongActivity.this,
                App.getCurrentTrack(),
                R.drawable.ic_pause,
                App.getCurrentSong(),
                App.getSize()-1);
        App.setIsPlaying(true);
        App.setIsAnotherSong(false);
        startService(App.getPlayerService());
        play.setBackgroundResource(R.drawable.ic_pause);
        songNameView.setText(App.getCurrentTitle());
    }

    @Override
    public void onTrackPause() {
        CreateNotification.createNotification(SongActivity.this,
                App.getCurrentTrack(),
                R.drawable.ic_play,
                App.getCurrentSong(),
                App.getSize()-1);
        App.setIsPlaying(false);
        App.setIsAnotherSong(false);
        stopService(App.getPlayerService());
        play.setBackgroundResource(R.drawable.ic_play);
        songNameView.setText(App.getCurrentTitle());
        Toast.makeText(getApplicationContext(), "stopped", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onTrackNext() {
        App.setWasSongSwitched(true);
        App.setCurrentSong(App.getCurrentSong()+1);
        stopService(App.getPlayerService());
        App.setIsAnotherSong(true);
        startService(App.getPlayerService());

        CreateNotification.createNotification(SongActivity.this,
                App.getCurrentTrack(),
                R.drawable.ic_pause,
                App.getCurrentSong(),
                App.getSize()-1);
        songNameView.setText(App.getCurrentTitle());
    }
}
