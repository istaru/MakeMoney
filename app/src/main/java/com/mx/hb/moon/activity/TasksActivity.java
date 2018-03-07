package com.mx.hb.moon.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.moonmmxhb.DevInit;
import com.moonmmxhb.GetAdListListener;
import com.mx.hb.moon.AdInfo;
import com.mx.hb.moon.AppConnect;
import com.mx.hb.moon.R;
import com.mx.hb.moon.adapter.TasksAdapter;
import com.mx.hb.moon.base.AsyncHttpClientUtil;
import com.mx.hb.moon.base.AsyncUtil;
import com.mx.hb.moon.base.Constants;
import com.mx.hb.moon.base.NetWorkUtils;
import com.mx.hb.moon.base.PrefShared;
import com.mx.hb.moon.dialog.T;
import com.mx.hb.moon.entity.ADInfo;
import com.mx.hb.moon.entity.DlInfo;
import com.mx.hb.moon.entity.DrInfo;
import com.mx.hb.moon.entity.WanpuInfo;
import com.mx.hb.moon.entity.YoumiInfo;
import com.mx.hb.moon.loading.SVProgressHUD;
import com.mx.hb.moon.swipeRefresh.SwipeRefreshLayout;
import com.mx.hb.moon.view.StatusBarUtil;
import com.nhaarman.listviewanimations.swinginadapters.prepared.SwingBottomInAnimationAdapter;
import com.yql.dr.http.DRCallback;
import com.yql.dr.sdk.DRSdk;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import xz.ax.qr.os.df.AppSummaryDataInterface;
import xz.ax.qr.os.df.AppSummaryObject;
import xz.ax.qr.os.df.AppSummaryObjectList;
import xz.ax.qr.os.df.DiyOfferWallManager;

/**
 * Created by Kiven on 16/7/6.
 */
public class TasksActivity extends BaseActivity implements View.OnClickListener, SwipeRefreshLayout.OnRefreshListener, AdapterView.OnItemClickListener{
    private static SVProgressHUD mSVProgressHUD;
    private LinearLayout onBack;
    private static SwipeRefreshLayout swipeLayout;
    /**
     * 请求页码
     */
    private int mPageIndex = 1;
    /**
     * 每页请求数量
     */
    private final static int AD_PER_NUMBER = 10000;
    private ListView tkListView;
    private TasksAdapter adapter;
    List<Map<String,Object>> list = new ArrayList<>();

