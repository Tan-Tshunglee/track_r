package com.antilost.app.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import com.antilost.app.R;

/**
 * Created by Tan on 2015/4/27.
 */
public class BaseActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View rootView = getWindow().getDecorView();
        rootView.setBackgroundResource(R.drawable.background);
    }
}
