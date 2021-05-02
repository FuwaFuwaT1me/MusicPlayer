package com.example.musicplayer.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {Track.class, Playlist.class, TrackPlaylist.class}, version = 6, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    public abstract TrackDao trackDao();
    public abstract PlaylistDao playlistDao();
    public abstract TrackPlaylistDao trackPlaylistDao();
}
