package com.mx.hb.moon.base;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.ImageView;

import com.mx.hb.moon.R;
import com.mx.hb.moon.application.Options;
import com.nostra13.universalimageloader.core.ImageLoader;
/**
 * ImageView创建工厂
 */
public class ViewFactory {

	/**
	 * 获取ImageView视图的同时加载显示url
	 * 
	 * @param
	 * @return
	 */
	public static ImageView getImageView(Context context, String url) {
		ImageView imageView = (ImageView)LayoutInflater.from(context).inflate(R.layout.view_banner, null);
		ImageLoader.getInstance().displayImage(url, imageView);
		return imageView;
	}
}
