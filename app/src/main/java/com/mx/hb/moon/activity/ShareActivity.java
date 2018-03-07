package com.mx.hb.moon.activity;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.ClipboardManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.mx.hb.moon.R;
import com.mx.hb.moon.base.BaseTools;
import com.mx.hb.moon.base.Constants;
import com.mx.hb.moon.base.Encoder;
import com.mx.hb.moon.base.NetWorkUtils;
import com.mx.hb.moon.base.PrefShared;
import com.mx.hb.moon.dialog.NormalDialog;
import com.mx.hb.moon.dialog.OnBtnClickL;
import com.mx.hb.moon.loading.SVProgressHUD;
import com.mx.hb.moon.swipeRefresh.SwipeRefreshLayout;
import com.mx.hb.moon.view.StatusBarUtil;
import com.tencent.mm.sdk.modelmsg.SendMessageToWX;
import com.tencent.mm.sdk.modelmsg.WXMediaMessage;
import com.tencent.mm.sdk.modelmsg.WXWebpageObject;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by Kiven on 16/8/10.
 */
public class ShareActivity extends BaseActivity implements View.OnClickListener{
    private static SVProgressHUD mSVProgressHUD;
    private LinearLayout onBack;
    private SwipeRefreshLayout swipeRefreshLayout;
    private WebView webView;
    private RelativeLayout webViewBg;
    private TextView webViewTitle;
    private IWXAPI iwxapi;
    private String createEncode;
    private Encoder mEncoder;
    private Bitmap bitmap;
    private static String WXAPPID = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);
        mSVProgressHUD = new SVProgressHUD(context);
        initView();
        createEncode = PrefShared.getString(context,"qrcode");
    }

    @Override
    protected void setStatusBar() {
        StatusBarUtil.setColor(this, getResources().getColor(R.color.share),0);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @JavascriptInterface
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
        webViewTitle = (TextView) findViewById(R.id.webView_title);
        webViewTitle.setText("邀请好友");
        webViewBg.setBackgroundColor(getResources().getColor(R.color.share));
        webView = (WebView) findViewById(R.id.webView);
//        ReboundScrollView.LayoutParams params = new ReboundScrollView.LayoutParams((BaseTools.getWindowsWidth(this)), (BaseTools.getWindowsHeight(this)));
//        webView.setLayoutParams(params);

        webView.loadUrl(Constants.SHARE_HTML5);
        //精致长按事件
        webView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                return true;
            }
        });
        webView.getSettings().setJavaScriptEnabled(true);//支持JavaScript
        webView.getSettings().setAllowFileAccess(true);//允许访问文件
        webView.getSettings().setAllowFileAccessFromFileURLs(true);//通过此API可以设置是否允许通过file url加载的Javascript读取其他的本地文件
        webView.getSettings().setAllowUniversalAccessFromFileURLs(true);//通过此API可以设置是否允许通过file url加载的Javascript可以访问其他的源，包括其他的文件和http,https等其他的源

        //将webView的横向竖向的scrollBar都禁用掉，将不再与ScrollView冲突，解决了大面积空白的问题
