package com.example.musicplayer;

import android.media.MediaPlayer;
import android.util.Log;

import com.example.musicplayer.database.AppDatabase;
import com.example.musicplayer.database.Playlist;
import com.example.musicplayer.database.Radio;
import com.example.musicplayer.database.Track;

import java.util.ArrayList;
import java.util.List;

public class Player {
    private boolean isPlaying;
    private boolean isAnotherSong;

    private int currentDuration;
    private int playerId;
    private int mediaPlayerCurrentPosition = 0;
    private boolean wasSongSwitched;
    private boolean seekWhilePause;

    private List<Track> queue = new ArrayList<>();
    private List<Integer> playlistIndexes = new ArrayList<>();
    private List<Integer> radioIndexes = new ArrayList<>();
    private List<Integer> selected = new ArrayList<>();

    private MediaPlayer mediaPlayer;
    private String source = ".";
    private int currentRadio = -1;
    private boolean isRepeated = false;
    private boolean isShuffled = false;

    private int playlistIndex = 0;
    private int playlistToView;
    private int currentPlaylist = -1;

    private int radioIndex = 0;

    private AppDatabase db;

    private int currentQueueTrack = -1;


    public Player() {
        db = App.getApp().getDb();

        db.radioDao().insert(new Radio(radioIndex++, "Chill-out radio", "http://air.radiorecord.ru:8102/chil_320"));
        db.radioDao().insert(new Radio(radioIndex++, "Pop radio", "http://ice-the.musicradio.com/CapitalXTRANationalMP3"));
        db.radioDao().insert(new Radio(radioIndex++, "Anime radio", "http://pool.anison.fm:9000/AniSonFM(320)?nocache=0.98"));
        db.radioDao().insert(new Radio(radioIndex++, "Rock radio", "http://galnet.ru:8000/hard"));
        db.radioDao().insert(new Radio(radioIndex++, "Dubstep radio", "http://air.radiorecord.ru:8102/dub_320"));

        for (Radio radio : db.radioDao().getAll()) {
            radioIndexes.add(radio.getId());
        }

        if (App.getApp().getDb().playlistDao().ifExist()) {
            clearPlaylistIndexes();
            List<Playlist> list = App.getApp().getDb().playlistDao().getAll();
            for (Playlist playlist : list) {
                this.addPlaylistIndex(playlist.getId());
            }
            playlistIndex = list.get(list.size() - 1).getId();
            incPlaylistIndex();
        }
    }

    public boolean isSeekWhilePause() {
        return seekWhilePause;
    }

    public void setSeekWhilePause(boolean seekWhilePause) {
        this.seekWhilePause = seekWhilePause;
    }

    public int getCurrentQueueTrack() {
        return currentQueueTrack;
    }

    public void setCurrentQueueTrack(int currentQueueTrack) {
        this.currentQueueTrack = currentQueueTrack;
    }

    public int getRadioIndex() {
        return radioIndex;
    }

    public void incRadioIndex() {
        radioIndex++;
    }

    public void setCurrentPlaylist(int currentPlaylist) {
        this.currentPlaylist = currentPlaylist;
    }

    public int getCurrentPlaylist() {
        return currentPlaylist;
    }

    public void addSelected(int id) {
        selected.add(id);
    }

    public void removeSelected(int id) {
        selected.remove((Integer)id);
    }

    public List<Integer> getSelected() {
        return selected;
    }

    public int getSelectedIndex(int id) {
        return selected.get(id);
    }

    public void clearSelected() {
        selected.clear();
    }

    public void addRadioIndex(int id) {
        radioIndexes.add(id);
    }

    public void removeRadioIndex(int id) {
        radioIndexes.remove(id);
    }

    public int getRadioIndexById(int id) {
        return radioIndexes.get(id);
    }

    public void clearRadioIndexes() {
        radioIndexes.clear();
    }

    public List<Integer> getRadioIndexes() {
        return radioIndexes;
    }

    public void addPlaylistIndex(int id) {
        playlistIndexes.add(id);
    }

    public void removePlaylistIndex(int id) {
        playlistIndexes.remove(id);
    }

