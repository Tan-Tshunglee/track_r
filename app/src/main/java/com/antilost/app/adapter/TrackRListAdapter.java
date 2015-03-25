package com.antilost.app.adapter;

import android.bluetooth.BluetoothProfile;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.antilost.app.R;
import com.antilost.app.activity.MainTrackRListActivity;
import com.antilost.app.activity.TrackREditActivity;
import com.antilost.app.model.TrackR;
import com.antilost.app.prefs.PrefsManager;
import com.antilost.app.service.BluetoothLeService;
import com.antilost.app.util.CsstSHImageData;

import java.util.ArrayList;

/**
 * Created by Tan on 2015/1/17.
 */
public class TrackRListAdapter extends BaseAdapter implements View.OnClickListener {

    private static final String LOG_TAG = "TrackRListAdapter";
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

        Uri customIconUri = CsstSHImageData.getIconImageUri(address);

        if(customIconUri != null) {
            icon.setImageURI(customIconUri);
            icon.setScaleType(ImageView.ScaleType.FIT_XY);
        } else {
            icon.setImageResource(TrackREditActivity.DrawableIds[track.type]);
            icon.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        }


        icon.setTag(position);
        icon.setOnClickListener(this);
        BluetoothLeService service = mActivity.getBluetoothLeService();

        if (mPrefs.isClosedTrack(address)) {
            state.setText(mActivity.getString(R.string.closed));
            state.setTextColor(Color.LTGRAY);
            icon.setBackgroundResource(R.drawable.closed_icon_bkg);
        } else {
            if(service != null) {
                int stateValue =  service.getGattConnectState(address);
                state.setText(getString(stateValue));
                switch (stateValue) {
                    case BluetoothProfile.STATE_DISCONNECTED:
                        icon.setBackgroundResource(R.drawable.disconnected_icon_bkg);
                        state.setTextColor(Color.RED);
                        break;
                    case BluetoothProfile.STATE_CONNECTED:
                        icon.setBackgroundResource(R.drawable.connected_icon_bkg);
                        state.setTextColor(Color.GREEN);
                        break;
                    case BluetoothProfile.STATE_CONNECTING:
                    case BluetoothProfile.STATE_DISCONNECTING:
                        icon.setBackgroundResource(R.drawable.connecting_icon_bkg);
                        state.setTextColor(Color.RED);
                        break;
                }
            } else {
                Log.v(LOG_TAG, "service is null");
                icon.setBackgroundResource(R.drawable.closed_icon_bkg);
                state.setText(mActivity.getString(R.string.disconnected));
                state.setTextColor(Color.RED);
            }
        }
    }

    private String getString(int stateValue) {
        if(stateValue == BluetoothProfile.STATE_CONNECTED) {
            return mActivity.getString(R.string.connected);
        } else if(stateValue == BluetoothProfile.STATE_DISCONNECTED) {
            return mActivity.getString(R.string.disconnected);
        } else if(stateValue == BluetoothProfile.STATE_CONNECTING) {
            return mActivity.getString(R.string.connecting);
        } else {
            return mActivity.getString(R.string.disconnecting)
                    ;
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
        Intent i = new Intent(mActivity, TrackREditActivity.class);
        i.putExtra(TrackREditActivity.BLUETOOTH_ADDRESS_BUNDLE_KEY, address);
        mActivity.startActivity(i);

    }

}
