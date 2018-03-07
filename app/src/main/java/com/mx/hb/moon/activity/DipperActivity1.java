package com.mx.hb.moon.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
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
import com.mx.hb.moon.base.AES;
import com.mx.hb.moon.base.AsyncHttpClientUtil;
import com.mx.hb.moon.base.BaseTools;
import com.mx.hb.moon.base.Constants;
import com.mx.hb.moon.base.PrefShared;
import com.mx.hb.moon.dialog.NormalDialog;
import com.mx.hb.moon.dialog.OnBtnClickL;
import com.mx.hb.moon.entity.WheelInfo;
import com.mx.hb.moon.listener.OnAddressChangeListener;
import com.mx.hb.moon.loading.SVProgressHUD;
import com.mx.hb.moon.view.ChooseAddressWheel;
import com.mx.hb.moon.view.StatusBarUtil;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Kiven on 16/6/22.
 */
public class DipperActivity1 extends BaseActivity implements View.OnClickListener{
    private static SVProgressHUD mSVProgressHUD;
    private LinearLayout onBack;
    private RelativeLayout w1Btn,w2Btn;
    private TextView wheelMoney,wheelpayType,dipperNum,bindingTitle,remake;
    private TextView dipperMText1,dipperMText2,dipperMText3,dipperMText4,dipperMText5;
    private ChooseAddressWheel moneyWheel = null,payTypeWheel = null;
    private Button btnSubmit;

    private boolean processFlag = true; //默认可以点击

