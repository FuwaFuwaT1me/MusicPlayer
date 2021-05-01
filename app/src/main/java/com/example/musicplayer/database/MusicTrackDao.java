package com.example.musicplayer.database;

import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

public interface MusicTrackDao {

    @Query("SELECT * FROM MusicTrack")
    List<MusicTrack> getAll();

    @Query("SELECT * FROM MusicTrack WHERE id = :id")
    MusicTrack getById(int id);

    @Insert
    void insert(MusicTrack track);

    @Update
    void update(MusicTrack track);

    @Delete
    void delete(MusicTrack track);
}
