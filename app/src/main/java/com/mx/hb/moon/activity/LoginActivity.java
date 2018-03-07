package com.mx.hb.moon.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.Selection;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.mx.hb.moon.R;
import com.mx.hb.moon.application.MainApplication;
import com.mx.hb.moon.base.AES;
import com.mx.hb.moon.base.AsyncHttpClientUtil;
import com.mx.hb.moon.base.AsyncUtil;
import com.mx.hb.moon.base.BaseTools;
import com.mx.hb.moon.base.Constants;
import com.mx.hb.moon.base.NetWorkUtils;
import com.mx.hb.moon.base.PrefShared;
import com.mx.hb.moon.dialog.NormalDialog;
import com.mx.hb.moon.dialog.OnBtnClickL;
import com.mx.hb.moon.entity.WXLoginEvent;
import com.mx.hb.moon.loading.SVProgressHUD;
import com.mx.hb.moon.base.CountDownTimerUtils;
import com.mx.hb.moon.view.EditTextWithClear;
import com.mx.hb.moon.view.StatusBarUtil;
import com.tencent.mm.sdk.modelmsg.SendAuth;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Moon on 2016/5/23.
 */
public class LoginActivity extends BaseActivity implements View.OnClickListener {
    private static SVProgressHUD mSVProgressHUD;
    private LinearLayout loginllt1;
    private TextView wLogin, pLogin;
    private WebView webView;
    private IWXAPI iwxapi;
    private static String unionid,nickname,imgUrl;