    public int getPlaylistIndexById(int id) {
        return playlistIndexes.get(id);
    }

    public void clearPlaylistIndexes() {
        playlistIndexes.clear();
    }

    public List<Integer> getPlaylistIndexes() {
        return playlistIndexes;
    }

    public int getPlaylistToView() {
        return playlistToView;
    }

    public void setPlaylistToView(int playlistToView) {
        this.playlistToView = playlistToView;
    }

    public  int getPlaylistIndex() {
        return playlistIndex;
    }

    public  void setPlaylistIndex(int playlistIndex) {
        this.playlistIndex = playlistIndex;
    }

    public  void incPlaylistIndex() {
        playlistIndex++;
    }

    public void setIsRepeated(boolean temp) {
        isRepeated = temp;
    }

    public boolean isRepeated() {
        return isRepeated;
    }

    public void setPlayer(MediaPlayer player) {
        this.mediaPlayer = player;
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

    public Radio getCurrentRadioTrack() {
        return db.radioDao().getById(radioIndexes.get(currentRadio));
    }

    public  void setCurrentRadio(int index) {
        currentRadio = index;
    }

    public  int getCurrentRadio() {
        return currentRadio;
    }

    public  void addRadio(Radio radio) {
        db.radioDao().insert(radio);
    }

    public  void removeRadio(int index) {
        db.radioDao().delete(index);
    }

    public  int getRadioListSize() {
        return db.radioDao().getAll().size();
    }

    public  void setSource(String source) {
        this.source = source;
    }

    public  String getSource() {
        return source;
    }

    public  void createEmptyPlayer() {
        mediaPlayer = new MediaPlayer();
    }

    public  int getCurrentDuration() {
        return currentDuration;
    }

    public  void setCurrentDuration(int currentDuration) {
        this.currentDuration = currentDuration;
    }

    public  int getQueueSize() {
        return queue.size();
    }

    public  Track getCurrentTrack() {
        if (currentQueueTrack == -1) return null;
        return queue.get(currentQueueTrack);
    }

    public  Track getTrackFromQueue(int index) {
        return queue.get(index);
    }

    public  String getCurrentPath() {
        if (currentQueueTrack == -1) return "";
        return getCurrentTrack().getPath();
    }

    public  String getCurrentTitle() {
        if (currentQueueTrack == -1) return "";
        return getCurrentTrack().getName();
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

//    public  int getCurrentSong() {
//        return currentSong;
//    }
//
//    public  void setCurrentSong(int currentSong) {
//        this.currentSong = currentSong;
//    }

    public  boolean isAnotherSong() {
        return isAnotherSong;
    }

    public  void setIsAnotherSong(boolean isAnotherSong) {
        this.isAnotherSong = isAnotherSong;
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

    public  MediaPlayer getMediaPlayer() {
        return mediaPlayer;
    }

    public  void setMediaPlayer(MediaPlayer mediaPlayer) {
        this.mediaPlayer = mediaPlayer;
    }

    public int getCurrentPosition() {
        return mediaPlayer.getCurrentPosition();
    }

    public void setLooping(boolean b) {
        this.mediaPlayer.setLooping(b);
    }

    public int getAudioSessionId() {
        return this.mediaPlayer.getAudioSessionId();
    }

    public int getDuration() {
        return this.mediaPlayer.getDuration();
    }

    public List<Track> getQueue() {
        return queue;
    }

    public String updateTitle(String data) {
        data = data.replace(".mp3", "").replace(".wav", "");
        String title = "";
        String text = "";
        if (App.getApp().getPlayer().getSource().equals(".")) {
            String[] info = data.replace("_", " ").split("-");
            title = info[0];
            if (info.length >= 2) {
                for (int i = 1; i < info.length; i++) text += info[i];
            }
            if (text.indexOf(" ") == 0) text = text.substring(1);
        }
        else {
            title = "Radio";
            String temp = data;
            if (text.split(" ").length >= 2) text = temp.substring(0, temp.lastIndexOf(" "));
        }
        String dash = text.isEmpty() ? "" : " - ";
        return title + dash + text;
    }
}
