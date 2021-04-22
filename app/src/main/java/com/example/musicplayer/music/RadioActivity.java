package com.example.musicplayer.music;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.musicplayer.App;
import com.example.musicplayer.MainActivity;
import com.example.musicplayer.R;
import com.example.musicplayer.Services.OnClearFromRecentService;
import com.example.musicplayer.adapter.TrackAdapter;
import com.example.musicplayer.notification.CreateNotification;
import com.example.musicplayer.notification.Playable;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class RadioActivity extends AppCompatActivity implements Playable {
    Button setRadioButton, backButton;
    EditText radioUrl, radioTitle;
    ListView radioList;
    TrackAdapter adapter;
    Button play, prev, next;
    TextView title;
    NotificationManager notificationManager;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_radio);

        setRadioButton = findViewById(R.id.setRadioButton);
        radioUrl = findViewById(R.id.radioUrl);
        radioList = findViewById(R.id.radioList);
        backButton = findViewById(R.id.backButton);
        radioTitle = findViewById(R.id.radioTitle);
        play = findViewById(R.id.play);
        title = findViewById(R.id.songName);
        prev = findViewById(R.id.previous);
        next = findViewById(R.id.next);

        if (App.isPlaying()) {
            play.setBackgroundResource(R.drawable.ic_pause);
        } else {
            play.setBackgroundResource(R.drawable.ic_play);
        }

        if (!App.getSource().equals(".") && App.getCurrentRadio() != -1) {
            title.setText(App.getCurrentRadioTrack().getTitle());
        }
        else if (App.getCurrentSong() != -1) {
            title.setText(App.getCurrentTitle());
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannel();
            registerReceiver(broadcastReceiver, new IntentFilter("TRACKS_TRACKS"));
            startService(new Intent(getBaseContext(), OnClearFromRecentService.class));
        }

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
                    App.setSource(url.toString());;
                    App.addRadio(new Track(radioTitle.getText().toString(), radioUrl.getText().toString()));
                    App.setCurrentRadio(App.getRadioListSize() - 1);
                    adapter.setData(App.getRadioList());
                    adapter.notifyDataSetChanged();
                    title.setText(App.getCurrentRadioTrack().getTitle());
                    App.setIsPlaying(true);
                    App.setIsAnotherSong(true);
                    play.setBackgroundResource(R.drawable.ic_pause);
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
                if (App.getCurrentRadio() == position) return;
                App.setCurrentRadio(position);
                stopService(App.getPlayerService());
                App.setIsPlaying(true);
                App.setIsAnotherSong(true);
                App.setSource(App.getCurrentRadioTrack().getPath());
                play.setBackgroundResource(R.drawable.ic_pause);
                title.setText(App.getCurrentRadioTrack().getTitle());
                startService(App.getPlayerService());
                CreateNotification.createNotification(RadioActivity.this,
                        App.getCurrentRadioTrack(),
                        R.drawable.ic_pause, 1, App.getRadioListSize()-1);
            }
        });
        radioList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                App.setCurrentRadio(position);
                return false;
            }
        });
        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (App.isPlaying()) {
                    if (App.getSource().equals(".")) {
                        CreateNotification.createNotification(getApplicationContext(),
                                App.getCurrentTrack(),
                                R.drawable.ic_play,
                                App.getCurrentSong(),
                                App.getTrackListSize()-1);
                    }
                    else {
                        CreateNotification.createNotification(getApplicationContext(),
                                App.getCurrentRadioTrack(),
                                R.drawable.ic_play,
                                App.getCurrentRadio(),
                                App.getRadioListSize() - 1);
                    }

                    App.setIsPlaying(false);
                    App.setIsAnotherSong(false);
                    play.setBackgroundResource(R.drawable.ic_play);
                    stopService(App.getPlayerService());
                } else {
                    if (App.getSource().equals(".")) {
                        CreateNotification.createNotification(getApplicationContext(),
                                App.getCurrentTrack(),
                                R.drawable.ic_pause,
                                App.getCurrentSong(),
                                App.getTrackListSize()-1);
                    }
                    else {
                        CreateNotification.createNotification(getApplicationContext(),
                                App.getCurrentRadioTrack(),
                                R.drawable.ic_pause,
                                App.getCurrentRadio(),
                                App.getRadioListSize() - 1);
                    }

                    App.setIsPlaying(true);
                    App.setIsAnotherSong(false);
                    play.setBackgroundResource(R.drawable.ic_pause);
                    startService(App.getPlayerService());
                }
            }
        });
        prev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (App.getSource().equals(".") && App.getCurrentSong() - 1 >= 0) {
                    App.setWasSongSwitched(true);
                    App.setCurrentSong(App.getCurrentSong()+1);
                    stopService(App.getPlayerService());
                    App.setIsAnotherSong(true);
                    title.setText(App.getCurrentTitle());
                    startService(App.getPlayerService());

                    CreateNotification.createNotification(getApplicationContext(),
                            App.getCurrentTrack(),
                            R.drawable.ic_play,
                            App.getCurrentSong(),
                            App.getTrackListSize()-1);
                }
                else if (App.getCurrentRadio() - 1 >= 0) {
                    stopService(App.getPlayerService());
                    App.setCurrentRadio(App.getCurrentRadio()-1);
                    App.setSource(App.getCurrentRadioTrack().getPath());
                    App.setIsAnotherSong(true);
                    App.setWasSongSwitched(true);
                    title.setText(App.getCurrentRadioTrack().getTitle());
                    startService(App.getPlayerService());

                    CreateNotification.createNotification(getApplicationContext(),
                            App.getCurrentRadioTrack(),
                            R.drawable.ic_pause,
                            App.getCurrentRadio(),
                            App.getRadioListSize()-1);
                }
            }
        });
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (App.getSource().equals(".") && App.getCurrentSong() + 1 < App.getTrackListSize()) {
                    App.setWasSongSwitched(true);
                    App.setCurrentSong(App.getCurrentSong()-1);
                    stopService(App.getPlayerService());
                    App.setIsAnotherSong(true);
                    title.setText(App.getCurrentTitle());
                    startService(App.getPlayerService());

                    CreateNotification.createNotification(getApplicationContext(),
                            App.getCurrentTrack(),
                            R.drawable.ic_play,
                            App.getCurrentSong(),
                            App.getTrackListSize()-1);
                }
                else if (App.getCurrentRadio() +1 < App.getRadioListSize()) {
                    stopService(App.getPlayerService());
                    App.setCurrentRadio(App.getCurrentRadio() + 1);
                    App.setSource(App.getCurrentRadioTrack().getPath());
                    App.setIsAnotherSong(true);
                    App.setWasSongSwitched(true);
                    title.setText(App.getCurrentRadioTrack().getTitle());
                    startService(App.getPlayerService());

                    CreateNotification.createNotification(getApplicationContext(),
                            App.getCurrentRadioTrack(),
                            R.drawable.ic_pause,
                            App.getCurrentRadio(),
                            App.getRadioListSize() - 1);
                }
            }
        });
    }

    private void createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CreateNotification.CHANNEL_ID,
                    "KOD DEV", NotificationManager.IMPORTANCE_LOW);

            notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.context_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        if (item.getTitle().equals("Delete")) {
            App.removeRadio(App.getCurrentRadio());
            adapter.setData(App.getRadioList());
            adapter.notifyDataSetChanged();
        }
        return true;
    }

    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getExtras().getString("actionName");

            switch (action) {
                case CreateNotification.ACTION_PREVIOUS:
                    onTrackPrevious();
                    break;
                case CreateNotification.ACTION_PLAY:
                    if (!App.isPlaying()) onTrackPause();
                    else onTrackPlay();
                    break;
                case CreateNotification.ACTION_NEXT:
                    onTrackNext();
                    break;
            }
        }
    };

    @Override
    public void onTrackPrevious() {
        title.setText(App.getCurrentRadioTrack().getTitle());
        play.setBackgroundResource(R.drawable.ic_pause);
    }

    @Override
    public void onTrackPlay() {
        play.setBackgroundResource(R.drawable.ic_pause);
    }

    @Override
    public void onTrackPause() {
        play.setBackgroundResource(R.drawable.ic_play);
    }

    @Override
    public void onTrackNext() {
        title.setText(App.getCurrentRadioTrack().getTitle());
        play.setBackgroundResource(R.drawable.ic_pause);
    }
}