package com.example.musicplayer.Services;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.example.musicplayer.App;
import com.example.musicplayer.Player;

import java.io.IOException;

public class BackgroundMusicService extends Service {
    Player player;
    Boolean isFromSource = false;
    Boolean isPlayedBefore = false;

    @SuppressLint("NewApi")
    @Override
    public void onCreate() {
        super.onCreate();
        player = App.getApp().getPlayer();
        if (player.getSource().equals(".")) {
            isFromSource = false;
            player.setMediaPlayer(MediaPlayer.create(this, Uri.parse(player.getCurrentPath())));
        }
        else {
            try {
                player.createEmptyPlayer();
                player.getMediaPlayer().setAudioAttributes(
                        new AudioAttributes.Builder()
                                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                .setUsage(AudioAttributes.USAGE_MEDIA)
                                .build()
                );
                player.getMediaPlayer().setAudioStreamType(AudioManager.STREAM_MUSIC);
                player.getMediaPlayer().setDataSource(player.getSource());
                player.getMediaPlayer().prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }
            isFromSource = true;
        }
        player.getMediaPlayer().setVolume(100, 100);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!isFromSource) {
            if (!isPlayedBefore) {
                if (player.isAnotherSong()) player.getMediaPlayer().seekTo(0);
                else player.getMediaPlayer().seekTo(player.getMediaPlayerCurrentPosition());
                isPlayedBefore = true;
            }
        }
        player.setIsPlaying(true);
        player.getMediaPlayer().start();
        player.setPlayerId(player.getMediaPlayer().getAudioSessionId());
        player.setCurrentDuration(player.getMediaPlayer().getDuration());
        Toast.makeText(this, "started", Toast.LENGTH_SHORT).show();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        player.setIsPlaying(false);
        if (!isFromSource) player.setMediaPlayerCurrentPosition(player.getCurrentPosition());
        isPlayedBefore = false;
        player.setPlayerId(-1);
        player.setCurrentDuration(player.getMediaPlayer().getDuration());
        player.getMediaPlayer().stop();
        player.getMediaPlayer().release();
        //App.setPlayer(null);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}

