package com.antilost.app.common;

import android.os.Environment;

import java.io.File;

/**
 * 常量字段默认值
 * @author liuyang
 */
public interface ICsstSHConstant {

	long toid = 109860810560L;//109860798346L;
	
	/** 电视遥控器按键标识符 */
	int TV_POWER_RCKEY_IDENTIFY = 0x00;//电源
	int TV_MUTE_RCKEY_IDENTIFY = TV_POWER_RCKEY_IDENTIFY + 1;//静音
	int TV_SIGNAL_RCKEY_IDENTIFY = TV_POWER_RCKEY_IDENTIFY + 2;//信号源
	int TV_SOUND_RCKEY_IDENTIFY = TV_POWER_RCKEY_IDENTIFY + 3;//声音
	int TV_IMAGE_RCKEY_IDENTIFY = TV_POWER_RCKEY_IDENTIFY + 4;//图像
	int TV_MENU_RCKEY_IDENTIFY = TV_POWER_RCKEY_IDENTIFY + 5;//菜单
	int TV_FAV_RCKEY_IDENTIFY = TV_POWER_RCKEY_IDENTIFY + 6;//FAV
	int TV_CHADD_RCKEY_IDENTIFY = TV_POWER_RCKEY_IDENTIFY + 7;//CH+
	int TV_CHDEL_RCKEY_IDENTIFY = TV_POWER_RCKEY_IDENTIFY + 8;//CH-
	int TV_VOLADD_RCKEY_IDENTIFY = TV_POWER_RCKEY_IDENTIFY + 9;//VOL+
	int TV_VOLDEL_RCKEY_IDENTIFY = TV_POWER_RCKEY_IDENTIFY + 10;//VOL-
	int TV_OK_RCKEY_IDENTIFY = TV_POWER_RCKEY_IDENTIFY + 11;//OK
	int TV_EPG_RCKEY_IDENTIFY = TV_POWER_RCKEY_IDENTIFY + 12;//EPG
	int TV_EXIT_RCKEY_IDENTIFY = TV_POWER_RCKEY_IDENTIFY + 13;//EXIT
	int TV_ADD_RCKEY_IDENTIFY = TV_POWER_RCKEY_IDENTIFY + 14;//EPG
	int TV_DEC_RCKEY_IDENTIFY = TV_POWER_RCKEY_IDENTIFY + 15;//EXIT
	int TV_STOP_RCKEY_IDENTIFY = TV_POWER_RCKEY_IDENTIFY + 16;//EPG
	
	/** 空调遥控器按键标识符 */
	int AC_POWER_RCKEY_IDENTIFY = 0x00;//电源
	int AC_ADDT_RCKEY_IDENTIFY = AC_POWER_RCKEY_IDENTIFY + 1;//升温
	int AC_DELT_RCKEY_IDENTIFY = AC_POWER_RCKEY_IDENTIFY + 2;//降温
	int AC_WIND_RCKEY_IDENTIFY = AC_POWER_RCKEY_IDENTIFY + 3;//风速
	int AC_COLD_RCKEY_IDENTIFY = AC_POWER_RCKEY_IDENTIFY + 4;//制冷
	int AC_HOT_RCKEY_IDENTIFY = AC_POWER_RCKEY_IDENTIFY + 5;//制热
	int AC_LRWIND_RCKEY_IDENTIFY = AC_POWER_RCKEY_IDENTIFY + 6;//左右扫风
	int AC_TBWIND_RCKEY_IDENTIFY = AC_POWER_RCKEY_IDENTIFY + 7;//上下扫风
	int AC_MODE_RCKEY_IDENTIFY = AC_POWER_RCKEY_IDENTIFY + 8;//上下扫风
	
