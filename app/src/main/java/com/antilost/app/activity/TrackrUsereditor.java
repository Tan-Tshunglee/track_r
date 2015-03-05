package com.antilost.app.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.antilost.app.R;
import com.antilost.app.common.TrackRInitialize;
import com.antilost.app.dao.TrackRDataBase;
import com.antilost.app.dao.UserDataTable;
import com.antilost.app.model.UserdataBean;
import com.antilost.app.util.CsstSHImageData;

import java.io.File;

public class TrackrUsereditor extends Activity implements TrackRInitialize {
    private String TAG = "TrackrUsereditor";
    private Button btmBack, btmDone;
    private ImageView Imguser_usericon;
    private TextView tvtitle ;
    private RelativeLayout rluser_smallname, rluser_bord, rluser_xuexing,
            rluser_likes, rluser_qianming, rluser_homepage, rlusereditor_icon;
    private TextView tvusereditor_name,tvusereditor_board,tvusereditorxuexing,tvusereditorlikes,tvSignature,tvusereditorhomepage;
    /** �������*/
    private BtnListener mBtnListener = null;
    //数据库
    private TrackRDataBase trackRDataBase;
    /** 数据库对象 */
    private SQLiteDatabase mDb = null;

    private UserdataBean   curUserDataBean;

    //picture
    public static final int     REQUEST_CODE_PICK_PICTURE = 1;
    public static final int     REQUEST_CODE_TAKE_PICTURE = 2;
    private static final int GET_ICON_FROM_ALBUM = 0x00;
    private static final int GET_ICON_FROM_CROP = 0x01;
    private static final int GET_ICON_FROM_TAKE = 0x02;
    private static final int SCAN_UUID_REQUEST = 0x03;
    private File mDeviceIconTempFile = null;
    private String              mLastUpdatedIconFileName  = null;

