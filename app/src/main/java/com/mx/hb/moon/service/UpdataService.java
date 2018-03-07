package com.mx.hb.moon.service;

import android.annotation.TargetApi;
import android.app.DownloadManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;

import com.mx.hb.moon.activity.MessageActivity;
import com.mx.hb.moon.base.Constants;
import com.mx.hb.moon.base.PrefShared;

/**
 * Created by Moon on 2016/5/9.
 */
public class UpdataService extends Service {

    /**
     * 安卓系统下载类
     **/
    DownloadManager manager;
    /**
     * 接收下载完的广播
     **/
    DownloadCompleteReceiver receiver;

    /**
     * 初始化下载器
     **/
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void initDownManager() {
        manager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        receiver = new DownloadCompleteReceiver();
        //设置下载地址
        DownloadManager.Request down = new DownloadManager.Request(Uri.parse(MessageActivity.apkUrl));
        //先删除之前下载的文件
        long downId = PrefShared.getLong(getApplicationContext(), "downId");
        if(downId+"" != null || !TextUtils.equals("",downId+"")){
            manager.remove(PrefShared.getLong(getApplicationContext(), "downId"));
            PrefShared.removeData(getApplicationContext(), "downId");
        } else {
            Log.e("downId", "为空");
        }
//        try {
//            manager.remove(PrefShared.getLong(getApplicationContext(), "downId"));
//        } catch (Exception e){
//            e.printStackTrace();
//        }
        // 设置允许使用的网络类型，这里是移动网络和wifi都可以
        down.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE | DownloadManager.Request.NETWORK_WIFI);
        // 下载时，通知栏显示途中
        down.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        // 显示下载界面
        down.setVisibleInDownloadsUi(true);
        // 设置下载后文件存放的位置
        down.setDestinationInExternalPublicDir(Constants.CACHEDIRECTORY_PATH + "files", "来赚.apk");
        // 将下载请求放入队列
        manager.enqueue(down);
        //注册下载广播
        registerReceiver(receiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // 调用下载
        initDownManager();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        // 注销下载广播
        if (receiver != null)
            unregisterReceiver(receiver);
        super.onDestroy();
    }

    // 接受下载完成后的intent
    public class DownloadCompleteReceiver extends BroadcastReceiver {
        @TargetApi(Build.VERSION_CODES.HONEYCOMB)
        @Override
        public void onReceive(Context context, Intent intent) {
            //判断是否下载完成的广播
            if (intent.getAction().equals(DownloadManager.ACTION_DOWNLOAD_COMPLETE)) {
                //获取下载的文件id
                long downId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
                PrefShared.saveLong(context,"downId",downId);
                //自动安装apk
                installAPK(manager.getUriForDownloadedFile(downId));
                //停止服务并关闭广播
                UpdataService.this.stopSelf();
                MessageActivity.updateApp.setEnabled(true);
            } else {
                MessageActivity.updateApp.setEnabled(false);
            }
        }
    }

    /**
     * 安装apk文件
     */
    public void installAPK(Uri apk) {
        // 通过Intent安装APK文件
        Intent intents = new Intent();
        intents.setAction("android.intent.action.VIEW");
        intents.addCategory("android.intent.category.DEFAULT");
        intents.setType("application/vnd.android.package-archive");
        intents.setData(apk);
        intents.setDataAndType(apk, "application/vnd.android.package-archive");
        intents.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//          android.os.Process.killProcess(android.os.Process.myPid());
        // 如果不加上这句的话在apk安装完成之后点击单开会崩溃
        startActivity(intents);
    }

}
