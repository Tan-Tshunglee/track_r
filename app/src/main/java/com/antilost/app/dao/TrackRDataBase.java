package com.antilost.app.dao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * 数据库管理
 *
 */
public class TrackRDataBase extends SQLiteOpenHelper {
	
	public static final int DB_VERSION = 1;
	public static final String DB_NAME = "TrackR.db";
	private final Context mContext;

	public TrackRDataBase(Context context) {
		super(context, DB_NAME, null, DB_VERSION);
		mContext = context;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		updateDatabase(mContext, db, 0, DB_VERSION);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		updateDatabase(mContext, db, oldVersion, newVersion);
	}
	
	private static void updateDatabase(Context context, SQLiteDatabase db, int fromVersion, int toVersion){
		// 如果表存在，先删除表
		db.execSQL(LocationTable.GEN_DROP_TABLE);
		db.execSQL(UserDataTable.GEN_DROP_TABLE);

		
		// 创建表
		db.execSQL(LocationTable.GEN_CREATE_TABLE);
		db.execSQL(UserDataTable.GEN_CREATE_TABLE);
	}

	
	public final Context getContext(){
		return mContext;
	}

	/**
	 * 获取只读数据库
	 */
	public final SQLiteDatabase getReadDatabase(){
		return getReadableDatabase();
	}
	
	/**
	 * 获取只写数据库
	 */
	public final SQLiteDatabase getWritDatabase(){
		return getWritableDatabase();
	}
	
	/**
	 * 关闭数据库
	 */
	public final void closeDatabase(){
		close();
	}
	
}
