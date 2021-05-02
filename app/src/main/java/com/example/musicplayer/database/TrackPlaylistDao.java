package com.example.musicplayer.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface TrackPlaylistDao {

    @Query("SELECT * FROM TrackPlaylist")
    List<TrackPlaylist> getAll();

    @Query("SELECT * FROM TrackPlaylist WHERE trackId = :id")
    TrackPlaylist getByTrackId(int id);

    @Query("SELECT * FROM TrackPlaylist WHERE playlistId = :id")
    List<TrackPlaylist> getAllByPlaylistId(int id);

    @Query("DELETE FROM TrackPlaylist")
    void deleteAll();

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(TrackPlaylist trackPlaylist);

    @Update
    void update(TrackPlaylist trackPlaylist);

    @Delete
    void delete(TrackPlaylist trackPlaylist);
}
