package com.example.musicplayer.database;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(foreignKeys = {
        @ForeignKey(entity = Track.class, parentColumns = "id", childColumns = "trackId", onDelete = ForeignKey.CASCADE),
        @ForeignKey(entity = Playlist.class, parentColumns = "id", childColumns = "playlistId", onDelete = ForeignKey.CASCADE)
})
public class TrackPlaylist {

    @PrimaryKey(autoGenerate = true)
    private long id;

    @ColumnInfo(index = true)
    private int trackId;

    @ColumnInfo(index = true)
    private int playlistId;

    public TrackPlaylist(int trackId, int playlistId) {
        this.trackId = trackId;
        this.playlistId = playlistId;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getTrackId() {
        return trackId;
    }

    public void setTrackId(int trackId) {
        this.trackId = trackId;
    }

    public int getPlaylistId() {
        return playlistId;
    }

    public void setPlaylistId(int playlistId) {
        this.playlistId = playlistId;
    }
}
