package com.example.musicplayer.database.radio;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.musicplayer.database.radio.Radio;

import java.util.List;

@Dao
public interface RadioDao {

    @Query("SELECT * FROM Radio")
    List<Radio> getAll();

    @Query("SELECT * FROM Radio WHERE id = :id")
    Radio getById(int id);

    @Query("DELETE FROM Radio")
    void deleteAll();

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(Radio radio);

    @Query("UPDATE Radio SET playing = :playing WHERE id = :id")
    void updatePlaying(int id, boolean playing);

    @Query("DELETE FROM Radio WHERE id LIKE :id")
    void delete(int id);
}