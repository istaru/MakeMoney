package com.mx.hb.moon.activity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.mx.hb.moon.R;
import com.mx.hb.moon.base.BaseTools;
import com.mx.hb.moon.base.Constants;
import com.mx.hb.moon.base.NetWorkUtils;
import com.mx.hb.moon.base.Util;
import com.mx.hb.moon.loading.SVProgressHUD;
import com.mx.hb.moon.swipeRefresh.SwipeRefreshLayout;
import com.mx.hb.moon.view.StatusBarUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import xz.ax.qr.os.df.AppDetailDataInterface;
import xz.ax.qr.os.df.AppDetailObject;
import xz.ax.qr.os.df.AppExtraTaskObjectList;
import xz.ax.qr.os.df.AppSummaryObject;
import xz.ax.qr.os.df.DiyAppNotify;
import xz.ax.qr.os.df.DiyOfferWallManager;


/**
 * Created by Moon on 2016/6/1.
 */
public class YoumiItemActivity extends BaseActivity implements SwipeRefreshLayout.OnRefreshListener,View.OnClickListener,DiyAppNotify {
    private SwipeRefreshLayout swipeRefreshLayout;
    private AppSummaryObject appSummaryObject;
    private LinearLayout onBack;
    private WebView webView;
    private RelativeLayout webViewBg;
    private TextView webViewTitle;

