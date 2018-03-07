package com.mx.hb.moon.activity;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.moonmmxhb.DevInit;
import com.mx.hb.moon.AppConnect;
import com.mx.hb.moon.R;
import com.mx.hb.moon.application.MainApplication;
import com.mx.hb.moon.base.AES;
import com.mx.hb.moon.base.AsyncHttpClientUtil;
import com.mx.hb.moon.base.AsyncUtil;
import com.mx.hb.moon.base.BaseTools;
import com.mx.hb.moon.base.Constants;
import com.mx.hb.moon.base.NetWorkUtils;
import com.mx.hb.moon.base.PrefShared;
import com.mx.hb.moon.base.ViewFactory;
import com.mx.hb.moon.dialog.NormalDialog;
import com.mx.hb.moon.dialog.OnBtnClickL;
import com.mx.hb.moon.entity.ADInfo;
import com.mx.hb.moon.entity.AlipayEvent;
import com.mx.hb.moon.entity.WXLoginEvent;
import com.mx.hb.moon.loading.SVProgressHUD;
import com.mx.hb.moon.swipeRefresh.MSwipeRefreshLayout;
import com.mx.hb.moon.view.BannerImageView;
import com.mx.hb.moon.view.StatusBarUtil;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.tencent.mm.sdk.modelmsg.SendAuth;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.yql.dr.sdk.DRSdk;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import xz.ax.qr.AdManager;
import xz.ax.qr.os.OffersManager;
import xz.ax.qr.os.df.DiyOfferWallManager;


/**
 * Created by Moon on 2016/4/18.
 */
public class MainActivity extends BaseActivity implements View.OnClickListener,SwipeRefreshLayout.OnRefreshListener/*, PointsChangeNotify, UpdatePointsListener, DataListener,DRScoreInterface*/ {
    public static MSwipeRefreshLayout mSwipeLayout;
    private long mExitTime;
    private RelativeLayout userIcon;
    private ImageView icon;
    private TextView balance, uuId,nickName;
    private BannerImageView cycleViewPager;
    private LinearLayout mianllt1, mianllt2, itemClick;
    private LinearLayout wallet, makeMoney, share, deposit, help;
    String UUID = "";
    String objectId = "";

