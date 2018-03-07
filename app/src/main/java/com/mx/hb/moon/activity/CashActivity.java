package com.mx.hb.moon.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.mx.hb.moon.R;
import com.mx.hb.moon.adapter.CashDAdapter;
import com.mx.hb.moon.adapter.TaskDAdapter;
import com.mx.hb.moon.base.AsyncHttpClientUtil;
import com.mx.hb.moon.base.BaseTools;
import com.mx.hb.moon.base.Constants;
import com.mx.hb.moon.base.PrefShared;
import com.mx.hb.moon.dialog.T;
import com.mx.hb.moon.loading.SVProgressHUD;
import com.mx.hb.moon.swipeRefresh.SwipeRefreshLayout;
import com.mx.hb.moon.view.StatusBarUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Kiven on 16/6/29.
 */
public class CashActivity extends BaseActivity implements View.OnClickListener,SwipeRefreshLayout.OnRefreshListener, SwipeRefreshLayout.OnLoadListener {
    private static SVProgressHUD mSVProgressHUD;
    private TextView titleText;
    private ExpandableListView cashListView;
    private CashDAdapter adapter;
    private LinearLayout onBack;
    private SwipeRefreshLayout swipeLayout;
    private int mPageIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_deposit);
        mSVProgressHUD = new SVProgressHUD(context);
        initView();
        refreshRequestList();
    }

    @Override
    protected void setStatusBar() {
        StatusBarUtil.setColor(this, getResources().getColor(R.color.black), 0);
    }

    private void initView() {
        titleText = (TextView) findViewById(R.id.titleText);
        titleText.setText("提现记录");
        cashListView = (ExpandableListView) findViewById(R.id.taskEListView);
        swipeLayout = (SwipeRefreshLayout) findViewById(R.id.taskdeposit_swipe);
        swipeLayout.setColor(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
        swipeLayout.setMode(SwipeRefreshLayout.Mode.BOTH);
        swipeLayout.setOnRefreshListener(this);
        swipeLayout.setOnLoadListener(this);

        onBack = (LinearLayout) findViewById(R.id.onBack);
        onBack.setOnClickListener(this);

        setClickListener();
        adapter = new CashDAdapter(context);
        cashListView.setAdapter(adapter);
    }

    /**
     * 下拉更新广告列表
     */
    private void refreshRequestList() {
        mPageIndex = 1;
        swipeLayout.setRefreshing(true);
        requestList();
    }

    /**
     * 上拉更新广告列表
     */
    private void loadRequestList() {
        ++mPageIndex;
        swipeLayout.setLoading(true);
        requestList();
    }

    private void setClickListener() {
        cashListView.setGroupIndicator(null);// 设置默认图标为不显示状态
        // 设置一级item点击的监听器
        cashListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                if (cashListView.isGroupExpanded(groupPosition)) {
//                    cashListView.collapseGroupWithAnimation(groupPosition);
                } else {
//                    cashListView.expandGroupWithAnimation(groupPosition);
                }
                // 刷新界面
                adapter.notifyDataSetChanged();
                return false;
            }
        });

        // 设置二级item点击的监听器
        cashListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                // 刷新界面
                adapter.notifyDataSetChanged();
                return false;
            }
        });
    }

    /**
     * 发起列表请求
     */
    private void requestList() {
        Intent intent = new Intent(Constants.SENDMSG_REFRESH);
        context.sendBroadcast(intent);
        AsyncHttpClientUtil asyncHttpClientUtil = new AsyncHttpClientUtil(0, new HashMap<String, String>());
        RequestParams params = new RequestParams();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("token", Constants.TOKENT);
        jsonObject.put("id", PrefShared.getString(context, "userId"));
        jsonObject.put("page", mPageIndex);
        params.put(Constants.REQUEST_MSG, jsonObject.toString());
        asyncHttpClientUtil.doPost(Constants.FIND_CASH_RECORD, params, new AsyncHttpResponseHandler() {
            @Override
            public void onStart() {
                super.onStart();
            }

            @Override
            public void onSuccess(int statusCode, org.apache.http.Header[] headers, byte[] bytes) {
                try {
                    String result = new String(bytes, "UTF-8");
                    updateListView(result);
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
                swipeLayout.setRefreshing(false);
                swipeLayout.setLoading(false);
            }
        });
    }

    /**
     * 更新ExpandableListView的数据
     *
     * @param result
     */
    private void updateListView(final String result) {
        if (mPageIndex == 1) {
            adapter.reset();
        }
        try {
            JSONObject jsonObject = JSONObject.parseObject(result);
//            Log.e("requestList2", jsonObject.toString());
            String code = jsonObject.getString("code");
            if (!TextUtils.equals("10", code)) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        swipeLayout.setRefreshing(false);
                        swipeLayout.setLoading(false);
                        mSVProgressHUD.showInfoWithStatus("没有记录啦~", SVProgressHUD.SVProgressHUDMaskType.GradientCancel);
                    }
                });
            } else {
                JSONArray jsonArray = jsonObject.getJSONArray("data");
                List<Map<String, Object>> parents = new ArrayList<Map<String, Object>>();
                String accountType = "";
                for (int i = 0; i < jsonArray.size(); i++) {
                    JSONObject json = jsonArray.getJSONObject(i);
                    Map<String, Object> map = new HashMap<String, Object>();
                    String state = json.getString("state");//0审核中 1处理中 2已到账 3提现失败
                    accountType = json.getString("type");
                    if(TextUtils.equals("alipay",accountType)){
                        map.put("type", "支付宝");
                    } else if(TextUtils.equals("1",accountType)){
                        map.put("type", "内部提现");
                    } else {
                        map.put("type", "未知提现方式");
                    }
                    map.put("money", json.getString("balance"));
                    if(TextUtils.equals("0",state)){
                        map.put("state", "审核中");
                    } else if(TextUtils.equals("1",state)){
                        map.put("state", "处理中");
                    } else if(TextUtils.equals("2",state)){
                        map.put("state", "已到账");
                    } else {
                        map.put("state", "提现失败");
                    }
                    parents.add(map);
                }
                List<List<Map<String, Object>>> childs = new ArrayList<List<Map<String, Object>>>();
                for (int i = 0; i < jsonArray.size(); i++) {
                    JSONObject json = jsonArray.getJSONObject(i);
                    String aDate = BaseTools.changeDate(1,json.getString("atime")).trim();//到账时间
                    String cDate = BaseTools.changeDate(1,json.getString("ctime")).trim();//提现时间
                    List<Map<String, Object>> ls = new ArrayList<Map<String, Object>>();
                    Map<String, Object> map2 = new HashMap<String, Object>();
                    String state = json.getString("state");//0审核中 1处理中 2已到账 3提现失败
                    String num = json.getString("oid");
                    String account = json.getString("account");
                    String name = json.getString("name");
                    String remark = json.getString("remark");
                    if(TextUtils.equals("0",state)){
                        map2.put("stateName", "审核中");
                    } else if(TextUtils.equals("1",state)){
                        map2.put("stateName", "处理中");
                    } else if(TextUtils.equals("2",state)){
                        map2.put("stateName", "已到账");
                    } else {
                        map2.put("stateName", "提现失败");
                    }
                    if(TextUtils.equals("0",state)){//还没有到账时间
                        map2.put("aDate", "0");
                    } else {
                        map2.put("aDate", aDate);
                    }
                    if(!TextUtils.equals(num,"0")){
                        map2.put("num", num.substring(num.length()-15,num.length()));
                    } else {
                        map2.put("num", "订单处理中，请耐心等待!");
                    }
                    map2.put("zfb", account);
                    map2.put("zfbName", name);
                    map2.put("cDate", cDate);
                    map2.put("remark",remark);
                    ls.add(map2);
                    childs.add(ls);
                }
                adapter.addData(parents, childs);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        swipeLayout.setRefreshing(false);
                        swipeLayout.setLoading(false);
                        adapter.notifyDataSetChanged();
                        if (mPageIndex == 1) {
                            cashListView.setSelectionAfterHeaderView();
                        } else {
                            cashListView.setSelection(mPageIndex * 10);
                        }
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 下拉刷新
     */
    @Override
    public void onRefresh() {
        refreshRequestList();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                swipeLayout.setRefreshing(false);
            }
        }, 2000);
    }

    /**
     * 上拉加载
     */
    @Override
    public void onLoad() {
        loadRequestList();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                swipeLayout.setLoading(false);
            }
        }, 500);
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