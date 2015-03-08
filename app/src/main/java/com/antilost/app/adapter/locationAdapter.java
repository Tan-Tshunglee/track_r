package com.antilost.app.adapter;

import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.antilost.app.R;
import com.antilost.app.activity.MainTrackRListActivity;
import com.antilost.app.activity.TrackREditActivity;
import com.antilost.app.model.TrackR;
import com.antilost.app.service.BluetoothLeService;
import com.antilost.app.model.LocationBean;


import android.content.Context;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import android.widget.RelativeLayout;

/**
 * Created by Tan on 2015/1/17.
 */
public class LocationAdapter extends BaseAdapter implements View.OnClickListener {

    private static final String LOG_TAG = "locationAdapter";
    private  List<LocationBean> locationBeans;
    private Context context;

    public LocationAdapter(Context context, List<LocationBean> locationBeans) {
        this.locationBeans = locationBeans;
        this.context = context;
    }
    @Override
    public int getCount() {
        return  locationBeans.size();
    }

    @Override
    public Object getItem(int position) {
        return locationBeans.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.location_list, null);
        }
        bindView(convertView, position);
        return convertView;
    }

    private void bindView(View convertView, int position) {
        TextView titile = (TextView) convertView.findViewById(R.id.tvlocationtitle);
        TextView time = (TextView) convertView.findViewById(R.id.tvlocationtime);
        RelativeLayout relativeLayout = (RelativeLayout) convertView.findViewById(R.id.rllocation);
        LocationBean locationBean = locationBeans.get(position);
        titile.setText(locationBean.getmLocationName());
        time.setText(locationBean.getmLocationTime());
    }

    public void updateData() {

        notifyDataSetChanged();
    }

    @Override
    public void onClick(View v) {}

}
