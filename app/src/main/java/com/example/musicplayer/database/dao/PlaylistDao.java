package com.example.musicplayer.database.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.musicplayer.database.entities.Playlist;

import java.util.List;

@Dao
public interface PlaylistDao {

    @Query("SELECT * FROM Playlist")
    List<Playlist> getAll();

    @Query("SELECT * FROM Playlist WHERE id = :id")
    Playlist getById(int id);

    @Query("DELETE FROM Playlist")
    void deleteAll();

    @Query("SELECT EXISTS(SELECT * FROM Playlist)")
    boolean ifExist();

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(Playlist playlist);

    @Query("DELETE FROM Playlist WHERE id LIKE :id")
    void delete(int id);
}
