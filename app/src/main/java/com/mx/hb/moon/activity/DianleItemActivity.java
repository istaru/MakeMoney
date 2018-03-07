package com.mx.hb.moon.activity;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
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
import com.moonmmxhb.AdType;
import com.moonmmxhb.DevInit;
import com.moonmmxhb.OnAddPointsListener;
import com.mx.hb.moon.R;
import com.mx.hb.moon.base.AsyncUtil;
import com.mx.hb.moon.base.BaseTools;
import com.mx.hb.moon.base.Constants;
import com.mx.hb.moon.base.NetWorkUtils;
import com.mx.hb.moon.base.Util;
import com.mx.hb.moon.entity.DlInfo;
import com.mx.hb.moon.loading.SVProgressHUD;
import com.mx.hb.moon.swipeRefresh.SwipeRefreshLayout;
import com.mx.hb.moon.view.StatusBarUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Moon on 2016/6/6.
 */
public class DianleItemActivity extends BaseActivity implements View.OnClickListener,SwipeRefreshLayout.OnRefreshListener,OnAddPointsListener {

    private SwipeRefreshLayout swipeRefreshLayout;
    private DlInfo dlInfo;
    private LinearLayout onBack;
    private WebView webView;
    private RelativeLayout webViewBg;
    private TextView webViewTitle;
    private static SVProgressHUD mSVProgressHUD;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dlInfo = (DlInfo) getIntent().getSerializableExtra("dlInfo");
        setContentView(R.layout.activity_webview);
        mSVProgressHUD = new SVProgressHUD(context);
        initView();
    }

    @Override
    protected void setStatusBar() {
        StatusBarUtil.setColor(this, getResources().getColor(R.color.dianle), 0);
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
        webViewTitle.setText(dlInfo.getName());
        webViewBg.setBackgroundColor(getResources().getColor(R.color.dianle));
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
            findByData(dlInfo);
            view.loadUrl("javascript:window.local_obj.showSource('<head>'+" + "document.getElementsByTagName('html')[0].innerHTML+'</head>');");
            super.onPageFinished(view, url);
        }
    }

    @Override
    public void onRefresh() {
        findByData(dlInfo);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                swipeRefreshLayout.setRefreshing(false);
            }
        }, 2000);
    }

    /**
     * 查询子任务
     * @param dlInfo
     */
    @android.webkit.JavascriptInterface
    private void findByData(final DlInfo dlInfo) {
        final JSONObject jsonObject = new JSONObject();
        final List<Map<String, Object>> listMap = new ArrayList<Map<String, Object>>();
        AsyncUtil asyncUtil = new AsyncUtil() {
            @Override
            public void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            public Long doInBackground(String... params) {
                try {
                    String ssUrls = dlInfo.getThumbnail();
                    String [] str = ssUrls.substring(1,ssUrls.length() - 1).replaceAll("\\\\","").split(",");
                    String[] urls = new String[2];
                    for(int i = 0;i < str.length;i++){
                        String url = str[i].replaceAll("\"", "");
                        urls[i] = url;
                    }
                    JSONArray jsonArray = (JSONArray) JSONArray.parse(dlInfo.getTasks());
                    for(int i = 0;i < jsonArray.size();i++){
                        JSONObject jsonObj = jsonArray.getJSONObject(i);
                        Map<String,Object> map = new HashMap<String,Object>();
                        String points = jsonObj.getString("step_rmb");
                        map.put("adText",jsonObj.getString("name"));
                        map.put("points",BaseTools.pointhangeMoney(Double.parseDouble(points)));
                        map.put("status",0);
                        listMap.add(map);
                    }
                    boolean isPackageExist = Util.checkLocalAppExistOrNot(context, dlInfo.getPackName());
                    if(isPackageExist == true){
                        jsonObject.put("dlStatus", 1);// 已安装
                    } else {
                        jsonObject.put("dlStatus", 0);// 未安装
                    }
                    jsonObject.put("adName",dlInfo.getName());// 获取 app 的名称
                    jsonObject.put("adIconUrl", dlInfo.getIcon());// 获取 app 的图标地址
                    jsonObject.put("adSlogan",dlInfo.getText());// 获取广告标语
                    jsonObject.put("points",BaseTools.pointhangeMoney(dlInfo.getNumber()));// 获取广告的积分
                    jsonObject.put("size",dlInfo.getSize().replace("B",""));// 获取 app 的大小
                    jsonObject.put("list", listMap);
                    jsonObject.put("ssUrls", urls);// 获取 app 的截图地址列表
                    jsonObject.put("desc", dlInfo.getDescription());// 获取广告的详细描述
//                    Log.e("json",jsonObject.toString());
                } catch (Exception e) {
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
                super.onPostExecute(result);
            }
        };
        asyncUtil.execute();
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
            DevInit.download(context,dlInfo.getName(), AdType.ADLIST,this);
//        }
        }
    }

    @Override
    public void addPointsSucceeded(String ad_name, String pack_name, int number) {
        Log.i("add", "加分成功");
    }

    @Override
    public void addPointsFailed(String error) {
        Log.i("faild", "加分失败");
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