    private static SVProgressHUD mSVProgressHUD;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        appSummaryObject = (AppSummaryObject) getIntent().getSerializableExtra("appSummaryObject");
        setContentView(R.layout.activity_webview);
        mSVProgressHUD = new SVProgressHUD(context);
        initView();
        // 如：在Activity的onCreate方法中注册接口，那么当广告在下载/安装的时候就会通知这个接口
        DiyOfferWallManager.getInstance(context).registerListener(this);
    }

    @Override
    protected void setStatusBar() {
        StatusBarUtil.setColor(this, getResources().getColor(R.color.gules), 0);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 如：在Activity的OnDestory方法中注销接口，那么就不会在受到广告下载/安装的相关信息
        DiyOfferWallManager.getInstance(context).removeListener(this);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @JavascriptInterface
    private void initView() {
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe);
        swipeRefreshLayout.setColor(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
        swipeRefreshLayout.setMode(SwipeRefreshLayout.Mode.PULL_FROM_START);
        swipeRefreshLayout.setOnRefreshListener(this);

        onBack = (LinearLayout) findViewById(R.id.onBack);
        onBack.setOnClickListener(this);

        webViewBg = (RelativeLayout) findViewById(R.id.webView_bg);
        webViewTitle = (TextView) findViewById(R.id.webView_title);
        webViewTitle.setText(appSummaryObject.getAppName());
        webViewBg.setBackgroundColor(getResources().getColor(R.color.gules));
        webView = (WebView) findViewById(R.id.webView);

        webView.loadUrl(Constants.HTML5);
        webView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                return true;
            }
        });
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setAllowFileAccess(true);
        webView.getSettings().setAllowFileAccessFromFileURLs(true);
        webView.getSettings().setAllowUniversalAccessFromFileURLs(true);
        //图片显示
        webView.getSettings().setLoadsImagesAutomatically(true);
        //自适应屏幕
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (newProgress == 100) {// 加载完成
                    swipeRefreshLayout.setRefreshing(false);
                } else {// 加载中
                    swipeRefreshLayout.setRefreshing(true);
                }
                super.onProgressChanged(view, newProgress);
            }
        });
        webView.addJavascriptInterface(this, "Android");
        webView.setWebViewClient(new MyWebViewClient());
    }

    class MyWebViewClient extends WebViewClient {
        //页面开始加载
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }

        //页面开始加载前
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
        }

        //页面加载出错
        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
            super.onReceivedError(view, request, error);
        }

        //页面加载完毕
        public void onPageFinished(WebView view, String url) {
            findAppDetailObject(appSummaryObject);
            view.loadUrl("javascript:window.local_obj.showSource('<head>'+" + "document.getElementsByTagName('html')[0].innerHTML+'</head>');");
            super.onPageFinished(view, url);
        }
    }

    @Override
    public void onRefresh() {
        findAppDetailObject(appSummaryObject);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                swipeRefreshLayout.setRefreshing(false);
            }
        }, 2000);
    }

    /**
     * 异步加载积分墙某个广告的详细数据
     */
    @android.webkit.JavascriptInterface
    public void findAppDetailObject(AppSummaryObject appSummaryObject) {
        final JSONObject jsonObject = new JSONObject();
        DiyOfferWallManager.getInstance(context).loadAppDetailData(appSummaryObject,
                new AppDetailDataInterface() {
                    /**
                     * 当成功加载到广告数据的时候，会回调本方法
                     * 注意：广告详细数据有可能为空（比如：广告下架了），开发者处理之前，请先判断是否为空
                     * 注意：本接口不在UI线程中执行， 所以请不要在本接口中进行UI线程方面的操作
                     */
                    @Override
                    public void onLoadAppDetailDataSuccess(Context c, AppDetailObject appDetailObject) {
                        if (appDetailObject != null) {
                            List<Map<String, Object>> listMap = new ArrayList<Map<String, Object>>();
                            Map<String, Object> zMap = new HashMap<String, Object>();
                            //获取主任务的数据
                            int integral = appDetailObject.getPoints();//主任务的积分
                            String zAdText = appDetailObject.getTaskSteps();//主任务的描述
                            int zStatus = appDetailObject.getAdTaskStatus();//主任务的完成状态（1:未完成，4:有追加任务，2:有追加任务）
                            //设置主任务的值
                            zMap.put("adText",zAdText);
                            zMap.put("points", BaseTools.pointhangeMoney((double)integral));
                            zMap.put("status",zStatus);
                            listMap.add(zMap);
                            //获取追加任务
                            AppExtraTaskObjectList appExtraTaskObject = appDetailObject.getExtraTaskList();
                            if(appExtraTaskObject != null){
                                for (int i = 0; i < appExtraTaskObject.size(); i++) {
                                    Map<String, Object> map = new HashMap<String, Object>();
                                    //获取追加任务的数据
                                    String adText = appExtraTaskObject.get(i).getAdText(); // 获取追加任务的描述
                                    int points = appExtraTaskObject.get(i).getPoints(); // 获取本个追加任务的积分
                                    int status = appExtraTaskObject.get(i).getStatus(); // 获取追加任务的状态（0:任务未开始，1:任务进行中，2:任务已完成，3:任务已过期）
                                    long startTime = appExtraTaskObject.get(i).getStartTimeStamp(); // 获取追加任务起始时间戳(秒)
                                    long endTime = appExtraTaskObject.get(i).getEndTimeStamp(); // 获取追加任务结束时间戳(秒)
                                    //设置追加任务的值
                                    map.put("adText", adText);
                                    map.put("points", BaseTools.pointhangeMoney((double)points));
                                    map.put("status", status);
                                    listMap.add(map);
                                    jsonObject.put("code", 0);//设置状态码（0:成功，其他:失败）
                                }
                                jsonObject.put("list", listMap);
                            } else {
                                jsonObject.put("list", listMap);
                            }
                            jsonObject.put("id", appDetailObject.getAdId());// 获取广告 id
                            jsonObject.put("adName", appDetailObject.getAppName());// 获取 app 的名称
                            jsonObject.put("pn", appDetailObject.getPackageName());// 获取 app 的包名
                            jsonObject.put("versionCode", appDetailObject.getVersionCode());// 获取 app 的版本号
                            jsonObject.put("adIconUrl", appDetailObject.getIconUrl());// 获取 app 的图标地址
                            jsonObject.put("adSlogan", appDetailObject.getAdSlogan());// 获取广告标语
                            jsonObject.put("size", appDetailObject.getAppSize());// 获取 app 的大小
                            jsonObject.put("points", BaseTools.pointhangeMoney((double)integral));// 获取广告的积分（已完成状态下的广告积分返回值为0）
                            jsonObject.put("pointsUnit", appDetailObject.getPointsUnit());// 获取广告的积分单位
//                            jsonObject.put("adStatus", appDetailObject.getAdTaskStatus());// 获取广告的完成状态
                            int down = appDetailObject.getAdDownloadStatus();
                            String packageName = appDetailObject.getPackageName().trim();
                            boolean isPackageExist = Util.checkLocalAppExistOrNot(context, packageName);
                            if(isPackageExist == true || down == 1){
                                jsonObject.put("dlStatus", 1);// 已安装
                            } else {
                                jsonObject.put("dlStatus", 0);// 未安装
                            }
//                            jsonObject.put("dlStatus", appDetailObject.getAdDownloadStatus());// 获取广告的下载状态
                            jsonObject.put("steps", appDetailObject.getTaskSteps());// 任务步骤流程指引
                            jsonObject.put("adForm", appDetailObject.getAdForm());// 广告形式，下面有说明
                            jsonObject.put("url", appDetailObject.getUrl());// 广告对应的url，url含义视乎广告形式，下面有说明
                            jsonObject.put("rewardCount", appDetailObject.getRewardsCount());// 今天本广告获得奖励的用户个数
                            jsonObject.put("versionName", appDetailObject.getVersionName());// 获取 app 的版本名
                            jsonObject.put("ssUrls", appDetailObject.getScreenShotUrls());// 获取 app 的截图地址列表
                            jsonObject.put("desc", appDetailObject.getDescription());// 获取广告的详细描述
                            jsonObject.put("appCat", appDetailObject.getAppCategory());// 获取应用类型
                            jsonObject.put("author", appDetailObject.getAuthor());// 获取该 app 的作者名
//                            Log.e("json", jsonObject.toString());
                        } else {
                            jsonObject.put("code", "1");
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                webView.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        webView.loadUrl("javascript:findByData(" + jsonObject.toString() + ");");
                                    }
                                });
                            }
                        });
                    }

                    /**
                     * 因为网络问题而导致请求失败时，会回调本方法
                     * 注意：本接口不在UI线程中执行， 所以请不要在本接口中进行UI线程方面的操作
                     */
                    @Override
                    public void onLoadAppDetailDataFailed() {
                        mSVProgressHUD.showInfoWithStatus("网络不可用", SVProgressHUD.SVProgressHUDMaskType.GradientCancel);
                    }

                    /**
                     * 请求成功，但是返回有米错误代码时候，会回调本方法
                     * 注意：本接口不在UI线程中执行， 所以请不要在本接口中进行UI线程方面的操作
                     */
                    @Override
                    public void onLoadAppDetailDataFailedWithErrorCode(int code) {
                        mSVProgressHUD.showInfoWithStatus("initialization error", SVProgressHUD.SVProgressHUDMaskType.GradientCancel);
//                                Toast.makeText(context, "initialization error", Toast.LENGTH_SHORT).show();
                        Log.e("点乐", code+"");
                    }
                });
    }

    @android.webkit.JavascriptInterface
    public void downloadOrOpen(){
        if(NetWorkUtils.isNetworkConnected(this) == false){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mSVProgressHUD.showInfoWithStatus("网络不可用", SVProgressHUD.SVProgressHUDMaskType.GradientCancel);
                }
            });
        } else {
            boolean isPackageExist = Util.checkLocalAppExistOrNot(context, appSummaryObject.getPackageName());
            if(isPackageExist == true){
                Util.startActivityByPackageName(context,appSummaryObject.getPackageName());
            } else {
                DiyOfferWallManager.getInstance(context).openOrDownloadApp((Activity) context, appSummaryObject);
            }
        }
    }

    @Override
    public void onDownloadStart(int i) {

    }

    /**
     *  下载进度变更通知，在 UI 线程中执行。
     *  @param  id                 广告id
     *  @param  contentLength      本次下载总长度
     *  @param  completeLength     已完成的长度
     *  @param  percent            已完成百分比
     *  @param  speedBytesPerS     每秒的下载速度 B/s
     */
    @Override
    @android.webkit.JavascriptInterface
    public void onDownloadProgressUpdate(int id, long contentLength, long completeLength, int percent, long speedBytesPerS) {
        webView.post(new Runnable() {
            @Override
            public void run() {
                webView.loadUrl("javascript:isDownload(" + true + ");");
            }
        });
    }

    @Override
    @android.webkit.JavascriptInterface
    public void onDownloadSuccess(int i) {
        webView.post(new Runnable() {
            @Override
            public void run() {
                webView.loadUrl("javascript:isDownload(" + false + ");");
            }
        });
    }

    @Override
    @android.webkit.JavascriptInterface
    public void onDownloadFailed(int i) {
        webView.post(new Runnable() {
            @Override
            public void run() {
                webView.loadUrl("javascript:isDownload(" + false + ");");
            }
        });
    }

    @Override
    public void onInstallSuccess(int i) {

    }

//    private void showWithProgress(){
//        progress = 0;
//        mSVProgressHUD.getProgressBar().setProgress(progress);//先重设了进度再显示，避免下次再show会先显示上一次的进度位置所以要先将进度归0
//        mSVProgressHUD.showWithProgress("进度 " + progress + "%", SVProgressHUD.SVProgressHUDMaskType.Black);
//        mHandler.sendEmptyMessageDelayed(0,500);
//    }
//
//    private Handler mHandler = new Handler(){
//        @Override
//        public void handleMessage(Message msg) {
//            super.handleMessage(msg);
//            progress = progress + 5;
//            if (mSVProgressHUD.getProgressBar().getMax() != mSVProgressHUD.getProgressBar().getProgress()) {
//                mSVProgressHUD.getProgressBar().setProgress(progress);
//                mSVProgressHUD.setText("进度 "+progress+"%");
//                mHandler.sendEmptyMessageDelayed(0,500);
//            } else {
//                mSVProgressHUD.dismiss();
//            }
//
//        }
//    };

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.onBack:
                finish();
                break;
            default:
                break;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
