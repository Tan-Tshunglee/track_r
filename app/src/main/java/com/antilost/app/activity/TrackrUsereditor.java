package com.antilost.app.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
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
import java.util.Calendar;

public class TrackrUsereditor extends Activity implements TrackRInitialize {
    private String TAG = "TrackrUsereditor";
    private ImageButton btmBack;
    private ImageView Imguser_usericon;
    private TextView tvtitle ;
    private RelativeLayout rluser_smallname, rluser_bord, rluser_xuexing,
            rluser_likes, rluser_qianming, rluser_homepage, rlusereditor_icon;
    private TextView tvusereditor_name,tvusereditor_board,tvusereditorxuexing,tvusereditorlikes,tvSignature,tvusereditorhomepage;
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
    private File                mDeviceIconTempFile = null;
    private String mTempFileForSavedCroppedImage = null;

    private AlertDialog mImageSourceDialog;



    private static final int DATE_DIALOG_ID = 1;

    private static final int SHOW_DATAPICK = 0;

    private int mYear;

    private int mMonth;

    private int mDay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.usereditor);
        initWidget();
        initWidgetState();
        initWidgetListener();
        addWidgetListener();
        initDataSource();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(trackRDataBase != null) {
            trackRDataBase.close();
            trackRDataBase = null;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        return true;
    }

    @Override
    public void initDataSource() {
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
            if(!curUserDataBean.getMimage().equals("3")){
                mTempFileForSavedCroppedImage = curUserDataBean.getMimage();
                Log.d(TAG,"the mlastupdateIconpath is "+ mTempFileForSavedCroppedImage);
                Imguser_usericon.setImageURI(Uri.fromFile(new File(mTempFileForSavedCroppedImage)));
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
        btmBack = (ImageButton) findViewById(R.id.mBtnCancel);
//        btmDone = (Button) findViewById(R.id.mBtnDone);
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
        mBtnListener = new BtnListener();
//        btmDone.setVisibility(View.GONE);
    }

    @Override
    public void initWidgetListener() {

    }

    @Override
    public void addWidgetListener() {
        btmBack.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                TrackrUsereditor.this.finish();
            }
        });
        Imguser_usericon.setOnClickListener(mBtnListener);
        rlusereditor_icon.setOnClickListener(mBtnListener);
        rluser_smallname.setOnClickListener(mBtnListener);
        rluser_bord.setOnClickListener(new DateButtonOnClickListener());
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
                    //save cropped file to temp file for later use
                    mTempFileForSavedCroppedImage = CsstSHImageData.zoomIconTempFile().getPath();
                    Uri savedCropFile = Uri.fromFile(new File(mTempFileForSavedCroppedImage));
                    CsstSHImageData.cropDeviceIconPhoto(this, Uri.fromFile(mDeviceIconTempFile), savedCropFile, GET_ICON_FROM_CROP);
                }
                break;

            case GET_ICON_FROM_CROP:
                if (null != data){
                    try{
//                        Bundle extras = data.getExtras();
//                        Bitmap source = extras.getParcelable("data");
//                        mTempFileForSavedCroppedImage = CsstSHImageData.zoomIconTempFile().getPath();
//                        source = CsstSHImageData.zoomBitmap(source, mTempFileForSavedCroppedImage);
//
//                        int targetWidth = 100;
//                        int targetHeight = 100;
//                        Bitmap targetBitmap = Bitmap.createBitmap(
//                                targetWidth,
//                                targetHeight,
//                                Bitmap.Config.ARGB_8888);
//                        Canvas canvas = new Canvas(targetBitmap);
//                        Path path = new Path();
//                        path.addCircle(
//                                ((float)targetWidth - 1) / 2,
//                                ((float)targetHeight - 1) / 2,
//                                (Math.min(((float)targetWidth), ((float)targetHeight)) / 2),
//                                Path.Direction.CCW);
//                        canvas.clipPath(path);
//                        Bitmap sourceBitmap =source;
//                        canvas.drawBitmap(
//                                sourceBitmap,
//                                new Rect(0, 0, sourceBitmap.getWidth(), sourceBitmap.getHeight()),
//                                new Rect(0, 0, targetWidth, targetHeight),
//                                null);
//                        Imguser_usericon.setImageBitmap(CsstSHImageData.toRoundCorner(source));
//                        curUserDataBean.setMimage(mTempFileForSavedCroppedImage);
//                        UserDataTable.getInstance().update(mDb,curUserDataBean);

                        Bitmap croppedBitmap = BitmapFactory.decodeFile(mTempFileForSavedCroppedImage);

                    }catch(Exception ex ){
                        System.out.println("the error is "+ex.toString());
                    }

                }
                break;

            case GET_ICON_FROM_ALBUM:
                if (resultCode == RESULT_OK){
                    Uri sourceImageUri = data.getData();
                    //save cropped file to temp file for later use
                    mTempFileForSavedCroppedImage = CsstSHImageData.zoomIconTempFile().getPath();
                    Uri savedCropFile = Uri.fromFile(new File(mTempFileForSavedCroppedImage));
                    CsstSHImageData.cropDeviceIconPhoto(this, sourceImageUri, savedCropFile, GET_ICON_FROM_CROP);
                }
                break;
        }
    }

    private final class BtnListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.tvusereditor_icon:
                case R.id.rlusereditor_icon:
                    showImageSourceDialog();
                    break;
                case R.id.rlusereditor_xuexing:
                    showBloodTypeSelector();
                    break;
                case R.id.rlusereditor_smallname:
                case R.id.rlusereditor_likes:
                case R.id.rlusereditor_qianming:
                case R.id.rlusereditor_homepage:
                    Dailog(v);
                    break;
                case R.id.takePhoto:
                    CsstSHImageData.tackPhoto(TrackrUsereditor.this, mDeviceIconTempFile, GET_ICON_FROM_TAKE);
                    dismissImageSourceDialog();
                    break;
                case R.id.choosePicture:
                    CsstSHImageData.pickAlbum(TrackrUsereditor.this, GET_ICON_FROM_ALBUM);
                    dismissImageSourceDialog();
                    break;
            }
        }
    }

    private void showBloodTypeSelector(){
        new AlertDialog.Builder(TrackrUsereditor.this)
                .setTitle(getResources().getString(R.string.usereditor_select) + getResources().getString(R.string.user_xuexing))
                .setIcon(android.R.drawable.ic_dialog_info)
                .setSingleChoiceItems(new String[]{"O", "A", "AB", "B"}, 0,
                        new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int which) {
                                switch (which) {
                                    case 0:
                                        tvusereditorxuexing.setText(getResources().getString(R.string.user_xuexing) + "O");
                                        curUserDataBean.setMbloodType(tvusereditorxuexing.getText().toString());
                                        break;
                                    case 1:
                                        tvusereditorxuexing.setText(getResources().getString(R.string.user_xuexing) + "A");
                                        curUserDataBean.setMbloodType(tvusereditorxuexing.getText().toString());
                                        break;
                                    case 2:
                                        tvusereditorxuexing.setText(getResources().getString(R.string.user_xuexing) + "AB");
                                        curUserDataBean.setMbloodType(tvusereditorxuexing.getText().toString());
                                        break;
                                    case 3:
                                        tvusereditorxuexing.setText(getResources().getString(R.string.user_xuexing) + "B");
                                        curUserDataBean.setMbloodType(tvusereditorxuexing.getText().toString());
                                        break;
                                }
                                UserDataTable.getInstance().update(mDb,curUserDataBean);
                                dialog.dismiss();
                            }
                        }
                )
                .show();
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
                title= getResources().getString(R.string.usereditor_select)+getResources().getString(R.string.user_smallname);
                break;
            case R.id.rlusereditor_borad:
                title= getResources().getString(R.string.usereditor_select)+getResources().getString(R.string.user_borddate);
                break;
            case R.id.rlusereditor_xuexing:
                title= getResources().getString(R.string.usereditor_select)+getResources().getString(R.string.user_xuexing);
                break;
            case R.id.rlusereditor_likes:
                title= getResources().getString(R.string.usereditor_select)+getResources().getString(R.string.user_liks);
                break;
            case R.id.rlusereditor_qianming:
                title= getResources().getString(R.string.usereditor_select)+getResources().getString(R.string.user_editorself);
                break;
            case R.id.rlusereditor_homepage:
                title= getResources().getString(R.string.usereditor_select)+getResources().getString(R.string.user_homepage);
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


    private void setDateTime() {

        final Calendar c = Calendar.getInstance();

        mYear = c.get(Calendar.YEAR);

        mMonth = c.get(Calendar.MONTH);

        mDay = c.get(Calendar.DAY_OF_MONTH);
        updateDisplay();

    }



    /**

     * 更新日期

     */

    private void updateDisplay() {
        tvusereditor_board.setText(getResources().getString(R.string.user_borddate) + new StringBuilder().append(mYear).append(

                (mMonth + 1) < 10 ? "0" + (mMonth + 1) : (mMonth + 1)).append(

                (mDay < 10) ? "0" + mDay : mDay));
        curUserDataBean.setMbirthday(tvusereditor_board.getText().toString());
        UserDataTable.getInstance().update(mDb,curUserDataBean);

    }



    /**

     * 日期控件的事件

     */

    private DatePickerDialog.OnDateSetListener mDateSetListener = new DatePickerDialog.OnDateSetListener() {

        public void onDateSet(DatePicker view, int year, int monthOfYear,

                              int dayOfMonth) {

            mYear = year;

            mMonth = monthOfYear;

            mDay = dayOfMonth;

            updateDisplay();

        }

    };



    /**

     * 选择日期Button的事件处理

     *

     * @author Raul

     *

     */

    class DateButtonOnClickListener implements

            android.view.View.OnClickListener {

        @Override

        public void onClick(View v) {
            final Calendar c = Calendar.getInstance();

            mYear = c.get(Calendar.YEAR);

            mMonth = c.get(Calendar.MONTH);

            mDay = c.get(Calendar.DAY_OF_MONTH);

            setDateTime();
            Message msg = new Message();
            msg.what = TrackrUsereditor.SHOW_DATAPICK;
            TrackrUsereditor.this.saleHandler.sendMessage(msg);

        }

    }

    @Override
    protected Dialog onCreateDialog(int id) {

        switch (id) {

            case DATE_DIALOG_ID:

                return new DatePickerDialog(this, mDateSetListener, mYear, mMonth,

                        mDay);

        }

        return null;

    }



    @Override

    protected void onPrepareDialog(int id, Dialog dialog) {

        switch (id) {

            case DATE_DIALOG_ID:

                ((DatePickerDialog) dialog).updateDate(mYear, mMonth, mDay);

                break;

        }

    }


    /**

     * 处理日期控件的Handler

     */

    Handler saleHandler = new Handler() {

        @Override

        public void handleMessage(Message msg) {

            switch (msg.what) {

                case TrackrUsereditor.SHOW_DATAPICK:

                    showDialog(DATE_DIALOG_ID);

                    break;

            }

        }

    };


    public boolean onKeyDown(int keyCode,KeyEvent event) {
        // 是否触发按键为back键
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            // 弹出 退出确认框
            TrackrUsereditor.this.finish();
            return true;
        }
        return true;
    }

}