	/** 机顶盒遥控器按键标识符 */
	int STB_MUTE_RCKEY_IDENTIFY = 0x00;//静音
	int STB_POWER_RCKEY_IDENTIFY = STB_MUTE_RCKEY_IDENTIFY + 1;//电源
	int STB_SETTING_RCKEY_IDENTIFY = STB_MUTE_RCKEY_IDENTIFY + 2;//设置
	int STB_TRACK_RCKEY_IDENTIFY = STB_MUTE_RCKEY_IDENTIFY + 3;//声道
	int STB_VOLADD_RCKEY_IDENTIFY = STB_MUTE_RCKEY_IDENTIFY + 4;//VOL+
	int STB_VOLDEL_RCKEY_IDENTIFY = STB_MUTE_RCKEY_IDENTIFY + 5;//VOL-
	int STB_ITV_RCKEY_IDENTIFY = STB_MUTE_RCKEY_IDENTIFY + 6;//ITV
	int STB_TVAV_RCKEY_IDENTIFY = STB_MUTE_RCKEY_IDENTIFY + 7;//TV/AV
	int STB_PREPAGE_RCKEY_IDENTIFY = STB_MUTE_RCKEY_IDENTIFY + 8;//上一页
	int STB_NAVIGATION_RCKEY_IDENTIFY = STB_MUTE_RCKEY_IDENTIFY + 9;//导视
	int STB_NEXTPAGE_RCKEY_IDENTIFY = STB_MUTE_RCKEY_IDENTIFY + 10;//下一页
	int STB_HOME_RCKEY_IDENTIFY = STB_MUTE_RCKEY_IDENTIFY + 11;//主页
	int STB_INFO_RCKEY_IDENTIFY = STB_MUTE_RCKEY_IDENTIFY + 12;//信息
	int STB_CHADD_RCKEY_IDENTIFY = STB_MUTE_RCKEY_IDENTIFY + 13;//CH+
	int STB_CHDEL_RCKEY_IDENTIFY = STB_MUTE_RCKEY_IDENTIFY + 14;//CH-
	int STB_OK_RCKEY_IDENTIFY = STB_MUTE_RCKEY_IDENTIFY + 15;//OK
	int STB_BACK_RCKEY_IDENTIFY = STB_MUTE_RCKEY_IDENTIFY + 16;//返回
	int STB_EXIT_RCKEY_IDENTIFY = STB_MUTE_RCKEY_IDENTIFY + 17;//退出
	//第二页
	int STB_FR_RCKEY_IDENTIFY = STB_MUTE_RCKEY_IDENTIFY + 18;//快退
	int STB_RECORD_RCKEY_IDENTIFY = STB_MUTE_RCKEY_IDENTIFY + 19;//停止
	int STB_FF_RCKEY_IDENTIFY = STB_MUTE_RCKEY_IDENTIFY + 20;//快进
	int STB_0_RCKEY_IDENTIFY = STB_MUTE_RCKEY_IDENTIFY + 21;//0
	int STB_1_RCKEY_IDENTIFY = STB_MUTE_RCKEY_IDENTIFY + 22;//1
	int STB_2_RCKEY_IDENTIFY = STB_MUTE_RCKEY_IDENTIFY + 23;//2
	int STB_3_RCKEY_IDENTIFY = STB_MUTE_RCKEY_IDENTIFY + 24;//3
	int STB_4_RCKEY_IDENTIFY = STB_MUTE_RCKEY_IDENTIFY + 25;//4
	int STB_5_RCKEY_IDENTIFY = STB_MUTE_RCKEY_IDENTIFY + 26;//5
	int STB_6_RCKEY_IDENTIFY = STB_MUTE_RCKEY_IDENTIFY + 27;//6
	int STB_7_RCKEY_IDENTIFY = STB_MUTE_RCKEY_IDENTIFY + 28;//7
	int STB_8_RCKEY_IDENTIFY = STB_MUTE_RCKEY_IDENTIFY + 29;//8
	int STB_9_RCKEY_IDENTIFY = STB_MUTE_RCKEY_IDENTIFY + 30;//9
	int STB_DONE_RCKEY_IDENTIFY = STB_MUTE_RCKEY_IDENTIFY + 31;//确定
	int STB_DEL_RCKEY_IDENTIFY = STB_MUTE_RCKEY_IDENTIFY + 32;//删除
	