//        webView.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
//        webView.setVerticalScrollBarEnabled(false);
//        webView.setVerticalScrollbarOverlay(false);
//        webView.setHorizontalScrollBarEnabled(false);
//        webView.setHorizontalScrollbarOverlay(false);

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

        @JavascriptInterface
        //页面开始加载前
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
        }

        //页面加载出错
        public void onReceivedError(WebView view, int errorCode,String description, String failingUrl) {
            super.onReceivedError(view, errorCode, description, failingUrl);
            Log.e("错误信息",errorCode+"");
            view.loadUrl(Constants.ERROR_HTML5);
            if(errorCode == -2){
                mSVProgressHUD.showInfoWithStatus("网络不可用", SVProgressHUD.SVProgressHUDMaskType.GradientCancel);
            } else if(errorCode == -8){
                mSVProgressHUD.showInfoWithStatus("请求超时", SVProgressHUD.SVProgressHUDMaskType.GradientCancel);
            }
            swipeRefreshLayout.setRefreshing(false);
        }

        //页面加载完毕
        public void onPageFinished(WebView view, String url) {
            sendDataHtml();
            super.onPageFinished(view, url);
        }
    }

    @android.webkit.JavascriptInterface
    private void sendDataHtml() {
        final JSONObject jsonObject = new JSONObject();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mEncoder = new Encoder.Builder()
                        .setBackgroundColor(0xffffffff) // 指定背景颜色，默认为白色
                        .setCodeColor(0xff000000) // 指定编码块颜色，默认为黑色
                        .setOutputBitmapWidth((int) BaseTools.dpChangePx(ShareActivity.this,255)) // 生成图片宽度
                        .setOutputBitmapHeight((int) BaseTools.dpChangePx(ShareActivity.this,255)) // 生成图片高度
                        .setOutputBitmapPadding(1) // 设置为没有白边
                        .build();
                createEncode = PrefShared.getString(context,"qrcode");
                bitmap = mEncoder.encode(createEncode);
                final String url = BaseTools.bitmapToBase64(bitmap);
                webView.post(new Runnable() {
                    @Override
                    public void run() {
                        jsonObject.put("url",url);
                        webView.loadUrl("javascript:getData(" + jsonObject.toString() + ");");
                    }   
                });
            }
        });
    }

    @android.webkit.JavascriptInterface
    public void sendData(String wxAppId){
        if(NetWorkUtils.isNetworkConnected(this) == false){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mSVProgressHUD.showInfoWithStatus("网络不可用", SVProgressHUD.SVProgressHUDMaskType.GradientCancel);
                }
            });
        } else {
            initWeixin(wxAppId);
        }
    }

    /**
     * 初始化微信SDK
     * @param wxAppId
     */
    public void initWeixin(String wxAppId) {
        WXAPPID = wxAppId;
        iwxapi = WXAPIFactory.createWXAPI(this, wxAppId, false);
        iwxapi.registerApp(wxAppId);
    }

    @android.webkit.JavascriptInterface
    public void openShare(String num){
        if(TextUtils.equals("1",num)){
            weChatShare(1);
        } else if(TextUtils.equals("2",num)){
            weChatShare(0);
        } else if(TextUtils.equals("3",num)){
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            clipboard.setText(createEncode);
            dialog("","已复制邀请链接", Color.parseColor("#0876FE"),createEncode+"\n可发送该链接给[微信好友]\n(该链接仅微信中使用)",15, Gravity.CENTER,"","OK");
        } else if(TextUtils.equals("4",num)){
            saveImageToGallery(context, bitmap);
        }
    }

    /**
     * 保存二维码
     * @param context
     * @param bmp
     */
    public void saveImageToGallery(final Context context, final Bitmap bmp) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // 首先保存图片
                File appDir = new File(Environment.getExternalStorageDirectory(), Constants.CACHEDIRECTORY_PATH + "images");
                if (!appDir.exists()) {
                    appDir.mkdir();
                }
                String fileName = PrefShared.getString(context, "UUID") + ".jpg";
                File file = new File(appDir, fileName);
                try {
                    FileOutputStream fos = new FileOutputStream(file);
                    bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                    fos.flush();
                    fos.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                // 其次把文件插入到系统图库
                try {
                    MediaStore.Images.Media.insertImage(context.getContentResolver(), file.getAbsolutePath(), fileName, null);
                    // 最后通知图库更新
                    context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,	Uri.fromFile(new File(file.getPath()))));
                    dialog("","保存到相册成功",Color.parseColor("#0876FE"),"可以在微信扫一扫，打开相册中的二维码或发送给微信好友",15,Gravity.CENTER,"","OK");
                } catch (FileNotFoundException e) {
                    Toast.makeText(ShareActivity.this,"保存失败",Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
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
    private void dialog(final String type,String title,int titleColor,
                        String content,int contentSize,int contentGravity,
                        String leftBtn, String rightBtn) {
        final NormalDialog dialog = new NormalDialog(ShareActivity.this);
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
            }
        });
    }

    /**
     *
     * @param type 1为朋友圈 0为好友
     */
    private void weChatShare(final int type) {
        if (iwxapi.isWXAppInstalled() == false) {
            mSVProgressHUD.showInfoWithStatus("请先安装微信", SVProgressHUD.SVProgressHUDMaskType.GradientCancel);
        } else {
            if (!iwxapi.isWXAppSupportAPI()) {
                mSVProgressHUD.showInfoWithStatus("微信版本不支持", SVProgressHUD.SVProgressHUDMaskType.GradientCancel);
            } else {
                WXWebpageObject webpage = new WXWebpageObject();
                webpage.webpageUrl = createEncode;
                WXMediaMessage msg = new WXMediaMessage(webpage);
                //这里替换一张自己工程里的图片资源
                Bitmap thumb = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
                msg.setThumbImage(thumb);
                if (type == 1) {
                    msg.title = "听说很给力，赶紧过来瞧瞧吧";
                } else {
                    msg.title = "来赚";
                    msg.description = "听说很给力，赶紧过来瞧瞧吧";
                }
                SendMessageToWX.Req req = new SendMessageToWX.Req();
                req.transaction = String.valueOf(System.currentTimeMillis());
                req.message = msg;
                req.scene = type == 0 ? SendMessageToWX.Req.WXSceneSession : SendMessageToWX.Req.WXSceneTimeline;
                iwxapi.sendReq(req);
            }
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
