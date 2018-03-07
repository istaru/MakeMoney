package com.mx.hb.moon.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.mx.hb.moon.R;
import com.mx.hb.moon.application.MainApplication;
import com.mx.hb.moon.base.AES;
import com.mx.hb.moon.base.AsyncUtil;
import com.mx.hb.moon.base.BaseTools;
import com.mx.hb.moon.base.Constants;
import com.mx.hb.moon.base.PermissionsChecker;
import com.mx.hb.moon.base.PrefShared;
import com.mx.hb.moon.entity.AppInfo;
import com.mx.hb.moon.service.LocationService;
import com.mx.hb.moon.view.StatusBarUtil;
import com.umeng.analytics.AnalyticsConfig;
import com.umeng.message.PushAgent;
import com.umeng.message.UmengRegistrar;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Created by Moon on 2016/4/5.
 */
public class WelcomeActivity extends BaseActivity {
    private AlphaAnimation start_anima;
    View view;
    private LocationService locationService;

//    private static final int REQUEST_CODE = 0; // 请求码
//    // 所需的全部权限
//    static final String[] PERMISSIONS = new String[]{
//            Manifest.permission.GET_ACCOUNTS,//访问GMail账户列表
//            Manifest.permission.ACCESS_FINE_LOCATION,//通过GPS芯片接收卫星的定位信息，定位精度达10米以内
//            Manifest.permission.ACCESS_COARSE_LOCATION,//通过WiFi或移动基站的方式获取用户错略的经纬度信息，定位精度大概误差在30~1500米
//            Manifest.permission.READ_PHONE_STATE,//访问电话状态
//            Manifest.permission.CALL_PHONE,//允许程序从非系统拨号器里输入电话号码
//            Manifest.permission.WRITE_EXTERNAL_STORAGE//允许程序写入外部存储，如SD卡上写文件
//    };
//
//    private PermissionsChecker mPermissionsChecker; // 权限检测器

    final Map<String,Object> map = new HashMap<String,Object>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,  WindowManager.LayoutParams.FLAG_FULLSCREEN);
        view = View.inflate(this, R.layout.activity_welcom, null);
//        mPermissionsChecker = new PermissionsChecker(this);
//        // 缺少权限时, 进入权限配置页面
//        if (mPermissionsChecker.lacksPermissions(PERMISSIONS)) {
//            startPermissionsActivity();
//        } else {
            setContentView(view);
            phoneMessage();
            initSDK();
            initData();
