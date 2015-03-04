package com.antilost.app.activity;

import android.app.Activity;
import android.os.Bundle;
import android.content.SharedPreferences;
import android.content.Intent;

import com.antilost.app.loginfisrt.ScrollLayoutActivity;

public class LoginFirst extends Activity  {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences  preferences = getSharedPreferences("count", 0);
        int count = preferences.getInt("count", 0); // 取出数据
        if (count == 0) { // 判断程序与第几次运行，如果是第一次运行则跳转到引导页面
            SharedPreferences.Editor editor = preferences.edit(); // 让preferences处于编辑状态
            editor.putInt("count", 1); // 存入数据
            editor.commit(); // 提交修改
            Intent intent = new Intent();
            intent.setClass(getApplicationContext(),ScrollLayoutActivity.class);
            startActivity(intent);
            finish();
        }else{
            Intent intent = new Intent();
            intent.setClass(getApplicationContext(),LoginActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

}