    //自定义的弹出框类
    private pLoginPopupWindow popupWindow;
    private View viewmenu;
    private EditTextWithClear telphone, code;
    private Button queryCode,loginBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mSVProgressHUD = new SVProgressHUD(context);
        initView();
        initWeixin();
//        try {
//            getOpendId(getIntent().getExtras().getString("code"));
//        } catch (Exception e){
//
//        }
    }

    @Override
    protected void setStatusBar() {
        StatusBarUtil.setTransparent(LoginActivity.this);
    }

    private void initView() {
        loginllt1 = (LinearLayout) findViewById(R.id.loginllt1);
        wLogin = (TextView) findViewById(R.id.wLogin);
        pLogin = (TextView) findViewById(R.id.pLogin);
        webView = (WebView) findViewById(R.id.loginWebView);
        setHeaderWH();
        initWebView();
        wLogin.setOnClickListener(this);
        pLogin.setOnClickListener(this);
        webView.loadUrl(Constants.LOGIN_HTML5);
        webView.setWebViewClient(new LoginWebViewClient(this));
        webView.setWebChromeClient(new WebChromeClient());
        webView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                return true;
            }
        });
    }
    /**
     * 初始化微信SDK
     */
    private void initWeixin() {
        iwxapi = WXAPIFactory.createWXAPI(this, Constants.WX_APPID, false);
        iwxapi.registerApp(Constants.WX_APPID);
    }


    /**
     * 设置头部宽高
     */
    private void setHeaderWH() {
        LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, (int) (BaseTools.getWindowsHeight(this) / 2.5));
        loginllt1.setLayoutParams(params1);
        final LinearLayout.LayoutParams weixin = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        ViewTreeObserver vto = loginllt1.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                loginllt1.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                weixin.setMargins((int) BaseTools.dpChangePx(context,20),0,(int) BaseTools.dpChangePx(context,20), loginllt1.getHeight() / 10);
            }
        });
        wLogin.setLayoutParams(weixin);
    }


    private void initWebView() {
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setRenderPriority(WebSettings.RenderPriority.HIGH);
        webView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);  //设置缓存模式
        // 开启 DOM storage API 功能
        webView.getSettings().setDomStorageEnabled(true);
        //开启 database storage API 功能
        webView.getSettings().setDatabaseEnabled(true);
        String cacheDirPath = getFilesDir().getAbsolutePath() + Constants.CACHEDIRECTORY_PATH + "webView";
        //设置数据库缓存路径
        webView.getSettings().setDatabasePath(cacheDirPath);
        //设置  Application Caches 缓存目录
        webView.getSettings().setAppCachePath(cacheDirPath);
        //开启 Application Caches 功能
        webView.getSettings().setAppCacheEnabled(true);
    }

    /**
     * 显示手机登录弹框
     */
    public void showPLoginPopupWindow() {
        popupWindow = new pLoginPopupWindow(this, new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        //显示窗口
        popupWindow.showAtLocation(pLogin, Gravity.CENTER_HORIZONTAL, 0, 0); //设置layout在PopupWindow中显示的位置
    }

    public class pLoginPopupWindow extends PopupWindow {

        public pLoginPopupWindow(final Activity context, View.OnClickListener onClickListener) {
            super(context);
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            viewmenu = inflater.inflate(R.layout.window_login, null);

            telphone = (EditTextWithClear) viewmenu.findViewById(R.id.telphone);
            BaseTools.phoneNumAddSpace(telphone);
            code = (EditTextWithClear) viewmenu.findViewById(R.id.code);

            queryCode = (Button) viewmenu.findViewById(R.id.queryCode);

            loginBtn = (Button) viewmenu.findViewById(R.id.loginBtn);
            popupWindow = this;
            popupWindow.setContentView(viewmenu);
            popupWindow.setWidth(BaseTools.getWindowsWidth(context));////设置窗体的宽
            popupWindow.setHeight(LinearLayout.LayoutParams.WRAP_CONTENT);//设置窗体的高
            // 设置窗体为透明效果
            ColorDrawable cd = new ColorDrawable(0x000000);
            popupWindow.setOutsideTouchable(true);// 点击外部可关闭窗口
            popupWindow.setBackgroundDrawable(cd);
            popupWindow.setFocusable(true);//设置窗体可点击
            popupWindow.setTouchable(true);
            popupWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE|WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
            popupWindow.setInputMethodMode(PopupWindow.INPUT_METHOD_NEEDED);//不被输入法挡住
            popupWindow.setAnimationStyle(R.style.AnimTop);//设置窗体从底部进入的动画效果
            popupWindow.update();
            //关闭窗体时
            popupWindow.setOnDismissListener(new OnDismissListener() {
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

            queryCode.setOnClickListener(LoginActivity.this);

            loginBtn.setOnClickListener(LoginActivity.this);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.wLogin://微信登录
                if (processFlag) {
                    setProcessFlag();
                    if(NetWorkUtils.isNetworkConnected(this) == false){
                        mSVProgressHUD.showInfoWithStatus("网络不可用", SVProgressHUD.SVProgressHUDMaskType.GradientCancel);
                    } else {
                        weChatLogin();
                    }
                    new TimeThread().start();
                }
                break;
            case R.id.pLogin://手机登录
                if (processFlag) {
                    setProcessFlag();
                    showPLoginPopupWindow();
//                    if(NetWorkUtils.isNetworkConnected(this) == false){
//                        mSVProgressHUD.showInfoWithStatus("网络不可用", SVProgressHUD.SVProgressHUDMaskType.GradientCancel);
//                    } else {
//                        pLogin(1);
//                    }
                    new TimeThread().start();
                }
                break;
            case R.id.queryCode:
                if(processFlag){
                    setProcessFlag();
                    String telphoneNum = telphone.getText().toString().replaceAll(" ","");
                    if(BaseTools.isNumeric(telphoneNum) && telphoneNum.length() == 11) {
                        getPhoneCode(telphoneNum);
                        CountDownTimerUtils mCountDownTimerUtils = new CountDownTimerUtils(queryCode, 60000, 1000);
                        mCountDownTimerUtils.start();
                    } else {
                        dialog("手机号码",(float) 0.70,false,"", Color.parseColor("#0876FE"),1,"请输入正确的手机号码",16, Gravity.CENTER,"OK","OK",15f,15f,getResources().getColor(R.color.dialog_blue),getResources().getColor(R.color.dialog_blue));
                    }
                    new TimeThread().start();
                }
                break;
            case R.id.loginBtn:
                if(processFlag){
                    setProcessFlag();
                    String telphoneNum = telphone.getText().toString().replaceAll(" ","");
                    String codeNum = code.getText().toString().trim();
                    if(BaseTools.isNumeric(telphoneNum) && telphoneNum.length() == 11) {
                        if(TextUtils.equals("",codeNum)) {
                            dialog("验证码",(float) 0.70,false,"", Color.parseColor("#0876FE"),1,"请输入验证码",16, Gravity.CENTER,"OK","OK",15f,15f,getResources().getColor(R.color.dialog_blue),getResources().getColor(R.color.dialog_blue));
                        } else {
                            popupWindow.dismiss();
                            nLogin(11,telphoneNum,codeNum);
                        }
                    } else{
                        dialog("手机号码",(float) 0.70,false,"", Color.parseColor("#0876FE"),1,"请输入正确的手机号码",16, Gravity.CENTER,"OK","OK",15f,15f,getResources().getColor(R.color.dialog_blue),getResources().getColor(R.color.dialog_blue));
                    }
                    new TimeThread().start();
                }
                break;
            default:
                break;
        }
    }

    /**
     * 手机登录
     * @param telphoneNum
     */
    private void getPhoneCode(String telphoneNum) {
        AsyncHttpClientUtil asyncHttpClientUtil = new AsyncHttpClientUtil(0,new HashMap<String,String>());

        RequestParams params = new RequestParams();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("token",Constants.TOKENT);//每个接口都需要的token
        jsonObject.put("phone",telphoneNum);//登录类型
        jsonObject.put("codeType","1");//设备码

        long num = AES.get10Random();
        String content = AES.encrypt(jsonObject.toString(),AES.md5(AES.longMinusNum(num+"")));
        JSONObject json = new JSONObject();
        json.put("content",content);
        json.put("secret",num+"");
        json.put("hehe",1);

        params.put(Constants.REQUEST_MSG,json.toString());

        asyncHttpClientUtil.doPost(Constants.GET_CODE, params, new AsyncHttpResponseHandler() {
            @Override
            public void onStart() {
                super.onStart();
            }

            @Override
            public void onSuccess(int statusCode, org.apache.http.Header[] headers, byte[] bytes) {
                try {
                    String result = new String(bytes, "UTF-8");
                    JSONObject json = JSONObject.parseObject(result);
                    String secret = json.getString("secret");
                    String content = json.getString("content");
                    result = AES.decrypt(content,AES.md5(AES.longMinusNum(secret)));
                    JSONObject jsonObject = JSONObject.parseObject(result);
                    String code = jsonObject.getString("code");
                    String msg = jsonObject.getString("msg");
                    dialog("获取验证码",(float) 0.70,false,"", Color.parseColor("#0876FE"),1,msg,16, Gravity.CENTER,"OK","OK",15f,15f,getResources().getColor(R.color.dialog_blue),getResources().getColor(R.color.dialog_blue));
                } catch (Exception e){
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int i, org.apache.http.Header[] headers, byte[] bytes, Throwable throwable) {
                try {
                    String error = throwable.toString();
                    Log.e("网络请求",error);
                    if(error.contains("ConnectTimeoutException")){
                        dialog("获取验证码",(float) 0.70,false,"", Color.parseColor("#0876FE"),1,"请求超时",16, Gravity.CENTER,"OK","OK",15f,15f,getResources().getColor(R.color.dialog_blue),getResources().getColor(R.color.dialog_blue));
                    } else if(error.contains("HttpHostConnectException")){
                        dialog("获取验证码",(float) 0.70,false,"", Color.parseColor("#0876FE"),1,"网络错误",16, Gravity.CENTER,"OK","OK",15f,15f,getResources().getColor(R.color.dialog_blue),getResources().getColor(R.color.dialog_blue));
                    } else {
                        dialog("获取验证码",(float) 0.70,false,"", Color.parseColor("#0876FE"),1,"请求错误",16, Gravity.CENTER,"OK","OK",15f,15f,getResources().getColor(R.color.dialog_blue),getResources().getColor(R.color.dialog_blue));
                    }
                } catch (Exception e){
                    e.printStackTrace();
                }
            }

            @Override
            public void onFinish() {
                super.onFinish();
                if (mSVProgressHUD.isShowing()) {
                    mSVProgressHUD.dismiss();
                }
            }
        });
    }

    /**
     *
     * @param type 提示框的类型（哪个方法执行的）
     * @param dialogWidth 弹窗的宽度
     * @param isShowTitle 是否显示标题
     * @param title 标题
     * @param titleColor 标题颜色
     * @param btnNum 弹框按钮的个数
     * @param content 内容
     * @param contentSize 内容字体大小
     * @param contentGravity 内容显示的位置
     * @param leftText 弹框的左边按钮
     * @param rightText 弹框的右边按钮
     */
    private void dialog(final String type,float dialogWidth, boolean isShowTitle, String title, int titleColor,
                        int btnNum, final String content, int contentSize, int contentGravity,
                        String leftText, String rightText, float leftSize, float rightSize, int leftColor, int rightColor) {
        final NormalDialog dialog = new NormalDialog(LoginActivity.this);
        /*弹窗*/
        dialog.widthScale(dialogWidth);//设整个弹窗的宽度
        dialog.cornerRadius(8);//设弹窗的圆角
//        dialog.style(btnNum == 1 ? NormalDialog.STYLE_ONE : NormalDialog.STYLE_TWO);//设为两个按钮
        dialog.isTitleShow(isShowTitle);//显示标题
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
        /*点缀部分*/
        dialog.dividerColor(Color.parseColor("#CCCCCC"));//分割线的颜色
        /*按钮*/
        dialog.btnNum(btnNum);//设置弹窗的按钮
        dialog.btnPressColor(Color.parseColor("#E5E5E5"));//设按钮点击时的背景颜色
        if(btnNum == 1){//如果有1个按钮
            dialog.btnText(rightText);//设按钮的文字内容
            dialog.btnTextSize(rightSize);//设按钮的字体大小
            dialog.btnTextColor(rightColor);//设按钮的字体颜色
            dialog.setOnBtnClickL(new OnBtnClickL() {//第一个按钮
                @Override
                public void onBtnClick() {
                    dialog.dismiss();
                }
            });
        } else {
            dialog.btnText(leftText,rightText);//设按钮的文字内容
            dialog.btnTextSize(leftSize,rightSize);//设按钮的字体大小
            dialog.btnTextColor(leftColor,rightColor);//设按钮的字体颜色
            dialog.setOnBtnClickL(new OnBtnClickL() {//第一个按钮
                @Override
                public void onBtnClick() {
                    dialog.dismiss();
                }
            }, new OnBtnClickL() {
                @Override
                public void onBtnClick() {//第二个按钮
                    dialog.dismiss();
                }
            });
        }
        dialog.show();
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
                }
                super.onPostExecute(result);
            }
        };
        asyncUtil.execute();
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(WXLoginEvent event) {
        if (mSVProgressHUD.isShowing()) {
            mSVProgressHUD.dismiss();
            if(!TextUtils.equals(event.message,"成功")){
                Toast.makeText(context,event.message,Toast.LENGTH_SHORT).show();
            }
        }
        if(event.code != null && !TextUtils.equals(event.code,"")){
            getOpendId(event.code);
        }
    }

    /**
     * 微信登录
     * @param loginType 登录类型 3
     * @param unionId
     * @param nickname
     * @param imgUrl
     */
    private void wLogin(final int loginType, String unionId, String nickname, String imgUrl){
        String uId = PrefShared.getString(context,"UUID");//获取手机设备码
        AsyncHttpClientUtil asyncHttpClientUtil = new AsyncHttpClientUtil(0,new HashMap<String,String>());

        RequestParams params = new RequestParams();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("token",Constants.TOKENT);//每个接口都需要的token
        jsonObject.put("type",loginType);//登录类型
        jsonObject.put("id",unionId);//登录账号
        jsonObject.put("only",uId);//设备码
        jsonObject.put("nickname",nickname);//用户昵称
        jsonObject.put("icon",imgUrl);//用户图标地址
        jsonObject.put("password","");//登录密码
        jsonObject.put("objectid",PrefShared.getString(context,"objectId"));//objectId

        final String UID = loginType+"_" + unionId;
        PrefShared.saveString(context, "UID", UID);//设置做任务成功后的回调用户ID（3_******）

        long num = AES.get10Random();
        String content = AES.encrypt(jsonObject.toString(),AES.md5(AES.longMinusNum(num+"")));
        JSONObject json = new JSONObject();
        json.put("content",content);
        json.put("secret",num+"");
        json.put("hehe",1);

        params.put(Constants.REQUEST_MSG,json.toString());

        asyncHttpClientUtil.doPost(Constants.REGISTER_OR_LOGIN_PATH, params, new AsyncHttpResponseHandler() {
            @Override
            public void onStart() {
                super.onStart();
                mSVProgressHUD.showWithStatus("加载中...", SVProgressHUD.SVProgressHUDMaskType.Black);
            }

            @Override
            public void onSuccess(int statusCode, org.apache.http.Header[] headers, byte[] bytes) {
                startMainActivity(bytes,UID,mSVProgressHUD);
            }

            @Override
            public void onFailure(int i, org.apache.http.Header[] headers, byte[] bytes, Throwable throwable) {
                try {
                    String error = throwable.toString();
                    Log.e("网络请求",error);
                    if(error.contains("ConnectTimeoutException")){
                        mSVProgressHUD.showInfoWithStatus("请求超时", SVProgressHUD.SVProgressHUDMaskType.GradientCancel);
                    } else if(error.contains("HttpHostConnectException")){
                        mSVProgressHUD.showInfoWithStatus("网络错误", SVProgressHUD.SVProgressHUDMaskType.GradientCancel);
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
                if (mSVProgressHUD.isShowing()) {
                    mSVProgressHUD.dismiss();
                }
            }
        });
    }

    /**
     * 硬件码登录
     * @param loginType 登录类型 1
     */
//    public void pLogin(final int loginType){
//        String uId = PrefShared.getString(context,"UUID");//获取手机设备码
//        AsyncHttpClientUtil asyncHttpClientUtil = new AsyncHttpClientUtil(0,new HashMap<String,String>());
//
//        RequestParams params = new RequestParams();
//        JSONObject jsonObject = new JSONObject();
//        jsonObject.put("token",Constants.TOKENT);//每个接口都需要的token
//        jsonObject.put("type",loginType);//登录类型
//        jsonObject.put("id",uId);//登录账号
//        jsonObject.put("only",uId);//设备码
//        jsonObject.put("nickname","");//用户昵称
//        jsonObject.put("icon","");//用户图标地址
//        jsonObject.put("password","");//登录密码
//        jsonObject.put("objectid",PrefShared.getString(context,"objectId"));//objectId
//
////        Log.e("手机登录",jsonObject.toString());
//
//        final String UID = loginType+"_"+uId;
//        PrefShared.saveString(context, "UID", UID);//设置做任务成功后的回调用户ID（1_******）
//
//        long num = AES.get10Random();
//        String content = AES.encrypt(jsonObject.toString(),AES.md5(AES.longMinusNum(num+"")));
//        JSONObject json = new JSONObject();
//        json.put("content",content);
//        json.put("secret",num+"");
//        json.put("hehe",1);
//
//        params.put(Constants.REQUEST_MSG,json.toString());
//
//        asyncHttpClientUtil.doPost(Constants.REGISTER_OR_LOGIN_PATH, params, new AsyncHttpResponseHandler() {
//            @Override
//            public void onStart() {
//                super.onStart();
//                mSVProgressHUD.showWithStatus("加载中...", SVProgressHUD.SVProgressHUDMaskType.Black);
//            }
//
//            @Override
//            public void onSuccess(int statusCode, org.apache.http.Header[] headers, byte[] bytes) {
//                startMainActivity(bytes,UID,mSVProgressHUD);
//            }
//
//            @Override
//            public void onFailure(int i, org.apache.http.Header[] headers, byte[] bytes, Throwable throwable) {
//                try {
//                    String error = throwable.toString();
//                    Log.e("网络请求",error);
//                    if(error.contains("ConnectTimeoutException")){
//                        mSVProgressHUD.showInfoWithStatus("请求超时", SVProgressHUD.SVProgressHUDMaskType.GradientCancel);
//                    } else if(error.contains("HttpHostConnectException")){
//                        mSVProgressHUD.showInfoWithStatus("网络错误", SVProgressHUD.SVProgressHUDMaskType.GradientCancel);
//                    } else {
//                        mSVProgressHUD.showErrorWithStatus("请求错误", SVProgressHUD.SVProgressHUDMaskType.GradientCancel);
//                    }
//                } catch (Exception e){
//                    e.printStackTrace();
//                }
//            }
//
//            @Override
//            public void onFinish() {
//                super.onFinish();
//                if (mSVProgressHUD.isShowing()) {
//                    mSVProgressHUD.dismiss();
//                }
//            }
//        });
//
//    }

    /**
     * 手机号码登录
     * @param loginType 登录类型 11
     * @param telphoneNum
     * @param codeNum
     */
    public void nLogin(final int loginType, String telphoneNum, String codeNum){
        String uId = PrefShared.getString(context,"UUID");//获取手机设备码
        AsyncHttpClientUtil asyncHttpClientUtil = new AsyncHttpClientUtil(0,new HashMap<String,String>());

        RequestParams params = new RequestParams();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("token",Constants.TOKENT);//每个接口都需要的token
        jsonObject.put("type",loginType);//登录类型
        jsonObject.put("id",telphoneNum);//登录账号
        jsonObject.put("vcode",codeNum);//登录密码
        jsonObject.put("only",uId);//设备码
        jsonObject.put("nickname","");//用户昵称
        jsonObject.put("icon","");//用户图标地址
        jsonObject.put("password","");//登录密码
        jsonObject.put("objectid",PrefShared.getString(context,"objectId"));//objectId

//        Log.e("手机登录",jsonObject.toString());

        final String UID = loginType+"_"+telphoneNum;
        PrefShared.saveString(context, "UID", UID);//设置做任务成功后的回调用户ID（11_******）

        long num = AES.get10Random();
        String content = AES.encrypt(jsonObject.toString(),AES.md5(AES.longMinusNum(num+"")));
        JSONObject json = new JSONObject();
        json.put("content",content);
        json.put("secret",num+"");
        json.put("hehe",1);

        params.put(Constants.REQUEST_MSG,json.toString());

        asyncHttpClientUtil.doPost(Constants.REGISTER_OR_LOGIN_PATH, params, new AsyncHttpResponseHandler() {
            @Override
            public void onStart() {
                super.onStart();
                mSVProgressHUD.showWithStatus("加载中...", SVProgressHUD.SVProgressHUDMaskType.Black);
            }

            @Override
            public void onSuccess(int statusCode, org.apache.http.Header[] headers, byte[] bytes) {
                startMainActivity(bytes,UID,mSVProgressHUD);
            }

            @Override
            public void onFailure(int i, org.apache.http.Header[] headers, byte[] bytes, Throwable throwable) {
                try {
                    String error = throwable.toString();
                    Log.e("网络请求",error);
                    if(error.contains("ConnectTimeoutException")){
                        mSVProgressHUD.showInfoWithStatus("请求超时", SVProgressHUD.SVProgressHUDMaskType.GradientCancel);
                    } else if(error.contains("HttpHostConnectException")){
                        mSVProgressHUD.showInfoWithStatus("网络错误", SVProgressHUD.SVProgressHUDMaskType.GradientCancel);
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
//                if (mSVProgressHUD.isShowing()) {
//                    mSVProgressHUD.dismiss();
//                }
            }
        });

    }

//    public Handler getCodeHandel = new Handler(){
//        @Override
//        public void handleMessage(Message msg) {
//            super.handleMessage(msg);
//            String code = (String) msg.obj;
//            getOpendId(code);
//        }
//    };

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
                        Log.e("登录时微信登录", "token或者opendId为null");
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
                    nickname = jsonObject.getString("nickname");
                    String openid = jsonObject.getString("openid");
                    imgUrl = jsonObject.getString("headimgurl");
                    unionid = jsonObject.getString("unionid");
                    if((null != unionid && null != nickname && null != imgUrl) &&
                      !TextUtils.equals(unionid, "") && !TextUtils.equals(nickname, "")  && !TextUtils.equals(imgUrl, "")){
                        wLogin(3,unionid,nickname,imgUrl);
                    } else {
                        Log.e("登录时微信登录", "unionid或者nickname或者imgUrl为null");
                        if (mSVProgressHUD.isShowing()) {
                            mSVProgressHUD.dismiss();
                        }
                    }
                    try {
                    } catch (Exception e) {
                        e.printStackTrace();
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
//                if (mSVProgressHUD.isShowing()) {
//                    mSVProgressHUD.dismiss();
//                }
            }
        });
    }

    /**
     * 处理成功以后跳转到MainActivity
     * @param bytes 返回的json数据
     * @param UID 登录类型加账户
     * @param mSVProgressHUD
     */
    private void startMainActivity(byte[] bytes, String UID, SVProgressHUD mSVProgressHUD) {
        int loginType = Integer.parseInt(UID.substring(0,UID.indexOf("_")));//登录类型
        String userAccount = UID.substring(UID.indexOf("_")+1,UID.length());//用户账号
        try {
            String result = new String(bytes, "UTF-8");
//            Log.e("result",result);
            JSONObject json = JSONObject.parseObject(result);
            String secret = json.getString("secret");
            String content = json.getString("content");
            result = AES.decrypt(content,AES.md5(AES.longMinusNum(secret)));
            JSONObject jsonObject = JSONObject.parseObject(result);
//            Log.e("login",jsonObject.toString());
            String type = jsonObject.getString("type");
            String code = jsonObject.getString("code");
            String msg = jsonObject.getString("msg");
            String qrcode = jsonObject.getString("qrcode");
            PrefShared.saveString(context,"qrcode",qrcode);
            JSONObject jsonData = null;
            if(jsonObject.getString("data") != null){
                jsonData = JSONObject.parseObject(jsonObject.getString("data"));
                if(!TextUtils.equals("",jsonData.getString("icon"))){
                    String userIcon = jsonData.getString("icon");
                    if(userIcon.contains("http:")){
                        PrefShared.saveString(context,"userIcon",userIcon);
                    } else {
                        PrefShared.saveString(context,"userIcon",Constants.REQUEST + userIcon);
                    }
                }
            }
            if(TextUtils.equals("10",code)){
                if(TextUtils.equals("1",type)) {//注册
                    PrefShared.saveString(context,"userId",jsonObject.getString("id"));
                    PrefShared.saveString(context,"objectId",jsonObject.getString("objectid"));
                } else {//登录
                    PrefShared.saveString(context,"userId",jsonData.getString("id"));
                    PrefShared.saveString(context,"objectId",jsonData.getString("objectid"));
                }
                startActivity(new Intent(context,MainActivity.class));
                finish();
            } else {
//                errorWindow(msg,code,loginType,userAccount, mSVProgressHUD);
                mSVProgressHUD.showInfoWithStatus(msg, SVProgressHUD.SVProgressHUDMaskType.GradientCancel);
            }
        } catch (Exception e) {
            mSVProgressHUD.showErrorWithStatus("数据错误", SVProgressHUD.SVProgressHUDMaskType.GradientCancel);
            e.printStackTrace();
        }
    }

    /**
     * 错误提示信息
     * @param msg 返回的信息介绍
     * @param code 返回码：0是设备码存在，3是设备码错误
     * @param loginType 登录类型：3是微信登录，1是手机登录
     * @param account 登录的账号
     * @param mSVProgressHUD 转菊花:loading效果
     */
//    private void errorWindow(String msg, final String code, final int loginType, final String account, SVProgressHUD mSVProgressHUD) {
//        if(mSVProgressHUD.isShowing()){
//            mSVProgressHUD.dismiss();
//        }
//        final NormalDialog dialog = new NormalDialog(context);
//        dialog.widthScale((float) 0.6);
//        dialog.title(msg);
//        dialog.style(NormalDialog.STYLE_TWO);
//        dialog.titleTextColor(Color.parseColor("#000000"));
//        dialog.isTitleShow(true)
//                //
//                .bgColor(Color.parseColor("#FFFFFF"))
//                //
//                .cornerRadius(8)
//                //
//                .content("是否重新绑定？")
//                //
//                .contentGravity(Gravity.CENTER)
//                //
//                .contentTextColor(Color.parseColor("#000000"))
//                .contentTextSize(15.5f)
//                .titleTextSize(15.5f)
//                //
//                .dividerColor(Color.parseColor("#CCCCCC"))
//                //
//                .btnTextSize(15.5f, 15.5f)
//                //
//                .btnText("取消", "确认")
//                .btnTextColor(Color.parseColor("#0876FE"), Color.parseColor("#0876FE"))//
//                .btnPressColor(Color.parseColor("#E5E5E5"))//
//                .show();
//
//        dialog.setOnBtnClickL(new OnBtnClickL() {
//            @Override
//            public void onBtnClick() {
//                dialog.dismiss();
//            }
//        }, new OnBtnClickL() {
//            @Override
//            public void onBtnClick() {
//                dialog.dismiss();
//                boundEquipment(code,loginType,account);
//            }
//        });
//    }

    /**
     * 绑定设备
     * @param code
     * @param account
     */
//    private void boundEquipment(String code, final int loginType, String account) {
//        String url = "";
//        AsyncHttpClientUtil asyncHttpClientUtil = new AsyncHttpClientUtil(0,new HashMap<String,String>());
//        RequestParams params = new RequestParams();
//        JSONObject jsonObject = new JSONObject();
//        jsonObject.put("token",Constants.TOKENT);
//        jsonObject.put("type",loginType);
//        jsonObject.put("id",account);
//        jsonObject.put("password","");
//        jsonObject.put("only",PrefShared.getString(context,"UUID"));
////        Log.e("绑定设备",jsonObject.toString());
//        if(TextUtils.equals("3",code)){//更新用户账户号设备码
//            url = Constants.EQUIPMENT_UPDATE;
//        } else {//绑定第三方账号
//            url = Constants.EQUIPMENT_BIND;
//        }
//        params.put(Constants.REQUEST_MSG,jsonObject.toString());
//        asyncHttpClientUtil.doPost(url, params, new AsyncHttpResponseHandler() {
//            @Override
//            public void onStart() {
//                mSVProgressHUD.showWithStatus("绑定中...", SVProgressHUD.SVProgressHUDMaskType.Black);
//                super.onStart();
//            }
//
//            @Override
//            public void onSuccess(int statusCode, org.apache.http.Header[] headers, byte[] bytes) {
//                try {
//                    String result = new String(bytes, "UTF-8");
////                    Log.e("result",result);
//                    JSONObject jsonObject = JSONObject.parseObject(result);
////                    Log.e("boundEquipment",jsonObject.toString());
//                    String code = jsonObject.getString("code");
//                    String msg = jsonObject.getString("msg");
//                    if(TextUtils.equals("10",code)){
//                        mSVProgressHUD.showSuccessWithStatus(msg + "\n请重新登录", SVProgressHUD.SVProgressHUDMaskType.GradientCancel);
//                        if(loginType == 1){//手机登录
//                            pLogin(1);
//                        } else {
//                            wLogin(3,unionid,nickname,imgUrl);
//                        }
//                    } else if(TextUtils.equals("2",code)){
//                        mSVProgressHUD.showInfoWithStatus(msg, SVProgressHUD.SVProgressHUDMaskType.GradientCancel);
//                    } else {
//                        mSVProgressHUD.showErrorWithStatus(msg, SVProgressHUD.SVProgressHUDMaskType.GradientCancel);
//                    }
//                } catch (Exception e) {
//                    mSVProgressHUD.showErrorWithStatus("数据错误", SVProgressHUD.SVProgressHUDMaskType.GradientCancel);
//                    e.printStackTrace();
//                }
//            }
//
//            @Override
//            public void onFailure(int i, org.apache.http.Header[] headers, byte[] bytes, Throwable throwable) {
//                try {
//                    if(throwable.toString().contains("ConnectTimeoutException")){
//                        mSVProgressHUD.showInfoWithStatus("请求超时", SVProgressHUD.SVProgressHUDMaskType.GradientCancel);
//                    } else {
//                        mSVProgressHUD.showErrorWithStatus("请求错误", SVProgressHUD.SVProgressHUDMaskType.GradientCancel);
//                    }
//                } catch (Exception e){
//                    e.printStackTrace();
//                }
//            }
//
//            @Override
//            public void onFinish() {
//                super.onFinish();
//            }
//        });
//    }

    /**
     * 自定义webClient监听网页加载过程
     */
    public class LoginWebViewClient extends WebViewClient {
        private Context context;

        public LoginWebViewClient(Context con) {
            this.context = con;
        }

        @Override
        public void onLoadResource(WebView view, String url) {
            Log.i("LoginWebView", "onLoadResource");
            super.onLoadResource(view, url);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView webview, String url) {
            Log.i("LoginWebView", "shouldOverrideUrlLoading");
            webview.loadUrl(url);
            return true;
        }

        /**
         * webview开始加载调用此方法
         */
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            Log.i("LoginWebView", "onPageStarted");
            super.onPageStarted(view, url, favicon);
        }

        /**
         * 1-webview 加载完成调用此方法; 2-查找页面中所有的<a>标签，然后动态添加onclick事件;
         * 3-事件中回调本地java的jsInvokeJava方法;
         * 注意：webtest别名和上面contentWebView.addJavascriptInterface(this,
         * "webtest")别名要一致;
         */
        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            addData();
        }

        //页面加载出错
        @Override
        public void onReceivedError(WebView view, int errorCode,String description, String failingUrl) {
            view.loadUrl(Constants.ERROR_HTML5);
//            if(errorCode == -2){
//                mSVProgressHUD.showInfoWithStatus("网络不可用", SVProgressHUD.SVProgressHUDMaskType.GradientCancel);
//            } else if(errorCode == -8){
//                mSVProgressHUD.showInfoWithStatus("请求超时", SVProgressHUD.SVProgressHUDMaskType.GradientCancel);
//            }
            super.onReceivedError(view, errorCode, description, failingUrl);
        }
    }

    private void addData() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String imgs[] = {
                        "http://es3.laizhuan.com/resource/app/img/1.jpg",
                        "http://es3.laizhuan.com/resource/app/img/2.jpg",
                        "http://es3.laizhuan.com/resource/app/img/3.jpg",
                        "http://es3.laizhuan.com/resource/app/img/4.jpg",
                        "http://es3.laizhuan.com/resource/app/img/5.jpg",
                        "http://es3.laizhuan.com/resource/app/img/6.jpg",
                        "http://es3.laizhuan.com/resource/app/img/7.jpg",
                        "http://es3.laizhuan.com/resource/app/img/8.jpg",
                        "http://es3.laizhuan.com/resource/app/img/9.jpg",
                        "http://es3.laizhuan.com/resource/app/img/10.jpg",
                        "http://es3.laizhuan.com/resource/app/img/11.jpg",
                        "http://es3.laizhuan.com/resource/app/img/12.jpg",
                        "http://es3.laizhuan.com/resource/app/img/13.jpg",
                        "http://es3.laizhuan.com/resource/app/img/14.jpg",
                        "http://es3.laizhuan.com/resource/app/img/15.jpg",
                        "http://es3.laizhuan.com/resource/app/img/16.jpg",
                        "http://es3.laizhuan.com/resource/app/img/17.jpg",
                        "http://es3.laizhuan.com/resource/app/img/18.jpg",
                        "http://es3.laizhuan.com/resource/app/img/19.jpg",
                        "http://es3.laizhuan.com/resource/app/img/20.png",
                        "http://es3.laizhuan.com/resource/app/img/21.jpg",
                        "http://es3.laizhuan.com/resource/app/img/22.jpg",
                        "http://es3.laizhuan.com/resource/app/img/23.jpg",
                        "http://es3.laizhuan.com/resource/app/img/24.jpg",
                        "http://es3.laizhuan.com/resource/app/img/25.jpg"
                };
                String names[] = {
                        "莎莎","春夏秋冬","最毒妇人心","凉薄少年心","奥特曼",
                        "乡村爱情","左右","努力奋斗","IF YOU","华",
                        "那一抹","轻轻潺潺","大云朵","希特勒","五平方",
                        "Mr.Xue","君君","福星","莎莎","-",
                        "向毛主席","孟苗苗","VAE","小星星","OICQ"
                };
                String taskNames[] = {
                        "完成了壹佰金融获得奖励","完成了乐金所获得奖励","完成了平安好房获得奖励","完成了视吧获得奖励","完成了来疯直播获得奖励","完成了优化大师获得奖励",
                        "完成了钱站获得奖励","完成了Feel获得奖励","完成了荷包获得奖励","完成了易车获得奖励","完成了360影视大全获得奖励","完成了腾讯视频获得奖励",
                        "完成了咕咚获得奖励","完成了万普游戏中心获得奖励","完成了草根投资获得奖励","完成了陌陌获得奖励","完成了大众点评获得奖励",
                        "完成了随手记获得奖励","完成了玩锁屏获得奖励","完成了开心消消乐获得奖励","完成了免费电子书获得奖励","完成了甜橙获得奖励","完成了360手机助手获得奖励",
                        "完成了优优市场获得奖励","完成了玩锁屏获得奖励","完成了MM商场获得奖励","完成了借贷宝获得奖励","完成了唔哩获得奖励",
                        "完成了游谱旅行获得奖励","完成了花椒直播获得奖励","完成了360卫士获得奖励","完成了天天快报获得奖励","完成了新浪新闻获得奖励"
                };
                final JSONArray jsonArray = new JSONArray();
                for(int i = 0;i < imgs.length;i++){
                    Map<String,Object> map = new HashMap<String, Object>();
                    map.put("head_img",imgs[i]);
                    map.put("name",names[i]);
                    map.put("desc",taskNames[i]);
                    map.put("time","1分钟前");
                    jsonArray.add(map);
                }
                webView.post(new Runnable() {
                    @Override
                    public void run() {
                        webView.loadUrl("javascript:setData(" + jsonArray.toString() + ");");
                    }
                });
            }
        });
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        MainApplication.exit();
    }

