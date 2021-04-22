package com.example.musicplayer.Services;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.os.IBinder;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.example.musicplayer.App;
import com.example.musicplayer.MainActivity;

import java.io.File;
import java.io.IOException;

public class BackgroundMusicService extends Service {
    Boolean isFromSource = false;
    Boolean isPlayedBefore = false;

    @SuppressLint("NewApi")
    @Override
    public void onCreate() {
        super.onCreate();

        if (App.getSource().equals(".")) {
            isFromSource = false;
            App.setPlayer(MediaPlayer.create(this, Uri.parse(App.getCurrentPath())));
            App.getPlayer().setLooping(true);
        }
        else {
            try {
                App.createEmptyPlayer();
                App.getPlayer().setAudioAttributes(
                        new AudioAttributes.Builder()
                                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                .setUsage(AudioAttributes.USAGE_MEDIA)
                                .build()
                );
                App.getPlayer().setAudioStreamType(AudioManager.STREAM_MUSIC);
                App.getPlayer().setDataSource(App.getSource());
                App.getPlayer().prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }
            isFromSource = true;
        }
        App.getPlayer().setVolume(100, 100);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!isFromSource) {
            if (!isPlayedBefore) {
                if (App.isAnotherSong()) App.getPlayer().seekTo(0);
                else App.getPlayer().seekTo(App.getMediaPlayerCurrentPosition());
                isPlayedBefore = true;
            }
        }
        App.setIsPlaying(true);
        App.getPlayer().start();
        App.setPlayerId(App.getPlayer().getAudioSessionId());
        App.setCurrentDuration(App.getPlayer().getDuration());
        Toast.makeText(this, "started", Toast.LENGTH_SHORT).show();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        App.setIsPlaying(false);
        if (!isFromSource) App.setMediaPlayerCurrentPosition(App.getPlayer().getCurrentPosition());
        isPlayedBefore = false;
        App.setPlayerId(-1);
        App.setCurrentDuration(App.getPlayer().getDuration());
        App.getPlayer().stop();
        App.getPlayer().release();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}

