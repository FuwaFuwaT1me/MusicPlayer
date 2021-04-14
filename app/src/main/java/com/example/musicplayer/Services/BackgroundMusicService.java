package com.example.musicplayer.Services;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
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

    Boolean isPlayedBefore = false;

    @SuppressLint("NewApi")
    @Override
    public void onCreate() {
        super.onCreate();

        App.setPlayer(MediaPlayer.create(this, Uri.parse(App.getCurrentPath())));
        App.getPlayer().setLooping(true);
        App.getPlayer().setVolume(100, 100);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!isPlayedBefore) {
            App.setIsPlaying(true);
            if (App.isAnotherSong()) App.getPlayer().seekTo(0);
            else App.getPlayer().seekTo(App.getMediaPlayerCurrentPosition());
            isPlayedBefore = true;
        }
        App.getPlayer().start();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        App.setIsPlaying(false);
        App.setMediaPlayerCurrentPosition(App.getPlayer().getCurrentPosition());
        isPlayedBefore = false;
        App.getPlayer().stop();
        App.getPlayer().release();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}

