package com.mx.hb.moon.base;



import android.content.Context;

public class PrefShared {

	public static void saveString(Context context, String key, String str) {
		context.getSharedPreferences("com.mx.hb.moon", 0).edit()
				.putString(key, str).commit();
	}

	public static String getString(Context context, String key) {
		return context.getSharedPreferences("com.mx.hb.moon", 0)
				.getString(key, null);
	}
	
	public static void saveLong(Context context, String key, long str) {
		context.getSharedPreferences("com.mx.hb.moon", 0).edit()
				.putLong(key, str).commit();
	}
	
	public static Long getLong(Context context, String key){
		return context.getSharedPreferences("com.mx.hb.moon", 0).getLong(key, 0);
	}

	public static void removeData(Context context, String key) {
		context.getSharedPreferences("com.mx.hb.moon", 0).edit()
				.remove(key).commit();
	}
}
