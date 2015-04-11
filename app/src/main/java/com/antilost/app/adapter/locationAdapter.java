package com.antilost.app.adapter;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.antilost.app.R;
import com.antilost.app.activity.ManualAddLocationActivity;
import com.antilost.app.dao.LocationTable;
import com.antilost.app.model.LocationBean;
import com.antilost.app.util.LocUtils;

import java.util.List;
import java.util.Locale;

/**
 * Created by Tan on 2015/1/17.
 */
public class locationAdapter extends BaseAdapter implements View.OnClickListener {

    private static final String LOG_TAG = "locationAdapter";
    private  List<LocationBean> locationBeans;
    private Context context;
    private SQLiteDatabase mDb;
    public locationAdapter(Context context, List<LocationBean> locationBeans,SQLiteDatabase mDb) {
        this.locationBeans = locationBeans;
        this.context = context;
        this.mDb = mDb;
    }
    @Override
    public int getCount() {
        if(locationBeans==null){
            return 0;
        }else{
            return  locationBeans.size();
        }

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

    private void bindView(View convertView,  final int position) {
        final int positions = position;
        final int mpositions = position;
        TextView titile = (TextView) convertView.findViewById(R.id.tvlocationtitle);
        TextView time = (TextView) convertView.findViewById(R.id.tvlocationtime);
        RelativeLayout relativeLayout = (RelativeLayout) convertView.findViewById(R.id.rllocation);
        ImageButton btnlocation  = (ImageButton) convertView.findViewById(R.id.btnlocation);
        btnlocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(LOG_TAG,"delete the position locationBeans ");
                deleteSensor(locationBeans.get(positions));
            }
            }
        );
//        relativeLayout.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                String uri = String.format(Locale.ENGLISH, "geo:%f,%f",locationBeans.get(positions).getMlatitude(),locationBeans.get(positions).getMlongitude());
////                        Uri uri = Uri.parse("geo:38.899533,-77.036476");
//                Log.d(LOG_TAG,"the string is "+uri);
//                Location location = new Location(LocationManager.NETWORK_PROVIDER);
//                double longitude= Double.valueOf(locationBeans.get(positions).getMlatitude());
//                double latitude  = Double.valueOf(locationBeans.get(positions).getMlongitude());
//
//                location.setLatitude(latitude);
//                location.setLongitude(longitude);
//                LocUtils.viewLocation(context, location);
//            }
//            }
//        );
//
//
//        relativeLayout.setLongClickable(new View.OnLongClickListener(){
//            @Override
//            public boolean onLongClick(View v) {
////                try {
//////                    modifyLocation(locationBeans.get(position));
////                } catch (Exception ex) {
////                    Log.d(LOG_TAG, ex.toString());
////                }
//                modifyLocation(locationBeans.get(positions));
//                return false;
//            }
//        });


        LocationBean locationBean = locationBeans.get(position);
        titile.setText(locationBean.getmLocationName());
        time.setText(locationBean.getmLocationTime());
    }





    /**
     */
    public boolean modifyLocation(final LocationBean locationBean) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(locationBean.getmLocationName());
        builder.setItems(R.array.modify_location, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    // 重命�?
                    case 0:
                        modifyLocationName(locationBean);
                        break;
                    // 删除
                    case 1:
                        deleteSensor(locationBean);
                        break;
                }
            }
        });
        builder.setNegativeButton(R.string.cancel, null);
        Dialog d = builder.show();
        d.setCanceledOnTouchOutside(true);
        return false;
    }

    /**
     * 修改名字
     *
     */
    private final void modifyLocationName(final LocationBean locationBean) {
        final EditText inputServer = new EditText(context);
        inputServer.setText(locationBean.getmLocationName());
        inputServer.setSelection(locationBean.getmLocationName().length());
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.modify_locationname);
        builder.setView(inputServer);
        builder.setNegativeButton(R.string.cancel, null);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String sensorName = inputServer.getText().toString();
                if (TextUtils.isEmpty(sensorName)) {
                    Toast.makeText(context, R.string.modify_locationname_null, Toast.LENGTH_SHORT).show();
                    return;
                }
                locationBean.setmLocationName(sensorName);
                LocationTable.getInstance().update(mDb,locationBean);

            }
        });
        builder.show();
    }

    private final void deleteSensor(final LocationBean locationBean) {
        LocationTable.getInstance().delete(mDb, locationBean);
        if(LocationTable.getInstance().query(mDb)!=null){
            locationBeans =  LocationTable.getInstance().query(mDb);
        }else{
            locationBeans = null;
        }
        notifyDataSetChanged();

    }


    public void updateData() {
        notifyDataSetChanged();
    }

    @Override
    public void onClick(View v) {


    }

}
