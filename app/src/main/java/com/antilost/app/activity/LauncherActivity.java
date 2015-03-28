package com.antilost.app.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;

import com.antilost.app.loginfisrt.ScrollLayoutActivity;
import com.antilost.app.network.CommandPerformer;
import com.antilost.app.network.VersionCheckCommand;

public class LauncherActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences preferences = getSharedPreferences("count", 0);
        int count = preferences.getInt("count", 0);

//        try {
//            Thread t = new CommandPerformer(new VersionCheckCommand(getApplicationContext()));
//            t.start();
//        } catch (PackageManager.NameNotFoundException e) {
//            e.printStackTrace();
//        }
        // 取出数据
        if (count == 0) { // 判断程序与第几次运行，如果是第一次运行则跳转到引导页面
            SharedPreferences.Editor editor = preferences.edit(); // 让preferences处于编辑状态
            editor.putInt("count", 1); // 存入数据
            editor.commit(); // 提交修改
            Intent intent = new Intent();
            intent.setClass(getApplicationContext(), ScrollLayoutActivity.class);
            startActivity(intent);
            finish();
        } else {
            Intent intent = new Intent();
            intent.setClass(getApplicationContext(), LoginActivity.class);
            startActivity(intent);
            finish();
        }
    }

}
