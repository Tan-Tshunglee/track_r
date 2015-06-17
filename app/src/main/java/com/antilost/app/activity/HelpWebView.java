package com.antilost.app.activity;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import com.antilost.app.R;

public class HelpWebView extends Activity implements View.OnClickListener {

    private static final int REQUEST_CODE_ADD_TRACK_R = 1;
    private static final String LOG_TAG = "HelpWebView";
    WebView webview =null;
    String strURL = null;
    TextView mTVTitle =null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.helpwebview);
        findViewById(R.id.mBtnCancel).setOnClickListener(this);
        mTVTitle = (TextView)findViewById(R.id.mTVTitle);
        mTVTitle.setText(getResources().getString(R.string.help_title));
        //加载需要显示的网页
        strURL = (String) this.getIntent().getStringExtra("URL");
        Log.d(LOG_TAG, "get string from help is " + strURL);
        webview = (WebView) findViewById(R.id.webview);
        //设置WebView属性，能够执行Javascript脚本
        webview.getSettings().setJavaScriptEnabled(true);
        //加载需要显示的网页
//        webview.loadUrl("http://www.ieasytec.com/help/eng/home.html");
        webview.loadUrl(strURL);
        //设置Web视图
        webview.setWebViewClient(new HelloWebViewClient ());
    }


    //Web视图
    private class HelloWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }
    }

    @Override
    //设置回退
    //覆盖Activity类的onKeyDown(int keyCoder,KeyEvent event)方法
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK) && webview.canGoBack()) {
            Log.d(LOG_TAG,"onKeyDown is here ");
            webview.goBack(); //goBack()表示返回WebView的上一页面
            return true;
        }else if(keyCode == KeyEvent.KEYCODE_BACK){
            this.finish();

        }
        return false;
    }



    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.mBtnCancel:
                if ( webview.canGoBack()) {
                    Log.d(LOG_TAG, "onKeyDown is here ");
                    webview.goBack(); //goBack()表示返回WebView的上一页面
                }else{
                    this.finish();
                }
                break;
        }
    }

}
