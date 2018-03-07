package com.mx.hb.moon.base;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import java.util.Iterator;
import java.util.Map;

/**
 * Created by Moon on 2016/4/5.
 */
public class AsyncHttpClientUtil {
    private static com.loopj.android.http.AsyncHttpClient client = new com.loopj.android.http.AsyncHttpClient();

    /**
     *
     * @param timeOut 多少秒超时
     * @param headers 请求头部的信息
     */
    public AsyncHttpClientUtil(int timeOut,Map<String,String> headers){
        //设置超时时间
        if(timeOut > 0) {
            client.setTimeout(timeOut);
        } else {
            client.setTimeout(5000);
        }
        //添加网络请求的头部
        if(!headers.isEmpty()){
            Iterator iterator = headers.entrySet().iterator();
            while(iterator.hasNext()){
                Map.Entry entry = (Map.Entry) iterator.next();
                client.addHeader(entry.getKey().toString(), entry.getValue().toString());
            }
        }
    }

    public static void doGet(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        client.get(url, params, responseHandler);
    }

    public static void doPost(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        client.post(url, params, responseHandler);
    }

//    client.post(url, params, new AsyncHttpResponseHandler() {
//        /**
//         * 开始执行的方法
//         */
//        @Override
//        public void onStart() {
//            super.onStart();
//        }
//
//        /**
//         * 请求成功的方法
//         * @param i
//         * @param headers
//         * @param bytes
//         */
//        @Override
//        public void onSuccess(int i, Header[] headers, byte[] bytes) {
//
//        }
//
//        /**
//         * 请求失败的方法
//         * @param i
//         * @param headers
//         * @param bytes
//         * @param throwable
//         */
//        @Override
//        public void onFailure(int i, Header[] headers, byte[] bytes, Throwable throwable) {
//
//        }
//
//        /**
//         * 请求完成
//         */
//        @Override
//        public void onFinish() {
//            super.onFinish();
//        }
//    });
}