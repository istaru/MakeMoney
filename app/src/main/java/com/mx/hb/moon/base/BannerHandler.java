package com.mx.hb.moon.base;

import android.content.Context;
import android.os.Handler;

/**
 * 为了防止内存泄漏，定义外部类，防止内部类对外部类的引用
 */
public class BannerHandler extends Handler {
	 Context context;

	public BannerHandler(Context context) {
		this.context = context;
	}
};