    private AlertDialog mImageSourceDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.usereditor);
        initWidget();
        initWidgetState();
        initWidgetListener();
        addWidgetListener();
        initDataSource();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        return true;
    }

    @Override
    public void initDataSource() {
        // TODO Auto-generated method stub
        trackRDataBase = new TrackRDataBase(this);
        mDb = trackRDataBase.getWritDatabase();
        //file init
        mDeviceIconTempFile = CsstSHImageData.deviceIconTempFile();
        //first to init
        if(UserDataTable.getInstance().countRecord(mDb)!=0){
            curUserDataBean = UserDataTable.getInstance().query(mDb);
            if(curUserDataBean==null){
                Log.d(TAG," curUserDataBean==null");
            }else{
                Log.d(TAG," curUserDataBean!=null");
            }
            if(curUserDataBean.getMimage()!="3"){
                Log.d(TAG,"the mlastupdateIconpath is "+curUserDataBean.getMimage());
                mLastUpdatedIconFileName = curUserDataBean.getMimage();
                Log.d(TAG,"the mlastupdateIconpath isqqqqq"+mLastUpdatedIconFileName);
                Imguser_usericon.setImageBitmap(BitmapFactory.decodeFile(mLastUpdatedIconFileName));
                Log.d(TAG,"the the bitmap  is"+BitmapFactory.decodeFile(mLastUpdatedIconFileName));
                Log.d(TAG,"the the bitmap  is"+ BitmapFactory.decodeFile(mLastUpdatedIconFileName));

            }
            tvusereditor_name.setText(curUserDataBean.getMnickname());
            tvusereditor_board.setText(curUserDataBean.getMbirthday());
            tvusereditorxuexing.setText(curUserDataBean.getMbloodType());
            tvusereditorlikes.setText(curUserDataBean.getmHobby());
            tvSignature.setText(curUserDataBean.getmSignature());
            tvusereditorhomepage.setText(curUserDataBean.getmHomePage());
        }

    }

    @Override
    public void initWidget() {
        // TODO Auto-generated method stub
        btmBack = (Button) findViewById(R.id.mBtnCancel);
        btmDone = (Button) findViewById(R.id.mBtnDone);
        tvtitle = (TextView) findViewById(R.id.mTVTitle);
        Imguser_usericon = (ImageView) findViewById(R.id.tvusereditor_icon);
        rlusereditor_icon = (RelativeLayout) findViewById(R.id.rlusereditor_icon);
        rluser_smallname = (RelativeLayout) findViewById(R.id.rlusereditor_smallname);
        rluser_bord = (RelativeLayout) findViewById(R.id.rlusereditor_borad);
        rluser_xuexing = (RelativeLayout) findViewById(R.id.rlusereditor_xuexing);
        rluser_likes = (RelativeLayout) findViewById(R.id.rlusereditor_likes);
        rluser_qianming = (RelativeLayout) findViewById(R.id.rlusereditor_qianming);
        rluser_homepage = (RelativeLayout) findViewById(R.id.rlusereditor_homepage);

        tvusereditor_name=(TextView) findViewById(R.id.tvusereditor_name);
        tvusereditor_board=(TextView) findViewById(R.id.tvusereditor_board);
        tvusereditorxuexing=(TextView) findViewById(R.id.tvusereditorxuexing);
        tvusereditorlikes=(TextView) findViewById(R.id.tvusereditorlikes);
        tvSignature=(TextView) findViewById(R.id.tvSignature);
        tvusereditorhomepage=(TextView) findViewById(R.id.tvusereditorhomepage);

    }

    @Override
    public void initWidgetState() {
        // TODO Auto-generated method stub
        mBtnListener = new BtnListener();
    }

    @Override
    public void initWidgetListener() {
        // TODO Auto-generated method stub

    }

    @Override
    public void addWidgetListener() {
        // TODO Auto-generated method stub
        btmBack.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                Intent intent = new Intent(TrackrUsereditor.this, UserProfileActivity.class);
                startActivity(intent);
                TrackrUsereditor.this.finish();
            }
        });
        Imguser_usericon.setOnClickListener(mBtnListener);
        rlusereditor_icon.setOnClickListener(mBtnListener);
        rluser_smallname.setOnClickListener(mBtnListener);
        rluser_bord.setOnClickListener(mBtnListener);
        rluser_xuexing.setOnClickListener(mBtnListener);
        rluser_likes.setOnClickListener(mBtnListener);
        rluser_qianming.setOnClickListener(mBtnListener);
        rluser_homepage.setOnClickListener(mBtnListener);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case GET_ICON_FROM_TAKE:
                if (RESULT_OK == resultCode){
                    CsstSHImageData.cropDeviceIconPhoto(this, Uri.fromFile(mDeviceIconTempFile), GET_ICON_FROM_CROP);
                }
                break;

            case GET_ICON_FROM_CROP:
                if (null != data){
                    try{
                        Bundle extras = data.getExtras();
                        Bitmap source = extras.getParcelable("data");
                        mLastUpdatedIconFileName = CsstSHImageData.zoomIconTempFile().getPath();
                        source = CsstSHImageData.zoomBitmap(source, mLastUpdatedIconFileName);
                        Imguser_usericon.setImageBitmap(source);
                        curUserDataBean.setMimage(mLastUpdatedIconFileName);
                        UserDataTable.getInstance().update(mDb,curUserDataBean);
                    }catch(Exception ex ){
                        System.out.println("the error is "+ex.toString());
                    }

                }
                break;

            case GET_ICON_FROM_ALBUM:
                if (resultCode == RESULT_OK){
                    Uri uri = data.getData();
                    CsstSHImageData.cropDeviceIconPhoto(this, uri, GET_ICON_FROM_CROP);
                }
                break;
        }
    }

    /**
     * �������
     *
     */
    private final class BtnListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.tvusereditor_icon:
                case R.id.rlusereditor_icon:
                    showImageSourceDialog();
                    break;
                case R.id.rlusereditor_smallname:
                case R.id.rlusereditor_borad:
                case R.id.rlusereditor_xuexing:
                case R.id.rlusereditor_likes:
                case R.id.rlusereditor_qianming:
                case R.id.rlusereditor_homepage:
                    Dailog(v);
                    break;
                case R.id.takePhoto:
