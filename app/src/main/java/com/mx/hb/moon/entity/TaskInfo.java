package com.mx.hb.moon.entity;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Created by Kiven on 16/7/5.
 */
public class TaskInfo implements Serializable {
    private String id;//广告Id
    private String appName;//APP的名称
    private String packageName;//APP包名
    private String versionCode;//APP版本号
    private String appIcon;//APP的图标地址
    private String appSize;//APP的大小
    private float points;//广告积分
    private int status;//广告的完成状态
    private int dStatus;//广告的下载状态
    private String steps;//任务步骤指引流程
    private List<Map<String,Object>> listMap;//深度任务列表
}
