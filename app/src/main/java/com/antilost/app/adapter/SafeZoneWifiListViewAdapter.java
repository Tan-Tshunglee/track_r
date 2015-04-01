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
import com.antilost.app.prefs.PrefsManager;


/**
 * @author zql
 */
public class SafeZoneWifiListViewAdapter extends BaseAdapter {
    private Context context = null;
    private List<String> wifilist = null;
    private String typewho;

    public SafeZoneWifiListViewAdapter(Context context, List<String> wifilist,String type) {
        this.context = context;
        this.wifilist = wifilist;
        this.typewho=type;
        System.out.println("the size of wifilist is " + this.wifilist.size());
        for (int i = 0; i < this.wifilist.size(); i++) {
            System.out.println("the name of action name is" + wifilist.get(i));
        }

    }

    public SafeZoneWifiListViewAdapter() {

    }

    public final void setDevices(List<String> wifilist) {
        this.wifilist = wifilist;
        notifyDataSetInvalidated();
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
        System.out.println("the CsstSHActionAdapter getview ");
        String wifi = wifilist.get(position);
        convertView = LayoutInflater.from(context).inflate(R.layout.safezonewifilistview, null);
        //Button btn_open = (Button) convertView.findViewById(R.id.item_open);
        TextView tvwifiname = (TextView) convertView.findViewById(R.id.tvwifiname);
        ImageView imgselect = (ImageView) convertView.findViewById(R.id.imgwifiselect);
        LinearLayout llselect = (LinearLayout) convertView.findViewById(R.id.llwifilistviewselect);
        if(wifi.equals(typewho)){
            imgselect.setBackgroundResource(R.drawable.select);
        }
        tvwifiname.setText(wifi);
        imgselect.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {

            }
        });

        convertView.setTag(wifi);
        return convertView;
    }
}
