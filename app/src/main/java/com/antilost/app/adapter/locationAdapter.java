package com.antilost.app.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.antilost.app.R;
import com.antilost.app.activity.AmapActivity;
import com.antilost.app.model.LocationBean;

import java.util.List;
import java.util.Locale;

/**
 * Created by Tan on 2015/1/17.
 */
public class locationAdapter extends BaseAdapter implements View.OnClickListener {

    private static final String LOG_TAG = "locationAdapter";
    private  List<LocationBean> locationBeans;
    private Context context;

    public locationAdapter(Context context, List<LocationBean> locationBeans) {
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
        final int positions = position;
        TextView titile = (TextView) convertView.findViewById(R.id.tvlocationtitle);
        TextView time = (TextView) convertView.findViewById(R.id.tvlocationtime);
        RelativeLayout relativeLayout = (RelativeLayout) convertView.findViewById(R.id.rllocation);
        relativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String uri = String.format(Locale.ENGLISH, "geo:%f,%f",locationBeans.get(positions).getMlatitude(),locationBeans.get(positions).getMlongitude());
//                        Uri uri = Uri.parse("geo:38.899533,-77.036476");
                Log.d(LOG_TAG,"the string is "+uri);
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                intent = new Intent(context, AmapActivity.class);
                intent.setAction(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(uri));
                try {
                    context.startActivity(intent);
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
            }
        );
        LocationBean locationBean = locationBeans.get(position);
        titile.setText(locationBean.getmLocationName());
        time.setText(locationBean.getmLocationTime());
    }

    public void updateData() {

        notifyDataSetChanged();
    }

    @Override
    public void onClick(View v) {


    }

}
