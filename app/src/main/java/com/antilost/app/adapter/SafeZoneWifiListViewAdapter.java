package com.antilost.app.adapter;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.antilost.app.R;


/**
 * @author zql
 */
public class SafeZoneWifiListViewAdapter extends BaseAdapter {
    private Context context = null;
    private List<String> wifilist = null;
    private String ssidSelected;

    public SafeZoneWifiListViewAdapter(Context context, List<String> wifilist, String selected) {
        this.context = context;
        this.wifilist = wifilist;
        this.ssidSelected = selected;
    }


    public final void updateSsidList(List<String> wifilist) {
        this.wifilist = wifilist;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return null == wifilist ? 0 : wifilist.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        if(convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.safezonewifilistview, null);
        }

        String wifi = wifilist.get(position);

        TextView tvwifiname = (TextView) convertView.findViewById(R.id.tvwifiname);
        ImageView imgselect = (ImageView) convertView.findViewById(R.id.imgwifiselect);
        LinearLayout llselect = (LinearLayout) convertView.findViewById(R.id.llwifilistviewselect);

        if(wifi.equals(ssidSelected)) {
            imgselect.setImageResource(R.drawable.select);
        } else {
            imgselect.setImageResource(R.drawable.unselect);
        }

        tvwifiname.setText(wifi);
        convertView.setTag(wifi);
        return convertView;
    }
}
