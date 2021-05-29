package com.example.musicplayer.activities.playlistview;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.Adapter;

import com.example.musicplayer.app.App;
import com.example.musicplayer.color.AppColor;
import com.example.musicplayer.R;
import com.example.musicplayer.database.entities.Track;

import java.util.ArrayList;
import java.util.List;

public class TrackAdapter extends Adapter<TrackAdapter.ViewHolder> {
    private List<Track> data = new ArrayList<>();
    AppColor appColor;

    public void setData(List<Track> mData) {
        data.clear();
        data.addAll(mData);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);
        TrackAdapter.ViewHolder holder = new TrackAdapter.ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        appColor = App.getApp().getAppColor();

        holder.info.setText(data.get(position).getName());
        holder.info.setSelected(true);

        if (data.get(position).isPlaying()) holder.image.setImageResource(appColor.getPlayColor());
        else holder.image.setImageResource(R.drawable.ic_music);

        holder.image.setBackgroundResource(appColor.getBgColor());
        holder.layout.setBackgroundResource(appColor.getBgColor());
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
        RelativeLayout layout;

        ViewHolder(View view) {
            super(view);
            this.info = view.findViewById(R.id.txtSongName);
            this.image = view.findViewById(R.id.imgSong);
            this.layout = view.findViewById(R.id.trackLayout);
        }
    }
}