	int STB_ADD_RCKEY_IDENTIFY = STB_MUTE_RCKEY_IDENTIFY + 33;//9
	int STB_STOP_RCKEY_IDENTIFY = STB_MUTE_RCKEY_IDENTIFY + 34;//确定
	int STB_DEC_RCKEY_IDENTIFY = STB_MUTE_RCKEY_IDENTIFY + 35;//删除
	    
	
	/** DVD ZQL遥控器按键标识符 */
	int DVDZQL_MUTE_RCKEY_IDENTIFY = 0x00;//静音
	int DVDZQL_POWER_RCKEY_IDENTIFY = DVDZQL_MUTE_RCKEY_IDENTIFY + 1;//电源
	int DVDZQL_SOUNDCHANEL_RCKEY_IDENTIFY = DVDZQL_MUTE_RCKEY_IDENTIFY + 2;//左声道
	int DVDZQL_UPPAGE_RCKEY_IDENTIFY = DVDZQL_MUTE_RCKEY_IDENTIFY + 3;//右声道
	int DVDZQL_DOWNPAGE_RCKEY_IDENTIFY = DVDZQL_MUTE_RCKEY_IDENTIFY + 4;//立体声
	int DVDZQL_STOP_RCKEY_IDENTIFY = DVDZQL_MUTE_RCKEY_IDENTIFY + 5;//画面
	int DVDZQL_NEXT_RCKEY_IDENTIFY = DVDZQL_MUTE_RCKEY_IDENTIFY + 6;//播放
	int DVDZQL_LAST_RCKEY_IDENTIFY = DVDZQL_MUTE_RCKEY_IDENTIFY + 7;//暂停
	int DVDZQL_PAUSE_RCKEY_IDENTIFY = DVDZQL_MUTE_RCKEY_IDENTIFY + 8;//停止
	int DVDZQL_UP_RCKEY_IDENTIFY = DVDZQL_MUTE_RCKEY_IDENTIFY + 9;//回放
	int DVDZQL_DOWN_RCKEY_IDENTIFY = DVDZQL_MUTE_RCKEY_IDENTIFY + 10;//上一首
	int DVDZQL_LEFT_RCKEY_IDENTIFY = DVDZQL_MUTE_RCKEY_IDENTIFY + 11;//快退
	int DVDZQL_RIGHT_RCKEY_IDENTIFY = DVDZQL_MUTE_RCKEY_IDENTIFY + 12;//快进
	int DVDZQL_OK_RCKEY_IDENTIFY = DVDZQL_MUTE_RCKEY_IDENTIFY + 13;//下一首
	int DVDZQL_SETTING_RCKEY_IDENTIFY = DVDZQL_MUTE_RCKEY_IDENTIFY + 14;//目录
	int DVDZQL_BACK_RCKEY_IDENTIFY = DVDZQL_MUTE_RCKEY_IDENTIFY + 15;//弹出
	int DVDZQL_MENU_RCKEY_IDENTIFY = DVDZQL_MUTE_RCKEY_IDENTIFY + 16;//弹出

	
	