//        }
    }

    private void initSDK() {
        initPush();
        initUmeng();
    }

    @Override
    protected void setStatusBar() {
        StatusBarUtil.setTranslucent(this,0);
    }

    private void initData(){
        start_anima = new AlphaAnimation(1.0f, 1.0f);
        start_anima.setDuration(2000);
        view.startAnimation(start_anima);
        start_anima.setAnimationListener(new Animation.AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                // TODO Auto-generated method stub
                startMain();
            }
        });
    }

    private void phoneMessage() {
        findByPhoneData();
        getNetworkType();
        getIpAddress();
//        queryAppInfo();
//        getUUID();
//        Log.e("硬件码",getUniquePsuedoID());
    }

    private void startMain() {
        String userId = PrefShared.getString(context,"userId");
        String userIcon = PrefShared.getString(context,"userIcon");
        String qrcode = PrefShared.getString(context,"qrcode");
        String UID = PrefShared.getString(context,"UID");
        String objectId = PrefShared.getString(context,"objectId");
        if(userId != null && !TextUtils.equals("",userId) &&
           userIcon != null && !TextUtils.equals("",userIcon) &&
           qrcode != null && !TextUtils.equals("",qrcode) &&
           UID != null && !TextUtils.equals("",UID) &&
           objectId!= null && !TextUtils.equals("",objectId)){
            startActivity(new Intent(context, MainActivity.class));
        } else {
            startActivity(new Intent(context, LoginActivity.class));
        }
        this.finish();
    }

    /**
     * 初始友盟推送
     */
    private void initPush() {
        String device_token = UmengRegistrar.getRegistrationId(context);
        Log.e("正式获取友盟的device_token",device_token);
        PushAgent mPushAgent = PushAgent.getInstance(context);
        mPushAgent.enable();
    }

    /**
     * 初始化友盟
     */
    private void initUmeng() {
        AnalyticsConfig.setAppkey(context, Constants.UM_APPID);//填写appkey
        AnalyticsConfig.setChannel("");//填写channel（应用的推广渠道名称）
//        MobclickAgent.openActivityDurationTrack(false);//禁止默认的页面统计方式，这样将不会再自动统计Activity。
        AnalyticsConfig.enableEncrypt(true);//设置是否对日志信息进行加密, 默认false(不加密)
    }

    /**
     * 注册EventBus
     */
    @Override
    public void onStart() {
        super.onStart();
        // -----------location config ------------
        locationService = MainApplication.locationService;
        //获取locationservice实例，建议应用中只初始化1个location实例，然后使用，可以参考其他示例的activity，都是通过此种方式获取locationservice实例的
        locationService.registerListener(mListener);
        //注册监听
        int type = getIntent().getIntExtra("from", 0);
        if (type == 0) {
            locationService.setLocationOption(locationService.getDefaultLocationClientOption());
        } else if (type == 1) {
            locationService.setLocationOption(locationService.getOption());
        }
        locationService.start();// 定位SDK
    }

    /**
     *定位回调
     */
    private BDLocationListener mListener = new BDLocationListener() {
        @Override
        public void onReceiveLocation(BDLocation location) {
            // TODO Auto-generated method stub
            if (null != location && location.getLocType() != BDLocation.TypeServerError) {
                double latitude = location.getLatitude();//latitude 经度
                double longitude = location.getLongitude();//longitude 纬度
                String address = location.getAddrStr();//详细地址
                map.put("latitude",latitude);
                map.put("longitude",longitude);
                map.put("address",address);
            }
            locationService.stop();
        }
    };


    @Override
    protected void onStop() {
        // TODO Auto-generated method stub
        locationService.unregisterListener(mListener); //注销掉监听
        locationService.stop(); //停止定位服务
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        PrefShared.saveString(context,"userData",JSON.toJSONString(map));
    }