    private boolean aliPay = false;
    public ZhifubaoPopupWindow zhifubaoWindow;
    private View viewmenu;
    private EditText account, name;
    private Button bindingBtn,lookRecord;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        this.setTranslucentStatus(R.color.black);
        setContentView(R.layout.activity_dipper1);
        mSVProgressHUD = new SVProgressHUD(context);
        initView();
        initWheel();
        initData1();
        initData2();
        findByUserMsg();
    }

    @Override
    protected void onStart() {
        super.onStart();
        dipperNum.setText(PrefShared.getString(context,"haveCash") + "元");
    }

    @Override
    protected void setStatusBar() {
        StatusBarUtil.setColor(this, getResources().getColor(R.color.black),0);
    }

    private void initView() {
        onBack = (LinearLayout) findViewById(R.id.onBack);
        w1Btn = (RelativeLayout) findViewById(R.id.w1Btn);
        w2Btn = (RelativeLayout) findViewById(R.id.w2Btn);
        wheelMoney = (TextView) findViewById(R.id.wheelMoney);
        wheelpayType = (TextView) findViewById(R.id.wheelpayType);
        dipperNum = (TextView) findViewById(R.id.dipperNum);
        dipperMText1 = (TextView) findViewById(R.id.dipperMText1);
        dipperMText2 = (TextView) findViewById(R.id.dipperMText2);
        dipperMText3 = (TextView) findViewById(R.id.dipperMText3);
        dipperMText4 = (TextView) findViewById(R.id.dipperMText4);
        dipperMText5 = (TextView) findViewById(R.id.dipperMText5);
        btnSubmit = (Button) findViewById(R.id.btnSubmit);
        lookRecord = (Button) findViewById(R.id.lookRecord);
        dipperMText1.setText(Html.fromHtml("·&nbsp;<font color='#FF5900' font-size='16px'>支付宝</font>&nbsp;每笔提现均会扣取&nbsp;<font color='#FF5900'>1元手续费</font>"));
        dipperMText2.setText(Html.fromHtml("·&nbsp;<font>到账时间最快当天，通常为第二天到账</font>"));
        dipperMText3.setText(Html.fromHtml("·&nbsp;<font color='#FF5900'>周末、法定节假日不处理提现</font>&nbsp;<font>(周5-6顺延周1，周7顺延周2)</font>"));
        dipperMText4.setText(Html.fromHtml("·&nbsp;<font>所有提现均人工审核，审核完成后由系统自动打款</font>"));
        dipperMText5.setText(Html.fromHtml("·&nbsp;<font>如支付宝账号错误，多设备多帐号等作弊行为会导致提现失败</font>"));
        onBack.setOnClickListener(this);
        w1Btn.setOnClickListener(this);
        w2Btn.setOnClickListener(this);
        btnSubmit.setOnClickListener(this);
        lookRecord.setOnClickListener(this);
    }

    private void initWheel() {
        moneyWheel = new ChooseAddressWheel(this);
        moneyWheel.setOnAddressChangeListener(moneyListener);
        payTypeWheel = new ChooseAddressWheel(this);
        payTypeWheel.setOnAddressChangeListener(payTypeListener);
    }

    private void initData1() {
        List<WheelInfo> lists = new ArrayList<>();
        for(int i = 0;i < 5;i++){
            WheelInfo wheelInfo = new WheelInfo();
            if(i == 0){
                wheelInfo.common = "10元";
            } else if(i == 1){
                wheelInfo.common = "30元";
            } else if(i == 2){
                wheelInfo.common = "50元";
            } else if(i == 3){
                wheelInfo.common = "100元";
            } else {
                wheelInfo.common = "200元";
            }
            lists.add(wheelInfo);
        }
        moneyWheel.setProvince(lists);
    }

    private void initData2() {
        List<WheelInfo> lists = new ArrayList<>();
        for(int i = 0;i < 1;i++){
            WheelInfo wheelInfo = new WheelInfo();
            wheelInfo.common = "支付宝";
            lists.add(wheelInfo);
        }
        payTypeWheel.setProvince(lists);
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
                    JSONObject haveObject = jsonObject.getJSONObject("have");
                    if(TextUtils.equals("10",code)){
                        if(!TextUtils.equals("",zfbName) && !TextUtils.equals("",zhifubao)) {
                            aliPay = true;
                        } else {
                            aliPay = false;
                        }
                    } else {
                        Log.e("查找提现记录","返回错误数据");
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
                    setProcessFlag();//
                    this.finish();
                    new TimeThread().start();
                }
                break;
            case R.id.w1Btn:
                if (processFlag) {
                    setProcessFlag();//
                    moneyWheel.show(v);
                    new TimeThread().start();
                }
                break;
            case R.id.w2Btn:
                if (processFlag) {
                    setProcessFlag();//
                    payTypeWheel.show(v);
                    new TimeThread().start();
                }
                break;
            case R.id.btnSubmit:
                if (processFlag) {
                    setProcessFlag();//
                    if(aliPay == true){
                        String timestamp = PrefShared.getString(context,"timestamp");
                        if(Double.valueOf(dipperNum.getText().toString().replace("元","")) > Integer.parseInt(wheelMoney.getText().toString().replace("元",""))){
                            if(null == timestamp || TextUtils.equals("null",timestamp) || TextUtils.equals("",timestamp)){
                                submitData();
                            } else {
                                String start = BaseTools.changeDate(1,timestamp);
                                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                String last = simpleDateFormat.format(new Date());
                                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                try {
                                    Date startDate = dateFormat.parse(start);
                                    Date lastDate = dateFormat.parse(last);
                                    if(BaseTools.isSameData(startDate,lastDate)){
                                        dialog("提示",(float) 0.70,false,"", Color.parseColor("#0876FE"),1,"您今天已申请过提现",16, Gravity.CENTER,"OK","OK",15f,15f,getResources().getColor(R.color.dialog_blue),getResources().getColor(R.color.dialog_blue));
                                    } else {
                                        submitData();
                                    }
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                            }
                        } else {
                            dialog("提示",(float) 0.70,false,"", Color.parseColor("#0876FE"),1,"可提现金额不足",16, Gravity.CENTER,"OK","OK",15f,15f,getResources().getColor(R.color.dialog_blue),getResources().getColor(R.color.dialog_blue));
                        }
                    } else {
                        showZhifubaoPopupWindow();
                    }
                    new TimeThread().start();
                }
                break;
            case R.id.lookRecord:
                if (processFlag) {
                    setProcessFlag();//
                    startActivity(new Intent(DipperActivity1.this,CashActivity.class));
                    new TimeThread().start();
                }
                break;
            default:
                break;
        }
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
        zhifubaoWindow.showAtLocation(btnSubmit, Gravity.CENTER_HORIZONTAL, 0, 0); //设置layout在PopupWindow中显示的位置
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
            dialog("支付宝",(float) 0.70,false,"", Color.parseColor("#0876FE"),1,"请输入支付宝账号",16, Gravity.CENTER,"OK","OK",15f,15f,getResources().getColor(R.color.dialog_blue),getResources().getColor(R.color.dialog_blue));
        } else if(!BaseTools.checkAlipay(account.getText().toString())){
            dialog("支付宝",(float) 0.70,false,"", Color.parseColor("#0876FE"),1,"请输入正确的支付宝账号",16, Gravity.CENTER,"OK","OK",15f,15f,getResources().getColor(R.color.dialog_blue),getResources().getColor(R.color.dialog_blue));
        } else if(TextUtils.equals("",name.getText().toString())){
            dialog("支付宝",(float) 0.70,false,"", Color.parseColor("#0876FE"),1,"请输入收款人姓名",16, Gravity.CENTER,"OK","OK",15f,15f,getResources().getColor(R.color.dialog_blue),getResources().getColor(R.color.dialog_blue));
        } else if(!BaseTools.isChinaName(name.getText().toString())){
            dialog("支付宝",(float) 0.70,false,"", Color.parseColor("#0876FE"),1,"请输入正确的收款人姓名",16, Gravity.CENTER,"OK","OK",15f,15f,getResources().getColor(R.color.dialog_blue),getResources().getColor(R.color.dialog_blue));
        } else {
            boundEquipment();
            zhifubaoWindow.dismiss();
        }
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
        jsonObject.put("objectid",PrefShared.getString(context,"objectId"));//objectId
        params.put(Constants.REQUEST_MSG,jsonObject.toString());
        asyncHttpClientUtil.doPost(Constants.EQUIPMENT_NEW_BIND, params, new AsyncHttpResponseHandler() {
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
                        findByUserMsg();
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
//                    Log.e("error",throwable.toString());
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

    /**
     * 提交提现申请
     */
    private void submitData() {
        AsyncHttpClientUtil asyncHttpClientUtil = new AsyncHttpClientUtil(0,new HashMap<String,String>());
        RequestParams params = new RequestParams();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("money", wheelMoney.getText().toString().replace("元",""));
        jsonObject.put("id", PrefShared.getString(context,"userId"));
        jsonObject.put("DBId",Constants.DB_KEY);
        jsonObject.put("timestamp",PrefShared.getString(context,"timestamp"));
        long num = AES.get10Random();
        String content = AES.encrypt(jsonObject.toString(),AES.md5(AES.longMinusNum(num+"")));
        JSONObject json = new JSONObject();
        json.put("content",content);
        json.put("secret",num+"");
        json.put("hehe",1);

        params.put(Constants.REQUEST_MSG,json.toString());
        asyncHttpClientUtil.doPost(Constants.APPLY_DIPPER, params, new AsyncHttpResponseHandler() {
            @Override
            public void onStart() {
                super.onStart();
                mSVProgressHUD.showWithStatus("提现中...", SVProgressHUD.SVProgressHUDMaskType.Black);
            }

            @Override
            public void onSuccess(int statusCode, org.apache.http.Header[] headers, byte[] bytes) {
                try {
//                    for(int i = 0;i < headers.length;i++){
//                        Log.e("请求头", String.valueOf(headers[i]));
//                    }
                    String result = new String(bytes, "UTF-8");
                    JSONObject json = JSONObject.parseObject(result);
                    String secret = json.getString("secret");
                    String content = json.getString("content");
                    result = AES.decrypt(content,AES.md5(AES.longMinusNum(secret)));
                    JSONObject jsonObject = JSONObject.parseObject(result);
                    int code = jsonObject.getInteger("code");
                    String msg = jsonObject.getString("msg");
                    if(10 == code){
                        int timestamp = jsonObject.getInteger("timestamp");
                        PrefShared.saveString(context,"timestamp",timestamp+"");
                        dialog("提现",(float) 0.70,false,"", Color.parseColor("#0876FE"),1,msg,16, Gravity.CENTER,"OK","OK",15f,15f,getResources().getColor(R.color.dialog_blue),getResources().getColor(R.color.dialog_blue));
                        Intent intent = new Intent(Constants.SENDMSG_REFRESH);
                        context.sendBroadcast(intent);

                        double money = (Double.valueOf(PrefShared.getString(context,"haveCash")) - Integer.parseInt(wheelMoney.getText().toString().replace("元","")));
                        BigDecimal bigDecimal = new BigDecimal(money);
                        bigDecimal = bigDecimal.setScale(2, BigDecimal.ROUND_HALF_UP);
                        money = bigDecimal.doubleValue();
                        dipperNum.setText(money + "元");
                    } else {
                        dialog("提现",(float) 0.70,false,"", Color.parseColor("#0876FE"),1,msg,16, Gravity.CENTER,"OK","OK",15f,15f,getResources().getColor(R.color.dialog_blue),getResources().getColor(R.color.dialog_blue));
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
                if (mSVProgressHUD.isShowing()) {
                    mSVProgressHUD.dismiss();
                }
            }
        });
    }

    OnAddressChangeListener moneyListener = new OnAddressChangeListener() {
        @Override
        public void onAddressChange(String money) {
            wheelMoney.setText(money);
        }
    };

    OnAddressChangeListener payTypeListener = new OnAddressChangeListener() {
        @Override
        public void onAddressChange(String payType) {
            wheelpayType.setText(payType);
        }
    };

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
        final NormalDialog dialog = new NormalDialog(DipperActivity1.this);
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.finish();
    }
}