	/** SOUND 遥控器按键标识符 */
	int SOUND_MUTE_RCKEY_IDENTIFY = 0x00;//静音
	int SOUND_POWER_RCKEY_IDENTIFY = SOUND_MUTE_RCKEY_IDENTIFY + 1;//电源
	int SOUND_SOUNDCHANEL_RCKEY_IDENTIFY = SOUND_MUTE_RCKEY_IDENTIFY + 2;//左声道
	int SOUND_UPPAGE_RCKEY_IDENTIFY = SOUND_MUTE_RCKEY_IDENTIFY + 3;//右声道
	int SOUND_DOWNPAGE_RCKEY_IDENTIFY = SOUND_MUTE_RCKEY_IDENTIFY + 4;//立体声
	int SOUND_STOP_RCKEY_IDENTIFY = SOUND_MUTE_RCKEY_IDENTIFY + 5;//画面
	int SOUND_NEXT_RCKEY_IDENTIFY = SOUND_MUTE_RCKEY_IDENTIFY + 6;//播放
	int SOUND_LAST_RCKEY_IDENTIFY = SOUND_MUTE_RCKEY_IDENTIFY + 7;//暂停
	int SOUND_PAUSE_RCKEY_IDENTIFY = SOUND_MUTE_RCKEY_IDENTIFY + 8;//停止
	int SOUND_UP_RCKEY_IDENTIFY = SOUND_MUTE_RCKEY_IDENTIFY + 9;//回放
	int SOUND_DOWN_RCKEY_IDENTIFY = SOUND_MUTE_RCKEY_IDENTIFY + 10;//上一首
	int SOUND_LEFT_RCKEY_IDENTIFY = SOUND_MUTE_RCKEY_IDENTIFY + 11;//快退
	int SOUND_RIGHT_RCKEY_IDENTIFY = SOUND_MUTE_RCKEY_IDENTIFY + 12;//快进
	int SOUND_OK_RCKEY_IDENTIFY = SOUND_MUTE_RCKEY_IDENTIFY + 13;//下一首
	int SOUND_SETTING_RCKEY_IDENTIFY = SOUND_MUTE_RCKEY_IDENTIFY + 14;//目录
	int SOUND_BACK_RCKEY_IDENTIFY = SOUND_MUTE_RCKEY_IDENTIFY + 15;//弹出
	int SOUND_ADD_RCKEY_IDENTIFY = SOUND_MUTE_RCKEY_IDENTIFY + 16;//目录
	int SOUND_DEC_RCKEY_IDENTIFY = SOUND_MUTE_RCKEY_IDENTIFY + 17;//弹出
	int SOUND_MENU_RCKEY_IDENTIFY = DVDZQL_MUTE_RCKEY_IDENTIFY + 18;//弹出

	
	
	
	
	
	
	
	/** DVD遥控器按键标识符 */
	int DVD_MUTE_RCKEY_IDENTIFY = 0x00;//静音
	int DVD_POWER_RCKEY_IDENTIFY = DVD_MUTE_RCKEY_IDENTIFY + 1;//电源
	int DVD_LTRACK_RCKEY_IDENTIFY = DVD_MUTE_RCKEY_IDENTIFY + 2;//左声道
	int DVD_RTRACK_RCKEY_IDENTIFY = DVD_MUTE_RCKEY_IDENTIFY + 3;//右声道
	int DVD_STEREO_RCKEY_IDENTIFY = DVD_MUTE_RCKEY_IDENTIFY + 4;//立体声
	int DVD_IMAGE_RCKEY_IDENTIFY = DVD_MUTE_RCKEY_IDENTIFY + 5;//画面
	int DVD_PLAY_RCKEY_IDENTIFY = DVD_MUTE_RCKEY_IDENTIFY + 6;//播放
	int DVD_PAUSE_RCKEY_IDENTIFY = DVD_MUTE_RCKEY_IDENTIFY + 7;//暂停
	int DVD_STOP_RCKEY_IDENTIFY = DVD_MUTE_RCKEY_IDENTIFY + 8;//停止
	int DVD_RPLAY_RCKEY_IDENTIFY = DVD_MUTE_RCKEY_IDENTIFY + 9;//回放
	int DVD_PREV_RCKEY_IDENTIFY = DVD_MUTE_RCKEY_IDENTIFY + 10;//上一首
	int DVD_REVERSE_RCKEY_IDENTIFY = DVD_MUTE_RCKEY_IDENTIFY + 11;//快退
	int DVD_FORWARD_RCKEY_IDENTIFY = DVD_MUTE_RCKEY_IDENTIFY + 12;//快进
	int DVD_NEXT_RCKEY_IDENTIFY = DVD_MUTE_RCKEY_IDENTIFY + 13;//下一首
	int DVD_LIST_RCKEY_IDENTIFY = DVD_MUTE_RCKEY_IDENTIFY + 14;//目录
	int DVD_POP_RCKEY_IDENTIFY = DVD_MUTE_RCKEY_IDENTIFY + 15;//弹出
	int DVD_BACK_RCKEY_IDENTIFY = DVD_MUTE_RCKEY_IDENTIFY + 16;//返回
	int DVD_EXIT_RCKEY_IDENTIFY = DVD_MUTE_RCKEY_IDENTIFY + 17;//退出
	int DVD_VOLADD_RCKEY_IDENTIFY = DVD_MUTE_RCKEY_IDENTIFY + 18;//VOL+
	int DVD_VOLDEL_RCKEY_IDENTIFY = DVD_MUTE_RCKEY_IDENTIFY + 19;//VOL-
	int DVD_CHADD_RCKEY_IDENTIFY = DVD_MUTE_RCKEY_IDENTIFY + 20;//CH+
	int DVD_CHDEL_RCKEY_IDENTIFY = DVD_MUTE_RCKEY_IDENTIFY + 21;//CH-
	int DVD_OK_RCKEY_IDENTIFY = DVD_MUTE_RCKEY_IDENTIFY + 22;//OK
	//第二页
	int DVD_FR_RCKEY_IDENTIFY = DVD_MUTE_RCKEY_IDENTIFY + 23;//快退
	int DVD_RECORD_RCKEY_IDENTIFY = DVD_MUTE_RCKEY_IDENTIFY + 24;//停止
	int DVD_FF_RCKEY_IDENTIFY = DVD_MUTE_RCKEY_IDENTIFY + 25;//快进
	int DVD_0_RCKEY_IDENTIFY = DVD_MUTE_RCKEY_IDENTIFY + 26;//0
	int DVD_1_RCKEY_IDENTIFY = DVD_MUTE_RCKEY_IDENTIFY + 27;//1
	int DVD_2_RCKEY_IDENTIFY = DVD_MUTE_RCKEY_IDENTIFY + 28;//2
	int DVD_3_RCKEY_IDENTIFY = DVD_MUTE_RCKEY_IDENTIFY + 29;//3
	int DVD_4_RCKEY_IDENTIFY = DVD_MUTE_RCKEY_IDENTIFY + 30;//4
	int DVD_5_RCKEY_IDENTIFY = DVD_MUTE_RCKEY_IDENTIFY + 31;//5
	int DVD_6_RCKEY_IDENTIFY = DVD_MUTE_RCKEY_IDENTIFY + 32;//6
	int DVD_7_RCKEY_IDENTIFY = DVD_MUTE_RCKEY_IDENTIFY + 33;//7
	int DVD_8_RCKEY_IDENTIFY = DVD_MUTE_RCKEY_IDENTIFY + 34;//8
	int DVD_9_RCKEY_IDENTIFY = DVD_MUTE_RCKEY_IDENTIFY + 35;//9
	int DVD_DONE_RCKEY_IDENTIFY = DVD_MUTE_RCKEY_IDENTIFY + 36;//确定
	int DVD_DEL_RCKEY_IDENTIFY = DVD_MUTE_RCKEY_IDENTIFY + 37;//删除
	
