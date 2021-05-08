package com.example.musicplayer.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

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

    @Update
    void update(Radio radio);

    @Query("DELETE FROM Radio WHERE id LIKE :id")
    void delete(int id);
}