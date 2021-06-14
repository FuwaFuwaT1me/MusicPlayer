package com.example.musicplayer.activities.selectingSongs;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.musicplayer.activities.playlist.PlaylistActivity;
import com.example.musicplayer.activities.playlistview.PlaylistViewActivity;
import com.example.musicplayer.app.App;
import com.example.musicplayer.color.AppColor;
import com.example.musicplayer.database.entities.TrackPlaylist;
import com.example.musicplayer.player.Player;
import com.example.musicplayer.R;
import com.example.musicplayer.database.AppDatabase;
import com.example.musicplayer.database.entities.Radio;
import com.example.musicplayer.database.entities.Track;
import com.example.musicplayer.notification.CreateNotification;

import java.util.ArrayList;
import java.util.List;

public class TrackAdapterSelect extends RecyclerView.Adapter<TrackAdapterSelect.ViewHolder> {
    private final List<Track> list = new ArrayList<>();
    TrackAdapterSelect.ViewHolder holder;
    Context context;
    Player player;
    AppDatabase db;
    AppColor appColor;

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
        appColor = App.getApp().getAppColor();

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_select, parent, false);
        holder = new TrackAdapterSelect.ViewHolder(view);
        return holder;
    }

    @SuppressLint({"UseCompatLoadingForColorStateLists", "NewApi"})
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.info.setText(list.get(position).getName());
        holder.info.setSelected(true);

        if (list.get(position).isPlaying()) holder.image.setImageResource(appColor.getPlayColor());
        else holder.image.setImageResource(R.drawable.ic_music);

        holder.box.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Track track = (Track) holder.box.getTag();
                track.setSelected(buttonView.isChecked());
                if (buttonView.isChecked()) {
                    if (!player.getSelected().contains(position)) {
                        player.addSelected(track.getId());
                        db.trackDao().updateSelected(track.getId(), true);
                    }
                }
                else {
                    player.removeSelected(track.getId());
                    db.trackDao().updateSelected(track.getId(), false);
                }
            }
        });
        holder.info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                context.stopService(App.getApp().getPlayerService());
                player.clearQueue();
                List<Track> reducedTracks = new ArrayList<>();

                if (App.getApp().getLastCondition() == 1) {
                    Log.d("testing", "in added");
                    List<TrackPlaylist> temp = db.trackPlaylistDao().getAllByPlaylistId(player.getPlaylistToView());
                    List<Integer> alreadyExistTracks = new ArrayList<>();
                    for (TrackPlaylist t : temp) {
                        Log.d("testing", "temp : " + t.getTrackId());
                        alreadyExistTracks.add(t.getTrackId());
                    }

                    for (Track track : db.trackDao().getAll()) {
                        if (!alreadyExistTracks.contains(track.getId())) {
                            reducedTracks.add(track);
                            player.addToQueue(track);
                        }
                        else Log.d("testing", ""+track.getName());
                    }
                }
                else if (App.getApp().getLastCondition() == 0) for (Track track : db.trackDao().getAll()) player.addToQueue(track);

                player.setCurrentQueueTrack(position);
                Log.d("testing", "qeueu = " + player.getCurrentQueueTrack());
                player.setIsPlaying(true);
                player.setIsAnotherSong(true);
                player.setSource(".");
                context.startService(App.getApp().getPlayerService());
                createTrackNotification(appColor.getPauseColor());

                for (Track track : db.trackDao().getAll()) db.trackDao().updatePlaying(track.getId(), false);

                if (App.getApp().getLastCondition() == 0) {
                    for (Track track : db.trackDao().getAll()) {
                        if (position == track.getId()) {
                            db.trackDao().updatePlaying(track.getId(), true);
                            track.setPlaying(true);
                        }
                    }
                }

                for (Track track : reducedTracks) track.setPlaying(false);

                if (App.getApp().getLastCondition() == 1) {
                    for (Track track : reducedTracks) {
                        if (player.getCurrentTrack().getId() == track.getId()) {
                            track.setPlaying(true);
                            db.trackDao().updatePlaying(track.getId(), true);
                        }
                    }
                }

                 for (Radio radio : db.radioDao().getAll()) db.radioDao().updatePlaying(radio.getId(), false);

                 if (App.getApp().getLastCondition() == 0) setData(db.trackDao().getAll());
                 else if (App.getApp().getLastCondition() == 1) setData(reducedTracks);
                 notifyDataSetChanged();

                player.setIsShuffled(false);
            }
        });
        holder.box.setTag(list.get(position));
        holder.box.setChecked(player.getSelected().contains(position));

        holder.image.setBackgroundResource(appColor.getBgColor());
        holder.box.setButtonTintList(context.getResources().getColorStateList(appColor.getColorCode()));
        holder.layout.setBackgroundResource(appColor.getBgColor());
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    void createTrackNotification(int index) {
        CreateNotification.createNotification(context.getApplicationContext(),
                index,
                player.getCurrentQueueTrack(),
                player.getQueueSize()-1);
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView info;
        CheckBox box;
        ImageView image;
        RelativeLayout layout;

        ViewHolder(View view) {
            super(view);
            this.info = view.findViewById(R.id.txtSongName);
            this.box = view.findViewById(R.id.checkbox);
            this.image = view.findViewById(R.id.imgSong);
            this.layout = view.findViewById(R.id.createLayout);
        }
    }
}
