package com.mx.hb.moon.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.mx.hb.moon.R;
import com.mx.hb.moon.application.MainApplication;
import com.mx.hb.moon.base.AsyncHttpClientUtil;
import com.mx.hb.moon.base.BaseTools;
import com.mx.hb.moon.base.Constants;
import com.mx.hb.moon.base.NetWorkUtils;
import com.mx.hb.moon.base.PrefShared;
import com.mx.hb.moon.dialog.NormalDialog;
import com.mx.hb.moon.dialog.OnBtnClickL;
import com.mx.hb.moon.entity.AlipayEvent;
import com.mx.hb.moon.loading.SVProgressHUD;
import com.mx.hb.moon.service.UpdataService;
import com.mx.hb.moon.view.CircleImageView;
import com.mx.hb.moon.view.StatusBarUtil;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.greenrobot.eventbus.EventBus;

import java.util.HashMap;

/**
 * Created by Moon on 2016/4/21.
 */
public class MessageActivity extends BaseActivity implements View.OnClickListener {
    String objectId = "";
    private static SVProgressHUD mSVProgressHUD;
    private CircleImageView messageIcon;
    //自定义的弹出框类
    private PhonePopupWindow menuWindow;

    private LinearLayout onBack;
    private LinearLayout btnShare;
    private RelativeLayout message2, cashDeposit,friendDeposit,taskDeposit;
    private TextView uuId,userName, updateText;
    public static RelativeLayout updateApp;

    private String WXUID = "";

