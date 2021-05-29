package com.example.musicplayer.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.musicplayer.database.entities.TrackPlaylist;

import java.util.List;

@Dao
public interface TrackPlaylistDao {

    @Query("SELECT * FROM TrackPlaylist")
    List<TrackPlaylist> getAll();

    @Query("SELECT * FROM TrackPlaylist WHERE playlistId = :id")
    List<TrackPlaylist> getAllByPlaylistId(int id);

    @Query("DELETE FROM TrackPlaylist")
    void deleteAll();

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(TrackPlaylist trackPlaylist);

    @Delete
    void delete(TrackPlaylist trackPlaylist);
}
