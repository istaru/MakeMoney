package com.mx.hb.moon.entity;

import java.io.Serializable;

/**
 * Created by Kiven on 16/9/7.
 */
public class YoumiInfo implements Serializable {
    private int adId;//获取广告 id
    private String iconUrl;//图标
    private String appName;//APP的名称
    private String adSlogan;//描述获取积分的操作说明
    private int points;//积分

    public int getAdId() {
        return adId;
    }

    public void setAdId(int adId) {
        this.adId = adId;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getAdSlogan() {
        return adSlogan;
    }

    public void setAdSlogan(String adSlogan) {
        this.adSlogan = adSlogan;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }
}
