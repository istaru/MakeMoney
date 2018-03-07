package com.mx.hb.moon.entity;

import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * Created by Moon on 2016/4/20.
 */
public class ViewHolder {
    /**
     * RelativeLayout
     */
    public RelativeLayout relativeLayout;
    /**
     * 显示积分的布局
     */
    public LinearLayout pointText;
    /**
     * Id
     */
    public int id;
    /**
     * 标题
     */
    public TextView title;
    /**
     * 描述
     */
    public TextView describe;
    /**
     * 积分(数字)
     */
    public TextView integral;
    /**
     * 积分(文字)
     */
    public TextView point;
    /**
     * 图标
     */
    public ImageView itemIcon;
    /**
     *任务状态
     */
    public TextView state;



    /**
     * ExpandableListView RelativeLayout
     */
    public RelativeLayout ERelativeLayout;
    /**
     * ExpandableListView title
     */
    public TextView ETitle;
    /**
     * ExpandableListView money
     */
    public TextView EMoney;
    /**
     * ExpandableListView state
     */
    public TextView EState;
    /**
     * ExpandableListView iamge
     */
    public ImageView EImage;

    /**
     * 子元素内容
     */
    public TextView ECContent;
}
