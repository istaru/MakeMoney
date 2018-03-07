package com.mx.hb.moon.view;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * 自定义高度的viewpapger
 */
public class BannerViewPager extends ViewPager {
	private boolean isCanScroll = false;

	public BannerViewPager(Context context) {
		super(context);
	}

	public BannerViewPager(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	/**
	 * 设置viewpager是否可以滚动
	 *
	 * @param isCanScroll
	 */
	public void setScrollable(boolean isCanScroll) {
		this.isCanScroll = isCanScroll;
	}

	@Override
	public void scrollTo(int x, int y) {
		super.scrollTo(x, y);
	}

	@Override
	public boolean onTouchEvent(MotionEvent arg0) {
		// TODO Auto-generated method stub
		if (isCanScroll) {
			return super.onTouchEvent(arg0);
		} else {
			return false;
		}

	}

	@Override
	public void setCurrentItem(int item, boolean smoothScroll) {
		// TODO Auto-generated method stub
		super.setCurrentItem(item, smoothScroll);
	}

	@Override
	public void setCurrentItem(int item) {
		// TODO Auto-generated method stub
		super.setCurrentItem(item);
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent arg0) {
		// TODO Auto-generated method stub
		if (isCanScroll) {
			return super.onInterceptTouchEvent(arg0);
		} else {
			return false;
		}

	}
}