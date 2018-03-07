package com.mx.hb.moon.activity;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.mx.hb.moon.R;
import com.mx.hb.moon.base.AES;
import com.mx.hb.moon.base.AsyncHttpClientUtil;
import com.mx.hb.moon.base.AsyncUtil;
import com.mx.hb.moon.base.BaseTools;
import com.mx.hb.moon.base.Constants;
import com.mx.hb.moon.base.CountDownTimerUtils;
import com.mx.hb.moon.base.PrefShared;
import com.mx.hb.moon.dialog.NormalDialog;
import com.mx.hb.moon.dialog.OnBtnClickL;
import com.mx.hb.moon.entity.WXLoginEvent;
import com.mx.hb.moon.loading.SVProgressHUD;
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
 * Created by Kiven on 16/9/18.
 */
public class BindingNum extends BaseActivity implements View.OnClickListener {
    private IWXAPI iwxapi;
    private static SVProgressHUD mSVProgressHUD;
    private TextView bindingAlipayTitle,bindingPhoneTitle,remake,bindingMsg,textPhone,weChatText,alipayText;
    private String /*telphoneNum,codeNum,*/weChatId,alipayNum,alipayName;
    private LinearLayout onBack;
    private RelativeLayout rltPhone,rltWeChat,rltAlipay;

    private View viewmenu;
    //自定义支付宝弹出框类
    public ZhifubaoPopupWindow zhifubaoWindow;
    private EditTextWithClear account, name;
    private Button bindingBtn;
    //自定义手机号码弹出框类
    private pLoginPopupWindow popupWindow;
    private EditTextWithClear telphone, code;
    private Button queryCode,loginBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_binding);
        mSVProgressHUD = new SVProgressHUD(context);
