package com.example.musicplayer.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.example.musicplayer.App;
import com.example.musicplayer.R;
import com.example.musicplayer.database.Track;

import java.util.List;

public class TrackAdapterSelect extends ArrayAdapter<Track> {
    private final List<Track> list;
    private final Activity context;

    public TrackAdapterSelect(Activity context, List<Track> list) {
        super(context, R.layout.list_item_select, list);

        this.context = context;
        this.list = list;
    }

    static class ViewHolder {
        protected TextView text;
        protected CheckBox checkBox;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = null;
        if (convertView == null) {
            LayoutInflater inflater = context.getLayoutInflater();
            view = inflater.inflate(R.layout.list_item_select, null);
            final TrackAdapterSelect.ViewHolder viewHolder = new TrackAdapterSelect.ViewHolder();
            viewHolder.text = view.findViewById(R.id.txtSongName);
            viewHolder.checkBox = view.findViewById(R.id.checkbox);
            viewHolder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    Track track = (Track) viewHolder.checkBox.getTag();
                    track.setSelected(buttonView.isChecked());
                    if (buttonView.isChecked()) {
                        App.getApp().getPlayer().addSelected(track.getId());
                    }
                    else {
                        App.getApp().getPlayer().removeSelected(track.getId());
                    }
                }
            });
            view.setTag(viewHolder);
            viewHolder.checkBox.setTag(list.get(position));
        }
        else {
            view = convertView;
            ((TrackAdapterSelect.ViewHolder) view.getTag()).checkBox.setTag(list.get(position));
        }
        TrackAdapterSelect.ViewHolder holder = (TrackAdapterSelect.ViewHolder) view.getTag();
        holder.text.setText(list.get(position).getName());
        holder.checkBox.setChecked(list.get(position).isSelected());
        return view;
    }
}
