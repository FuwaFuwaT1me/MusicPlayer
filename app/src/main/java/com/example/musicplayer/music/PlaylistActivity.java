package com.example.musicplayer.music;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.musicplayer.App;
import com.example.musicplayer.R;
import com.example.musicplayer.adapter.PlaylistAdapter;
import com.example.musicplayer.database.AppDatabase;
import com.example.musicplayer.database.Playlist;

import java.util.List;

public class PlaylistActivity extends AppCompatActivity {
    Button back, add;
    ListView playlists;
    PlaylistAdapter adapter;
    AppDatabase db;
    int selectedPlaylist;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist);

        init();
    }

    void init() {
        back = findViewById(R.id.playlistBackButton);
        add = findViewById(R.id.addPlaylistButton);
        playlists = findViewById(R.id.playlists);

        db = App.getDb();

        adapter = new PlaylistAdapter();
        adapter.setData(db.playlistDao().getAll());
        playlists.setAdapter(adapter);
        registerForContextMenu(playlists);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PlaylistActivity.this, CreatingPlaylistActivity.class);
                startActivity(intent);
            }
        });
        playlists.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                App.setPlaylistToView(App.getPlaylistIndexById(position));
                Intent intent = new Intent(PlaylistActivity.this, PlaylistViewActivity.class);
                startActivity(intent);
            }
        });
        playlists.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                selectedPlaylist = position;
                return false;
            }
        });
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.context_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.deleteContextMenu) {
            App.clearPlaylistIndexes();
            List<Playlist> list = db.playlistDao().getAll();
            for (Playlist playlist : list) {
                App.addPlaylistIndex(playlist.getId());
            }

            db.playlistDao().delete(App.getPlaylistIndexById(selectedPlaylist));
            App.removePlaylistIndex(selectedPlaylist);
            adapter.setData(db.playlistDao().getAll());
            adapter.notifyDataSetChanged();
        }
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        adapter.setData(db.playlistDao().getAll());
        adapter.notifyDataSetChanged();
    }
}