//    private void startPermissionsActivity() {
//        PermissionsActivity.startActivityForResult(this, REQUEST_CODE, PERMISSIONS);
//    }

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        // 拒绝时, 关闭页面, 缺少主要权限, 无法运行
//        if (requestCode == REQUEST_CODE && resultCode == PermissionsActivity.PERMISSIONS_DENIED) {
//            finish();
//        }
//    }

    /**
     * 查找用户使用手机的基本信息
     */
    private void findByPhoneData() {
        AsyncUtil asyncUtil = new AsyncUtil() {
            @Override
            public void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            public Long doInBackground(String... params) {
                //获取手机IMSI,imei
                TelephonyManager mTelephonyMgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
                String phoneName = android.os.Build.MANUFACTURER;// 手机名称
                String phoneType = android.os.Build.MODEL; // 手机型号
                String systemVersion = android.os.Build.VERSION.RELEASE;//系统版本
                int width = BaseTools.getWindowsWidth(WelcomeActivity.this);// 屏幕宽度
                int height = BaseTools.getWindowsHeight(WelcomeActivity.this); //屏幕高度
                String imsi = "";
                String imei = "";
                String phoneNumer = "";
                String network = "";
                String versionName = "";
                try {
                    imsi = mTelephonyMgr.getSubscriberId(); // 手机IMSI号码
                    imei = mTelephonyMgr.getDeviceId(); // 手机IMEI号码
                    phoneNumer = mTelephonyMgr.getLine1Number(); // 手机号码，有的可得，有的不可得
                    network = mTelephonyMgr.getNetworkOperator(); //手机MNC与MCC号码
                    if(null != imsi || !TextUtils.equals(imsi,"")){
//                        Log.e("IMSI号码:",imsi);
                    } else {
//                        Log.e("IMSI号码:","为空");
                        imsi = "";
                    }

                    if(null != imei || !TextUtils.equals(imei,"")){
                        PrefShared.saveString(context,"UUID",imei);
                    } else {
                        PrefShared.saveString(context,"UUID",null);
                        imei = "";
                    }

                    if(null != phoneName || !TextUtils.equals(phoneNumer,"")){
                        PrefShared.saveString(context, "phoneNumer", phoneNumer);
                    } else {
                        PrefShared.saveString(context, "phoneNumer","");
                        phoneNumer = "";
                    }

                    if(null != network || !TextUtils.equals(network,"")){
//                        Log.e("MNC与MCC号码:",network);
                    } else {
//                        Log.e("MNC与MCC号码:","为空");
                        network = "";
                    }
                    versionName = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
                } catch (Exception e) {
                    e.printStackTrace();
                }
//                Log.e("手机名称:", phoneName);
//                Log.e("手机型号:", phoneType);
//                Log.e("系统版本:",systemVersion);
//                Log.e("屏幕宽度:", width + "");
//                Log.e("屏幕高度:", height + "");

                map.put("phoneName",phoneName);// 手机名称
                map.put("phoneType",phoneType);// 手机型号
                map.put("systemVersion",systemVersion);//系统版本
//                map.put("width",width);//屏幕宽度
//                map.put("height",height);//屏幕高度
                map.put("imsi",imsi);// 手机IMSI号码
                map.put("imei",imei);// 手机IMEI号码
                map.put("phoneNumer",phoneNumer);// 手机号码，有的可得，有的不可得
                map.put("versionName",versionName);// APP版本信息
//                map.put("network",network);//手机MNC与MCC号码
                return null;
            }

            @Override
            public void onProgressUpdate(Integer... values) {
                super.onProgressUpdate(values);
            }

            @Override
            public void onPostExecute(Long result) {
                super.onPostExecute(result);
            }
        };
        asyncUtil.execute();
    }

    public void getIpAddress(){
        AsyncUtil asyncUtil = new AsyncUtil(){
            @Override
            public Long doInBackground(String... params) {
                try {
                    for (Enumeration en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                        NetworkInterface intf = (NetworkInterface) en.nextElement();
                        for (Enumeration enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                            InetAddress inetAddress = (InetAddress) enumIpAddr.nextElement();
                            if (!inetAddress.isLoopbackAddress() && !inetAddress.isLinkLocalAddress() && !TextUtils.equals(inetAddress.getHostAddress(),"10.0.2.15")) {
                                Log.e("ip",inetAddress.getHostAddress());
                                map.put("ip",inetAddress.getHostAddress());
                            }
                        }
                    }
                } catch (SocketException ex) {
                    Log.e("ip", ex.toString());
                    map.put("ip",ex.toString());
                }
                return null;
            }
        };
        asyncUtil.execute();
    }

    /**
     * 获取网络类型
     * @return
     */
    public void getNetworkType() {
        AsyncUtil asyncUtil = new AsyncUtil() {
            @Override
            public void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            public Long doInBackground(String... params) {
                try {
                    NetworkInfo networkInfo = ((ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
                    if (networkInfo != null && networkInfo.isConnected()) {
                        if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                            WifiInfo wifiInfo = ((WifiManager) context.getSystemService(WIFI_SERVICE)).getConnectionInfo();
                            String wifiSsId = wifiInfo.getSSID();
                            String wifiBssId = wifiInfo.getBSSID();
                            map.put("netType","WIFI");
//                            map.put("wifiSsId",wifiSsId);
                            map.put("wifiBssId",wifiBssId);
                        } else if (networkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
                            String _strSubTypeName = networkInfo.getSubtypeName();
//                            Log.e("cocos2d-x", "Network getSubtypeName : " + _strSubTypeName);
                            // TD-SCDMA   networkType is 17
                            int networkType = networkInfo.getSubtype();
                            switch (networkType) {
                                case TelephonyManager.NETWORK_TYPE_GPRS:
                                case TelephonyManager.NETWORK_TYPE_EDGE:
                                case TelephonyManager.NETWORK_TYPE_CDMA:
                                case TelephonyManager.NETWORK_TYPE_1xRTT:
                                case TelephonyManager.NETWORK_TYPE_IDEN: //api<8 : replace by 11
                                    map.put("netType","2G");
                                    break;
                                case TelephonyManager.NETWORK_TYPE_UMTS:
                                case TelephonyManager.NETWORK_TYPE_EVDO_0:
                                case TelephonyManager.NETWORK_TYPE_EVDO_A:
                                case TelephonyManager.NETWORK_TYPE_HSDPA:
                                case TelephonyManager.NETWORK_TYPE_HSUPA:
                                case TelephonyManager.NETWORK_TYPE_HSPA:
                                case TelephonyManager.NETWORK_TYPE_EVDO_B: //api<9 : replace by 14
                                case TelephonyManager.NETWORK_TYPE_EHRPD:  //api<11 : replace by 12
                                case TelephonyManager.NETWORK_TYPE_HSPAP:  //api<13 : replace by 15
                                    map.put("netType","3G");
                                    break;
                                case TelephonyManager.NETWORK_TYPE_LTE:    //api<11 : replace by 13
                                    map.put("netType","4G");
                                    break;
                                default:
                                    // http://baike.baidu.com/item/TD-SCDMA 中国移动 联通 电信 三种3G制式
                                    if (_strSubTypeName.equalsIgnoreCase("TD-SCDMA") || _strSubTypeName.equalsIgnoreCase("WCDMA") || _strSubTypeName.equalsIgnoreCase("CDMA2000")) {
                                        map.put("netType","3G");
                                    } else {
                                        map.put("netType",_strSubTypeName);
                                    }
                                    break;
                            }
//                            Log.e("cocos2d-x", "Network getSubtype : " + Integer.valueOf(networkType).toString());
                        }
                    }
//                    Log.e("网络连接类型为:", strNetworkType[0]);
                } catch (Exception e){
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            public void onProgressUpdate(Integer... values) {
                super.onProgressUpdate(values);
            }

            @Override
            public void onPostExecute(Long result) {
                super.onPostExecute(result);
            }
        };
        asyncUtil.execute();
    }

    /**
     * 获取手机中所有的APP包名
     */
    public List<AppInfo> queryAppInfo(){
        final List<AppInfo> appInfos = new ArrayList<AppInfo>();
        AsyncUtil asyncUtil = new AsyncUtil() {
            @Override
            public void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            public Long doInBackground(String... params) {
                PackageManager packageManager = context.getPackageManager(); //获得PackageManager对象
                List<PackageInfo> packageInfos = packageManager.getInstalledPackages(PackageManager.GET_UNINSTALLED_PACKAGES);//获取到所有安装了的应用程序的信息，包括那些卸载了的，但没有清除数据的应用程序
                for(PackageInfo info:packageInfos){
                    AppInfo appInfo = new AppInfo();
                    String packageName = info.packageName;//拿到包名
                    ApplicationInfo applicationInfo = info.applicationInfo;//拿到应用程序的信息
                    String appName = applicationInfo.loadLabel(context.getPackageManager()).toString();//应用程序名称
                    Drawable appIcon = applicationInfo.loadIcon(context.getPackageManager());//拿到应用程序的图标
                    appInfo.setPackageName(packageName);
                    appInfo.setAppName(appName);
                    appInfo.setAppIcon(appIcon);
                    if(filterApp(applicationInfo)){
                        appInfo.setIsSystem(false);
                    } else {
                        appInfo.setIsSystem(true);
                    }
                    appInfos.add(appInfo);
                }
//                Log.e("共有",appInfos.size()+"款软件");
                for (int i = 0; i < appInfos.size(); i++) {
                    String appType = "";
                    if(appInfos.get(i).isSystem()){
                        appType = "是系统软件";
                    } else {
                        appType = "是民间软件";
                    }
//                    Log.e("应用名为", appInfos.get(i).getAppName() + "，" + appType + "：" + appInfos.get(i).getPackageName());
                    System.out.println("软件类型："+appInfos.get(i).isSystem());
                    System.out.println("-------------------");
                }
                return null;
            }

            @Override
            public void onProgressUpdate(Integer... values) {
                super.onProgressUpdate(values);
            }

            @Override
            public void onPostExecute(Long result) {
                super.onPostExecute(result);
            }
        };
        asyncUtil.execute();

//        final List<AppInfo> appInfos = new ArrayList<AppInfo>();
//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                PackageManager packageManager = context.getPackageManager(); //获得PackageManager对象
//                List<PackageInfo> packageInfos = packageManager.getInstalledPackages(PackageManager.GET_UNINSTALLED_PACKAGES);//获取到所有安装了的应用程序的信息，包括那些卸载了的，但没有清除数据的应用程序
//                for (PackageInfo info : packageInfos) {
//                    AppInfo appInfo = new AppInfo();
//                    String packageName = info.packageName;//拿到包名
//                    ApplicationInfo applicationInfo = info.applicationInfo;//拿到应用程序的信息
//                    String appName = applicationInfo.loadLabel(context.getPackageManager()).toString();//应用程序名称
//                    Drawable appIcon = applicationInfo.loadIcon(context.getPackageManager());//拿到应用程序的图标
//                    appInfo.setPackageName(packageName);
//                    appInfo.setAppName(appName);
//                    appInfo.setAppIcon(appIcon);
//                    if (filterApp(applicationInfo)) {
//                        appInfo.setIsSystem(false);
//                    } else {
//                        appInfo.setIsSystem(true);
//                    }
//                    appInfos.add(appInfo);
//                }
//                Log.e("共有", appInfos.size() + "款软件");
//                for (int i = 0; i < appInfos.size(); i++) {
//                    String appType = "";
//                    if (appInfos.get(i).isSystem()) {
//                        appType = "是系统软件";
//                    } else {
//                        appType = "是民间软件";
//                    }
////                    Log.e("应用名为", appInfos.get(i).getAppName() + "，" + appType + "：" + appInfos.get(i).getPackageName());
////                    System.out.println("软件类型："+appInfos.get(i).isSystem());
////                    System.out.println("-------------------");
//                }
//            }
//        });
        return appInfos;
    }

    /**
     *判断某一个应用程序是不是系统的应用程序，
     *如果是返回true，否则返回false。
     */
    public static boolean filterApp(ApplicationInfo info){
        //有些系统应用是可以更新的，如果用户自己下载了一个系统的应用来更新了原来的，它还是系统应用，这个就是判断这种情况的
        if((info.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0){
            return true;
        }else if((info.flags & ApplicationInfo.FLAG_SYSTEM) == 0){//判断是不是系统应用
            return true;
        }
        return false;
    }

    private void getUUID() {
        try {
            final TelephonyManager tm = (TelephonyManager) getBaseContext().getSystemService(TELEPHONY_SERVICE);
            final String tmDevice, tmSerial, tmPhone, androidId;
            tmDevice = "" + tm.getDeviceId();
//          tmSerial = "" + tm.getSimSerialNumber();
            androidId = "" + android.provider.Settings.Secure.getString(getContentResolver(),android.provider.Settings.Secure.ANDROID_ID);
            UUID deviceUuid = new UUID(androidId.hashCode(),((long) tmDevice.hashCode() << 32)/* | tmSerial.hashCode()*/);
            String uniqueId = deviceUuid.toString();
            uniqueId = uniqueId.substring(9, uniqueId.length()-8).replaceAll("-","").toUpperCase();
            String md5 = AES.md5(uniqueId);
            PrefShared.saveString(context,"UUID",uniqueId);
//            Log.e("手机唯一标识：", uniqueId);
//            Log.e("加密后的",md5);
//            Log.e("getUniquePsuedoID",getUniquePsuedoID());
        } catch (Exception e){
            System.out.print("获取UUID错误！");
            PrefShared.saveString(context,"UUID",null);
            e.printStackTrace();
        }
    }

    //获得独一无二的Psuedo ID
    public static String getUniquePsuedoID() {
        String serial = null;
        String m_szDevIDShort = "35" +
                Build.BOARD.length() % 10 + Build.BRAND.length() % 10 +
                Build.CPU_ABI.length() % 10 + Build.DEVICE.length() % 10 +
                Build.DISPLAY.length() % 10 + Build.HOST.length() % 10 +
                Build.ID.length() % 10 + Build.MANUFACTURER.length() % 10 +
                Build.MODEL.length() % 10 + Build.PRODUCT.length() % 10 +
                Build.TAGS.length() % 10 + Build.TYPE.length() % 10 +
                Build.USER.length() % 10; //13 位
        try {
            serial = android.os.Build.class.getField("SERIAL").get(null).toString();
            //API>=9 使用serial号
            return new UUID(m_szDevIDShort.hashCode(), serial.hashCode()).toString();
        } catch (Exception exception) {
            //serial需要一个初始化
            serial = "serial"; // 随便一个初始化
        }
        //使用硬件信息拼凑出来的15位号码
        return new UUID(m_szDevIDShort.hashCode(), serial.hashCode()).toString();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
        //拦截返回按鈕按钮点击事件，让他无任何操作
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}

