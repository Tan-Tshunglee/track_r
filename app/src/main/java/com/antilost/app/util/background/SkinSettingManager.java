package com.antilost.app.util.background;

import android.app.Activity;
import android.content.SharedPreferences;

public class SkinSettingManager {
	public final static String SKIN_PREF = "skinSetting";
	public SharedPreferences skinSettingPreference;
	private int[] skinResources = { R.drawable.black,
	R.drawable.gray,R.drawable.login,R.drawable.silver,
	R.drawable.yellow
	};
	private Activity mActivity;
	public SkinSettingManager(Activity activity) {
	this.mActivity = activity;
	skinSettingPreference = mActivity.getSharedPreferences(SKIN_PREF, 3);
	}
	public int getSkinType() {
	String key = "skin_type";
	return skinSettingPreference.getInt(key, 0);
	}
	public void setSkinType(int j) {
	SharedPreferences.Editor editor = skinSettingPreference.edit();
	String key = "skin_type";
	editor.putInt(key, j);
	editor.commit();
	}
	public int getCurrentSkinRes() {
	int skinLen = skinResources.length;
	int getSkinLen = getSkinType();
	if(getSkinLen >= skinLen){
	getSkinLen = 0;
	}
	return skinResources[getSkinLen];
	}
	public void toggleSkins(){
	int skinType = getSkinType();
	if(skinType == skinResources.length - 1){
	skinType = 0;
	}else{
	skinType ++;
	}
	setSkinType(skinType);
	mActivity.getWindow().setBackgroundDrawable(null);
	try {
	mActivity.getWindow().setBackgroundDrawableResource(getCurrentSkinRes());
	} catch (Throwable e) {
	e.printStackTrace();
	}
	}
	public void initSkins(){
	mActivity.getWindow().setBackgroundDrawableResource(getCurrentSkinRes());
	}
}
