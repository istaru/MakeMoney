package com.mx.hb.moon.application;

import android.app.Activity;
import android.app.Application;
import android.app.Service;
import android.content.Context;
import android.os.Environment;
import android.os.Vibrator;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.alibaba.fastjson.JSONObject;
import com.baidu.mapapi.SDKInitializer;
import com.mx.hb.moon.base.Constants;
import com.mx.hb.moon.base.PrefShared;
import com.mx.hb.moon.service.LocationService;
import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiscCache;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.utils.StorageUtils;
import com.umeng.analytics.AnalyticsConfig;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import xz.ax.qr.os.df.DiyOfferWallManager;

/**
 * Created by Moon on 2016/4/5.
 */
public class MainApplication extends Application {
    private static Context context;
    public static LocationService locationService;
    public Vibrator mVibrator;

    private static MainApplication instance;
    private static List<Activity> activityList = new LinkedList<Activity>();

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
        initBaiduLoca();
        initImageLoader(context);
    }

    /**
     * 单例模式中获取唯一的MyApplication实例
     * @return
     */
    public static MainApplication getInstance() {
        if(null == instance) {
            instance = new MainApplication();
        }
        return instance;
    }

    /**
     * 添加Activity到容其中
     * @param activity
     */
    public static void addActivity(Activity activity){
        activityList.add(activity);
    }

    /**
     * 遍历所有Activity并finish
     * 回收有米SDK、万普SDK中的内容
     */
    public static void exit(){
        for(Activity activity : activityList){
            activity.finish();
        }
        DiyOfferWallManager.getInstance(context).onAppExit();
        System.exit(0);
    }

    /***
     * 初始化定位sdk，建议在Application中创建
     */
    private void initBaiduLoca() {
        locationService = new LocationService(context);
        mVibrator =(Vibrator)context.getSystemService(Service.VIBRATOR_SERVICE);
        SDKInitializer.initialize(context);
    }
    

    /** 初始化ImageLoader */
    public static void initImageLoader(Context context) {
        File cacheDir = StorageUtils.getOwnCacheDirectory(context, Constants.CACHEDIRECTORY_PATH + "images");//获取到缓存的目录地址
        //创建配置ImageLoader(所有的选项都是可选的,只使用那些你真的想定制)，这个可以设定在APPLACATION里面，设置为全局的配置参数
        ImageLoaderConfiguration config = new ImageLoaderConfiguration
                .Builder(context)
                //.memoryCacheExtraOptions(480, 800) // max width, max height，即保存的每个缓存文件的最大长宽
                //.discCacheExtraOptions(480, 800, CompressFormat.JPEG, 75, null) // Can slow ImageLoader, use it carefully (Better don't use it)设置缓存的详细信息，最好不要设置这个
                .threadPoolSize(3)//线程池内加载的数量
                .threadPriority(Thread.NORM_PRIORITY - 2)
                .denyCacheImageMultipleSizesInMemory()
                        //.memoryCache(new UsingFreqLimitedMemoryCache(2 * 1024 * 1024)) // You can pass your own memory cache implementation你可以通过自己的内存缓存实现
                        //.memoryCacheSize(2 * 1024 * 1024)
                        ///.discCacheSize(50 * 1024 * 1024)
                .discCacheFileNameGenerator(new Md5FileNameGenerator())//将保存的时候的URI名称用MD5 加密
                        //.discCacheFileNameGenerator(new HashCodeFileNameGenerator())//将保存的时候的URI名称用HASHCODE加密
                .tasksProcessingOrder(QueueProcessingType.LIFO)
                        //.discCacheFileCount(100) //缓存的File数量
                .discCache(new UnlimitedDiscCache(cacheDir))//自定义缓存路径
                        //.defaultDisplayImageOptions(DisplayImageOptions.createSimple())
                        //.imageDownloader(new BaseImageDownloader(context, 5 * 1000, 30 * 1000)) // connectTimeout (5 s), readTimeout (30 s)超时时间
                .writeDebugLogs() // Remove for release app
                .build();
        // Initialize ImageLoader with configuration.
        ImageLoader.getInstance().init(config);//全局初始化此配置
    }
}
