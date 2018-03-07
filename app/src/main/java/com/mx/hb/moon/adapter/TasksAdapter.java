package com.mx.hb.moon.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mx.hb.moon.AdInfo;
import com.mx.hb.moon.R;
import com.mx.hb.moon.application.Options;
import com.mx.hb.moon.base.BaseTools;
import com.mx.hb.moon.entity.DlInfo;
import com.mx.hb.moon.entity.DrInfo;
import com.mx.hb.moon.entity.ViewHolder;
import com.mx.hb.moon.entity.WanpuInfo;
import com.mx.hb.moon.entity.YoumiInfo;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import xz.ax.qr.os.df.AppSummaryObject;

/**
 * Created by Kiven on 16/7/6.
 */
public class TasksAdapter extends BaseAdapter {
    Context context;
    List<Map<String, Object>> list;
    private LayoutInflater inflater = null;

    public TasksAdapter(Context context, List<Map<String, Object>> list) {
        this.context = context;
        this.list = list;
        inflater = LayoutInflater.from(context);
    }

    public void addData(List<Map<String, Object>> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        if (this.list == null) {
            this.list = new ArrayList<>();
        }
        this.list.addAll(list);
    }

    public void reset() {
        if(null != list){
            list.clear();
            notifyDataSetChanged();
        }
    }

    @Override
    public int getCount() {
        return list == null ? 0 : list.size();
    }

    @Override
    public Map<String,Object> getItem(int position) {
        if(list != null && list.size() != 0){
            return list.get(position);
        } else {
            Map<String,Object> map = new HashMap<>();
            return map;
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        View view = convertView;
        if (view == null) {
            view = inflater.inflate(R.layout.item_tasks, null);
            holder = new ViewHolder();
            holder.pointText = (LinearLayout) view.findViewById(R.id.pointText);
            holder.itemIcon = (ImageView) view.findViewById(R.id.itemIcon);
            holder.title = (TextView) view.findViewById(R.id.itemTitle);
            holder.describe = (TextView) view.findViewById(R.id.itemDescribe);
            holder.integral = (TextView) view.findViewById(R.id.itemIntegral);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }
        Resources resources = context.getResources();
        /** 获取position对应的数据 */
        if(list != null || !list.isEmpty()){
            if(getItem(position) != null || !getItem(position).isEmpty()){
                if(getItem(position).get("appSummaryObject") != null){//有米的数据
                    YoumiInfo appSummaryObject = (YoumiInfo) getItem(position).get("appSummaryObject");//有米
                    holder.pointText.setBackgroundResource(R.drawable.item_text_youmi);
                    holder.integral.setTextColor(resources.getColor(R.color.gules));
                    /** 图标 */
                    String iconUrl = appSummaryObject.getIconUrl();
                    /** 名称 */
                    final String appName = appSummaryObject.getAppName();
                    /** 描述获取积分的操作说明 */
                    String describe = appSummaryObject.getAdSlogan();
                    /** 积分 */
                    int integral = appSummaryObject.getPoints();

                    ImageLoader.getInstance().displayImage(iconUrl, holder.itemIcon, Options.getListOptions(R.mipmap.icon_stub_z));
                    holder.title.setText(appName);
                    holder.describe.setText(describe);
                    holder.integral.setText("+ " + BaseTools.pointhangeMoney((double)integral) + "元");
                }
                if(getItem(position).get("adInfo") != null){//万普的数据
                    WanpuInfo adInfo = (WanpuInfo) getItem(position).get("adInfo");//万普
                    holder.pointText.setBackgroundResource(R.drawable.item_text_wanpu);
                    holder.integral.setTextColor(resources.getColor(R.color.wanpu));
                    /** 图标 */
                    String iconUrl = adInfo.getImageUrl();
                    /** 名称 */
                    final String appName = adInfo.getAdName();
                    /** 描述 */
                    String describe = adInfo.getAdText();
                    /** 积分 */
                    int integral = adInfo.getAdPoints();

                    ImageLoader.getInstance().displayImage(iconUrl, holder.itemIcon, Options.getListOptions(R.mipmap.icon_stub_z));
                    holder.title.setText(appName);
                    holder.describe.setText(describe);
                    holder.integral.setText("+ " + BaseTools.pointhangeMoney((double)integral) + "元");
                }
                if(getItem(position).get("drInfo") != null){//点入的数据
                    DrInfo drInfo = (DrInfo) getItem(position).get("drInfo");//点入
                    holder.pointText.setBackgroundResource(R.drawable.item_text_dianru);
                    holder.integral.setTextColor(resources.getColor(R.color.dianru));
                    /** 图标 */
                    String iconUrl = drInfo.getIcon();
                    /** 名称 */
                    final String appName = drInfo.getTitle();
                    /** 描述 */
                    String describe = drInfo.getText2();
                    /** 积分 */
                    String integral = drInfo.getScore();

                    ImageLoader.getInstance().displayImage(iconUrl, holder.itemIcon, Options.getListOptions(R.mipmap.icon_stub_z));
                    holder.title.setText(appName);
                    holder.describe.setText(describe);
                    holder.integral.setText("+ " + BaseTools.pointhangeMoney(Double.parseDouble(integral)) + "元");
                }
                if(getItem(position).get("dlInfo") != null){//点乐的数据
                    DlInfo dlInfo = (DlInfo) getItem(position).get("dlInfo");//点乐
                    holder.pointText.setBackgroundResource(R.drawable.item_text_dianle);
                    holder.integral.setTextColor(resources.getColor(R.color.dianle));
                    /** 图标 */
                    String iconUrl = dlInfo.getIcon();
                    /** 名称 */
                    final String appName = dlInfo.getName();
                    /** 描述 */
                    String describe = dlInfo.getText();
                    /** 积分 */
                    int integral = dlInfo.getNumber();

                    ImageLoader.getInstance().displayImage(iconUrl, holder.itemIcon, Options.getListOptions(R.mipmap.icon_stub_z));
                    holder.title.setText(appName);
                    holder.describe.setText(describe);
                    holder.integral.setText("+ " + BaseTools.pointhangeMoney((double)integral) + "元");
                }
            }
        }
        return view;
    }
}
