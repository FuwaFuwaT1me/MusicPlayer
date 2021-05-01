package com.example.musicplayer.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {MusicTrack.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract MusicTrackDao musicTrackDao();
}
