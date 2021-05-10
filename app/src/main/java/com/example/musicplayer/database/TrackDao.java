package com.example.musicplayer.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface TrackDao {

    @Query("SELECT * FROM Track")
    List<Track> getAll();

    @Query("SELECT * FROM Track WHERE id = :id")
    Track getById(int id);

    @Query("DELETE FROM Track")
    void deleteAll();

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(Track track);

    @Query("UPDATE Track SET playing = :playing WHERE id = :id")
    void update(int id, boolean playing);

    @Delete
    void delete(Track track);
}
