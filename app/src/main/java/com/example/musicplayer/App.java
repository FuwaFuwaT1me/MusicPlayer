package com.example.musicplayer;

import android.app.Application;
import android.content.Intent;
import android.media.MediaPlayer;

import java.util.ArrayList;
import java.util.List;

public class App extends Application {
    private static App uniqueInstance;

    private static boolean isPlaying;
    private static int currentSong = 0;
    private static boolean isAnotherSong;
    private static Intent playerService;
    private static int mediaPlayerCurrentPosition = 0;
    private static boolean wasSongSwitched;
    private final static List<Track> trackList = new ArrayList<>();
    private static MediaPlayer player;

    private App() {}

    public static App getInstance() {
        if (uniqueInstance == null) {
            uniqueInstance = new App();
        }
        return uniqueInstance;
    }

    public static void addTrack(Track track) {
        trackList.add(track);
    }

    public static void removeTrack(Track track) {
        trackList.remove(track);
    }

    public static void clearTrackList() {
        trackList.clear();
    }

    public static int getSize() {
        return trackList.size();
    }

    public static Track getCurrentTrack() {
        if (trackList.isEmpty()) return null;
        return trackList.get(currentSong);
    }

    public static String getCurrentPath() {
        if (trackList.isEmpty()) return "";
        return getCurrentTrack().getPath();
    }

    public static String getCurrentTitle() {
        if (trackList.isEmpty()) return "";
        return getCurrentTrack().getTitle();
    }

    public static boolean isPlaying() {
        return isPlaying;
    }

    public static void setIsPlaying(boolean isPlaying) {
        App.isPlaying = isPlaying;
    }

    public static int getCurrentSong() {
        return currentSong;
    }

    public static void setCurrentSong(int currentSong) {
        App.currentSong = currentSong;
    }

    public static boolean isAnotherSong() {
        return isAnotherSong;
    }

    public static void setIsAnotherSong(boolean isAnotherSong) {
        App.isAnotherSong = isAnotherSong;
    }

    public static Intent getPlayerService() {
        return playerService;
    }

    public static void setPlayerService(Intent playerService) {
        App.playerService = playerService;
    }

    public static int getMediaPlayerCurrentPosition() {
        return mediaPlayerCurrentPosition;
    }

    public static void setMediaPlayerCurrentPosition(int mediaPlayerCurrentPosition) {
        App.mediaPlayerCurrentPosition = mediaPlayerCurrentPosition;
    }

    public static boolean isWasSongSwitched() {
        return wasSongSwitched;
    }

    public static void setWasSongSwitched(boolean wasSongSwitched) {
        App.wasSongSwitched = wasSongSwitched;
    }

    public static List<Track> getTrackList() {
        return trackList;
    }

    public static MediaPlayer getPlayer() {
        return player;
    }

    public static void setPlayer(MediaPlayer player) {
        App.player = player;
    }
}