//                    Toast.makeText(this, R.string.take_photo, Toast.LENGTH_LONG).show();
                    CsstSHImageData.tackPhoto(TrackrUsereditor.this, mDeviceIconTempFile, GET_ICON_FROM_TAKE);
                    dismissImageSourceDialog();
                    break;
                case R.id.choosePicture:
//                    Toast.makeText(this, R.string.choose_picture, Toast.LENGTH_LONG).show();
                    CsstSHImageData.pickAlbum(TrackrUsereditor.this, GET_ICON_FROM_ALBUM);
                    dismissImageSourceDialog();
                    break;
            }
        }
    }

    /**
     *
     */
    private void showImageSourceDialog() {
        if(mImageSourceDialog != null) {
            mImageSourceDialog.show();
            return;
        }
        AlertDialog.Builder b = new AlertDialog.Builder(this);
        View customView = getLayoutInflater().inflate(R.layout.custom_photo_source_dialog, null);
        b.setView(customView);
        b.setTitle(getString(R.string.change_image));
        customView.findViewById(R.id.takePhoto).setOnClickListener(mBtnListener);
        customView.findViewById(R.id.choosePicture).setOnClickListener(mBtnListener);
        b.setNegativeButton(R.string.cancel, new Dialog.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mImageSourceDialog.dismiss();
            }
        });

        mImageSourceDialog = b.create();
        mImageSourceDialog.show();
    }
    private void dismissImageSourceDialog() {
        if(mImageSourceDialog != null) {
            mImageSourceDialog.dismiss();
        }
    }
    private void Dailog(View view){
        final View v = view;
        String title=null;
        final EditText inputServer = new EditText(TrackrUsereditor.this);
        AlertDialog.Builder builder = new AlertDialog.Builder(TrackrUsereditor.this);
        switch (v.getId()) {
            case R.id.rlusereditor_smallname:
                title= getResources().getString(R.string.user_smallname);
                break;
            case R.id.rlusereditor_borad:
                title= getResources().getString(R.string.user_borddate);
                break;
            case R.id.rlusereditor_xuexing:
                title= getResources().getString(R.string.user_xuexing);
                break;
            case R.id.rlusereditor_likes:
                title= getResources().getString(R.string.user_liks);
                break;
            case R.id.rlusereditor_qianming:
                title= getResources().getString(R.string.user_editorself);
                break;
            case R.id.rlusereditor_homepage:
                title= getResources().getString(R.string.user_homepage);
                break;
        }
        builder.setTitle(title);
        builder.setView(inputServer);
        builder.setNegativeButton(R.string.cancel, null);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String  Name = inputServer.getText().toString().trim();
                        switch (v.getId()) {
                            case R.id.rlusereditor_smallname:
                                tvusereditor_name.setText(getResources().getString(R.string.user_smallname) + Name);
                                curUserDataBean.setMnickname(tvusereditor_name.getText().toString());
                                break;
                            case R.id.rlusereditor_borad:
                                tvusereditor_board.setText(getResources().getString(R.string.user_borddate) + Name);
                                curUserDataBean.setMbirthday(tvusereditor_board.getText().toString());
                                break;
                            case R.id.rlusereditor_xuexing:
                                tvusereditorxuexing.setText(getResources().getString(R.string.user_xuexing) + Name);
                                curUserDataBean.setMbloodType(tvusereditorxuexing.getText().toString());
                                break;
                            case R.id.rlusereditor_likes:
                                tvusereditorlikes.setText(getResources().getString(R.string.user_liks) + Name);
                                curUserDataBean.setmHobby(tvusereditorlikes.getText().toString());
                                break;
                            case R.id.rlusereditor_qianming:
                                tvSignature.setText(getResources().getString(R.string.user_editorself) + Name);
                                curUserDataBean.setmSignature(tvSignature.getText().toString());
                                break;
                            case R.id.rlusereditor_homepage:
                                tvusereditorhomepage.setText(getResources().getString(R.string.user_homepage)+Name);
                                curUserDataBean.setmHomePage(tvusereditorhomepage.getText().toString());
                                break;
                        }
                        UserDataTable.getInstance().update(mDb,curUserDataBean);
                    }
                }
        );
        builder.show();

    }

}
