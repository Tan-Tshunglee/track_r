package com.antilost.app.util;

import android.annotation.SuppressLint;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 时间工具类
 * yyyy-MM-dd hh:mm:ss
 * @author liuyang
 */
public final class CsstSHDateUtil {

	/**
	 * 设备图片名字
	 * @return
	 */
	@SuppressLint("SimpleDateFormat")
	public static final String deviceIconName(){
		return new SimpleDateFormat("yyyyMMddhhmmss").format(new Date()) + ".jpg";
	}
	
	
	
}
