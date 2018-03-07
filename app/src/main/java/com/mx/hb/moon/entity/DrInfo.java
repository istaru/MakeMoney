package com.mx.hb.moon.entity;

import java.io.Serializable;

/**
 * Created by Moon on 2016/4/20.
 */
public class DrInfo implements Serializable {
    /** 广告主ID */
    private String cid;
    /** 广告ID */
    private String adid;
    /** 应用简介，用于详情页 */
    private String intro;
    /** icon图标地址 */
    private String icon;
    /** 应用名称 */
    private String title;
    /** 简介下载量，包大小 */
    private String text1;
    /** 简介，用于列表展示 */
    private String text2;
    /**  */
    private String images;
    /** 广告主ID */
    private String url;
    /** 任务步骤描述，以及当前步 */
    private String androidUrl;
    /** 包大小 */
    private String psize;
    /** 广告主ID */
    private String processName;
    private String processName1;
    private String ptype;
    /** 广告图片3张，用于详情页展示 */
    private String image1;
    private String image2;
    private String image3;
    /** 广告激活时间 */
    private String activeTime;
    /** 运行时间 */
    private String runtime;
    /** 当前步骤激条件 */
    private String currNote;
    /** 激活吃说 */
    private String activeNum;
    /** 任务总积分 */
    private String score;

    public String getCid() {
        return cid;
    }

    public void setCid(String cid) {
        this.cid = cid;
    }

    public String getAdid() {
        return adid;
    }

    public void setAdid(String adid) {
        this.adid = adid;
    }

    public String getIntro() {
        return intro;
    }

    public void setIntro(String intro) {
        this.intro = intro;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getText1() {
        return text1;
    }

    public void setText1(String text1) {
        this.text1 = text1;
    }

    public String getText2() {
        return text2;
    }

    public void setText2(String text2) {
        this.text2 = text2;
    }

    public String getImages() {
        return images;
    }

    public void setImages(String images) {
        this.images = images;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getAndroidUrl() {
        return androidUrl;
    }

    public void setAndroidUrl(String androidUrl) {
        this.androidUrl = androidUrl;
    }

    public String getPsize() {
        return psize;
    }

    public void setPsize(String psize) {
        this.psize = psize;
    }

    public String getProcessName() {
        return processName;
    }

    public void setProcessName(String processName) {
        this.processName = processName;
    }

    public String getProcessName1() {
        return processName1;
    }

    public void setProcessName1(String processName1) {
        this.processName1 = processName1;
    }

    public String getPtype() {
        return ptype;
    }

    public void setPtype(String ptype) {
        this.ptype = ptype;
    }

    public String getImage1() {
        return image1;
    }

    public void setImage1(String image1) {
        this.image1 = image1;
    }

    public String getImage2() {
        return image2;
    }

    public void setImage2(String image2) {
        this.image2 = image2;
    }

    public String getImage3() {
        return image3;
    }

    public void setImage3(String image3) {
        this.image3 = image3;
    }

    public String getActiveTime() {
        return activeTime;
    }

    public void setActiveTime(String activeTime) {
        this.activeTime = activeTime;
    }

    public String getRuntime() {
        return runtime;
    }

    public void setRuntime(String runtime) {
        this.runtime = runtime;
    }

    public String getCurrNote() {
        return currNote;
    }

    public void setCurrNote(String currNote) {
        this.currNote = currNote;
    }

    public String getActiveNum() {
        return activeNum;
    }

    public void setActiveNum(String activeNum) {
        this.activeNum = activeNum;
    }

    public String getScore() {
        return score;
    }

    public void setScore(String score) {
        this.score = score;
    }
}