//    public class LoginWebChromeClient extends WebChromeClient {
//        private Context context;
//
//        public LoginWebChromeClient(Context context) {
//            this.context = context;
//        }
//
//        @Override
//        public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
//            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
//            result.confirm();
//            return true;
//        }
//
//        @Override
//        public boolean onJsConfirm(WebView view, String url, String message, JsResult result) {
//            return super.onJsConfirm(view, url, message, result);
//        }
//
//        @Override
//        public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, JsPromptResult result) {
//            return super.onJsPrompt(view, url, message, defaultValue, result);
//        }
//    }


//    /**
//     * 清除WebView缓存
//     */
//    public void clearWebViewCache(){
//        //清理Webview缓存数据库
//        try {
//            deleteDatabase("webview.db");
//            deleteDatabase("webviewCache.db");
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        //WebView 缓存文件
//        File appCacheDir = new File(getFilesDir().getAbsolutePath() + Constants.CACHEDIRECTORY_PATH + "webView");
//        File webviewCacheDir = new File(getCacheDir().getAbsolutePath()+"/webviewCache");
//        //删除webview 缓存目录
//        if(webviewCacheDir.exists()){
//            deleteFile(webviewCacheDir);
//        }
//        //删除webview 缓存 缓存目录
//        if(appCacheDir.exists()){
//            deleteFile(appCacheDir);
//        }
//    }
//
//    /**
//     * 递归删除 文件/文件夹
//     *
//     * @param file
//     */
//    public void deleteFile(File file) {
//        if (file.exists()) {
//            if (file.isFile()) {
//                file.delete();
//            } else if (file.isDirectory()) {
//                File files[] = file.listFiles();
//                for (int i = 0; i < files.length; i++) {
//                    deleteFile(files[i]);
//                }
//            }
//            file.delete();
//        } else {
//            Log.e("LoginWebView", "delete file no exists " + file.getAbsolutePath());
//        }
//    }
}
