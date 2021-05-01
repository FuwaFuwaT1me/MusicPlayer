package com.example.musicplayer.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class MusicTrack {

    public MusicTrack(int id, String name, String path) {
        this.id = id;
        this.name = name;
        this.path = path;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @PrimaryKey
    private int id;

    private String name;

    private String path;
}
