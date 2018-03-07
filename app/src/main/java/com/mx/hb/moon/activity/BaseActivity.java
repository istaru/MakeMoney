package com.mx.hb.moon.activity;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.mx.hb.moon.R;
import com.mx.hb.moon.application.MainApplication;
import com.mx.hb.moon.view.StatusBarUtil;
import com.mx.hb.moon.view.SystemBarTintManager;
import com.umeng.analytics.MobclickAgent;
import com.umeng.message.PushAgent;

/**
 * Created by Moon on 2016/4/5.
 */
public class BaseActivity extends AppCompatActivity {
    public static Context context;
    public SystemBarTintManager tintManager;
    public boolean processFlag = true; //默认可以点击

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        setStatusBar();
    }

    @Override
    public void setContentView(View view) {
        super.setContentView(view);
        setStatusBar();
    }

    protected void setStatusBar() {
//        StatusBarUtil.setColor(this, getResources().getColor(R.color.gules));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        MainApplication.getInstance().addActivity(this);
        PushAgent.getInstance(context).onAppStart();//统计应用启动数据(此方法与统计分析sdk中统计日活的方法无关！请务必调用此方法！)
//        setTranslucentStatus(R.color.gules);
    }

    /**
     * 设置状态栏背景状态
     */
    public void setTranslucentStatus(int color) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window win = getWindow();
            WindowManager.LayoutParams winParams = win.getAttributes();
            final int bits = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
            winParams.flags |= bits;
            win.setAttributes(winParams);
        }
        tintManager = new SystemBarTintManager(this);// 创建状态栏的管理实例
        tintManager.setStatusBarTintEnabled(true);// 激活状态栏设置
//        tintManager.setNavigationBarTintEnabled(true);// 激活导航栏设置
        tintManager.setStatusBarTintColor(getResources().getColor(color));//设置状态栏颜色
    }

    /**
     * 添加统计应用时长的(也就是Session时长,当然还包括一些其他功能)
     */
    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(context);
    }

    /**
     * 添加统计页面跳转
     */
    public void onPause() {
        super.onPause();
        MobclickAgent.onPause(context);
    }

    /**
     * 设置按钮在短时间内被重复点击的有效标识（true表示点击有效，false表示点击无效）
     */
    protected synchronized void setProcessFlag() {
        processFlag = false;
    }

    /**
     * 计时线程（防止在一定时间段内重复点击按钮）
     */
    protected class TimeThread extends Thread {
        public void run() {
            try {
                sleep(1000);
                processFlag = true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
