package com.mx.hb.moon.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.mx.hb.moon.R;
import com.mx.hb.moon.base.AsyncHttpClientUtil;
import com.mx.hb.moon.base.Constants;
import com.mx.hb.moon.base.PrefShared;
import com.mx.hb.moon.dialog.T;
import com.mx.hb.moon.view.StatusBarUtil;

import java.util.HashMap;

/**
 * Created by Moon on 2016/4/22.
 */
public class DepositActivity extends BaseActivity implements View.OnClickListener{
    private RelativeLayout cashDeposit,taskDeposit,friendDeposit;
    private LinearLayout onBack;
    private Button dipperBtn;
    private TextView depositText1,depositText2,depositText3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deposit);
        initView();
    }

    @Override
    protected void setStatusBar() {
        StatusBarUtil.setColor(this, getResources().getColor(R.color.deposit),0);
    }

    private void initView() {
        depositText1 = (TextView) findViewById(R.id.depositText1);
        depositText2 = (TextView) findViewById(R.id.depositText2);
        depositText3 = (TextView) findViewById(R.id.depositText3);
        onBack = (LinearLayout) findViewById(R.id.onBack);
        onBack.setOnClickListener(this);
        cashDeposit = (RelativeLayout) findViewById(R.id.cashDeposit);
        cashDeposit.setOnClickListener(this);
        taskDeposit = (RelativeLayout) findViewById(R.id.taskDeposit);
        taskDeposit.setOnClickListener(this);
        friendDeposit = (RelativeLayout) findViewById(R.id.friendsDeposit);
        friendDeposit.setOnClickListener(this);
        dipperBtn = (Button) findViewById(R.id.dipperBtn);
        dipperBtn.setOnClickListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        depositText1.setText(PrefShared.getString(context,"haveCash") + "元");
        depositText2.setText(PrefShared.getString(context,"usedCash") + "元");
        depositText3.setText(PrefShared.getString(context,"walletCash") + "元");
    }

    /**
     * 查找金额信息
     */
//    private void findByMsg() {
//        AsyncHttpClientUtil asyncHttpClientUtil = new AsyncHttpClientUtil(0,new HashMap<String,String>());
//        RequestParams params = new RequestParams();
//        JSONObject jsonObject = new JSONObject();
//        jsonObject.put("token", Constants.TOKENT);
//        jsonObject.put("id", PrefShared.getString(context,"userId"));
//        params.put(Constants.REQUEST_MSG,jsonObject.toString());
//        asyncHttpClientUtil.doPost(Constants.FIND_USER_MSG, params, new AsyncHttpResponseHandler() {
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
////                    Log.e("findByMsg",jsonObject.toString());
//                    String code = jsonObject.getString("code");
//                    jsonObject = JSONObject.parseObject(jsonObject.getString("data"));
//                    JSONObject haveObject = jsonObject.getJSONObject("have");
//                    String haveCash = haveObject.getString("cash");
//                    JSONObject usedObject = jsonObject.getJSONObject("used");
//                    String usedCash = usedObject.getString("cash");
//                    JSONObject walletObject = jsonObject.getJSONObject("wallet");
//                    String walletCash = walletObject.getString("cash");
//                    if(TextUtils.equals("10",code)){
//                        if(Float.parseFloat(haveCash) > 0){
//                            depositText1.setText(haveCash + "");
//                        } else {
//                            depositText1.setText("0.00");
//                        }
//                        if(Float.parseFloat(usedCash) > 0){
//                            depositText2.setText(usedCash + "");
//                        } else {
//                            depositText2.setText("0.00");
//                        }
//                        if(Float.parseFloat(walletCash) > 0){
//                            depositText3.setText(walletCash + "");
//                        } else {
//                            depositText3.setText("0.00");
//                        }
//                    } else {
//                        Log.e("查找用户","返回错误数据");
//                    }
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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.onBack:
                finish();
                break;
            case R.id.cashDeposit:
                startActivity(new Intent(DepositActivity.this,CashActivity.class));
                break;
            case R.id.taskDeposit:
                startActivity(new Intent(DepositActivity.this,TaskDepositActivity.class));
                break;
            case R.id.friendsDeposit:
                startActivity(new Intent(DepositActivity.this,FriendsActivity.class));
                break;
            case R.id.dipperBtn:
                startActivity(new Intent(DepositActivity.this,DipperActivity1.class));
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
