package com.example.musicplayer.music;

import android.os.Bundle;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.musicplayer.App;
import com.example.musicplayer.R;
import com.example.musicplayer.adapter.TrackAdapter;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class RadioActivity extends AppCompatActivity {
    Button setRadioButton, backButton;
    EditText radioUrl, radioTitle;
    ListView radioList;
    List<String> radios = new ArrayList<>();
    List<String> urls = new ArrayList<>();
    ArrayAdapter<String> radioAdapter;
    TrackAdapter adapter;
    int currPosition;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_radio);

        setRadioButton = findViewById(R.id.setRadioButton);
        radioUrl = findViewById(R.id.radioUrl);
        radioList = findViewById(R.id.radioList);
        backButton = findViewById(R.id.backButton);
        radioTitle = findViewById(R.id.radioTitle);

        radios.clear();
        for (int i = 0; i < App.getRadioListSize(); i++) {
            radios.add(App.getRadio(i).getTitle());
            urls.add(App.getRadio(i).getPath());
        }
        //radioAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, radios);
        adapter = new TrackAdapter();
        adapter.setData(App.getRadioList());
        radioList.setAdapter(adapter);
        registerForContextMenu(radioList);

        setRadioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    URL url = new URL(radioUrl.getText().toString());
                    if (!url.toString().startsWith("http://") && url.toString().startsWith("http")
                            || !url.toString().startsWith("https://") && url.toString().startsWith("https"))
                        throw new MalformedURLException("Неверный формат URL");
                    stopService(App.getPlayerService());
                    App.setSource(url.toString());
                    radios.add(radioTitle.getText().toString());
                    urls.add(radioUrl.getText().toString());
                    App.addRadio(new Track(radioTitle.getText().toString(), radioUrl.getText().toString()));
                    currPosition = App.getRadioListSize() - 1;
                    adapter.setData(App.getRadioList());
                    adapter.notifyDataSetChanged();
                    startService(App.getPlayerService());
                } catch (MalformedURLException e) {
                    Toast.makeText(RadioActivity.this, "Неверный формат URL", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }
        });
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        radioList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (currPosition == position) return;
                stopService(App.getPlayerService());
                App.setSource(urls.get(position));
                startService(App.getPlayerService());
                currPosition = position;
            }
        });
        radioList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                currPosition = position;
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
        if (item.getTitle().equals("Удалить")) {
            radios.remove(currPosition);
            urls.remove(currPosition);
            App.removeRadio(currPosition);
            Toast.makeText(this, "" + currPosition, Toast.LENGTH_SHORT).show();
            adapter.setData(App.getRadioList());
            adapter.notifyDataSetChanged();
        }
        return true;
    }
}