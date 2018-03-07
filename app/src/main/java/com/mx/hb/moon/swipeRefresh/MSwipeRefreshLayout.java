package com.mx.hb.moon.swipeRefresh;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewConfiguration;

/**
 * Created by Kiven on 16/7/7.
 */
public class MSwipeRefreshLayout extends android.support.v4.widget.SwipeRefreshLayout {

    private int mTouchSlop;
    private float mPrevX;

    public MSwipeRefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        //判断用户在进行滑动操作的最小距离
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mPrevX = MotionEvent.obtain(event).getX();
                break;

            case MotionEvent.ACTION_MOVE:
                final float eventX = event.getX();
                //获取水平移动距离
                float xDiff = Math.abs(eventX - mPrevX);
                //当水平移动距离大于滑动操作的最小距离的时候就认为进行了横向滑动
                //不进行事件拦截,并将这个事件交给子View处理
                if (xDiff > mTouchSlop) {
                    return false;
                }
        }
        return super.onInterceptTouchEvent(event);
    }
}
