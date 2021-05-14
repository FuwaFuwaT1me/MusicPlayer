package com.example.musicplayer.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.musicplayer.App;
import com.example.musicplayer.Player;
import com.example.musicplayer.R;
import com.example.musicplayer.database.AppDatabase;
import com.example.musicplayer.database.Radio;
import com.example.musicplayer.database.Track;
import com.example.musicplayer.notification.CreateNotification;

import java.util.ArrayList;
import java.util.List;

public class TrackAdapterSelect extends RecyclerView.Adapter<TrackAdapterSelect.ViewHolder> {
    private final List<Track> list = new ArrayList<>();
    TrackAdapterSelect.ViewHolder holder;
    Context context;
    Player player;
    AppDatabase db;

    public TrackAdapterSelect(Context context) {
        this.context = context;
        player = App.getApp().getPlayer();
        db = App.getApp().getDb();
    }

    public void setData(List<Track> mData) {
        list.clear();
        list.addAll(mData);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TrackAdapterSelect.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_select, parent, false);
        holder = new TrackAdapterSelect.ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.info.setText(list.get(position).getName());
        holder.info.setSelected(true);
        if (list.get(position).isPlaying()) holder.image.setImageResource(R.drawable.ic_play);
        else holder.image.setImageResource(R.drawable.ic_music);
        holder.box.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Track track = (Track) holder.box.getTag();
                track.setSelected(buttonView.isChecked());
                if (buttonView.isChecked()) {
                    if (!player.getSelected().contains(position)) player.addSelected(track.getId());
                }
                else {
                    player.removeSelected(track.getId());
                }
            }
        });
        holder.info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                context.stopService(App.getApp().getPlayerService());
                player.clearQueue();
                for (Track track : db.trackDao().getAll()) player.addToQueue(track);
                player.setCurrentQueueTrack(position);
                player.setIsPlaying(true);
                player.setIsAnotherSong(true);
                player.setSource(".");
                context.startService(App.getApp().getPlayerService());
                createTrackNotification(R.drawable.ic_pause);

                for (Track track : db.trackDao().getAll()) db.trackDao().updatePlaying(track.getId(), false);

                for (Track track : db.trackDao().getAll()) {
                    if (position == track.getId()) {
                        db.trackDao().updatePlaying(track.getId(), true);
                        track.setPlaying(true);
                    }
                }

                for (Radio radio : db.radioDao().getAll()) db.radioDao().updatePlaying(radio.getId(), false);

                setData(db.trackDao().getAll());
                notifyDataSetChanged();

                player.setIsShuffled(false);
            }
        });
        holder.box.setTag(list.get(position));
        holder.box.setChecked(player.getSelected().contains(position));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    void createTrackNotification(int index) {
        CreateNotification.createNotification(context.getApplicationContext(),
                player.getCurrentTrack(),
                index,
                player.getCurrentQueueTrack(),
                player.getQueueSize()-1);
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView info;
        CheckBox box;
        ImageView image;

        ViewHolder(View view) {
            super(view);
            this.info = view.findViewById(R.id.txtSongName);
            this.box = view.findViewById(R.id.checkbox);
            this.image = view.findViewById(R.id.imgSong);
        }
    }
}