    private static SVProgressHUD mSVProgressHUD;
    private String WXUID = "";
    private IWXAPI iwxapi;
    int versionCode = 1;
    String versionName = "1.0";
    String versionN = "";
    String updateMsg = "";
    public static String apkUrl = "";
    private String danjiType = "";
    //是否是第一次使用
    private boolean isFirstRefresh;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        UUID = PrefShared.getString(context, "UUID");
        objectId = PrefShared.getString(context,"objectId");
//        setTranslucentStatus(R.color.bTransparent);
        setContentView(R.layout.activity_main);
        mSVProgressHUD = new SVProgressHUD(context);
        EventBus.getDefault().register(this);//注册EventBus
//        initGuide();
        initView();
        initWX();
        initSDK();
        findByUserMes();
        setBanner();
        findByUpdateMsg();
        sendUseData();
        IntentFilter intentFilter = new IntentFilter(Constants.SENDMSG_REFRESH);
        context.registerReceiver(broadcastReceiver, intentFilter);
//        findByAPK();
    }

    private void initGuide(){
        //读取SharedPreferences中需要的数据
        SharedPreferences preferences = context.getSharedPreferences("isFirstRefresh",0);
        isFirstRefresh = preferences.getBoolean("isFirstRefresh", true);
        if(isFirstRefresh) {
            guideRefresh();
        }
        //实例化Editor对象
        SharedPreferences.Editor editor = preferences.edit();
        //存入数据
        editor.putBoolean("isFirstRefresh", false);
        //提交修改
        editor.commit();
    }

    private void guideRefresh() {
        final Dialog dialog = new Dialog(context, R.style.Dialog_Fullscreen);
        dialog.setContentView(R.layout.guide_main);
        RelativeLayout relativeLayout = (RelativeLayout) dialog.findViewById(R.id.guideMain);
        dialog.show();
        relativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }

    /**
     * 注册广播实时更新用户信息
     */
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            findByUserMes();
        }
    };

    private void initWX() {
        iwxapi = WXAPIFactory.createWXAPI(context, Constants.WX_APPID, false);
        iwxapi.registerApp(Constants.WX_APPID);
    }

    /**
     * 查找用户可提现金额
     */
    private void findByUserMes() {
        AsyncHttpClientUtil asyncHttpClientUtil = new AsyncHttpClientUtil(0,new HashMap<String,String>());
        RequestParams params = new RequestParams();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("token", Constants.TOKENT);
        jsonObject.put("id", PrefShared.getString(context,"userId"));

//        long num = AES.get10Random();
//        String content = AES.encrypt(jsonObject.toString(),AES.md5(AES.longMinusNum(num+"")));
//        JSONObject json = new JSONObject();
//        json.put("content",content);
//        json.put("secret",num+"");
//        json.put("hehe",1);

        params.put(Constants.REQUEST_MSG,jsonObject.toString());
        asyncHttpClientUtil.doPost(Constants.FIND_USER_MSG, params, new AsyncHttpResponseHandler() {
            @Override
            public void onStart() {
                super.onStart();
            }

            @Override
            public void onSuccess(int statusCode, org.apache.http.Header[] headers, byte[] bytes) {
                try {
                    String result = new String(bytes, "UTF-8");
                    JSONObject jsonObject = JSONObject.parseObject(result);
//                    Log.e("findByUserMes",jsonObject.toString());
                    String code = jsonObject.getString("code");
                    jsonObject = JSONObject.parseObject(jsonObject.getString("data"));

                    JSONObject haveObject = jsonObject.getJSONObject("have");
                    String haveCash = haveObject.getString("cash");//可提现

                    JSONObject usedObject = jsonObject.getJSONObject("used");
                    String usedCash = usedObject.getString("cash");//已提现

                    JSONObject walletObject = jsonObject.getJSONObject("wallet");
                    String walletCash = walletObject.getString("cash");//总收入

                    JSONObject infoObject = jsonObject.getJSONObject("info");
                    String nickname = infoObject.getString("nickname");
                    String zfbName = infoObject.getString("name");
                    String zhifubao = infoObject.getString("zhifubao");
                    String wxId = infoObject.getString("wxid");
                    String iconUrl = infoObject.getString("icon");
                    WXUID = wxId;
                    if(TextUtils.equals("10",code)){
                        if(Float.parseFloat(haveCash) != 0){
                            balance.setText("￥" + haveCash);
                            PrefShared.saveString(context,"haveCash",haveCash);
                        } else {
                            balance.setText("￥0.00");
                            PrefShared.saveString(context,"haveCash","0.00");
                        }
                        if(Float.parseFloat(usedCash) > 0){
                            PrefShared.saveString(context,"usedCash",usedCash);
                        } else {
                            PrefShared.saveString(context,"usedCash","0.00");
                        }
                        if(Float.parseFloat(walletCash) > 0){
                            PrefShared.saveString(context,"walletCash",walletCash);
                        } else {
                            PrefShared.saveString(context,"walletCash","0.00");
                        }
                        if(!TextUtils.equals("",nickname)){
                            nickName.setText(nickname);
                        } else {
                            nickName.setText("");
                        }
                        if(iconUrl.contains("http://")){
                            ImageLoader.getInstance().displayImage(iconUrl, icon);
                        } else {
                            ImageLoader.getInstance().displayImage(Constants.REQUEST + iconUrl, icon);
                        }
                    } else {
                        Log.e("查找用户","返回错误数据");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int i, org.apache.http.Header[] headers, byte[] bytes, Throwable throwable) {
                Log.e("throwable.toString()",throwable.toString());
                if(throwable.toString().contains("ConnectTimeoutException")){
                    mSVProgressHUD.showInfoWithStatus("请求超时", SVProgressHUD.SVProgressHUDMaskType.GradientCancel);
                } else {
//                    mSVProgressHUD.showErrorWithStatus("请求错误", SVProgressHUD.SVProgressHUDMaskType.GradientCancel);
                }
            }

            @Override
            public void onFinish() {
                super.onFinish();
            }
        });
    }

    @Override
    protected void setStatusBar() {
        StatusBarUtil.setTransparent(MainActivity.this);
    }

    @SuppressLint("NewApi")
    private void initView() {
        mSwipeLayout = (MSwipeRefreshLayout) findViewById(R.id.main_swipe);
        mSwipeLayout.setColorSchemeResources(R.color.gules);
        mSwipeLayout.setOnRefreshListener(this);
        mSwipeLayout.setProgressViewOffset (false,0,172);
        userIcon = (RelativeLayout) findViewById(R.id.userIcon);
        icon = (ImageView) findViewById(R.id.icon);
        nickName = (TextView) findViewById(R.id.nickName);
        uuId = (TextView) findViewById(R.id.uuId);
        wallet = (LinearLayout) findViewById(R.id.wallet);
        balance = (TextView) findViewById(R.id.balance);
        cycleViewPager = (BannerImageView) getFragmentManager().findFragmentById(R.id.ad_view);
        mianllt1 = (LinearLayout) findViewById(R.id.mianllt1);
        mianllt2 = (LinearLayout) findViewById(R.id.mianllt2);
        itemClick = (LinearLayout) findViewById(R.id.itemClick);
        setItemHeight();
        makeMoney = (LinearLayout) findViewById(R.id.makeMoney);
        share = (LinearLayout) findViewById(R.id.share);
        deposit = (LinearLayout) findViewById(R.id.deposit);
        help = (LinearLayout) findViewById(R.id.help);
        userIcon.setOnClickListener(this);
        wallet.setOnClickListener(this);
        makeMoney.setOnClickListener(this);
        share.setOnClickListener(this);
        deposit.setOnClickListener(this);
        help.setOnClickListener(this);
        setUserMsg();
    }

    /**
     * 设置用户基本信息
     */
    private void setUserMsg() {
        uuId.setText(objectId);
    }

    private void initSDK() {
        initYoumi();
        initAppOffer();
        initDr();
        initDevNative();
    }

    /**
     * 初始化有米(源数据版)
     */
    private void initYoumi(){
        AdManager.getInstance(this).init(Constants.YM_APPID, Constants.YM_SECRET, false, true);
        // 请务必调用以下代码，告诉积分墙源数据SDK应用启动，可以让SDK进行一些初始化操作。该接口务必在SDK的初始化接口之后调用。
        DiyOfferWallManager.getInstance(this).onAppLaunch();
        // userid 不能为空 或者 空串,否则设置无效, 字符串长度必须要小于50
        OffersManager.getInstance(this).setCustomUserId(PrefShared.getString(this, "UID"));
        // 有米Android SDK v4.10之后的sdk还需要配置下面代码，以告诉sdk使用了服务器回调
        OffersManager.getInstance(this).setUsingServerCallBack(true);
    }

    /**
     * 初始化万普(源数据版)
     */
    private void initAppOffer(){
        AppConnect.getInstance(Constants.WP_APPID, Constants.WP_APPPID, this);//分别为：应用标识、分发渠道标识
        AppConnect.getInstance(context).initAdInfo(PrefShared.getString(this, "UID"));//初始化
    }

    /**
     * 初始化点入(源数据)
     */
    private void initDr(){
        DRSdk.initialize(this, false, PrefShared.getString(this, "UID"));//context:上下文  isLoc:是否开启定位  appuserid:用户id一般游戏的角色id,如果没有id的话传””
    }

    /**
     * 初始化点乐(源数据)
     */
    private void initDevNative(){
        DevInit.initGoogleContext(this, Constants.DL_DIANLE_APPID);
        DevInit.setCurrentUserID(this, PrefShared.getString(this, "UID"));
    }

    @Override
    public void onRefresh() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                mHandler.sendEmptyMessage(1);
            }
        }).start();
    }

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    if(mSwipeLayout.isRefreshing()) {
                        mSwipeLayout.setRefreshing(false);
                    }
                    if(NetWorkUtils.isNetworkConnected(context) == false){
                        mSVProgressHUD.showInfoWithStatus("网络不可用", SVProgressHUD.SVProgressHUDMaskType.GradientCancel);
                    } else {
                        findByUserMes();
                        setUserMsg();
                        setBanner();
//                        findByAPK();
                    }
                    break;
                default:
                    break;
            }
        }
    };

    /**
     * 设置按钮的高度
     */
    private void setItemHeight() {
        LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams((BaseTools.getWindowsWidth(this)), (int) (BaseTools.getWindowsHeight(this) / 3.2));
        mianllt1.setLayoutParams(params1);
        LinearLayout.LayoutParams params2 = new LinearLayout.LayoutParams((BaseTools.getWindowsWidth(this)), (int) (BaseTools.getWindowsHeight(this) / 3.5));
        mianllt2.setLayoutParams(params2);
        LinearLayout.LayoutParams params3 = new LinearLayout.LayoutParams((BaseTools.getWindowsWidth(this)), BaseTools.getWindowsHeight(this) / 3);
        itemClick.setLayoutParams(params3);
    }

    /**
     * 查询Banner
     */
    private void setBanner() {
        AsyncHttpClientUtil asyncHttpClientUtil = new AsyncHttpClientUtil(0,new HashMap<String,String>());
        RequestParams params = new RequestParams();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("token", Constants.TOKENT);
        jsonObject.put("lunbo_token", Constants.U_TOKENT);
        params.put(Constants.REQUEST_MSG,jsonObject.toString());
        asyncHttpClientUtil.doPost(Constants.LUNBO_PATH, params, new AsyncHttpResponseHandler() {
            @Override
            public void onStart() {
                super.onStart();
            }

            @Override
            public void onSuccess(int statusCode, org.apache.http.Header[] headers, byte[] bytes) {
                setBannerView(bytes);
            }

            @Override
            public void onFailure(int i, org.apache.http.Header[] headers, byte[] bytes, Throwable throwable) {
            }

            @Override
            public void onFinish() {
                super.onFinish();
            }
        });
    }

    /**
     * 设置Banner广告位
     * @param bytes
     */
    private void setBannerView(byte[] bytes) {
        try {
            List<ADInfo> infos = new ArrayList<ADInfo>();
            List<ImageView> views = new ArrayList<ImageView>();
            String result = new String(bytes, "UTF-8");
            JSONObject jsonObject = JSONObject.parseObject(result);
//            Log.e("setBanner",jsonObject.toString());
            String code = jsonObject.getString("code");
            if(TextUtils.equals("10",code)){
                JSONArray jsonArray = jsonObject.getJSONArray("data");
                for (int i = 0; i < jsonArray.size(); i++) {
                    jsonObject = jsonArray.getJSONObject(i);
                    ADInfo info = new ADInfo();
                    String url = jsonObject.getString("url");
                    if(url.contains("http://")){
                        info.setUrl(url);
                    } else {
                        info.setUrl(Constants.REQUEST +"/"+ url);
                    }
                    if(jsonObject.getString("href") == null){
                        info.setContent("");
                    } else {
                        info.setContent(Constants.REQUEST +"/"+ jsonObject.getString("href"));
                    }
                    infos.add(info);
                }
                try {
                    // 将最后一个ImageView添加进来
                    views.add(ViewFactory.getImageView(context, infos.get(infos.size() - 1).getUrl()));
                    for (int i = 0; i < infos.size(); i++) {
                        views.add(ViewFactory.getImageView(context, infos.get(i).getUrl()));
                    }
                    // 将第一个ImageView添加进来
                    views.add(ViewFactory.getImageView(context, infos.get(0).getUrl()));
                    // 设置循环，在调用setData方法前调用
                    cycleViewPager.setCycle(true);
                    // 在加载数据前设置是否循环
                    cycleViewPager.setData(views, infos, mAdCycleViewListener);
                    if (infos.size() > 1) {//如果大于1张图片就设置轮播
                        cycleViewPager.setWheel(true);
                    } else {
                        cycleViewPager.setWheel(false);
                    }
                    // 设置轮播时间，默认8000ms
                    cycleViewPager.setTime(8000);
                    //设置圆点指示图标组居中显示，默认靠右
                    cycleViewPager.setIndicatorCenter();
                } catch (Exception e){
                    e.printStackTrace();
//                    Log.e("setBannerView","设置Banner位错误");
                }
            } else {
                Log.e("setBannerView","错误代码"+code);
            }
        } catch (Exception e) {
            e.printStackTrace();
//            Log.e("setBannerView",Constants.ANALYTICAL_ERROR);
        }
    }

    private BannerImageView.ImageCycleViewListener mAdCycleViewListener = new BannerImageView.ImageCycleViewListener() {
        @Override
        public void onImageClick(ADInfo info, int position, View imageView) {
            String content = info.getContent();
            if (cycleViewPager.isCycle()) {
                if (content == null || TextUtils.equals("",content)) {

                } else {
                    Intent intent = new Intent(MainActivity.this, BannerActivity.class);
                    intent.putExtra("url", info.getContent());
                    startActivity(intent);
                }
            }
        }
    };

    /**
     * 查找更新信息
     */
    private void findByUpdateMsg() {
        try {
            versionCode = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
            versionName = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (Exception e) {
            e.printStackTrace();
        }
        AsyncHttpClientUtil asyncHttpClientUtil = new AsyncHttpClientUtil(0,new HashMap<String,String>());
        RequestParams params = new RequestParams();
        params.put("token",Constants.TOKENT);
        asyncHttpClientUtil.doGet(Constants.UPDATE_APP, params, new AsyncHttpResponseHandler() {
            @Override
            public void onStart() {
                super.onStart();
            }

            @Override
            public void onSuccess(int statusCode, org.apache.http.Header[] headers, byte[] bytes) {
                try {
                    String result = new String(bytes, "UTF-8");
                    JSONObject jsonObject = JSONObject.parseObject(result);
                    int code = jsonObject.getInteger("code");
                    if(code == 10){
                        int number = jsonObject.getInteger("number");
                        versionN = jsonObject.getString("version");
                        if(number > versionCode){
                            apkUrl = jsonObject.getString("apk");
                            apkUrl = Constants.REQUEST + apkUrl;
                            updateMsg = jsonObject.getString("changelogs");
                            messageDialogTwo("更新","发现新版本V"+versionN,Color.parseColor("#0876FE"),updateMsg,15,Gravity.LEFT,"稍后再来","马上更新",15f,15f,getResources().getColor(R.color.dialog_blue),getResources().getColor(R.color.dialog_blue));
                        } else {

                        }
                    } else {
                        Log.e("查找用户","返回错误数据");
                    }
                } catch (Exception e) {
                    Log.e("findByUpdateMsg",Constants.ANALYTICAL_ERROR);
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int i, org.apache.http.Header[] headers, byte[] bytes, Throwable throwable) {
            }

            @Override
            public void onFinish() {
                super.onFinish();
            }
        });
    }

        private void sendUseData() {
            AsyncHttpClientUtil asyncHttpClientUtil = new AsyncHttpClientUtil(0,new HashMap<String,String>());
            RequestParams params = new RequestParams();
            String userData = PrefShared.getString(context,"userData");
            userData = userData.substring(0,userData.length()-1)+","+"\"token\""+":\""+Constants.TOKENT+"\","+"\"id\""+":\""+PrefShared.getString(context,"userId")+"\"}";
            Log.e("登录数据为",userData);
            //加密算法
            long num = AES.get10Random();
            String content = AES.encrypt(userData,AES.md5(AES.longMinusNum(num+"")));
            JSONObject json = new JSONObject();
            json.put("content",content);
            json.put("secret",num+"");
            json.put("hehe",1);
            params.put(Constants.REQUEST_MSG,json.toString());
            asyncHttpClientUtil.doPost(Constants.LOGIN_UPDATE, params, new AsyncHttpResponseHandler() {
                @Override
                public void onStart() {
                    super.onStart();
                }

                @Override
                public void onSuccess(int statusCode, org.apache.http.Header[] headers, byte[] bytes) {
                }

                @Override
                public void onFailure(int i, org.apache.http.Header[] headers, byte[] bytes, Throwable throwable) {
                    try {
                    } catch (Exception e){
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFinish() {
                    super.onFinish();
                }
            });
    }

    /**
     *
     * @param type
     * @param title
     * @param titleColor
     * @param content
     * @param contentSize
     * @param contentGravity
     * @param leftBtn
     * @param rightBtn
     */
    private void messageDialogTwo(final String type, String title, int titleColor,
                                  String content, int contentSize, int contentGravity,
                                  String leftBtn, String rightBtn, float leftSize, float rightSize, int leftColor, int rightColor) {
        final NormalDialog dialog = new NormalDialog(MainActivity.this);
        /*弹窗*/
        dialog.widthScale((float) 0.75);//设整个弹窗的宽度
        dialog.cornerRadius(8);//设弹窗的圆角
        dialog.style(NormalDialog.STYLE_TWO);//设为两个按钮
        dialog.isTitleShow(true);//显示标题
        dialog.bgColor(getResources().getColor(R.color.white));//设弹窗的背景颜色
        /*标题*/
        dialog.title(title);//设标题
        dialog.titleTextSize(20);//设标题字体大小
        dialog.titleTextColor(titleColor);//设标题样色 Color.parseColor("#000000")
        /*内容*/
        dialog.content(content);//设内容
        dialog.contentTextSize(contentSize);//设内容的字体大小
        dialog.contentTextColor(getResources().getColor(R.color.black));//设内容的字体颜色 Color.parseColor("#000000")
        dialog.contentGravity(contentGravity);//设内容显示的位置

        dialog.dividerColor(Color.parseColor("#CCCCCC"));//

        /*按钮*/
        dialog.btnText(leftBtn, rightBtn);//设按钮的文字内容
        dialog.btnTextSize(leftSize, rightSize);//设按钮的字体大小 15.5f
        dialog.btnTextColor(leftColor, rightColor);//设按钮的字体颜色 getResources().getColor(R.color.dialog_blue)
        dialog.btnPressColor(Color.parseColor("#E5E5E5"));//设按钮点击时的背景颜色
        dialog.show();

        dialog.setOnBtnClickL(new OnBtnClickL() {
            @Override
            public void onBtnClick() {
                dialog.dismiss();
                if(TextUtils.equals("赚钱",type)){
                    startActivity(new Intent(MainActivity.this, TasksActivity.class));
                }
            }
        }, new OnBtnClickL() {
            @Override
            public void onBtnClick() {
                dialog.dismiss();
                if(TextUtils.equals("赚钱",type)){
                    danjiType = type;
                    weChatLogin();
                } else {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(apkUrl));
                    startActivity(intent);
                }
            }
        });
    }

    /**
     * 查找手机上所有的apk文件
     */
    private void findByAPK() {
        AsyncUtil asyncUtil = new AsyncUtil() {
            @Override
            public void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            public Long doInBackground(String... params) {
                boolean sdCardExist = Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED); //判断sd卡是否存在
                if (sdCardExist) {
                    File path = Environment.getExternalStorageDirectory();// 获得SD卡路径
                    File[] files = path.listFiles();// 读取
                    getFileName(files);
                } else {
                    Log.e("警告","暂无SD卡");
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

    private void getFileName(File[] files) {
        if (files != null) {// 先判断目录是否为空，否则会报空指针
            for (File file : files) {
                if(file.isDirectory()){
                    getFileName(file.listFiles());
                } else {
                    String fileName = file.getName();
                    if (fileName.endsWith(".apk")) {
                        Log.e("文件为：",fileName + "-----创建时间：" + BaseTools.changeDate(0,file.lastModified()+"") + "-----文件位置：" + file.getPath());
                    }
                }
            }
        }
    }

    @Override
    public void onStart() {
//        mAdView.startImageCycle();//开始Banne
//        PointsManager.getInstance(context).registerNotify(this);//注册有米积分监听
//        float pointsBalance = PointsManager.getInstance(context).queryPoints();//查询有米积分
//        Log.e("有米积分余额", pointsBalance + "");
//        AppConnect.getInstance(context).getPoints(this);//注册万普积分监听
//        DOW.getInstance(this).checkPoints(this);//注册多盟积分
//        DRSdk.getScore(context, this);
        super.onStart();
    }

    @Override
    public void onDestroy() {
//        cycleViewPager.stopBannerHandler();
//        mAdView.pushImageCycle();//销毁Banner
        EventBus.getDefault().unregister(this);//注销EventBus
//        PointsManager.getInstance(context).unRegisterNotify(this);//销毁有米积分监听
//        AppConnect.getInstance(this).close();//销毁万普积分监听
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.userIcon:
                if (processFlag) {
                    setProcessFlag();//
                    startActivity(new Intent(this, MessageActivity.class));
                    new TimeThread().start();
                }
                break;
            case R.id.wallet:
                if (processFlag) {
                    setProcessFlag();//
                    startActivity(new Intent(this, DepositActivity.class));
                    new TimeThread().start();
                }
                break;
            case R.id.makeMoney:
                if (processFlag) {
                    setProcessFlag();//
                    if(!TextUtils.equals("",WXUID)){
                        startActivity(new Intent(MainActivity.this, TasksActivity.class));
                    } else {
//                        messageDialogOne("赚钱","温馨提示", Color.parseColor("#0876FE"),"开始赚钱需要先绑定微信账号，检测到您还未绑定微信请先进行绑定",16, Gravity.LEFT,"取消","绑定");
                        messageDialogTwo("赚钱","温馨提示",Color.parseColor("#0876FE"),"推荐您先绑定微信账号以便更好体验",16, Gravity.LEFT,"稍后绑定","立即绑定",13.5f,15f,getResources().getColor(R.color.dialog_blue),getResources().getColor(R.color.dialog_blue));
                    }
                    new TimeThread().start();
                }
                break;
            case R.id.share:
                if (processFlag) {
                    setProcessFlag();//
                    if(!TextUtils.equals("",WXUID)){
                        startActivity(new Intent(this, ShareActivity.class));
                    } else {
                        messageDialogOne("邀请","温馨提示", Color.parseColor("#0876FE"),"邀请好友须先绑定微信账号",16, Gravity.CENTER,"取消","立即绑定");
                    }
                    new TimeThread().start();
                }
                break;
            case R.id.deposit:
                if (processFlag) {
                    setProcessFlag();//
                    startActivity(new Intent(this, DepositActivity.class));
                    new TimeThread().start();
                }
                break;
            case R.id.help:
                if (processFlag) {
                    setProcessFlag();//
                    startActivity(new Intent(this, HelpActivity.class));
                    new TimeThread().start();
                }
                break;
            default:
                break;
        }
    }

    /**
     *
     * @param type
     * @param title
     * @param titleColor
     * @param content
     * @param contentSize
     * @param contentGravity
     * @param leftBtn
     * @param rightBtn
     */
    private void messageDialogOne(final String type,String title,int titleColor,
                                  String content,int contentSize,int contentGravity,
                                  String leftBtn, String rightBtn) {
        final NormalDialog dialog = new NormalDialog(MainActivity.this);
        /*弹窗*/
        dialog.widthScale((float) 0.75);//设整个弹窗的宽度
        dialog.cornerRadius(8);//设弹窗的圆角
        dialog.style(NormalDialog.STYLE_TWO);//设为两个按钮
        dialog.isTitleShow(true);//显示标题
        dialog.bgColor(getResources().getColor(R.color.white));//设弹窗的背景颜色
        dialog.btnNum(1);//设置弹窗只有一个按钮
        /*标题*/
        dialog.title(title);//设标题
        dialog.titleTextSize(20);//设标题字体大小
        dialog.titleTextColor(titleColor);//设标题样色 Color.parseColor("#000000")
        /*内容*/
        dialog.content(content);//设内容
        dialog.contentTextSize(contentSize);//设内容的字体大小
        dialog.contentTextColor(getResources().getColor(R.color.black));//设内容的字体颜色 Color.parseColor("#000000")
        dialog.contentGravity(contentGravity);//设内容显示的位置

        dialog.dividerColor(Color.parseColor("#CCCCCC"));//

        /*按钮*/
        dialog.btnText(rightBtn);//设按钮的文字内容
        dialog.btnTextSize(15.5f, 15.5f);//设按钮的字体大小
        dialog.btnTextColor(getResources().getColor(R.color.dialog_blue));//设按钮的字体颜色
        dialog.btnPressColor(Color.parseColor("#E5E5E5"));//设按钮点击时的背景颜色
        dialog.show();

        dialog.setOnBtnClickL(new OnBtnClickL() {
            @Override
            public void onBtnClick() {
                dialog.dismiss();
                danjiType = type;
                weChatLogin();
            }
        });
    }

    private void weChatLogin() {
        final int[] wxErrorType = {3};
        AsyncUtil asyncUtil = new AsyncUtil() {
            @Override
            public void onPreExecute() {
                super.onPreExecute();
                mSVProgressHUD.showWithStatus("授权中...",SVProgressHUD.SVProgressHUDMaskType.Black);
            }

            @Override
            public Long doInBackground(String... params) {
                if(iwxapi.isWXAppInstalled() == false){
                    wxErrorType[0] = 0;
                } else {
                    if(!iwxapi.isWXAppSupportAPI()){
                        wxErrorType[0] = 1;
                    } else {
                        SendAuth.Req req = new SendAuth.Req();
                        req.scope = Constants.WX_SCOPE;
                        req.state = Constants.WX_STATE;
                        iwxapi.sendReq(req);
                    }
                }
                return null;
            }

            @Override
            public void onProgressUpdate(Integer... values) {
                super.onProgressUpdate(values);
            }

            @Override
            public void onPostExecute(Long result) {
                if(wxErrorType[0] == 0){
                    mSVProgressHUD.showInfoWithStatus("请先安装微信", SVProgressHUD.SVProgressHUDMaskType.GradientCancel);
                } else if(wxErrorType[0] == 1){
                    mSVProgressHUD.showInfoWithStatus("微信版本不支持", SVProgressHUD.SVProgressHUDMaskType.GradientCancel);
                }
                super.onPostExecute(result);
            }
        };
        asyncUtil.execute();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessage(WXLoginEvent event) {
        if (mSVProgressHUD.isShowing()) {
            mSVProgressHUD.dismiss();
        }
        if(event.code != null && !TextUtils.equals(event.code,"")){
            getOpendId(event.code);
        }
    }

    /**
     * 根据code获取access_token与opendId
     * @param code
     */
    public void getOpendId(String code){
        AsyncHttpClientUtil asyncHttpClientUtil = new AsyncHttpClientUtil(0,new HashMap<String,String>());
        RequestParams params = new RequestParams();
        params.put("appid",Constants.WX_APPID);
        params.put("secret",Constants.WX_SECRET);
        params.put("code",code);
        params.put("grant_type","authorization_code");
        asyncHttpClientUtil.doGet(Constants.WX_ACCESS_TOKEN, params, new AsyncHttpResponseHandler() {
            @Override
            public void onStart() {
                super.onStart();
            }

            @Override
            public void onSuccess(int statusCode, org.apache.http.Header[] headers, byte[] bytes) {
                try {
                    String result = new String(bytes, "UTF-8");
                    JSONObject jsonObject = JSONObject.parseObject(result);
                    String token = jsonObject.getString("access_token");
                    String opendId = jsonObject.getString("openid");
                    if ((null != token || null != opendId) && !TextUtils.equals(token, "") && !TextUtils.equals(opendId, "")) {
                        getUMessage(token, opendId);
                    } else {
                        Log.e("绑定时微信登录", "token或者opendId为null");
                        if (mSVProgressHUD.isShowing()) {
                            mSVProgressHUD.dismiss();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int i, org.apache.http.Header[] headers, byte[] bytes, Throwable throwable) {

            }

            @Override
            public void onFinish() {
                super.onFinish();
            }
        });
    }

    /**
     * 根据token与openId获取用户信息
     * @param token
     * @param opendId
     */
    private void getUMessage(String token, String opendId) {
        Map<String,String> header = new HashMap<String,String>();
        header.put("","");
        AsyncHttpClientUtil asyncHttpClientUtil = new AsyncHttpClientUtil(3000,header);
        RequestParams params = new RequestParams();
        params.put("access_token", token);
        params.put("openid", opendId);
        asyncHttpClientUtil.doGet(Constants.WX_USER_INFO, params, new AsyncHttpResponseHandler() {
            @Override
            public void onStart() {
                super.onStart();
            }

            @Override
            public void onSuccess(int statusCode, org.apache.http.Header[] headers, byte[] bytes) {
                try {
                    String result = new String(bytes, "UTF-8");
                    JSONObject jsonObject = JSONObject.parseObject(result);
                    String nickname = jsonObject.getString("nickname");
                    String openid = jsonObject.getString("openid");
                    String imgUrl = jsonObject.getString("headimgurl");
                    String unionid = jsonObject.getString("unionid");
                    if((null != unionid && null != nickname && null != imgUrl) &&
                            !TextUtils.equals(unionid, "") && !TextUtils.equals(nickname, "")  && !TextUtils.equals(imgUrl, "")){
                        boundEquipment(3,unionid,nickname,imgUrl);
                    } else {
                        Log.e("绑定时微信登录", "unionid或者nickname或者imgUrl为null");
                        if (mSVProgressHUD.isShowing()) {
                            mSVProgressHUD.dismiss();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int i, org.apache.http.Header[] headers, byte[] bytes, Throwable throwable) {
            }

            @Override
            public void onFinish() {
                super.onFinish();
            }
        });
    }

    /**
     * 绑定设备
     * @param loginType
     * @param unionid
     * @param nickname
     * @param imgUrl
     */
    private void boundEquipment(int loginType,String unionid,String nickname,String imgUrl) {
        AsyncHttpClientUtil asyncHttpClientUtil = new AsyncHttpClientUtil(0,new HashMap<String,String>());
        RequestParams params = new RequestParams();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("token",Constants.TOKENT);
        jsonObject.put("type",loginType);
        jsonObject.put("id",unionid);
        jsonObject.put("password","");
        jsonObject.put("only",PrefShared.getString(context,"UUID"));
        jsonObject.put("nickname",nickname);
        jsonObject.put("imgUrl",imgUrl);
//        Log.e("绑定设备",jsonObject.toString());
        params.put(Constants.REQUEST_MSG,jsonObject.toString());
        asyncHttpClientUtil.doPost(Constants.EQUIPMENT_BIND, params, new AsyncHttpResponseHandler() {
            @Override
            public void onStart() {
                super.onStart();
                mSVProgressHUD.showWithStatus("绑定中...", SVProgressHUD.SVProgressHUDMaskType.Black);
            }

            @Override
            public void onSuccess(int statusCode, org.apache.http.Header[] headers, byte[] bytes) {
                try {
                    String result = new String(bytes, "UTF-8");
//                    Log.e("result",result);
                    JSONObject jsonObject = JSONObject.parseObject(result);
//                    Log.e("boundEquipment",jsonObject.toString());
                    String code = jsonObject.getString("code");
                    String msg = jsonObject.getString("msg");
                    if(TextUtils.equals("10",code)){
                        mSVProgressHUD.showSuccessWithStatus(msg, SVProgressHUD.SVProgressHUDMaskType.GradientCancel);
                        if(TextUtils.equals("赚钱",danjiType)){
                            startActivity(new Intent(context, TasksActivity.class));
                        } else if(TextUtils.equals("邀请",danjiType)){
                            startActivity(new Intent(context, ShareActivity.class));
                        }
                        findByUserMes();
                    } else if(TextUtils.equals("2",code)){
                        mSVProgressHUD.showInfoWithStatus(msg, SVProgressHUD.SVProgressHUDMaskType.GradientCancel);
                    } else {
                        mSVProgressHUD.showErrorWithStatus(msg, SVProgressHUD.SVProgressHUDMaskType.GradientCancel);
                    }
                } catch (Exception e) {
                    mSVProgressHUD.showErrorWithStatus("数据错误", SVProgressHUD.SVProgressHUDMaskType.GradientCancel);
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int i, org.apache.http.Header[] headers, byte[] bytes, Throwable throwable) {
                try {
                    if(throwable.toString().contains("ConnectTimeoutException")){
                        mSVProgressHUD.showInfoWithStatus("请求超时", SVProgressHUD.SVProgressHUDMaskType.GradientCancel);
                    } else {
                        mSVProgressHUD.showErrorWithStatus("请求错误", SVProgressHUD.SVProgressHUDMaskType.GradientCancel);
                    }
                } catch (Exception e){
                    e.printStackTrace();
                }
            }

            @Override
            public void onFinish() {
                super.onFinish();
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if ((System.currentTimeMillis() - mExitTime) > 2000) {
                Toast.makeText(this, "再按一次退出", Toast.LENGTH_SHORT).show();
                mExitTime = System.currentTimeMillis();
            } else {
                MainApplication.exit();
                PrefShared.removeData(context,"tasksJson");
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}