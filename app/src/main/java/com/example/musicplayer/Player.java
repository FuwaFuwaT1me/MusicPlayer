package com.example.musicplayer;

import android.content.Intent;
import android.media.MediaPlayer;

import com.example.musicplayer.music.Track;

import java.util.ArrayList;
import java.util.List;

public class Player {
    private boolean isPlaying;
    private int currentSong = -1;
    private boolean isAnotherSong;
    private Intent playerService;
    private int currentDuration;
    private int playerId;
    private int mediaPlayerCurrentPosition = 0;
    private boolean wasSongSwitched;
    private List<Track> trackList = new ArrayList<>();
    private List<Track> radioList = new ArrayList<>();
    private MediaPlayer player;
    private String source = ".";
    private int currentRadio = -1;
    private List<Track> queue = new ArrayList<>();
    private boolean isRepeated = false;
    private boolean isShuffled = false;

    public Player() {
        radioList.add(new Track("Chill-out radio", "http://air.radiorecord.ru:8102/chil_320"));
        radioList.add(new Track("Pop radio", "http://ice-the.musicradio.com/CapitalXTRANationalMP3"));
        radioList.add(new Track("Anime radio", "http://pool.anison.fm:9000/AniSonFM(320)?nocache=0.98"));
        radioList.add(new Track("Rock radio", "http://galnet.ru:8000/hard"));
        radioList.add(new Track("Dubstep radio", "http://air.radiorecord.ru:8102/dub_320"));

    }

    public  void setIsRepeated(boolean temp) {
        isRepeated = temp;
    }

    public  boolean isRepeated() {
        return isRepeated;
    }

    public  void setIsShuffled(boolean temp) {
        isShuffled = temp;
    }

    public  boolean isShuffled() {
        return isShuffled;
    }

    public  void addToQueue(Track track) {
        queue.add(track);
    }

    public  void clearQueue() {
        queue.clear();
    }

    public  Track getCurrentRadioTrack() {
        return radioList.get(currentRadio);
    }

    public  void setCurrentRadio(int index) {
        currentRadio = index;
    }

    public  int getCurrentRadio() {
        return currentRadio;
    }

    public  void addRadio(Track track) {
        radioList.add(track);
    }

    public  void removeRadio(int index) {
        radioList.remove(index);
    }

    public  List<Track> getRadioList() {
        return radioList;
    }

    public  int getRadioListSize() {
        return radioList.size();
    }

    public  void setSource(String source) {
        this.source = source;
    }

    public  String getSource() {
        return source;
    }

    public  void createEmptyPlayer() {
        player = new MediaPlayer();
    }

    public  int getCurrentDuration() {
        return currentDuration;
    }

    public  void setCurrentDuration(int currentDuration) {
        this.currentDuration = currentDuration;
    }

    public  void addTrack(Track track) {
        trackList.add(track);
    }

    public  void removeTrack(int index) {
        trackList.remove(index);
    }

    public  void clearTrackList() {
        trackList.clear();
    }

    public  int getQueueSize() {
        return queue.size();
    }

    public  Track getCurrentTrack() {
        if (currentSong == -1) return null;
        return queue.get(currentSong);
    }

    public  int getTrackListSize() {
        return trackList.size();
    }

    public  Track getTrackFromQueue(int index) {
        return queue.get(index);
    }

    public  Track getTrack(int index) {
        return trackList.get(index);
    }

    public  String getCurrentPath() {
        if (currentSong == -1) return "";
        return getCurrentTrack().getPath();
    }

    public  String getCurrentTitle() {
        if (currentSong == -1) return "";
        return getCurrentTrack().getTitle();
    }

    public  int getPlayerId() {
        return playerId;
    }

    public  void setPlayerId(int playerId) {
        this.playerId = playerId;
    }

    public  boolean isPlaying() {
        return isPlaying;
    }

    public  void setIsPlaying(boolean isPlaying) {
        this.isPlaying = isPlaying;
    }

    public  int getCurrentSong() {
        return currentSong;
    }

    public  void setCurrentSong(int currentSong) {
        this.currentSong = currentSong;
    }

    public  boolean isAnotherSong() {
        return isAnotherSong;
    }

    public  void setIsAnotherSong(boolean isAnotherSong) {
        isAnotherSong = isAnotherSong;
    }

    public  Intent getPlayerService() {
        return playerService;
    }

    public  void setPlayerService(Intent playerService) {
        this.playerService = playerService;
    }

    public  int getMediaPlayerCurrentPosition() {
        return mediaPlayerCurrentPosition;
    }

    public  void setMediaPlayerCurrentPosition(int mediaPlayerCurrentPosition) {
        this.mediaPlayerCurrentPosition = mediaPlayerCurrentPosition;
    }

    public  boolean wasSongSwitched() {
        return wasSongSwitched;
    }

    public  void setWasSongSwitched(boolean wasSongSwitched) {
        this.wasSongSwitched = wasSongSwitched;
    }

    public  List<Track> getTrackList() {
        return trackList;
    }

    public  MediaPlayer getMediaPlayer() {
        return player;
    }

    public  void setPlayer(MediaPlayer player) {
        this.player = player;
    }
}
