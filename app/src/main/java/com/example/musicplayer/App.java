package com.example.musicplayer;

import android.app.Application;
import android.content.Intent;
import android.media.MediaPlayer;

import com.example.musicplayer.music.Track;

import java.util.ArrayList;
import java.util.List;

public class App extends Application {
    private static App uniqueInstance;

    private static boolean isPlaying;
    private static int currentSong = -1;
    private static boolean isAnotherSong;
    private static Intent playerService;
    private static int currentDuration;
    private static int playerId;
    private static int mediaPlayerCurrentPosition = 0;
    private static boolean wasSongSwitched;
    private final static List<Track> trackList = new ArrayList<>();
    private final static List<Track> radioList = new ArrayList<>();
    private static MediaPlayer player;
    private static String source = ".";
    private static int currentRadio = -1;

    private App() {}

    public static App getInstance() {
        if (uniqueInstance == null) {
            radioList.add(new Track("Chill-out radio", "http://air.radiorecord.ru:8102/chil_320"));
            radioList.add(new Track("Pop radio", "http://ice-the.musicradio.com/CapitalXTRANationalMP3"));
            radioList.add(new Track("Anime radio", "http://pool.anison.fm:9000/AniSonFM(320)?nocache=0.98"));
            radioList.add(new Track("Rock radio", "http://galnet.ru:8000/hard"));
            radioList.add(new Track("Dubstep radio", "http://air.radiorecord.ru:8102/dub_320"));
            uniqueInstance = new App();
        }
        return uniqueInstance;
    }

    public static Track getCurrentRadioTrack() {
        return radioList.get(currentRadio);
    }

    public static void setCurrentRadio(int index) {
        currentRadio = index;
    }

    public static int getCurrentRadio() {
        return currentRadio;
    }

    public static void addRadio(Track track) {
        radioList.add(track);
    }

    public static void removeRadio(int index) {
        radioList.remove(index);
    }

    public static Track getRadio(int index) {
        return radioList.get(index);
    }

    public static List<Track> getRadioList() {
        return radioList;
    }

    public static int getRadioListSize() {
        return radioList.size();
    }

    public static void setSource(String source) {
        App.source = source;
    }

    public static String getSource() {
        return source;
    }

    public static void createEmptyPlayer() {
        player = new MediaPlayer();
    }

    public static int getCurrentDuration() {
        return currentDuration;
    }

    public static void setCurrentDuration(int currentDuration) {
        App.currentDuration = currentDuration;
    }

    public static void addTrack(Track track) {
        trackList.add(track);
    }

    public static void removeTrack(int index) {
        trackList.remove(index);
    }

    public static void clearTrackList() {
        trackList.clear();
    }

    public static int getTrackListSize() {
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

    public static int getPlayerId() {
        return playerId;
    }

    public static void setPlayerId(int playerId) {
        App.playerId = playerId;
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

    public static boolean wasSongSwitched() {
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
