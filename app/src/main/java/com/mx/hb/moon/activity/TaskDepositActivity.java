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

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.mx.hb.moon.R;
import com.mx.hb.moon.adapter.TaskDAdapter;
import com.mx.hb.moon.base.AsyncHttpClientUtil;
import com.mx.hb.moon.base.BaseTools;
import com.mx.hb.moon.base.Constants;
import com.mx.hb.moon.base.NetWorkUtils;
import com.mx.hb.moon.base.PrefShared;
import com.mx.hb.moon.dialog.T;
import com.mx.hb.moon.loading.SVProgressHUD;
import com.mx.hb.moon.swipeRefresh.SwipeRefreshLayout;
import com.mx.hb.moon.view.AnimatedExpandableListView;
import com.mx.hb.moon.view.StatusBarUtil;
import com.nhaarman.listviewanimations.swinginadapters.prepared.SwingBottomInAnimationAdapter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Kiven on 16/6/21.
 */
public class TaskDepositActivity extends BaseActivity implements View.OnClickListener,SwipeRefreshLayout.OnRefreshListener, SwipeRefreshLayout.OnLoadListener {
    private static SVProgressHUD mSVProgressHUD;
    private TextView titleText;
    private ExpandableListView taskEListView;
    private TaskDAdapter adapter;
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
        StatusBarUtil.setColor(this, getResources().getColor(R.color.black),0);
    }

    private void initView() {
        titleText = (TextView) findViewById(R.id.titleText);
        titleText.setText("任务收入");
        taskEListView = (ExpandableListView) findViewById(R.id.taskEListView);
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
        adapter = new TaskDAdapter(context);
        taskEListView.setAdapter(adapter);
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
        taskEListView.setGroupIndicator(null);// 设置默认图标为不显示状态
        // 设置一级item点击的监听器
        taskEListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                if(taskEListView.isGroupExpanded(groupPosition)){
//                    taskEListView.collapseGroupWithAnimation(groupPosition);
                } else {
//                    taskEListView.expandGroupWithAnimation(groupPosition);
                }
                // 刷新界面
                adapter.notifyDataSetChanged();
                return false;
            }
        });

        // 设置二级item点击的监听器
        taskEListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v,int groupPosition, int childPosition, long id) {
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
        AsyncHttpClientUtil asyncHttpClientUtil = new AsyncHttpClientUtil(0,new HashMap<String,String>());
        RequestParams params = new RequestParams();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("token", Constants.TOKENT);
        jsonObject.put("id", PrefShared.getString(context,"userId"));
        jsonObject.put("page", mPageIndex);
        params.put(Constants.REQUEST_MSG,jsonObject.toString());
        asyncHttpClientUtil.doPost(Constants.FIND_TASK_RECORD, params, new AsyncHttpResponseHandler() {
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
                swipeLayout.setRefreshing(false);
                swipeLayout.setLoading(false);
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

    /**
     * 更新ExpandableListView的数据
     * @param result
     */
    private void updateListView(final String result) {
        if(mPageIndex == 1){
            adapter.reset();
        }
        try{
//            Log.e("result",result);
            JSONObject jsonObject = JSONObject.parseObject(result);
//            Log.e("requestList",jsonObject.toString());
            String code = jsonObject.getString("code");
            if(!TextUtils.equals("10",code)){
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
                List<Map<String,Object>> parents = new ArrayList<Map<String, Object>>();
                for(int i = 0;i < jsonArray.size();i++){
                    JSONObject json = jsonArray.getJSONObject(i);
                    String date = BaseTools.changeDate(1,json.getString("ctime")).trim();
                    Map<String,Object> map = new HashMap<String, Object>();
                    map.put("type",json.getString("tname"));
                    map.put("money",json.getString("bouns"));
                    map.put("state",date.substring(0,date.length() - 9));
                    parents.add(map);
                }
                List<List<Map<String,Object>>> childs = new ArrayList<List<Map<String,Object>>>();
                for(int i = 0;i < jsonArray.size();i++){
                    JSONObject json = jsonArray.getJSONObject(i);
                    String date = BaseTools.changeDate(1,json.getString("ctime")).trim();
                    List<Map<String,Object>> ls = new ArrayList<Map<String, Object>>();
                    Map<String,Object> map2 = new HashMap<String, Object>();
                    String tId = json.getString("tid");
                    map2.put("stateName",json.getString("supplier"));
                    map2.put("num",tId.substring(0,17));
                    map2.put("time", date.substring(date.length()-9,date.length()));
                    ls.add(map2);
                    childs.add(ls);
                }
                adapter.addData(parents,childs);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        swipeLayout.setRefreshing(false);
                        swipeLayout.setLoading(false);
                        adapter.notifyDataSetChanged();
                        if (mPageIndex == 1) {
                            taskEListView.setSelectionAfterHeaderView();
                        } else {
                            taskEListView.setSelection(mPageIndex * 10);
                        }
                    }
                });
            }//            for(int i = 0; i < adapter.getGroupCount(); i++){//默认全部展开

//                taskEListView.expandGroup(i);
//            }
        }catch (Exception e){
            swipeLayout.setRefreshing(false);
            swipeLayout.setLoading(false);
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
