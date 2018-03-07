package com.mx.hb.moon.adapter;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mx.hb.moon.R;
import com.mx.hb.moon.entity.ViewHolder;
import com.mx.hb.moon.view.AnimatedExpandableListView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Kiven on 16/6/21.
 */
public class TaskDAdapter extends BaseExpandableListAdapter {
    private Context context;
    private LayoutInflater inflater = null;
    private List<Map<String, Object>> parents;
    private List<List<Map<String, Object>>> childs;
    public TaskDAdapter(Context context) {
        this.context = context;
        inflater = LayoutInflater.from(context);
    }

    public void addData(List<Map<String, Object>> parents, List<List<Map<String, Object>>> childs) {
        if (parents == null || parents.isEmpty()) {
            return;
        }
        if (this.parents == null) {
            this.parents = new ArrayList<>();
        }
        this.parents.addAll(parents);
        if (childs == null || childs.isEmpty()) {
            return;
        }
        if (this.childs == null) {
            this.childs = new ArrayList<>();
        }
        this.childs.addAll(childs);
    }

    public void reset() {
        parents = null;
        childs = null;
    }

    /**
     * 获取一级标签总数
     */
    @Override
    public int getGroupCount() {
        return parents == null ? 0 : parents.size();
    }

    /**
     * 获取一级标签内容
     */
    @Override
    public Object getGroup(int groupPosition) {
        if(parents != null && parents.size() != 0){
            return parents.get(groupPosition);
        }
        return null;
    }

    /**
     * 获取一级标签的ID
     */
    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    /**
     * 二级标签的总数
     */
    @Override
    public int getChildrenCount(int groupPosition) {
        return childs == null ? 0 : childs.get(groupPosition).size();
    }

    /**
     * 二级标签的内容
     */
    @Override
    public Object getChild(int groupPosition, int childPosition) {
        if(childs != null && childs.size() != 0){
            return childs.get(groupPosition).get(childPosition);
        }
        return null;
    }

    /**
     * 获取二级标签的ID
     */
    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    /**
     * 指定位置相应的组视图
     */
    @Override
    public boolean hasStableIds() {
        return true;
    }

    /**
     * 对一级标签进行设置
     */
    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.item_taskdeposit_parent, null);
            holder.ERelativeLayout = (RelativeLayout) convertView.findViewById(R.id.group_layout);
            holder.ETitle = (TextView) convertView.findViewById(R.id.parent_title);
            holder.EMoney = (TextView) convertView.findViewById(R.id.parent_money);
            holder.EState = (TextView) convertView.findViewById(R.id.parent_state);
            holder.EImage = (ImageView) convertView.findViewById(R.id.parent_img);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.ETitle.setText(parents.get(groupPosition).get("type")+"");
        holder.EMoney.setText(parents.get(groupPosition).get("money")+"元");
        holder.EState.setText(parents.get(groupPosition).get("state")+"");

        if(groupPosition % 2 == 1){
            holder.EImage.setBackgroundResource(R.mipmap.group_up);
            holder.ERelativeLayout.setBackgroundResource(R.drawable.btnbg_out_item);
            if(isExpanded){
                holder.ERelativeLayout.setBackgroundResource(R.drawable.btnbg_out_item_e);
                holder.EImage.setBackgroundResource(R.mipmap.group_down);
            }
        } else {
            holder.EImage.setBackgroundResource(R.mipmap.group_up);
            holder.ERelativeLayout.setBackgroundResource(R.drawable.btnbg_out_item);
            if(isExpanded){
                holder.ERelativeLayout.setBackgroundResource(R.drawable.btnbg_out_item_e);
                holder.EImage.setBackgroundResource(R.mipmap.group_down);
            }
        }
        return convertView;
    }

    /**
     * 对二级标签进行设置
     */
    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.item_taskdeposit_child, null);
            holder.ECContent = (TextView) convertView.findViewById(R.id.child_text);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.ECContent.setText(
                "任务编号："+Html.fromHtml("<font>"+childs.get(groupPosition).get(childPosition).get("num")+"</font>")+"\n"+"\n"+
                "任务来源："+Html.fromHtml("<font>"+childs.get(groupPosition).get(childPosition).get("stateName")+"任务</font>")+"\n"+"\n"+
                "完成时间："+Html.fromHtml("<font>"+childs.get(groupPosition).get(childPosition).get("time")+"</font>")
        );
        return convertView;
    }

    /**
     * 当选择子节点的时候，调用该方法
     */
    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}
