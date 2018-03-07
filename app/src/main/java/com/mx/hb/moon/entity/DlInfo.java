package com.mx.hb.moon.entity;

import java.io.Serializable;

/**
 * Created by Moon on 2016/4/20.
 */
public class DlInfo implements Serializable {
    /** 广告icon图片的存放地址 */
    private String icon;

    /** 广告简介 */
    private String text;

    /** 广告包名*/
    private String packName;

    /** 广告描述 */
    private String description;

    /** 广告名字 */
    private String name;

    /** 广告类型(task_count=-1表示没有深度任务,大于零表示有深度任务) */
    private int taskCount;

    /** 广告积分 */
    private int number;

    /** 广告版本号 */
    private String ver;

    /** 广告安装包大小 */
    private String size;

    /** 深度任务列表 */
    private String tasks;//

    /** 总下载数 */
    private String allDownCount;

    /** 任务提示信息 */
    private String setupTips;

    /** 广告详情页的图片 */
    private String thumbnail;//

    /** 广告类型（类型对应关系见下表） */
    private String adType;

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getPackName() {
        return packName;
    }

    public void setPackName(String packName) {
        this.packName = packName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getTaskCount() {
        return taskCount;
    }

    public void setTaskCount(int taskCount) {
        this.taskCount = taskCount;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public String getVer() {
        return ver;
    }

    public void setVer(String ver) {
        this.ver = ver;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getTasks() {
        return tasks;
    }

    public void setTasks(String tasks) {
        this.tasks = tasks;
    }

    public String getAllDownCount() {
        return allDownCount;
    }

    public void setAllDownCount(String allDownCount) {
        this.allDownCount = allDownCount;
    }

    public String getSetupTips() {
        return setupTips;
    }

    public void setSetupTips(String setupTips) {
        this.setupTips = setupTips;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public String getAdType() {
        return adType;
    }

    public void setAdType(String adType) {
        this.adType = adType;
    }
}
