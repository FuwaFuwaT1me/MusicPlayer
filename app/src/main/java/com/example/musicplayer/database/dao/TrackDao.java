package com.example.musicplayer.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.musicplayer.database.entities.Track;

import java.util.List;

@Dao
public interface TrackDao {

    @Query("SELECT * FROM Track")
    List<Track> getAll();

    @Query("SELECT * FROM Track WHERE id = :id")
    Track getById(int id);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(Track track);

    @Query("UPDATE Track SET playing = :playing WHERE id = :id")
    void updatePlaying(int id, boolean playing);

    @Query("UPDATE Track SET liked = :liked WHERE id = :id")
    void updateLiked(int id, boolean liked);

    @Query("UPDATE Track SET selected = :selected WHERE id = :id")
    void updateSelected(int id, boolean selected);

    @Delete
    void delete(Track track);
}