    private RadioButton btn1,btn2;
    private int tasksType = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tasks);
        mSVProgressHUD = new SVProgressHUD(context);
        initView();
        refreshRequestList();
    }

    @Override
    protected void setStatusBar() {
        StatusBarUtil.setColor(this, getResources().getColor(R.color.gules),0);
    }

    private void initView() {
        tkListView = (ListView) findViewById(R.id.tkListView);
        swipeLayout = (SwipeRefreshLayout) findViewById(R.id.tasks_swipe);
        swipeLayout.setColor(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
        swipeLayout.setOnRefreshListener(this);
        swipeLayout.setMode(SwipeRefreshLayout.Mode.DISABLED);
        onBack = (LinearLayout) findViewById(R.id.onBack);
        onBack.setOnClickListener(this);
        btn1 = (RadioButton) findViewById(R.id.btn_1);
        btn2 = (RadioButton) findViewById(R.id.btn_2);
        btn1.setOnClickListener(new TasksonClickListener(1));
        btn2.setOnClickListener(new TasksonClickListener(2));

        adapter = new TasksAdapter(context, null);

        tkListView.setOnItemClickListener(this);
    }

    /**
     * 哪种类型的任务
     */
    class TasksonClickListener implements View.OnClickListener {
        private int index = 1;

        public TasksonClickListener(int i) {
            index = i;
        }

        public void onClick(View v) {
            switch (index) {
                case 1:
                    if (processFlag) {
                        setProcessFlag();//

                        btn1.setChecked(true);
                        if(swipeLayout.isRefreshing() == false){
                            tasksType = 1;
                            refreshRequestList();
                        }

                        new TimeThread().start();
                    }
                    break;
                case 2:
                    if (processFlag) {
                        setProcessFlag();//

                        btn2.setChecked(true);
                        if(swipeLayout.isRefreshing() == false){
                            tasksType = 2;
                            refreshRequestList();
                        }

                        new TimeThread().start();
                    }
                    break;
            }
        }

    }

    /**
     * 下拉更新广告列表
     */
    private void refreshRequestList() {
        mPageIndex = 1;
        swipeLayout.setRefreshing(true);
        String tasksJson = PrefShared.getString(context,"tasksJson");

        SwingBottomInAnimationAdapter swingBottomInAnimationAdapter = new SwingBottomInAnimationAdapter(adapter);
        swingBottomInAnimationAdapter.setAbsListView(tkListView);
        tkListView.setAdapter(swingBottomInAnimationAdapter);

        if(null != tasksJson && !TextUtils.equals("",tasksJson)){
//            list.clear();
            adapter.reset();
            JSONObject resultJson = JSONObject.parseObject(tasksJson);
            setData(resultJson);
        } else {
//            list.clear();
            adapter.reset();
            requestList1();
        }
    }

    /**
     * 发起列表请求
     */
    private void requestList1() {
        // 获取指定类型 的广告，并更新listview，下面展示两种加载方式，开发者可选择适合自己的方式
        // 异步加载方式
        // 请求类型，页码，请求数量，回调接口
        DiyOfferWallManager.getInstance(context).loadOfferWallAdList(DiyOfferWallManager.REQUEST_SPECIAL_SORT, mPageIndex, AD_PER_NUMBER, new AppSummaryDataInterface() {
            /**
             * 当成功获取到积分墙列表数据的时候，会回调这个方法（注意:本接口不在UI线程中执行， 所以请不要在本接口中进行UI线程方面的操作）
             * 注意：列表数据有可能为空（比如：没有广告的时候），开发者处理之前，请先判断列表是否为空，大小是否大与0
             */
            @Override
            public void onLoadAppSumDataSuccess(Context context, AppSummaryObjectList adList) {
                updateListView1(adList);
            }

            /**
             * 因为网络问题而导致请求失败时，会回调这个接口（注意:本接口不在UI线程中执行， 所以请不要在本接口中进行UI线程方面的操作）
             */
            @Override
            public void onLoadAppSumDataFailed() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // 如果是请求第一页的时候，请求失'败，就致列表为空
                        if (mPageIndex == 1) {
                            adapter.reset();
                            adapter.notifyDataSetChanged();
                        }
                        if(swipeLayout.isRefreshing()){
                            swipeLayout.setRefreshing(false);
                        }
                        mSVProgressHUD.showInfoWithStatus("网络不可用", SVProgressHUD.SVProgressHUDMaskType.GradientCancel);
                    }
                });
            }

            /**
             * 请求成功，但是返回有米错误代码时候，会回调这个接口（注意:本接口不在UI线程中执行， 所以请不要在本接口中进行UI线程方面的操作）
             */
            @Override
            public void onLoadAppSumDataFailedWithErrorCode(final int code) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // 如果是请求第一页的时候，请求失败，就致列表为空
                        if (mPageIndex == 1) {
                            adapter.reset();
                            adapter.notifyDataSetChanged();
                        }
                        swipeLayout.setRefreshing(false);
                        mSVProgressHUD.showInfoWithStatus("initialization error", SVProgressHUD.SVProgressHUDMaskType.GradientCancel);
                        Log.e("有米", code+"");
                        Log.e("有米失败：",code+"");

                    }
                });
            }
        });
    }

    /**
     * 更新ListView的数据
     *
     * @param adList
     */
    private void updateListView1(final AppSummaryObjectList adList) {
        try {
            if (adList == null ||  adList.isEmpty()) {
                Log.e("appSummaryObject","null");
            } else {
                for (int i = 0; i < adList.size(); ++i) {
                    Map<String,Object> map = new HashMap<>();
                    AppSummaryObject appSummaryObject = adList.get(i);
                    map.put("appSummaryObject",appSummaryObject);
                    list.add(map);
                }
            }
            requestList2();
        } catch (Exception e){
            e.printStackTrace();
            swipeLayout.setRefreshing(false);
        }
    }

    private void requestList2() {
        try {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    List<AdInfo> adInfoList = AppConnect.getInstance(context).getAdInfoList();
                    try {
                        if(adInfoList == null || adInfoList.isEmpty()){
                            Log.e("adInfoList","null");
                        } else {
                            for(int i = 0;i < adInfoList.size();i++){
                                Map<String,Object> map = new HashMap<>();
                                AdInfo adInfo = adInfoList.get(i);
                                WanpuInfo wanpuInfo = new WanpuInfo();
                                wanpuInfo.setAdId(adInfo.getAdId());
                                wanpuInfo.setAdName(adInfo.getAdName());
                                wanpuInfo.setAdText(adInfo.getAdText());
                                wanpuInfo.setAdPoints(adInfo.getAdPoints());
                                wanpuInfo.setDescription(adInfo.getDescription());
                                wanpuInfo.setVersion(adInfo.getVersion());
                                wanpuInfo.setFileSize(adInfo.getFilesize());
                                wanpuInfo.setProvider(adInfo.getProvider());
                                wanpuInfo.setImageUrl(adInfo.getImageUrl());
                                wanpuInfo.setImageUrls(adInfo.getImageUrls());
                                wanpuInfo.setAdPackage(adInfo.getAdPackage());
                                wanpuInfo.setAction(adInfo.getAction());
                                wanpuInfo.setAppType(adInfo.getAppType());
                                map.put("adInfo",wanpuInfo);
                                list.add(map);
                            }
                        }
                    } catch (Exception e){
                        e.printStackTrace();
                    }
                    requestList3();
                }
            });
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public void requestList3(){
        try {
            if(NetWorkUtils.isNetworkConnected(context) == false){
                swipeLayout.setRefreshing(false);
                mSVProgressHUD.showInfoWithStatus("网络不可用", SVProgressHUD.SVProgressHUDMaskType.GradientCancel);
            } else {
                DRSdk.getData(DRSdk.DR_OFFER, context, new DRCallback() {
                    @Override
                    public void callback(String data) {
                        updateListView3(data);
                    }
                });
            }
        } catch (Exception e){
            Log.e("网路不可用","123");
            e.printStackTrace();
        }
    }

    private void updateListView3(String data) {
        try {
            if (data == null ||  TextUtils.equals(data,"")) {
                Log.e("drInfo","null");
            } else {
                JSONObject jsonObject = JSONObject.parseObject(data);
                String title = jsonObject.getString("title");
                String table = jsonObject.getString("table");
                JSONObject json = JSONObject.parseObject(table);
                JSONArray jsonArray = json.getJSONArray("score");
                if(jsonArray.isEmpty()){
                    Log.e("drInfojsonArray","null");
//                    mSVProgressHUD.showInfoWithStatus(title, SVProgressHUD.SVProgressHUDMaskType.GradientCancel);
//                    swipeLayout.setRefreshing(false);
                } else {
                    for (int i = 0; i < jsonArray.size(); i++) {
                        Map<String,Object> map = new HashMap<>();
                        json = jsonArray.getJSONObject(i);
                        DrInfo drInfo = new DrInfo();
                        drInfo.setCid(json.getString("cid"));
                        drInfo.setAdid(json.getString("adid"));
                        drInfo.setIntro(json.getString("intro"));
                        drInfo.setIcon(json.getString("icon"));
                        drInfo.setTitle(json.getString("title"));
                        drInfo.setText1(json.getString("text1"));
                        drInfo.setText2(json.getString("text2"));
                        drInfo.setImages(json.getString("images"));
                        drInfo.setUrl(json.getString("url"));
                        drInfo.setAndroidUrl(json.getString("android_url"));
                        drInfo.setPsize(json.getString("psize")+"M");
                        drInfo.setProcessName(json.getString("process_name"));
                        drInfo.setProcessName1(json.getString("process_name1"));
                        drInfo.setPtype(json.getString("ptype"));
                        drInfo.setImage1(json.getString("image1"));
                        drInfo.setImage2(json.getString("image2"));
                        drInfo.setImage3(json.getString("image3"));
                        drInfo.setActiveTime(json.getString("active_time"));
                        drInfo.setRuntime(json.getString("runtime"));
                        drInfo.setCurrNote(json.getString("curr_note"));
                        drInfo.setActiveNum(json.getString("active_num"));
                        drInfo.setScore(json.getString("score"));
                        map.put("drInfo",drInfo);
                        list.add(map);
                    }
                }
            }
            requestList4();
        } catch (Exception e){
            e.printStackTrace();
            swipeLayout.setRefreshing(false);
        }
    }

    private void requestList4() {
        /**
         * 调用此接口会从后台获取最新的广告列表并刷新.
         */
        DevInit.getList(context, mPageIndex, AD_PER_NUMBER, new GetAdListListener() {

            @Override
            public void getAdListSucceeded(List adList) {
                updateListView4(adList);
            }

            @Override
            public void getAdListFailed(final String error) {
                if (NetWorkUtils.isNetworkConnected(context) == false) {
                    swipeLayout.setRefreshing(false);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (mPageIndex == 1) {
                                adapter.reset();
                                adapter.notifyDataSetChanged();
                            }
                        }
                    });
                    mSVProgressHUD.showInfoWithStatus("网络不可用", SVProgressHUD.SVProgressHUDMaskType.GradientCancel);
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // 如果是请求第一页的时候，请求失败，就致列表为空
                            if (mPageIndex == 1) {
                                adapter.reset();
                                adapter.notifyDataSetChanged();
                            }
                            swipeLayout.setRefreshing(false);
                            mSVProgressHUD.showInfoWithStatus("initialization error", SVProgressHUD.SVProgressHUDMaskType.GradientCancel);
//                            Log.e("点乐", error);
                        }
                    });
                }
            }
        });
    }

    private void updateListView4(List adList) {
        try {
            if ((adList == null ||  adList.isEmpty()) && (list == null || list.isEmpty())) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        swipeLayout.setRefreshing(false);
                        mSVProgressHUD.showInfoWithStatus("没有任务啦~", SVProgressHUD.SVProgressHUDMaskType.GradientCancel);
                    }
                });
            } else {
                for (int i = 0; i < adList.size(); ++i) {
                    Map<String, Object> map = (Map<String, Object>) adList.get(i);
                    Map<String,Object> maps = new HashMap<>();
                    DlInfo dlInfo = new DlInfo();
                    dlInfo.setIcon(map.get("icon") + ""); //广告icon图片的存放地址
                    dlInfo.setText(map.get("text") + ""); //广告简介
                    dlInfo.setPackName(map.get("pack_name") + ""); //广告包名
                    dlInfo.setDescription(map.get("description") + ""); //广告描述
                    dlInfo.setName(map.get("name") + ""); //广告名字
                    dlInfo.setTaskCount((Integer) map.get("task_count")); //广告类型(task_count=-1表示没有深度任务,大于零表示有深度任务)
                    dlInfo.setNumber((Integer) map.get("number")); //广告积分
                    dlInfo.setVer(map.get("ver") + ""); //广告版本号
                    dlInfo.setSize(map.get("size") + ""); //广告安装包大小
                    dlInfo.setTasks(map.get("tasks") + ""); //深度任务列表
                    dlInfo.setAllDownCount(map.get("all_down_count") + ""); //总下载数
                    dlInfo.setSetupTips(map.get("setup_tips") + ""); //任务提示信息
                    dlInfo.setThumbnail(map.get("thumbnail") + ""); //广告详情页的图片
                    dlInfo.setAdType(map.get("ad_type") + ""); //广告类型（类型对应关系见下表）
                    maps.put("dlInfo",dlInfo);
                    list.add(maps);
                }
            }
            uploadTasks(list);
        } catch (Exception e){
            e.printStackTrace();
            swipeLayout.setRefreshing(false);
        }
    }

    /**
     * 上传任务
     * @param list
     */
    private void uploadTasks(List<Map<String,Object>> list) {
        AsyncHttpClientUtil asyncHttpClientUtil = new AsyncHttpClientUtil(5000, new HashMap<String, String>());
        RequestParams params = new RequestParams();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("token", Constants.TOKENT);
        jsonObject.put("lists", JSON.toJSON(list));
        params.put(Constants.REQUEST_MSG, jsonObject.toString());
        asyncHttpClientUtil.doPost(Constants.UPLOAD_TASKS, params, new AsyncHttpResponseHandler() {
            @Override
            public void onStart() {
                super.onStart();
            }

            @Override
            public void onSuccess(int statusCode, org.apache.http.Header[] headers, byte[] bytes) {
                try {
                    String result = new String(bytes, "UTF-8");
                    PrefShared.saveString(context,"tasksJson",result);
                    JSONObject resultJson = JSONObject.parseObject(result);
                    setData(resultJson);
                } catch (Exception e) {
                    e.printStackTrace();
                    mSVProgressHUD.showInfoWithStatus("任务数据有误", SVProgressHUD.SVProgressHUDMaskType.GradientCancel);
                }
            }

            @Override
            public void onFailure(int i, org.apache.http.Header[] headers, byte[] bytes, Throwable throwable) {
                try {
                    swipeLayout.setRefreshing(false);
                    String error = throwable.toString();
                    Log.e("网络请求", error);
                    if (error.contains("TimeoutException")) {
                        mSVProgressHUD.showInfoWithStatus("请求超时", SVProgressHUD.SVProgressHUDMaskType.GradientCancel);
                    } else if (error.contains("HttpHostConnectException")) {
                        mSVProgressHUD.showInfoWithStatus("网络错误", SVProgressHUD.SVProgressHUDMaskType.GradientCancel);
                    } else {
                        mSVProgressHUD.showErrorWithStatus("请求错误", SVProgressHUD.SVProgressHUDMaskType.GradientCancel);
                    }
                } catch (Exception e) {
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
     *
     * @param resultJson
     */
    public void setData(JSONObject resultJson) {
        swipeLayout.setRefreshing(false);
        List<Map<String, Object>> listMap = new ArrayList<>();
        JSONArray tasksArray = null;
        if (tasksType == 1) {//普通任务
            tasksArray = resultJson.getJSONArray("normal");
        } else {//高额任务
            tasksArray = resultJson.getJSONArray("high");
        }
        if (null != tasksArray && !tasksArray.isEmpty()) {
            for (int i = 0; i < tasksArray.size(); i++) {
                JSONObject jsonObject = tasksArray.getJSONObject(i);
                if (null != jsonObject.getJSONObject("appSummaryObject")) {
                    YoumiInfo youmiInfo = new YoumiInfo();
                    Map<String, Object> map = new HashMap<String, Object>();
                    youmiInfo.setAdId(jsonObject.getJSONObject("appSummaryObject").getInteger("adId"));
                    youmiInfo.setIconUrl(jsonObject.getJSONObject("appSummaryObject").getString("iconUrl"));
                    youmiInfo.setAppName(jsonObject.getJSONObject("appSummaryObject").getString("appName"));
                    youmiInfo.setAdSlogan(jsonObject.getJSONObject("appSummaryObject").getString("adSlogan"));
                    youmiInfo.setPoints(jsonObject.getJSONObject("appSummaryObject").getInteger("points"));
                    map.put("appSummaryObject", youmiInfo);
                    listMap.add(map);
                }
                if (null != jsonObject.getJSONObject("adInfo")) {
                    WanpuInfo wanpuInfo = new WanpuInfo();
                    Map<String, Object> map = new HashMap<String, Object>();
                    wanpuInfo.setAdId(jsonObject.getJSONObject("adInfo").getString("adId"));
                    wanpuInfo.setAdName(jsonObject.getJSONObject("adInfo").getString("adName"));
                    wanpuInfo.setAdText(jsonObject.getJSONObject("adInfo").getString("adText"));
                    wanpuInfo.setAdPoints(jsonObject.getJSONObject("adInfo").getInteger("adPoints"));
                    wanpuInfo.setDescription(jsonObject.getJSONObject("adInfo").getString("description"));
                    wanpuInfo.setVersion(jsonObject.getJSONObject("adInfo").getString("version"));
                    wanpuInfo.setFileSize(jsonObject.getJSONObject("adInfo").getString("fileSize"));
                    wanpuInfo.setProvider(jsonObject.getJSONObject("adInfo").getString("provider"));
                    wanpuInfo.setImageUrl(jsonObject.getJSONObject("adInfo").getString("imageUrl"));
                    JSONArray urls = jsonObject.getJSONObject("adInfo").getJSONArray("imageUrls");
                    String[] imageUrls = new String[urls.size()];
                    for (int j = 0; j < urls.size(); j++) {
                        imageUrls[j] = urls.getString(j);
                    }
                    wanpuInfo.setImageUrls(imageUrls);
                    wanpuInfo.setAdPackage(jsonObject.getJSONObject("adInfo").getString("adPackage"));
                    wanpuInfo.setAction(jsonObject.getJSONObject("adInfo").getString("action"));
                    wanpuInfo.setAppType(jsonObject.getJSONObject("adInfo").getString("appType"));
                    map.put("adInfo", wanpuInfo);
                    listMap.add(map);
                }
                if (null != jsonObject.getJSONObject("drInfo")) {
                    DrInfo drInfo = new DrInfo();
                    Map<String, Object> map = new HashMap<>();
                    drInfo.setCid(jsonObject.getJSONObject("drInfo").getString("cid"));
                    drInfo.setAdid(jsonObject.getJSONObject("drInfo").getString("adid"));
                    drInfo.setIntro(jsonObject.getJSONObject("drInfo").getString("intro"));
                    drInfo.setIcon(jsonObject.getJSONObject("drInfo").getString("icon"));
                    drInfo.setTitle(jsonObject.getJSONObject("drInfo").getString("title"));
                    drInfo.setText1(jsonObject.getJSONObject("drInfo").getString("text1"));
                    drInfo.setText2(jsonObject.getJSONObject("drInfo").getString("text2"));
                    drInfo.setImages(jsonObject.getJSONObject("drInfo").getString("images"));
                    drInfo.setUrl(jsonObject.getJSONObject("drInfo").getString("url"));
                    drInfo.setAndroidUrl(jsonObject.getJSONObject("drInfo").getString("androidUrl"));
                    drInfo.setPsize(jsonObject.getJSONObject("drInfo").getString("psize"));
                    drInfo.setProcessName(jsonObject.getJSONObject("drInfo").getString("processName"));
                    drInfo.setProcessName1(jsonObject.getJSONObject("drInfo").getString("processName1"));
                    drInfo.setPtype(jsonObject.getJSONObject("drInfo").getString("ptype"));
                    drInfo.setImage1(jsonObject.getJSONObject("drInfo").getString("image1"));
                    drInfo.setImage2(jsonObject.getJSONObject("drInfo").getString("image2"));
                    drInfo.setImage3(jsonObject.getJSONObject("drInfo").getString("image3"));
                    drInfo.setActiveTime(jsonObject.getJSONObject("drInfo").getString("activeTime"));
                    drInfo.setRuntime(jsonObject.getJSONObject("drInfo").getString("runtime"));
                    drInfo.setCurrNote(jsonObject.getJSONObject("drInfo").getString("currNote"));
                    drInfo.setActiveNum(jsonObject.getJSONObject("drInfo").getString("activeNum"));
                    drInfo.setScore(jsonObject.getJSONObject("drInfo").getString("score"));
                    map.put("drInfo", drInfo);
                    listMap.add(map);
                }
                if (null != jsonObject.getJSONObject("dlInfo")) {
                    DlInfo dlInfo = new DlInfo();
                    Map<String, Object> map = new HashMap<>();
                    dlInfo.setIcon(jsonObject.getJSONObject("dlInfo").getString("icon")); //广告icon图片的存放地址
                    dlInfo.setText(jsonObject.getJSONObject("dlInfo").getString("text")); //广告简介
                    dlInfo.setPackName(jsonObject.getJSONObject("dlInfo").getString("packName")); //广告包名
                    dlInfo.setDescription(jsonObject.getJSONObject("dlInfo").getString("description")); //广告描述
                    dlInfo.setName(jsonObject.getJSONObject("dlInfo").getString("name")); //广告名字
                    dlInfo.setTaskCount(jsonObject.getJSONObject("dlInfo").getInteger("taskCount")); //广告类型(task_count=-1表示没有深度任务,大于零表示有深度任务)
                    dlInfo.setNumber(jsonObject.getJSONObject("dlInfo").getInteger("number")); //广告积分
                    dlInfo.setVer(jsonObject.getJSONObject("dlInfo").getString("ver")); //广告版本号
                    dlInfo.setSize(jsonObject.getJSONObject("dlInfo").getString("size")); //广告安装包大小
                    dlInfo.setTasks(jsonObject.getJSONObject("dlInfo").getString("tasks")); //深度任务列表
                    dlInfo.setAllDownCount(jsonObject.getJSONObject("dlInfo").getString("allDownCount")); //总下载数
                    dlInfo.setSetupTips(jsonObject.getJSONObject("dlInfo").getString("setupTips")); //任务提示信息
                    dlInfo.setThumbnail(jsonObject.getJSONObject("dlInfo").getString("thumbnail")); //广告详情页的图片
                    dlInfo.setAdType(jsonObject.getJSONObject("dlInfo").getString("adType")); //广告类型（类型对应关系见下表）
                    map.put("dlInfo", dlInfo);
                    listMap.add(map);
                }
            }
            adapter.addData(listMap);
            tkListView.requestLayout();//解决拟补数据数量不一致导致报错
            adapter.notifyDataSetChanged();
        } else {
            if (tasksType == 1) {//显示普通任务提示
                mSVProgressHUD.showInfoWithStatus("没有普通任务啦~过会再来吧", SVProgressHUD.SVProgressHUDMaskType.GradientCancel);
            } else {//显示高额任务提示
                mSVProgressHUD.showInfoWithStatus("没有高额任务啦~过会再来吧", SVProgressHUD.SVProgressHUDMaskType.GradientCancel);
            }
        }
    }

//    /**
//     * 上传任务
//     * @param list
//     */
//    private void uploadTasks(final List<Map<String,Object>> list) {
//        AsyncHttpClientUtil asyncHttpClientUtil = new AsyncHttpClientUtil(2000,new HashMap<String,String>());
//        RequestParams params = new RequestParams();
//        JSONObject jsonObject = new JSONObject();
//        jsonObject.put("token", Constants.TOKENT);
//        params.put(Constants.REQUEST_MSG,jsonObject.toString());
//        asyncHttpClientUtil.doPost(Constants.UPLOAD_TASKS, params, new AsyncHttpResponseHandler() {
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
//
//                    JSONObject YMJson = jsonObject.getJSONObject("有米");
//                    String ymFilter = YMJson.getString("filter");
//                    String ymDisable = YMJson.getString("disable");
//                    String[] yms = ymFilter.split(",");
//
//                    JSONObject WPJson = jsonObject.getJSONObject("万普");
//                    String wpFilter = WPJson.getString("filter");
//                    String wpDisable = WPJson.getString("disable");
//                    String[] wps = wpFilter.split(",");
//
//                    JSONObject DRJson = jsonObject.getJSONObject("点入");
//                    String drFilter = DRJson.getString("filter");
//                    String drDisable = DRJson.getString("disable");
//                    String[] drs = drFilter.split(",");
//
//                    JSONObject DLJson = jsonObject.getJSONObject("点乐");
//                    String dlFilter = DLJson.getString("filter");
//                    String dlDisable = DLJson.getString("disable");
//                    String[] dls = dlFilter.split(",");
//
//                    Iterator<Map<String,Object>> iter = list.iterator();
//                    while(iter.hasNext()){
//                        Map<String, Object> map = iter.next();
//                        if(map.get("appSummaryObject") != null){
//                            if(TextUtils.equals("1",ymDisable)){
//                                iter.remove();
//                            } else {
//                                AppSummaryObject appSummaryObject = (AppSummaryObject) map.get("appSummaryObject");//有米
//                                for(int j = 0;j < yms.length;j++){
//                                    if(TextUtils.equals(yms[j],appSummaryObject.getAppName())){
//                                        iter.remove();
//                                    }
//                                }
//                            }
//                        }
//                        if(map.get("adInfo") != null){
//                            if(TextUtils.equals("1",wpDisable)){
//                                iter.remove();
//                            } else {
//                                AdInfo adInfo = (AdInfo) map.get("adInfo");//万普
//                                for(int j = 0;j < wps.length;j++){
//                                    if(TextUtils.equals(wps[j],adInfo.getAdName())){
//                                        iter.remove();
//                                    }
//                                }
//                            }
//                        }
//                        if(map.get("drInfo") != null){
//                            if(TextUtils.equals("1",drDisable)){
//                                iter.remove();
//                            } else {
//                                DrInfo drInfo = (DrInfo) map.get("drInfo");//点入
//                                for(int j = 0;j < drs.length;j++){
//                                    if(TextUtils.equals(drs[j],drInfo.getTitle())){
//                                        iter.remove();
//                                    }
//                                }
//                            }
//                        }
//                        if(map.get("dlInfo") != null){
//                            if(TextUtils.equals("1",dlDisable)){
//                                iter.remove();
//                            } else {
//                                DlInfo dlInfo = (DlInfo) map.get("dlInfo");//点乐
//                                for(int j = 0;j < dls.length;j++){
//                                    if(TextUtils.equals(dls[j],dlInfo.getName())){
//                                        iter.remove();
//                                    }
//                                }
//                            }
//                        }
//                    }
//                    if(null == list || list.isEmpty()){
//                        mSVProgressHUD.showInfoWithStatus("没有任务啦~过会再来吧", SVProgressHUD.SVProgressHUDMaskType.GradientCancel);
//                    } else {
//                        adapter.addData(list);
//                        tkListView.requestLayout();//解决拟补数据数量不一致导致报错
//                        adapter.notifyDataSetChanged();
//                    }
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//
//            @Override
//            public void onFailure(int i, org.apache.http.Header[] headers, byte[] bytes, Throwable throwable) {
//                swipeLayout.setRefreshing(false);
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
//                swipeLayout.setRefreshing(false);
//            }
//        });
//    }

    /**
     * 下拉刷新
     */
    @Override
    public void onRefresh() {
        refreshRequestList();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if(NetWorkUtils.isNetworkConnected(context) == false){
            mSVProgressHUD.showInfoWithStatus("网络不可用", SVProgressHUD.SVProgressHUDMaskType.GradientCancel);
        } else {
            Map<String, Object> map = adapter.getItem(position);
            YoumiInfo appSummaryObject = (YoumiInfo) map.get("appSummaryObject");
            WanpuInfo adInfo = (WanpuInfo) map.get("adInfo");//万普
            DrInfo drInfo = (DrInfo) map.get("drInfo");//点入
            DlInfo dlInfo = (DlInfo) map.get("dlInfo");//点乐
            if(appSummaryObject != null){
                Iterator<Map<String,Object>> iter = list.iterator();
                while(iter.hasNext()) {
                    Map<String, Object> ymMap = iter.next();
                    AppSummaryObject summaryObject = (AppSummaryObject) ymMap.get("appSummaryObject");//有米
                    if (summaryObject.getAdId() == appSummaryObject.getAdId()) {
                        Intent intent = new Intent(context,YoumiItemActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("appSummaryObject", summaryObject);
                        intent.putExtras(bundle);
                        startActivity(intent);
                        break;
                    }
                }
            } else if(adInfo != null){
                Intent intent = new Intent(context,WanpuItemActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("adInfo", adInfo);
                intent.putExtras(bundle);
                startActivity(intent);
            } else if(drInfo != null){
                Intent intent = new Intent(context,DianruItemActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("drInfo", drInfo);
                intent.putExtras(bundle);
                startActivity(intent);
            } else if(dlInfo != null){
                Intent intent = new Intent(context,DianleItemActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("dlInfo", dlInfo);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.onBack:
                PrefShared.removeData(context,"tasksJson");
                finish();
                break;
            default:
                break;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        PrefShared.removeData(context,"tasksJson");
        finish();
    }
}
