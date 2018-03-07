package com.mx.hb.moon.wxapi;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.mx.hb.moon.application.MainApplication;
import com.mx.hb.moon.base.Constants;
import com.mx.hb.moon.entity.WXLoginEvent;
import com.mx.hb.moon.loading.SVProgressHUD;
import com.tencent.mm.sdk.modelbase.BaseReq;
import com.tencent.mm.sdk.modelbase.BaseResp;
import com.tencent.mm.sdk.modelmsg.SendAuth;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by Moon on 2016/4/5.
 */
public class WXEntryActivity extends Activity implements IWXAPIEventHandler {
    private static SVProgressHUD mSVProgressHUD;
    Activity activity;
    Context context;
    private IWXAPI iwxapi;
    private String wxAppId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        activity = this;
        MainApplication.addActivity(activity);
        mSVProgressHUD = new SVProgressHUD(context);
//        this.setTranslucentStatus(R.color.black);
        initWeixin();
    }

    /** 初始化微信 */
    private void initWeixin() {
        iwxapi = WXAPIFactory.createWXAPI(this, Constants.WX_APPID, false);
        iwxapi.handleIntent(getIntent(), this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        iwxapi.handleIntent(intent, this);
    }

    /**微信主动请求我们**/
    @Override
    public void onReq(BaseReq baseReq) {

    }

    /***请求微信的相应码**/
    @Override
    public void onResp(BaseResp baseResp) {
        String result = "";
        String code = "";
        switch (baseResp.errCode){
            case BaseResp.ErrCode.ERR_OK:
                result = "成功";
                try {
                    code = ((SendAuth.Resp) baseResp).code; //即为所需的code
                    if(!TextUtils.equals("", code)) {
//                        Intent intent = new Intent(activity,LoginActivity.class);
//                        intent.putExtra("code",code);
//                        startActivity(intent);
                    } else {
                        Log.e("返回码:", "为空");
                    }
                } catch (Exception e){
                    Log.e("微信分享:", "朋友圈或者好友分享成功");
                }
                break;
            case BaseResp.ErrCode.ERR_SENT_FAILED:
                result = "失败";
                break;
            case BaseResp.ErrCode.ERR_AUTH_DENIED:
                result = "拒绝";
                break;
            case BaseResp.ErrCode.ERR_USER_CANCEL:
                result = "取消";
                break;
            default:
                result = "失败";
                break;
        }
        EventBus.getDefault().post(new WXLoginEvent(result,code));
        activity.finish();
    }
}