	/** 灯光遥控器按键标识符 */
	int LIGHT_POWER_RCKEY_IDENTIFY = 0x00;
	
	/** 风扇遥控器按键标识符  */
	int FAN_WIND_RCKEY_IDENTIFY = 0x00;//风量
	int FAN_SHAKE_RCKEY_IDENTIFY = FAN_WIND_RCKEY_IDENTIFY + 1;//摇头
	int FAN_CLOCK_RCKEY_IDENTIFY = FAN_WIND_RCKEY_IDENTIFY + 2;//定时/预约
	int FAN_POWER_RCKEY_IDENTIFY = FAN_WIND_RCKEY_IDENTIFY + 3;//电源
	
	/** 窗帘 */
	int CURTAIN_OPEN_RCKEY_IDENTIFY = 0x00;//打开
	int CURTAIN_PAUSE_RCKEY_IDENTIFY = CURTAIN_OPEN_RCKEY_IDENTIFY + 1;//暂停
	int CURTAIN_CLOSE_RCKEY_IDENTIFY = CURTAIN_OPEN_RCKEY_IDENTIFY + 2;//关闭
	
	/** 智能家居配置文件名字 */
	String CsstSHPreference = "smarthome";
	/** 数据库是否初始化 */
	String DATABASE_INIT = "database_init";
	/** 当前场景存储ID */
	String CUR_FLOOR_ID_KEY = "cur_floor_id_key";
	/** 当前房间存储ID */
	String CUR_FLOOR_ROOM_KEY = "cur_floor_room_key";
	//中控MAC 地址
	String CONTROL_MAC_ADDR = "control_mac_addr";
	//默认中控mac地址
	String CONTROL_MAC_ADDR_DEFAUL = "888888888888";
	/** 手动设防标志位 */
	String SAFE_ALARM_FLAG = "safe_alarm_flag";
	/** 自动设防标志位 */
	String SAFE_AUTOALARM_FLAG = "safe_auto_alarm_flag";
	
	/** 按键小尺寸 */
	byte LSIZE = 0x00;
	/** 按键中尺寸 */
	byte MSIZE = 0x01;
	/** 按键高尺寸 */
	byte HSIZE = 0x02;
	
	/** 数据库中的false */
	int DAOFALSE = 0x00;
	/** 数据库中的true */
	int DAOTRUE = 0x01;
	
	/** sd卡根目录 */
	String SDCARD = Environment.getExternalStorageDirectory().getPath();
	/** 设备封面路径 */
	String DEVICE_ICON_PATH = SDCARD + File.separator + "iTrack" + File.separator;
	/** 设备封面缩略图宽 */
	int DEVICE_ICON_WIDTH = 180;
	/** 设备封面缩略图高 */
	int DEVICE_ICON_HEIGHT = 180; 
	
	/** 电视类型 */
	int REMOTE_TYPE_1 = 1;
	/** 空调类型 */
	int REMOTE_TYPE_2 = 2;
	/** 机顶盒类型 */
	int REMOTE_TYPE_3 = 3;
	/** DVD类型 */
	int REMOTE_TYPE_4 = 4;
	/** 灯光类型 */
	int REMOTE_TYPE_5 = 5;
	/** 窗帘类型 */
	int REMOTE_TYPE_6 = 6;
	/** 风扇类型 */
	int REMOTE_TYPE_7 = 7;
	
	/** 内置图片 */
	public static final int[] PRESET_DEVICE_ICON = {
	};
	
}
