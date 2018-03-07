package com.mx.hb.moon.entity;

import android.graphics.Bitmap;

import java.io.Serializable;

/**
 * Created by Moon on 2016/6/3.
 */
public class WanpuInfo implements Serializable {
    private String adId;//广告id
    private String adName;//广告标语
    private String adText;// 获取广告标语
    private Bitmap adIcon;//广告图标(48*48像素)
    private int adPoints;//广告积分
    private String description;//应用描述
    private String version;//程序版本
    private String fileSize;//安装包大小
    private String provider;//作者
    private String imageUrl;//未知
    private String[] imageUrls;// 获取 app 的截图地址列表
    private String adPackage;//广告应用包名
    private String action;//安装状态
    private String appType;//应用类型

    public String getAdId() {
        return adId;
    }

    public void setAdId(String adId) {
        this.adId = adId;
    }

    public String getAdName() {
        return adName;
    }

    public void setAdName(String adName) {
        this.adName = adName;
    }

    public String getAdText() {
        return adText;
    }

    public void setAdText(String adText) {
        this.adText = adText;
    }

    public Bitmap getAdIcon() {
        return adIcon;
    }

    public void setAdIcon(Bitmap adIcon) {
        this.adIcon = adIcon;
    }

    public int getAdPoints() {
        return adPoints;
    }

    public void setAdPoints(int adPoints) {
        this.adPoints = adPoints;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getFileSize() {
        return fileSize;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setFileSize(String fileSize) {
        this.fileSize = fileSize;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String[] getImageUrls() {
        return imageUrls;
    }

    public void setImageUrls(String[] imageUrls) {
        this.imageUrls = imageUrls;
    }

    public String getAdPackage() {
        return adPackage;
    }

    public void setAdPackage(String adPackage) {
        this.adPackage = adPackage;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getAppType() {
        return appType;
    }

    public void setAppType(String appType) {
        this.appType = appType;
    }
}
