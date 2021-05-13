package com.example.musicplayer.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.musicplayer.App;
import com.example.musicplayer.Player;
import com.example.musicplayer.R;
import com.example.musicplayer.database.Track;

import java.util.ArrayList;
import java.util.List;

public class LikeTrackAdapter extends RecyclerView.Adapter<LikeTrackAdapter.ViewHolder> {
    private List<Track> data = new ArrayList<>();

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
