package com.example.musicplayer.activities.liked;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.musicplayer.app.App;
import com.example.musicplayer.color.AppColor;
import com.example.musicplayer.player.Player;
import com.example.musicplayer.R;
import com.example.musicplayer.database.AppDatabase;
import com.example.musicplayer.database.entities.Radio;
import com.example.musicplayer.database.entities.Track;
import com.example.musicplayer.notification.CreateNotification;

import java.util.ArrayList;
import java.util.List;

public class LikeTrackAdapter extends RecyclerView.Adapter<LikeTrackAdapter.ViewHolder> {
    private List<Track> data = new ArrayList<>();
    Context context;
    Player player;
    AppDatabase db;
    AppColor appColor;

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
        appColor = App.getApp().getAppColor();

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_like, parent, false);

        LikeTrackAdapter.ViewHolder holder = new LikeTrackAdapter.ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.info.setText(data.get(position).getName());
        holder.info.setSelected(true);

        if (data.get(position).isPlaying()) {
            holder.image.setImageResource(appColor.getPlayColor());
        }
        else holder.image.setImageResource(R.drawable.ic_music);

        if (data.get(position).isLiked()) holder.like.setBackgroundResource(appColor.getLikedColor());
        else holder.like.setBackgroundResource(appColor.getUnlikedColor());

        holder.image.setBackgroundResource(appColor.getBgColor());
        holder.layout.setBackgroundResource(appColor.getBgColor());

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
                createTrackNotification(R.drawable.ic_pause_red);

                player.setIsShuffled(false);
            }
        });
        holder.like.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!data.get(position).isLiked()) {
                    holder.like.setBackgroundResource(appColor.getLikedColor());

                    db.trackDao().updateLiked(data.get(position).getId(), true);
                }
                else {
                    holder.like.setBackgroundResource(appColor.getUnlikedColor());

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
        RelativeLayout layout;

        ViewHolder(View view) {
            super(view);
            this.info = view.findViewById(R.id.txtSongName);
            this.image = view.findViewById(R.id.imgSong);
            this.like = view.findViewById(R.id.likeButton);
            this.layout = view.findViewById(R.id.likeLayout);
        }
    }
}
