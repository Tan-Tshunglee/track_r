package com.antilost.app.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.antilost.app.R;

/**
 * Created by Tan on 2015/1/17.
 */
public class TrackRListAdapter extends BaseAdapter {

    private final LayoutInflater mInflater;

    public TrackRListAdapter(LayoutInflater inflater) {
        mInflater = inflater;
    }
    @Override
    public int getCount() {
        return 4;
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
        if(convertView == null) {
            convertView = mInflater.inflate(R.layout.track_r_listview_item_layout, parent, false);
        }

        return convertView;
    }

    public void updateData() {

    }
}
