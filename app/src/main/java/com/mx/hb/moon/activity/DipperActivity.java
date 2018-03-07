package com.mx.hb.moon.activity;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.webkit.DownloadListener;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.mx.hb.moon.R;
import com.mx.hb.moon.base.AsyncHttpClientUtil;
import com.mx.hb.moon.base.AsyncUtil;
import com.mx.hb.moon.base.BaseTools;
import com.mx.hb.moon.base.Constants;
import com.mx.hb.moon.base.PrefShared;
import com.mx.hb.moon.dialog.NormalDialog;
import com.mx.hb.moon.dialog.OnBtnClickL;
import com.mx.hb.moon.loading.SVProgressHUD;
import com.mx.hb.moon.swipeRefresh.SwipeRefreshLayout;
import com.mx.hb.moon.view.StatusBarUtil;
import com.tencent.mm.sdk.modelmsg.SendAuth;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

import java.util.HashMap;

/**
 * Created by Moon on 2016/4/4.
 */
public class DipperActivity extends BaseActivity implements View.OnClickListener,SwipeRefreshLayout.OnRefreshListener{
    private static SVProgressHUD mSVProgressHUD;
    private LinearLayout onBack;
//    private ProgressBar pb;
    private SwipeRefreshLayout swipeRefreshLayout;
//    private Timer timer;
    private WebView webView;
    private RelativeLayout webViewBg;
    private TextView webViewTitle,bindingTitle,remake;

