package com.example.musicplayer.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.example.musicplayer.database.playlist.Playlist;
import com.example.musicplayer.database.playlist.PlaylistDao;
import com.example.musicplayer.database.radio.Radio;
import com.example.musicplayer.database.radio.RadioDao;
import com.example.musicplayer.database.track.Track;
import com.example.musicplayer.database.track.TrackDao;
import com.example.musicplayer.database.trackplaylist.TrackPlaylist;
import com.example.musicplayer.database.trackplaylist.TrackPlaylistDao;

@Database(entities = {Track.class, Playlist.class, TrackPlaylist.class, Radio.class}, version = 2, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    public abstract TrackDao trackDao();
    public abstract PlaylistDao playlistDao();
    public abstract TrackPlaylistDao trackPlaylistDao();
    public abstract RadioDao radioDao();
}
