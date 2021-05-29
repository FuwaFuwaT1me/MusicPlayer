package com.example.musicplayer.activities.playlist;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.musicplayer.app.App;
import com.example.musicplayer.color.AppColor;
import com.example.musicplayer.R;
import com.example.musicplayer.database.entities.Playlist;

import java.util.ArrayList;
import java.util.List;

public class PlaylistAdapter extends BaseAdapter {
    private List<Playlist> data = new ArrayList<>();
    AppColor appColor;

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
        appColor = App.getApp().getAppColor();

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

        holder.layout.setBackgroundResource(appColor.getBgColor());
        holder.image.setBackgroundResource(appColor.getBgColor());

        return convertView;
    }

    class ViewHolder {
        TextView info;
        RelativeLayout layout;
        ImageView image;

        ViewHolder(View view) {
            this.info = view.findViewById(R.id.txtSongName);
            this.layout = view.findViewById(R.id.trackLayout);
            this.image = view.findViewById(R.id.imgSong);
        }
    }
}
