package com.example.musicplayer.music;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.musicplayer.App;
import com.example.musicplayer.R;
import com.example.musicplayer.adapter.TrackAdapterSelect;
import com.example.musicplayer.database.AppDatabase;
import com.example.musicplayer.database.Playlist;
import com.example.musicplayer.database.Track;
import com.example.musicplayer.database.TrackPlaylist;

import java.util.ArrayList;
import java.util.List;

public class CreatingPlaylistActivity extends AppCompatActivity {
    Button create, back;
    ListView tracks;
    EditText playlistName;
    AppDatabase db;
    TrackAdapterSelect adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_creating_playlist);

        init();
    }

    void init() {
        create = findViewById(R.id.createPlaylist);
        back = findViewById(R.id.addingBack);
        tracks = findViewById(R.id.addingTracks);
        playlistName = findViewById(R.id.playlistNameET);

        db = App.getDb();

        adapter = new TrackAdapterSelect(this, db.trackDao().getAll());
        tracks.setAdapter(adapter);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Playlist playlist = new Playlist(App.getPlaylistIndex(), playlistName.getText().toString());
                App.incPlaylistIndex();
                db.playlistDao().insert(playlist);
                List<Track> tracks = new ArrayList<>();

                for (int i = 0; i < App.getSelected().size(); i++) {
                    tracks.add(db.trackDao().getById(App.getSelectedIndex(i)));
                }

//                for (Track track : db.trackDao().getAll()) {
//                    if (track.isSelected()) {
//                        tracks.add(track);
//                        track.setSelected(false);
//                    }
//                }

                for (Track track : tracks) {
                    db.trackPlaylistDao().insert(
                            new TrackPlaylist(track.getId(), playlist.getId())
                    );
                    Log.d("testing", "selected");
                }

                App.addPlaylistIndex(playlist.getId());

                Log.d("testing", "playlist = " + playlist.getId() + " " + playlist.getName());

                List<TrackPlaylist> list = db.trackPlaylistDao().getAllByPlaylistId(playlist.getId());
                for (TrackPlaylist item : list) {
                    Log.d("testing", item.getTrackId() + " " + item.getPlaylistId());
                }

                finish();
            }
        });
    }
}