    protected ImageLoader mImageLoader = ImageLoader.getInstance();
    private boolean processFlag = true; //默认可以点击
    int versionCode = 1;
    String versionName = "1.0";
    String versionN = "";
    public static String apkUrl = "";
    String updateMsg = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        objectId = PrefShared.getString(context, "objectId");
        setContentView(R.layout.activity_message);
        mSVProgressHUD = new SVProgressHUD(context);
        initView();
        findByMessage();
        findByUpdateMsg();
    }

    @Override
    protected void setStatusBar() {
        StatusBarUtil.setColor(this, getResources().getColor(R.color.gules),0);
    }

    private void initView() {
        uuId = (TextView) findViewById(R.id.uuId);
        userName = (TextView) findViewById(R.id.userName);
        onBack = (LinearLayout) findViewById(R.id.onBack);
        messageIcon = (CircleImageView) findViewById(R.id.messageIcon);
        btnShare = (LinearLayout) findViewById(R.id.btnShare);
        message2 = (RelativeLayout) findViewById(R.id.message2);
        updateText = (TextView) findViewById(R.id.updateText);
        updateApp = (RelativeLayout) findViewById(R.id.updateApp);
        taskDeposit = (RelativeLayout) findViewById(R.id.taskDeposit);
        friendDeposit = (RelativeLayout) findViewById(R.id.friendDeposit);
        cashDeposit = (RelativeLayout) findViewById(R.id.cashDeposit);
        onBack.setOnClickListener(this);
        messageIcon.setOnClickListener(this);
        btnShare.setOnClickListener(this);
        message2.setOnClickListener(this);
        taskDeposit.setOnClickListener(this);
        friendDeposit.setOnClickListener(this);
        cashDeposit.setOnClickListener(this);
        updateApp.setOnClickListener(this);
        setUserIcon();
    }

    /** 设置用户头像 */
    private void setUserIcon() {
        uuId.setText(objectId);
        updateApp.setEnabled(false);
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
                    String iconUrl = infoObject.getString("icon");
                    String wxId = infoObject.getString("wxid");
                    WXUID = wxId;
                    if(TextUtils.equals("10",code)){
                        if(!TextUtils.equals("",nickname)){
                            userName.setText(nickname);
                        } else {
                            userName.setText("");
                        }
                        if(iconUrl.contains("http://")){
                            ImageLoader.getInstance().displayImage(iconUrl, messageIcon);
                        } else {
                            ImageLoader.getInstance().displayImage(Constants.REQUEST + iconUrl, messageIcon);
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
            }

            @Override
            public void onFinish() {
                super.onFinish();
            }
        });
    }

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
                            updateText.setText("");
                            apkUrl = jsonObject.getString("apk");
                            apkUrl = Constants.REQUEST + apkUrl;
                            updateMsg = jsonObject.getString("changelogs");
                            Drawable drawableNew = getResources().getDrawable(R.mipmap.update_software);
                            drawableNew.setBounds(0, 0, drawableNew.getMinimumWidth(), drawableNew.getMinimumHeight());
                            updateText.setCompoundDrawables(drawableNew, null, null, null);
                            updateApp.setEnabled(true);
                        } else {
                            updateText.setCompoundDrawables(null,null,null,null);
                            updateText.setText("已是最新版本V"+versionName);
                            updateApp.setEnabled(false);
                        }
                    } else {
                        Log.e("查找用户","返回错误数据");
                    }
                } catch (Exception e) {
//                    Log.e("findByUpdateMsg",Constants.ANALYTICAL_ERROR);
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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.onBack:
                if (processFlag) {
                    setProcessFlag();//
                    this.finish();
                    new TimeThread().start();
                }
                break;
            case R.id.messageIcon:
                if (processFlag) {
                    setProcessFlag();//
                    showMessagePopupWindow();
                    new TimeThread().start();
                }
                break;
            case R.id.btnShare:
                if (processFlag) {
                    setProcessFlag();//
                    if(!TextUtils.equals("",WXUID)){
                        startActivity(new Intent(this, ShareActivity.class));
                    } else {
                        Toast.makeText(context,"请先绑定微信账号",Toast.LENGTH_SHORT).show();
                    }
                    new TimeThread().start();
                }
                break;
            case R.id.message2:
                if (processFlag) {
                    setProcessFlag();//
                    startActivity(new Intent(this,BindingNum.class));
                    new TimeThread().start();
                }
                break;
            case R.id.taskDeposit:
                if (processFlag) {
                    setProcessFlag();//
                    startActivity(new Intent(this, TaskDepositActivity.class));
                    new TimeThread().start();
                }
                break;
            case R.id.friendDeposit:
                if (processFlag) {
                    setProcessFlag();//
                    startActivity(new Intent(this,FriendsActivity.class));
                    new TimeThread().start();
                }
                break;
            case R.id.cashDeposit:
                if (processFlag) {
                    setProcessFlag();//
                    startActivity(new Intent(this,CashActivity.class));
                    new TimeThread().start();
                }
                break;
            case R.id.updateApp:
                if (processFlag) {
                    setProcessFlag();
                    messageDialogTwo("更新","新版本V"+versionN,Color.parseColor("#0876FE"),
                            updateMsg,15,Gravity.LEFT,
                            "稍后再来","马上更新");
                    new TimeThread().start();
                }
                break;
            default:
                break;
        }
    }

    /**
     * 更新App
     */
    private void updateApp() {
        if(NetWorkUtils.isNetworkConnected(this) == false){
            Toast.makeText(context, "网络不可用", Toast.LENGTH_SHORT).show();
            updateApp.setEnabled(true);
        } else {
            Toast.makeText(context,"正在下载...",Toast.LENGTH_LONG).show();
            Intent service = new Intent(this,UpdataService.class);
            startService(service);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
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
        final NormalDialog dialog = new NormalDialog(MessageActivity.this);
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
            }
        }, new OnBtnClickL() {
            @Override
            public void onBtnClick() {
                dialog.dismiss();
                if(TextUtils.equals("提现",type)){
//                    showZhifubaoPopupWindow();
                } else if(TextUtils.equals("更新",type)) {
                    updateApp.setEnabled(false);
                    updateApp();
                } else if(TextUtils.equals("退出",type)) {
                    PrefShared.removeData(context, "userIcon");
                    PrefShared.removeData(context, "userId");
                    PrefShared.removeData(context, "UID");
                    PrefShared.removeData(context, "qrcode");
                    PrefShared.removeData(context,"timestamp");
                    PrefShared.removeData(context,"objectId");
                    PrefShared.removeData(context,"haveCash");
                    PrefShared.removeData(context,"usedCash");
                    PrefShared.removeData(context,"walletCash");
                    PrefShared.removeData(context,"tasksJson");
                    MainApplication.exit();
                }
            }
        });
    }

    /**
     * 显示底部弹框
     */
    private void showMessagePopupWindow() {
        menuWindow = new PhonePopupWindow(this, new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        //显示窗口
        menuWindow.showAtLocation(messageIcon, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0); //设置layout在PopupWindow中显示的位置
    }

    public class PhonePopupWindow extends PopupWindow {
        private View viewmenu;
        private TextView empty,uLogin,cancel;

        public PhonePopupWindow(final Activity context, View.OnClickListener onClickListener) {
            super(context);
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            viewmenu = inflater.inflate(R.layout.window_message, null);
            empty = (TextView) viewmenu.findViewById(R.id.empty);
            uLogin = (TextView) viewmenu.findViewById(R.id.uLogin);
            cancel = (TextView) viewmenu.findViewById(R.id.cancel);
            this.setContentView(viewmenu);
            this.setWidth(BaseTools.getWindowsWidth(context));////设置窗体的高
            this.setHeight(LinearLayout.LayoutParams.WRAP_CONTENT);//设置窗体的高
            // 设置窗体为透明效果
            ColorDrawable cd = new ColorDrawable(0x000000);
            this.setBackgroundDrawable(cd);
            this.setFocusable(true);//设置窗体可点击
            this.setAnimationStyle(R.style.AnimBottom);//设置窗体从底部进入的动画效果
            this.setOutsideTouchable(true);// 点击外部可关闭窗口
            this.update();
            //关闭窗体时
            this.setOnDismissListener(new OnDismissListener() {
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

            empty.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dismiss();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mImageLoader.clearMemoryCache();
                            mImageLoader.clearDiscCache();
                            mSVProgressHUD.showSuccessWithStatus("清理成功", SVProgressHUD.SVProgressHUDMaskType.GradientCancel);
                        }
                    });
                }
            });

            uLogin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismiss();
                    messageDialogTwo("退出","提示",Color.parseColor("#0876FE"),
                            "确定退出当前账号？",16,Gravity.CENTER,
                            "取消","确认");
                }
            });

            cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dismiss();
                }
            });
            viewmenu.setOnTouchListener(new View.OnTouchListener() {
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        dismiss();
                    }
                    return true;
                }
            });
        }
    }

}