    private Handler mHandler = new Handler();
    private Handler fHandler = new Handler();
    public ZhifubaoPopupWindow zhifubaoWindow;
    private View viewmenu;
    private EditText account, name;
    private Button bindingBtn;
    private IWXAPI iwxapi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);
        mSVProgressHUD = new SVProgressHUD(context);
        initView();
        findByUserMsg();
    }

    @Override
    protected void setStatusBar() {
        StatusBarUtil.setColor(this, getResources().getColor(R.color.share),0);
    }

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
        webViewTitle.setText("申请提现");
        webViewBg.setBackgroundColor(getResources().getColor(R.color.share));
        webView = (WebView) findViewById(R.id.webView);
    }

    private void findByUserMsg() {
        AsyncHttpClientUtil asyncHttpClientUtil = new AsyncHttpClientUtil(0,new HashMap<String,String>());
        RequestParams params = new RequestParams();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("token", Constants.TOKENT);
        jsonObject.put("id", PrefShared.getString(context,"userId"));
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
                    String code = jsonObject.getString("code");
                    jsonObject = JSONObject.parseObject(jsonObject.getString("data"));
                    JSONObject infoObject = jsonObject.getJSONObject("info");
                    String zfbName = infoObject.getString("name");
                    String zhifubao = infoObject.getString("zhifubao");
                    String wxId = infoObject.getString("wxid");
                    if(TextUtils.equals("10",code)){
                        if(TextUtils.equals("",zhifubao) && TextUtils.equals("",zfbName)){
                            showZhifubaoPopupWindow();
                        } else if(TextUtils.equals("",wxId)){
                            iwxapi = WXAPIFactory.createWXAPI(context, Constants.WX_APPID, false);
                            iwxapi.registerApp(Constants.WX_APPID);
                            messageDialogTwo("0","温馨提示",Color.parseColor("#000000"),"提现需要绑定微信账号，检测到您还未绑定微信是否绑定微信账号？",16,Gravity.LEFT,"取消","绑定");
                        } else {
                            webView.loadUrl(Constants.ZFB_DIPPER + PrefShared.getString(context,"userId"));
//                          webView.loadUrl("http://www.google.com/");
                            webView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
                            webView.setHorizontalScrollBarEnabled(false);
                            webView.getSettings().setSupportZoom(true);
                            webView.getSettings().setBuiltInZoomControls(false);
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
                            webView.getSettings().setJavaScriptEnabled(true);
                            webView.getSettings().setDomStorageEnabled(true);
                            webView.setWebViewClient(new MyWebViewClient());
                            webView.setDownloadListener(new MyDownloadListener());
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
                    mSVProgressHUD.showErrorWithStatus("请求错误", SVProgressHUD.SVProgressHUDMaskType.GradientCancel);
                }
            }

            @Override
            public void onFinish() {
                super.onFinish();
            }
        });
    }

    /**
     * 打开微信客户端
     */
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
                } else {
                    if (mSVProgressHUD.isShowing()) {
                        mSVProgressHUD.dismiss();
                    }
                }
                super.onPostExecute(result);
            }
        };
        asyncUtil.execute();
    }

    /**
     * 显示支付宝弹框
     */
    public void showZhifubaoPopupWindow() {
        zhifubaoWindow = new ZhifubaoPopupWindow(this, new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        //显示窗口
        zhifubaoWindow.showAtLocation(webView, Gravity.CENTER_HORIZONTAL, 0, 0); //设置layout在PopupWindow中显示的位置
    }

    /**
     * 下拉刷新
     */
    @Override
    public void onRefresh() {
        findByUserMsg();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                swipeRefreshLayout.setRefreshing(false);
            }
        }, 2000);
    }

    public class ZhifubaoPopupWindow extends PopupWindow {

        public ZhifubaoPopupWindow(final Activity context, View.OnClickListener onClickListener) {
            super(context);
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            viewmenu = inflater.inflate(R.layout.window_binding, null);
            bindingTitle = (TextView) viewmenu.findViewById(R.id.bindingTitle);
            bindingTitle.setText(Html.fromHtml("<font color='#0876FE' font-size='16px'>支付宝</font>收款账号"));
            remake = (TextView) viewmenu.findViewById(R.id.remake);
            remake.setText("提示：\n\t\t请务必填写正确有效的支付宝账户和对应的账户名！");
            account = (EditText) viewmenu.findViewById(R.id.account);
            account.setHint("支付宝：账号");
            name = (EditText) viewmenu.findViewById(R.id.name);
            name.setHint("支付宝：姓名");
            bindingBtn = (Button) viewmenu.findViewById(R.id.bindingBtn);
            zhifubaoWindow = this;
            zhifubaoWindow.setContentView(viewmenu);
            zhifubaoWindow.setWidth(BaseTools.getWindowsWidth(context));////设置窗体的宽
            zhifubaoWindow.setHeight(LinearLayout.LayoutParams.WRAP_CONTENT);//设置窗体的高
            // 设置窗体为透明效果
            ColorDrawable cd = new ColorDrawable(0x000000);
            zhifubaoWindow.setOutsideTouchable(true);// 点击外部可关闭窗口
            zhifubaoWindow.setBackgroundDrawable(cd);
            zhifubaoWindow.setFocusable(true);//设置窗体可点击
            zhifubaoWindow.setTouchable(true);
            zhifubaoWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE|WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
            zhifubaoWindow.setInputMethodMode(PopupWindow.INPUT_METHOD_NEEDED);//不被输入法挡住
            zhifubaoWindow.setAnimationStyle(R.style.AnimTop);//设置窗体从底部进入的动画效果
            zhifubaoWindow.update();
            //关闭窗体时
            zhifubaoWindow.setOnDismissListener(new OnDismissListener() {
                // 在dismiss中恢复透明度
                public void onDismiss() {
                    WindowManager.LayoutParams lp = getWindow().getAttributes();
                    lp.alpha = 1f;
                    getWindow().setAttributes(lp);
                }
            });
            // 打开窗口时设置窗体背景透明度
            WindowManager.LayoutParams lp = getWindow().getAttributes();
            lp.alpha = 0.8f;
            getWindow().setAttributes(lp);

            bindingBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    submitZhifubao();
                }
            });
        }
    }

    /**
     * 提交支付宝账号
     */
    private void submitZhifubao() {
        if(TextUtils.equals("",account.getText().toString())){
            message3Dialog("请输入支付宝账号");
        } else if(!BaseTools.checkAlipay(account.getText().toString())){
            message3Dialog("请输入正确的支付宝账号");
        } else if(TextUtils.equals("",name.getText().toString())){
            message3Dialog("请输入收款人姓名");
        } else if(!BaseTools.isChinaName(name.getText().toString())){
            message3Dialog("请输入正确的收款人姓名");
        } else {
            boundEquipment();
            zhifubaoWindow.dismiss();
        }
    }

    private void message3Dialog(String content) {
        final NormalDialog dialog = new NormalDialog(DipperActivity.this);
        dialog.widthScale((float) 0.75);
        dialog.title("温馨提示");
        dialog.titleTextColor(Color.parseColor("#0876FE"));
        dialog.titleLineColor(Color.parseColor("#61AEDC"));
        dialog.isTitleShow(false)
                //
                .bgColor(Color.parseColor("#FFFFFF"))
                //
                .cornerRadius(8)
                //
                .content(content)
                //
                .contentGravity(Gravity.CENTER)
                //
                .contentTextColor(Color.parseColor("#000000"))
                //
                .dividerColor(Color.parseColor("#CCCCCC"))
                .contentTextColor(Color.parseColor("#000000"))
                .titleTextSize(10f)
                .btnNum(1)
                .btnText("OK")
                .btnTextColor(Color.parseColor("#0876FE")/*,Color.parseColor("#0876FE")*/)//
                .btnPressColor(Color.parseColor("#E5E5E5"))//
                //.widthScale(0.85f)//
                //.showAnim(bas_in)//
                //.dismissAnim(bas_out)//
                .show();
        dialog.setOnBtnClickL(new OnBtnClickL() {
            @Override
            public void onBtnClick() {
                dialog.dismiss();
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
    private void messageDialogTwo(final String type,String title,int titleColor,
                                  String content,int contentSize,int contentGravity,
                                  String leftBtn, String rightBtn) {
        final NormalDialog dialog = new NormalDialog(DipperActivity.this);
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
        dialog.btnTextSize(15.5f, 15.5f);//设按钮的字体大小
        dialog.btnTextColor(getResources().getColor(R.color.dialog_blue), getResources().getColor(R.color.dialog_blue));//设按钮的字体颜色
        dialog.btnPressColor(Color.parseColor("#E5E5E5"));//设按钮点击时的背景颜色
        dialog.show();

        dialog.setOnBtnClickL(new OnBtnClickL() {
            @Override
            public void onBtnClick() {
                dialog.dismiss();
                fHandler.postDelayed(fRunnable,100);
            }
        }, new OnBtnClickL() {
            @Override
            public void onBtnClick() {
                dialog.dismiss();
                weChatLogin();
            }
        });
    }

    /**
     * 绑定设备
     */
    private void boundEquipment() {
        AsyncHttpClientUtil asyncHttpClientUtil = new AsyncHttpClientUtil(0,new HashMap<String,String>());
        RequestParams params = new RequestParams();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("token", Constants.TOKENT);
        jsonObject.put("type",4);
        jsonObject.put("id",account.getText().toString());
        jsonObject.put("password","");
        jsonObject.put("name",name.getText().toString());
        jsonObject.put("only", PrefShared.getString(context,"UUID"));
//        Log.e("绑定设备",jsonObject.toString());
        params.put(Constants.REQUEST_MSG,jsonObject.toString());
        asyncHttpClientUtil.doPost(Constants.EQUIPMENT_BIND, params, new AsyncHttpResponseHandler() {
            @Override
            public void onStart() {
                mSVProgressHUD.showWithStatus("绑定中...", SVProgressHUD.SVProgressHUDMaskType.Black);
                super.onStart();
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
//                        PrefShared.saveString(context,"zifubao",account.getText().toString());
//                        PrefShared.saveString(context,"zfbName",name.getText().toString());
                        findByUserMsg();
                    } else {
                        mSVProgressHUD.showErrorWithStatus(msg, SVProgressHUD.SVProgressHUDMaskType.GradientCancel);
                        fHandler.postDelayed(fRunnable,2500);
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

    private Runnable fRunnable = new Runnable() {
        @Override
        public void run() {
            finish();
        }
    };

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
//            runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    timer = new Timer();
//                    TimerTask timerTask = new TimerTask() {
//                        @Override
//                        public void run() {
//                            if(webView.getProgress() < 100){
//                                mSVProgressHUD.showInfoWithStatus("请求超时", SVProgressHUD.SVProgressHUDMaskType.GradientCancel);
//                            }
//                        }
//                    };
//                    timer.schedule(timerTask, 5000, 1);
//                }
//            });
        }

        //页面加载出错
        @JavascriptInterface
        public void onReceivedError(WebView view, int errorCode,String description, String failingUrl) {
            Log.e("错误信息",errorCode+"");
            view.loadUrl(Constants.ERROR_HTML5);
            if(errorCode == -2){
                mSVProgressHUD.showInfoWithStatus("网络不可用", SVProgressHUD.SVProgressHUDMaskType.GradientCancel);
            } else if(errorCode == -8){
                mSVProgressHUD.showInfoWithStatus("请求超时", SVProgressHUD.SVProgressHUDMaskType.GradientCancel);
            }
            swipeRefreshLayout.setRefreshing(false);
            super.onReceivedError(view, errorCode, description, failingUrl);
        }

        @JavascriptInterface
        //页面加载完毕
        public void onPageFinished(WebView view, String url) {
            view.loadUrl("javascript:window.local_obj.showSource('<head>'+" + "document.getElementsByTagName('html')[0].innerHTML+'</head>');");
            super.onPageFinished(view, url);
//            timer.cancel();
//            timer.purge();
        }
    }

    class MyDownloadListener implements DownloadListener{
        @Override
        public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
            if(!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
                mSVProgressHUD.showInfoWithStatus("暂无SD卡", SVProgressHUD.SVProgressHUDMaskType.GradientCancel);
                return;
            } else {
                DownloaderTask task = new DownloaderTask();
                task.execute(url);
            }
        }
    }

    class DownloaderTask extends AsyncUtil{

        @Override
        public void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        public Long doInBackground(String... params) {
            return null;
        }

        @Override
        public void onPostExecute(Long result) {
            super.onPostExecute(result);
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
        finish();
    }

//    @Override
//    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        // TODO Auto-generated method stub
//        if (keyCode == KeyEvent.KEYCODE_BACK) {
//            if (webView.canGoBack()) {
//                webView.goBack();// 返回上一页面
//                return true;
//            } else {
//                finish();//结束当前Activity
//            }
//        }
//        return super.onKeyDown(keyCode, event);
//    }

}
