package com.example.musicplayer.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.example.musicplayer.database.entities.Playlist;
import com.example.musicplayer.database.dao.PlaylistDao;
import com.example.musicplayer.database.entities.Radio;
import com.example.musicplayer.database.dao.RadioDao;
import com.example.musicplayer.database.entities.Track;
import com.example.musicplayer.database.dao.TrackDao;
import com.example.musicplayer.database.entities.TrackPlaylist;
import com.example.musicplayer.database.dao.TrackPlaylistDao;

@Database(entities = {Track.class, Playlist.class, TrackPlaylist.class, Radio.class}, version = 2, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    public abstract TrackDao trackDao();
    public abstract PlaylistDao playlistDao();
    public abstract TrackPlaylistDao trackPlaylistDao();
    public abstract RadioDao radioDao();
}
