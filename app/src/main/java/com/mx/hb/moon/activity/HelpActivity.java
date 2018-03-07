package com.mx.hb.moon.activity;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mx.hb.moon.R;
import com.mx.hb.moon.base.Constants;
import com.mx.hb.moon.loading.SVProgressHUD;
import com.mx.hb.moon.swipeRefresh.SwipeRefreshLayout;
import com.mx.hb.moon.view.StatusBarUtil;

/**
 * Created by Kiven on 16/7/21.
 */
public class HelpActivity extends BaseActivity implements View.OnClickListener{

    private static SVProgressHUD mSVProgressHUD;
    private LinearLayout onBack;
    private SwipeRefreshLayout swipeRefreshLayout;
    private WebView webView;
//    private ProgressBar pb;
    private RelativeLayout webViewBg;
    private TextView webViewTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);
        mSVProgressHUD = new SVProgressHUD(context);
        initView();
    }

    @Override
    protected void setStatusBar() {
        StatusBarUtil.setColor(this, getResources().getColor(R.color.dianru),0);
    }

    private void initView() {
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe);
        swipeRefreshLayout.setColor(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
        swipeRefreshLayout.setMode(SwipeRefreshLayout.Mode.DISABLED);

        onBack = (LinearLayout) findViewById(R.id.onBack);
        onBack.setOnClickListener(this);
        webViewBg = (RelativeLayout) findViewById(R.id.webView_bg);
//        pb = (ProgressBar) findViewById(R.id.pb);
        webViewTitle = (TextView) findViewById(R.id.webView_title);
        webViewTitle.setText("常见问题");
        webViewBg.setBackgroundColor(getResources().getColor(R.color.dianru));
        webView = (WebView) findViewById(R.id.webView);
        webView.loadUrl(Constants.HELP_HTML5);
        webView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                return true;
            }
        });
        webView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
        webView.setHorizontalScrollBarEnabled(false);
        webView.getSettings().setSupportZoom(true);
        webView.getSettings().setBuiltInZoomControls(false);
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (newProgress == 100) {// 加载完成
                    swipeRefreshLayout.setRefreshing(false);
//                    pb.setVisibility(View.GONE);
                } else {// 加载中
                    swipeRefreshLayout.setRefreshing(true);
                }
                super.onProgressChanged(view, newProgress);
            }
        });
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.setWebViewClient(new MyWebViewClient());
    }

    class MyWebViewClient extends WebViewClient {
        //页面开始加载
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }

        @JavascriptInterface
        //页面开始加载前
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
        }

        //页面加载出错
        @JavascriptInterface
        public void onReceivedError(WebView view, int errorCode,String description, String failingUrl) {
            super.onReceivedError(view, errorCode, description, failingUrl);
            Log.e("错误信息",errorCode+"");
            view.loadUrl(Constants.ERROR_HTML5);
            if(errorCode == -2){
                mSVProgressHUD.showInfoWithStatus("网络不可用", SVProgressHUD.SVProgressHUDMaskType.GradientCancel);
            } else if(errorCode == -8){
                mSVProgressHUD.showInfoWithStatus("请求超时", SVProgressHUD.SVProgressHUDMaskType.GradientCancel);
            }
//            pb.setVisibility(View.GONE);
            swipeRefreshLayout.setRefreshing(false);
        }

        @JavascriptInterface
        //页面加载完毕
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            view.loadUrl("javascript:window.local_obj.showSource('<head>'+" + "document.getElementsByTagName('html')[0].innerHTML+'</head>');");
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.onBack:
                this.finish();
                break;
            default:
                break;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.finish();
    }
}
