package com.example.musicplayer.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

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

    @Update
    void update(Playlist playlist);

    @Query("DELETE FROM Playlist WHERE id LIKE :id")
    void delete(int id);
}
