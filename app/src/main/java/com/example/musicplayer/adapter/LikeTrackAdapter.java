package com.example.musicplayer.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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

public class LikeTrackAdapter extends RecyclerView.Adapter<LikeTrackAdapter.ViewHolder> {
    private List<Track> data = new ArrayList<>();
    Context context;
    Player player;
    AppDatabase db;

    public LikeTrackAdapter(Context context) {
        this.context = context;
        player = App.getApp().getPlayer();
        db = App.getApp().getDb();
    }

    public void setData(List<Track> mData) {
        data.clear();
        data.addAll(mData);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_like, parent, false);
        LikeTrackAdapter.ViewHolder holder = new LikeTrackAdapter.ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.info.setText(data.get(position).getName());
        holder.info.setSelected(true);

        if (data.get(position).isPlaying()) holder.image.setImageResource(R.drawable.ic_play);
        else holder.image.setImageResource(R.drawable.ic_music);

        if (data.get(position).isLiked()) holder.like.setBackgroundResource(R.drawable.ic_liked);
        else holder.like.setBackgroundResource(R.drawable.ic_unliked);

        holder.info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (player.isPlaying()) context.stopService(App.getApp().getPlayerService());
                player.setSource(".");
                player.setIsAnotherSong(true);

                player.clearQueue();;

                for (Track track : db.trackDao().getAll()) {
                    db.trackDao().updatePlaying(track.getId(), false);
                    if (track.getId() == position) db.trackDao().updatePlaying(track.getId(), true);

                    player.addToQueue(track);
                }

                setData(db.trackDao().getAll());
                notifyDataSetChanged();

                player.setCurrentQueueTrack(position);
                player.setSource(".");
                context.startService(App.getApp().getPlayerService());
                createTrackNotification(R.drawable.ic_pause);

                player.setIsShuffled(false);
            }
        });
        holder.like.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!data.get(position).isLiked()) {
                    holder.like.setBackgroundResource(R.drawable.ic_liked);
                    db.trackDao().updateLiked(data.get(position).getId(), true);
                }
                else {
                    holder.like.setBackgroundResource(R.drawable.ic_unliked);
                    db.trackDao().updateLiked(data.get(position).getId(), false);
                }

                for (Radio radio : db.radioDao().getAll()) db.radioDao().updatePlaying(radio.getId(), false);


                setData(db.trackDao().getAll());
                notifyDataSetChanged();
            }
        });
    }

    void createTrackNotification(int index) {
        CreateNotification.createNotification(context.getApplicationContext(),
                player.getCurrentTrack(),
                index,
                player.getCurrentQueueTrack(),
                player.getQueueSize()-1);
    }

    @Override
    public long getItemId(int position) {
        return data.get(position).getId();
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView info;
        ImageView image;
        Button like;

        ViewHolder(View view) {
            super(view);
            this.info = view.findViewById(R.id.txtSongName);
            this.image = view.findViewById(R.id.imgSong);
            this.like = view.findViewById(R.id.likeButton);
        }
    }
}
