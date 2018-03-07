package com.mx.hb.moon.activity;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
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

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.mx.hb.moon.R;
import com.mx.hb.moon.base.AsyncHttpClientUtil;
import com.mx.hb.moon.base.BaseTools;
import com.mx.hb.moon.base.Constants;
import com.mx.hb.moon.base.NetWorkUtils;
import com.mx.hb.moon.base.Util;
import com.mx.hb.moon.entity.DrInfo;
import com.mx.hb.moon.loading.SVProgressHUD;
import com.mx.hb.moon.swipeRefresh.SwipeRefreshLayout;
import com.mx.hb.moon.view.StatusBarUtil;
import com.yql.dr.sdk.DRSdk;
import com.yql.dr.util.DRParams;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Moon on 2016/6/3.
 */
public class DianruItemActivity extends BaseActivity implements View.OnClickListener,SwipeRefreshLayout.OnRefreshListener{
    private SwipeRefreshLayout swipeRefreshLayout;
    private DrInfo drInfo;
    private LinearLayout onBack;
    private WebView webView;
    private RelativeLayout webViewBg;
    private TextView webViewTitle;
    private static SVProgressHUD mSVProgressHUD;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        drInfo = (DrInfo) getIntent().getSerializableExtra("drInfo");
        setContentView(R.layout.activity_webview);
        mSVProgressHUD = new SVProgressHUD(context);
        initView();
    }

    @Override
    protected void setStatusBar() {
        StatusBarUtil.setColor(this, getResources().getColor(R.color.dianru), 0);
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
        webViewTitle.setText(drInfo.getTitle());
        webViewBg.setBackgroundColor(getResources().getColor(R.color.dianru));
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
            findByData(drInfo);
            view.loadUrl("javascript:window.local_obj.showSource('<head>'+" + "document.getElementsByTagName('html')[0].innerHTML+'</head>');");
            super.onPageFinished(view, url);
        }
    }

    @Override
    public void onRefresh() {
        findByData(drInfo);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                swipeRefreshLayout.setRefreshing(false);
            }
        }, 2000);
    }

    /**
     * 查询子任务
     * @param drInfo
     */
    @android.webkit.JavascriptInterface
    private void findByData(final DrInfo drInfo) {
        final JSONObject jsonObject = new JSONObject();
        AsyncHttpClientUtil asyncHttpClientUtil = new AsyncHttpClientUtil(0,new HashMap<String,String>());
        RequestParams params = new RequestParams();
        asyncHttpClientUtil.doGet(drInfo.getAndroidUrl(), params, new AsyncHttpResponseHandler() {
            @Override
            public void onStart() {
                super.onStart();
            }

            @Override
            public void onSuccess(int statusCode, org.apache.http.Header[] headers, byte[] bytes) {
                try {
                    String result = new String(bytes, "UTF-8");
                    if (!TextUtils.equals(result, "")) {
                        List<Map<String, Object>> listMap = new ArrayList<Map<String, Object>>();
                        JSONObject json = JSONObject.parseObject(result);
                        JSONArray jsonArray = json.getJSONArray("step");
                        for(int i = 0;i < jsonArray.size();i++){
                            json = jsonArray.getJSONObject(i);
                            Map<String,Object> map = new HashMap<String, Object>();
                            map.put("points", BaseTools.pointhangeMoney(Double.parseDouble(json.getString("score"))));
                            map.put("adText",json.getString("note"));
                            map.put("status",0);
                            listMap.add(map);
                        }
                        boolean isPackageExist = Util.checkLocalAppExistOrNot(context, drInfo.getProcessName());
                        if(isPackageExist == true){
                            jsonObject.put("dlStatus", 1);// 已安装
                        } else {
                            jsonObject.put("dlStatus", 0);// 未安装
                        }
                        String urls[] = {drInfo.getImage1(),drInfo.getImage2(),drInfo.getImage3()};
                        jsonObject.put("adName",drInfo.getTitle());// 获取 app 的名称
                        jsonObject.put("adIconUrl", drInfo.getIcon());// 获取 app 的图标地址
                        jsonObject.put("adSlogan",drInfo.getText2());// 获取广告标语
                        jsonObject.put("points",BaseTools.pointhangeMoney(Double.parseDouble(drInfo.getScore())));// 获取广告的积分
                        jsonObject.put("size",drInfo.getPsize());// 获取 app 的大小
                        jsonObject.put("list", listMap);
                        jsonObject.put("ssUrls", urls);// 获取 app 的截图地址列表
                        jsonObject.put("desc", drInfo.getIntro());// 获取广告的详细描述
//                        Log.e("json",jsonObject.toString());
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
                    } else {
                        Log.e("", "登录数据获取错误");
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
//        boolean isPackageExist = Util.checkLocalAppExistOrNot(context, appSummaryObject.getPackageName());
//        if(isPackageExist == true){
//            Util.startActivityByPackageName(context,appSummaryObject.getPackageName());
//        } else {
            /**
             * 跳转安装 DRParams，需要传7个参数，必须传的即使是null也传过去，我们内部处理了
             */
            DRParams params = new DRParams();
            params.put(DRParams.TYPE, DRSdk.DR_OFFER);// 类型 积分墙
            params.put(DRParams.TITLE, drInfo.getTitle()); // 应用名
            params.put(DRParams.CID, drInfo.getCid()); // cid 广告主id
            params.put(DRParams.ADID, drInfo.getAdid()); // adid 广告id
            params.put(DRParams.PKG_NAME, drInfo.getProcessName()); // 包名
            params.put(DRParams.RUNTIME, drInfo.getRuntime()); // 广告需要运行的时间
            params.put(DRParams.ACTIVE_NUM, drInfo.getActiveNum());// 激活次数
            params.put(DRParams.ACTIVE_TIME, drInfo.getActiveTime()); // 激活时间
            params.put(DRParams.CUR_NOTE, drInfo.getCurrNote()); // 激活时间
            params.put(DRParams.CUR_NOTE, drInfo.getCurrNote()); // 当前激活条件
            params.put(DRParams.URL, drInfo.getUrl()); // 下载链接
            DRSdk.onAdJumped(params, context);
//        }
        }
    }

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
