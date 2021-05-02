package com.example.musicplayer.music;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.musicplayer.App;
import com.example.musicplayer.R;
import com.example.musicplayer.adapter.TrackAdapter;
import com.example.musicplayer.database.AppDatabase;
import com.example.musicplayer.database.Track;
import com.example.musicplayer.database.TrackPlaylist;

import java.util.ArrayList;
import java.util.List;

public class PlaylistViewActivity extends AppCompatActivity {
    TextView playlistName;
    ListView tracks;
    Button back;
    TrackAdapter adapter;
    AppDatabase db;
    List<Track> trackList = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist_view);

        init();
    }

    void init() {
        playlistName = findViewById(R.id.playlistName);
        tracks = findViewById(R.id.playlistTracks);
        back = findViewById(R.id.playlistTracksBack);

        db = App.getDb();

        adapter = new TrackAdapter();
        adapter.setData(getPlaylistTracks(App.getPlaylistToView()));
        tracks.setAdapter(adapter);

        playlistName.setText(db.playlistDao().getById(App.getPlaylistToView()).getName());

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        tracks.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                stopService(App.getPlayerService());
                App.clearQueue();
                for (Track track : trackList) App.addToQueue(track);
                App.setCurrentSong(position);
                App.setIsPlaying(true);
                App.setIsAnotherSong(true);
                App.setSource(".");
                startService(App.getPlayerService());
            }
        });
    }

    List<Track> getPlaylistTracks(int playlistId) {
        List<TrackPlaylist> temp = db.trackPlaylistDao().getAllByPlaylistId(playlistId);

        for (TrackPlaylist item : temp) {
            trackList.add(db.trackDao().getById(item.getTrackId()));
        }

        return trackList;
    }
}
