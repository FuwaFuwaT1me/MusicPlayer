package com.example.musicplayer.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.musicplayer.R;
import com.example.musicplayer.database.Playlist;
import com.example.musicplayer.database.Track;

import java.util.ArrayList;
import java.util.List;

public class PlaylistAdapter extends BaseAdapter {
    private List<Playlist> data = new ArrayList<>();

    public void setData(List<Playlist> mData) {
        data.clear();
        data.addAll(mData);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        PlaylistAdapter.ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).
                    inflate(R.layout.list_item, parent, false);
            holder = new PlaylistAdapter.ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (PlaylistAdapter.ViewHolder) convertView.getTag();
        }
        holder.info.setText(data.get(position).getName());
        holder.info.setSelected(true);

        return convertView;
    }

    class ViewHolder {
        TextView info;

        ViewHolder(View view) {
            this.info = view.findViewById(R.id.txtSongName);
        }
    }
}