//        EventBus.getDefault().register(this);//注册EventBus
        initView();
        initWX();
        findByMessage();
    }

    @Override
    protected void setStatusBar() {
        StatusBarUtil.setColor(this, getResources().getColor(R.color.gules),0);
    }

    private void initView() {
        onBack = (LinearLayout) findViewById(R.id.onBack);
        rltPhone = (RelativeLayout) findViewById(R.id.rltPhone);
        rltWeChat = (RelativeLayout) findViewById(R.id.rltWeChat);
        rltAlipay = (RelativeLayout) findViewById(R.id.rltAlipay);
        bindingMsg = (TextView) findViewById(R.id.bindingMsg);
        bindingMsg.setText("绑定后，您可以获取更多的奖励与更好的体验。为了您的账户安全，在未绑定手机且绑定唯一第三方账号的情况下，不允许解绑。");
        textPhone = (TextView) findViewById(R.id.textPhone);
        weChatText = (TextView) findViewById(R.id.weChatText);
        alipayText = (TextView) findViewById(R.id.alipayText);
        rltPhone.setOnClickListener(this);
        rltWeChat.setOnClickListener(this);
        rltAlipay.setOnClickListener(this);
        onBack.setOnClickListener(this);
    }

    private void initWX() {
        iwxapi = WXAPIFactory.createWXAPI(context, Constants.WX_APPID, false);
        iwxapi.registerApp(Constants.WX_APPID);
    }

    /**
     * 查找个人信息
     */
    private void findByMessage() {
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
//                    Log.e("findByMessage",jsonObject.toString());
                    String code = jsonObject.getString("code");
                    jsonObject = JSONObject.parseObject(jsonObject.getString("data"));
                    JSONObject javaObject = jsonObject.getJSONObject("have");
                    float cash = javaObject.getFloat("cash");
                    JSONObject infoObject = jsonObject.getJSONObject("info");
                    String nickname = infoObject.getString("nickname");
                    String zfbName = infoObject.getString("name");
                    String zhifubao = infoObject.getString("zhifubao");
                    String wxId = infoObject.getString("wxid");
                    String phone = infoObject.getString("phone");
                    String iconUrl = infoObject.getString("icon");
                    if(TextUtils.equals("10",code)){
                        if(!TextUtils.equals("",zfbName) && !TextUtils.equals("",zhifubao)){
                            alipayText.setText(zfbName);
                            alipayText.setTextColor(getResources().getColor(R.color.textColorSecondary));
                            alipayName = zfbName;
                            alipayNum = zhifubao;
                        } else {
                            alipayText.setText("未绑定");
                            alipayText.setTextColor(getResources().getColor(R.color.dialog_blue));
                            alipayName = "";
                            alipayNum = "";
                        }
                        if(!TextUtils.equals("",wxId)){
                            weChatText.setText("已绑定");
                            weChatText.setTextColor(getResources().getColor(R.color.textColorSecondary));
                            weChatId = wxId;
                        } else {
                            weChatText.setText("未绑定");
                            weChatText.setTextColor(getResources().getColor(R.color.dialog_blue));
                        }
                        if(!TextUtils.equals("",phone)){
                            textPhone.setText(phone);
                            textPhone.setTextColor(getResources().getColor(R.color.textColorSecondary));
                        } else {
                            textPhone.setText("未绑定");
                            textPhone.setTextColor(getResources().getColor(R.color.dialog_blue));
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
//                Log.e("error",throwable.toString());
                if(throwable.toString().contains("ConnectTimeoutException")){
                    mSVProgressHUD.showInfoWithStatus("请求超时", SVProgressHUD.SVProgressHUDMaskType.GradientCancel);
                } else if(throwable.toString().contains("UnknownHostException")) {
                    mSVProgressHUD.showInfoWithStatus("网络不可用", SVProgressHUD.SVProgressHUDMaskType.GradientCancel);
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

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.onBack:
                if (processFlag) {
                    setProcessFlag();
                    this.finish();
                    new TimeThread().start();
                }
                break;
            case R.id.rltPhone:
                if (processFlag) {
                    setProcessFlag();
                    showPLoginPopupWindow();
                    new TimeThread().start();
                }
                break;
            case R.id.queryCode:
                if(processFlag){
                    setProcessFlag();
                    String telphoneNum = telphone.getText().toString().replaceAll(" ","");
                    if(BaseTools.isNumeric(telphoneNum) && telphoneNum.length() == 11) {
                        if(textPhone.getText().toString().length() == 11 && !TextUtils.equals(textPhone.getText().toString(),"未绑定")){
                            getPhoneCode(telphoneNum,3);
                        } else {
                            getPhoneCode(telphoneNum,2);
                        }
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
                            if(textPhone.getText().toString().length() == 11 && !TextUtils.equals(textPhone.getText().toString(),"未绑定")){//如果有显示11手机号码就是解绑操作，否则绑定操作
                                boundEquipment(11,0);
                            } else {
                                boundEquipment(11,1);
                            }
                        }
                    } else{
                        dialog("手机号码",(float) 0.70,false,"", Color.parseColor("#0876FE"),1,"请输入正确的手机号码",16, Gravity.CENTER,"OK","OK",15f,15f,getResources().getColor(R.color.dialog_blue),getResources().getColor(R.color.dialog_blue));
                    }
                    new TimeThread().start();
                }
                break;
            case R.id.rltWeChat:
                if (processFlag) {
                    setProcessFlag();
//                    weChatLogin();
                    new TimeThread().start();
                }
                break;
            case R.id.rltAlipay:
                if (processFlag) {
                    setProcessFlag();
                    if(!TextUtils.equals("",alipayName) && !TextUtils.equals(alipayNum,"")){
                        dialog("绑定支付宝",(float) 0.75,true,"您已成功绑定支付宝", Color.parseColor("#000000"),2,"姓名：" + alipayName + "\n账号：" + alipayNum,16, Gravity.CENTER,"取消","修改",15f,15f,getResources().getColor(R.color.dialog_blue),getResources().getColor(R.color.dialog_blue));
                    } else {
                        showZhifubaoPopupWindow();
                    }
                    new TimeThread().start();
                }
                break;
        }
    }

    /**
     * 验证手机验证码
     * @param telphoneNum
     * @param codeType 解绑3 绑定2
     */
    private void getPhoneCode(String telphoneNum, int codeType) {
        AsyncHttpClientUtil asyncHttpClientUtil = new AsyncHttpClientUtil(0,new HashMap<String,String>());

        RequestParams params = new RequestParams();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("token",Constants.TOKENT);//每个接口都需要的token
        jsonObject.put("phone",telphoneNum);//登录类型
        jsonObject.put("codeType",codeType);//设备码

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
                    if(error.contains("ConnectTimeoutException")){
                        dialog("获取验证码",(float) 0.70,false,"", Color.parseColor("#0876FE"),1,"请求超时",16, Gravity.CENTER,"OK","OK",15f,15f,getResources().getColor(R.color.dialog_blue),getResources().getColor(R.color.dialog_blue));
                    } else if(throwable.toString().contains("UnknownHostException")) {
                        dialog("获取验证码",(float) 0.70,false,"", Color.parseColor("#0876FE"),1,"网络不可用",16, Gravity.CENTER,"OK","OK",15f,15f,getResources().getColor(R.color.dialog_blue),getResources().getColor(R.color.dialog_blue));
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

//    private void weChatLogin() {
//        final int[] wxErrorType = {3};
//        AsyncUtil asyncUtil = new AsyncUtil() {
//            @Override
//            public void onPreExecute() {
//                super.onPreExecute();
//                mSVProgressHUD.showWithStatus("授权中...",SVProgressHUD.SVProgressHUDMaskType.Black);
//            }
//
//            @Override
//            public Long doInBackground(String... params) {
//                if(iwxapi.isWXAppInstalled() == false){
//                    wxErrorType[0] = 0;
//                } else {
//                    if(!iwxapi.isWXAppSupportAPI()){
//                        wxErrorType[0] = 1;
//                    } else {
//                        SendAuth.Req req = new SendAuth.Req();
//                        req.scope = Constants.WX_SCOPE;
//                        req.state = Constants.WX_STATE;
//                        iwxapi.sendReq(req);
//                    }
//                }
//                return null;
//            }
//
//            @Override
//            public void onProgressUpdate(Integer... values) {
//                super.onProgressUpdate(values);
//            }
//
//            @Override
//            public void onPostExecute(Long result) {
//                if(wxErrorType[0] == 0){
//                    mSVProgressHUD.showInfoWithStatus("请先安装微信", SVProgressHUD.SVProgressHUDMaskType.GradientCancel);
//                } else if(wxErrorType[0] == 1){
//                    mSVProgressHUD.showInfoWithStatus("微信版本不支持", SVProgressHUD.SVProgressHUDMaskType.GradientCancel);
//                }
//                super.onPostExecute(result);
//            }
//        };
//        asyncUtil.execute();
//    }
//
//    @Subscribe(threadMode = ThreadMode.MAIN)
//    public void onMessage(WXLoginEvent event) {
//        if (mSVProgressHUD.isShowing()) {
//            mSVProgressHUD.dismiss();
//        }
//        if(event.code != null && !TextUtils.equals(event.code,"")){
//            getOpendId(event.code);
//        }
//    }
//
//    /**
//     * 根据code获取access_token与opendId
//     * @param code
//     */
//    public void getOpendId(String code){
//        AsyncHttpClientUtil asyncHttpClientUtil = new AsyncHttpClientUtil(0,new HashMap<String,String>());
//        RequestParams params = new RequestParams();
//        params.put("appid",Constants.WX_APPID);
//        params.put("secret",Constants.WX_SECRET);
//        params.put("code",code);
//        params.put("grant_type","authorization_code");
//        asyncHttpClientUtil.doGet(Constants.WX_ACCESS_TOKEN, params, new AsyncHttpResponseHandler() {
//            @Override
//            public void onStart() {
//                super.onStart();
//            }
//
//            @Override
//            public void onSuccess(int statusCode, org.apache.http.Header[] headers, byte[] bytes) {
//                try {
//                    String result = new String(bytes, "UTF-8");
//                    JSONObject jsonObject = JSONObject.parseObject(result);
//                    String token = jsonObject.getString("access_token");
//                    String opendId = jsonObject.getString("openid");
//                    if (null != token || null != opendId) {
//                        getUMessage(token, opendId);
//                    } else {
//                        Log.e("BindingNum-getOpendId", "token、opendId=null");
//                    }
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//
//            @Override
//            public void onFailure(int i, org.apache.http.Header[] headers, byte[] bytes, Throwable throwable) {
//
//            }
//
//            @Override
//            public void onFinish() {
//                super.onFinish();
//            }
//        });
//    }
//
//    /**
//     * 根据token与openId获取用户信息
//     * @param token
//     * @param opendId
//     */
//    private void getUMessage(String token, String opendId) {
//        Map<String,String> header = new HashMap<String,String>();
//        header.put("","");
//        AsyncHttpClientUtil asyncHttpClientUtil = new AsyncHttpClientUtil(3000,header);
//        RequestParams params = new RequestParams();
//        params.put("access_token", token);
//        params.put("openid", opendId);
//        asyncHttpClientUtil.doGet(Constants.WX_USER_INFO, params, new AsyncHttpResponseHandler() {
//            @Override
//            public void onStart() {
//                super.onStart();
//            }
//
//            @Override
//            public void onSuccess(int statusCode, org.apache.http.Header[] headers, byte[] bytes) {
//                try {
//                    String result = new String(bytes, "UTF-8");
//                    JSONObject jsonObject = JSONObject.parseObject(result);
//                    String nickname = jsonObject.getString("nickname");
//                    String openid = jsonObject.getString("openid");
//                    String imgUrl = jsonObject.getString("headimgurl");
//                    String unionid = jsonObject.getString("unionid");
//                    boundEquipment(3,1,unionid);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//
//            @Override
//            public void onFailure(int i, org.apache.http.Header[] headers, byte[] bytes, Throwable throwable) {
//            }
//
//            @Override
//            public void onFinish() {
//                super.onFinish();
//            }
//        });
//    }

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
        zhifubaoWindow.showAtLocation(rltAlipay, Gravity.CENTER_HORIZONTAL, 0, 0); //设置layout在PopupWindow中显示的位置
    }

    public class ZhifubaoPopupWindow extends PopupWindow {

        public ZhifubaoPopupWindow(final Activity context, View.OnClickListener onClickListener) {
            super(context);
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            viewmenu = inflater.inflate(R.layout.window_binding, null);
            bindingAlipayTitle = (TextView) viewmenu.findViewById(R.id.bindingTitle);
            bindingAlipayTitle.setText(Html.fromHtml("<font color='#0876FE' font-size='16px'>支付宝</font>收款账号"));
            remake = (TextView) viewmenu.findViewById(R.id.remake);
            remake.setText("提示：\n\t\t请务必填写正确有效的支付宝账户和对应的账户名！");
            account = (EditTextWithClear) viewmenu.findViewById(R.id.account);
            account.setHint("支付宝：账号");
            name = (EditTextWithClear) viewmenu.findViewById(R.id.name);
            name.setHint("支付宝：姓名");
            if(!TextUtils.equals("",alipayName) && !TextUtils.equals(alipayNum,"")){
                account.setText(alipayNum);
                name.setText(alipayName);
            }
            bindingBtn = (Button) viewmenu.findViewById(R.id.bindingBtn);
            zhifubaoWindow = this;
            zhifubaoWindow.setContentView(viewmenu);
            zhifubaoWindow.setWidth(BaseTools.getWindowsWidth(context));////设置窗体的宽
            zhifubaoWindow.setHeight(LinearLayout.LayoutParams.WRAP_CONTENT);//设置窗体的高
            // 设置窗体为透明效果
            ColorDrawable cd = new ColorDrawable(0x000000);
            zhifubaoWindow.setOutsideTouchable(false);// 点击外部可关闭窗口
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
            dialog("支付宝",(float) 0.70,false,"", Color.parseColor("#0876FE"),1,"请输入支付宝账号",16, Gravity.CENTER,"OK","OK",15f,15f,getResources().getColor(R.color.dialog_blue),getResources().getColor(R.color.dialog_blue));
        } else if(!BaseTools.checkAlipay(account.getText().toString())){
            dialog("支付宝",(float) 0.70,false,"", Color.parseColor("#0876FE"),1,"请输入正确的支付宝账号",16, Gravity.CENTER,"OK","OK",15f,15f,getResources().getColor(R.color.dialog_blue),getResources().getColor(R.color.dialog_blue));
        } else if(TextUtils.equals("",name.getText().toString())){
            dialog("支付宝",(float) 0.70,false,"", Color.parseColor("#0876FE"),1,"请输入收款人姓名",16, Gravity.CENTER,"OK","OK",15f,15f,getResources().getColor(R.color.dialog_blue),getResources().getColor(R.color.dialog_blue));
        } else if(!BaseTools.isChinaName(name.getText().toString())){
            dialog("支付宝",(float) 0.70,false,"", Color.parseColor("#0876FE"),1,"请输入正确的收款人姓名",16, Gravity.CENTER,"OK","OK",15f,15f,getResources().getColor(R.color.dialog_blue),getResources().getColor(R.color.dialog_blue));
        } else {
            boundEquipment(4,1);
            zhifubaoWindow.dismiss();
        }
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
        popupWindow.showAtLocation(rltPhone, Gravity.CENTER_HORIZONTAL, 0, 0); //设置layout在PopupWindow中显示的位置
    }

    public class pLoginPopupWindow extends PopupWindow {

        public pLoginPopupWindow(final Activity context, View.OnClickListener onClickListener) {
            super(context);
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            viewmenu = inflater.inflate(R.layout.window_login, null);
            bindingPhoneTitle = (TextView) viewmenu.findViewById(R.id.bindingPhoneTitle);
            bindingPhoneTitle.setText("验证手机号码");
            telphone = (EditTextWithClear) viewmenu.findViewById(R.id.telphone);
            String telphoneText = textPhone.getText().toString().replace(" ","");
            if(!TextUtils.equals(telphoneText,"") && telphoneText.length() == 11){
                telphone.setText(telphoneText);
                telphone.setEnabled(false);
            } else {
                telphone.setText("");
                telphone.setEnabled(true);
            }
            BaseTools.phoneNumAddSpace(telphone);
            code = (EditTextWithClear) viewmenu.findViewById(R.id.code);

            queryCode = (Button) viewmenu.findViewById(R.id.queryCode);

            loginBtn = (Button) viewmenu.findViewById(R.id.loginBtn);
            loginBtn.setText("确认");
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

            queryCode.setOnClickListener(BindingNum.this);

            loginBtn.setOnClickListener(BindingNum.this);
        }
    }

    /**
     * 弹窗
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
        final NormalDialog dialog = new NormalDialog(BindingNum.this);
        /*弹窗*/
        dialog.widthScale(dialogWidth);//设整个弹窗的宽度
        dialog.cornerRadius(8);//设弹窗的圆角
        dialog.style(btnNum == 1 ? NormalDialog.STYLE_ONE : NormalDialog.STYLE_TWO);//设为两个按钮
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
                    if(TextUtils.equals(type,"绑定支付宝")){//绑定支付宝
                        showZhifubaoPopupWindow();
                    }
                }
            });
        }
        dialog.show();
    }

    /**
     *绑定支付宝
     * @param type 绑定类型 11手机号，3微信，4支付宝
     * @param testType testType 选择请求的接口，0 需要请求的是验证验证码是否正确的接口 1绑定账号的接口
     */
    private void boundEquipment(int type,final int testType) {
        String url = "";
        AsyncHttpClientUtil asyncHttpClientUtil = new AsyncHttpClientUtil(0,new HashMap<String,String>());
        RequestParams params = new RequestParams();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("token", Constants.TOKENT);
        if(type == 4){//支付宝传支付宝对应的真名、支付宝账号
            jsonObject.put("name",name.getText().toString().trim());
            jsonObject.put("id",account.getText().toString().trim());
        } else if(type == 11){//手机传验证码、手机号码
            jsonObject.put("vcode",code.getText().toString().trim());
            jsonObject.put("id",telphone.getText().toString().replaceAll(" ",""));
        } else {//微信传微信uniID
            jsonObject.put("id","");//unionid
        }
        jsonObject.put("type",type);
        jsonObject.put("only", PrefShared.getString(context,"UUID"));
        jsonObject.put("objectid",PrefShared.getString(context,"objectId"));//objectId
        if(testType == 0){
            url = Constants.TEST_VCODE_SU;
        } else {
            url = Constants.EQUIPMENT_NEW_BIND;
        }
        params.put(Constants.REQUEST_MSG,jsonObject.toString());
        asyncHttpClientUtil.doPost(url, params, new AsyncHttpResponseHandler() {
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
                        findByMessage();
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
                    } else if(throwable.toString().contains("UnknownHostException")) {
                        mSVProgressHUD.showInfoWithStatus("网络不可用", SVProgressHUD.SVProgressHUDMaskType.GradientCancel);
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

//    @Override
//    public void onDestroy() {
//        super.onDestroy();
//        EventBus.getDefault().unregister(this);//注销EventBus
//    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.finish();
    }
}
