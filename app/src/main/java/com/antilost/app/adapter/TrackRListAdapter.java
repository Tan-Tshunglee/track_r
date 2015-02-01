package com.antilost.app.adapter;

import android.bluetooth.BluetoothProfile;
import android.content.Intent;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.antilost.app.R;
import com.antilost.app.activity.MainTrackRListActivity;
import com.antilost.app.activity.TrackRActivity;
import com.antilost.app.activity.TrackREditActivity;
import com.antilost.app.model.TrackR;
import com.antilost.app.prefs.PrefsManager;
import com.antilost.app.service.BluetoothLeService;

import java.util.ArrayList;

/**
 * Created by Tan on 2015/1/17.
 */
public class TrackRListAdapter extends BaseAdapter implements View.OnClickListener {

    private final LayoutInflater mInflater;
    private final PrefsManager mPrefs;
    private final MainTrackRListActivity mActivity;
    private ArrayList<String> mIds;

    public TrackRListAdapter(MainTrackRListActivity activity, PrefsManager prefs) {
        mInflater = activity.getLayoutInflater();
        mActivity = activity;
        mPrefs = prefs;
    }
    @Override
    public int getCount() {
        if(mIds == null) {
            return 0;
        } else {
            return mIds.size();
        }
    }

    @Override
    public Object getItem(int position) {
        return mIds.get(position);
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

        bindView(convertView, position);
        return convertView;
    }

    private void bindView(View convertView, int position) {
        TextView type = (TextView) convertView.findViewById(R.id.trackType);
        TextView state = (TextView) convertView.findViewById(R.id.trackRState);
        TextView last = (TextView) convertView.findViewById(R.id.lastTimeAndLocation);
        ImageButton icon = (ImageButton) convertView.findViewById(R.id.icon);

        String address = mIds.get(position);
        TrackR track = mPrefs.getTrack(address);
        if(track == null) {
            track = new TrackR();
        }

        if(!TextUtils.isEmpty(track.name)) {
            type.setText(track.name);
        } else {
            String[] names = convertView.getResources().getStringArray(R.array.default_type_names);
            type.setText(names[track.type]);
        }

        if(TextUtils.isEmpty(track.path)) {
            icon.setImageResource(TrackREditActivity.DrawableIds[track.type]);
        }

        icon.setTag(position);
        icon.setOnClickListener(this);
        BluetoothLeService service = mActivity.getBluetoothLeService();
        if(service != null) {
            int stateValue =  service.getGattConnectState(address);
            state.setText(getString(stateValue));
        }



    }

    private String getString(int stateValue) {
        if(stateValue == BluetoothProfile.STATE_CONNECTED) {
            return "Connected";
        } else if(stateValue == BluetoothProfile.STATE_DISCONNECTED) {
            return "Disconnected";
        } else if(stateValue == BluetoothProfile.STATE_CONNECTING) {
            return "Connecting";
        } else {
            return "Disconnecting";
        }
    }

    private String getTypeName() {
        return "";
    }

    public void updateData() {
        mIds = new ArrayList<String>(mPrefs.getTrackIds());
        notifyDataSetChanged();
    }

    @Override
    public void onClick(View v) {
        int position = (Integer) v.getTag();
        String address = (String) getItem(position);
        if(mActivity.isBluetoothConnected(address)) {
            Intent i = new Intent(mActivity, TrackRActivity.class);
            i.putExtra(TrackRActivity.BLUETOOTH_ADDRESS_BUNDLE_KEY, address);
            mActivity.startActivity(i);
        } else {
            Toast.makeText(mActivity, "Device Not Connected", Toast.LENGTH_SHORT).show();
        };

    }

}
