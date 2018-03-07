package com.mx.hb.moon.view;

import android.app.Activity;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.mx.hb.moon.R;
import com.mx.hb.moon.adapter.ProvinceWheelAdapter;
import com.mx.hb.moon.base.BaseTools;
import com.mx.hb.moon.entity.WheelInfo;
import com.mx.hb.moon.listener.MyOnWheelChangedListener;
import com.mx.hb.moon.listener.OnAddressChangeListener;

import java.util.List;


public class ChooseAddressWheel implements MyOnWheelChangedListener {

    MyWheelView wheelView;
    TextView cancelBtn,confirmBtn;

    private Activity context;
    private View parentView;
    private PopupWindow popupWindow = null;
    private WindowManager.LayoutParams layoutParams = null;
    private LayoutInflater layoutInflater = null;

    private List<WheelInfo> province = null;

    private OnAddressChangeListener onAddressChangeListener = null;

    public ChooseAddressWheel(Activity context) {
        this.context = context;
        init();
    }

    private void init() {
        layoutParams = context.getWindow().getAttributes();
        layoutInflater = context.getLayoutInflater();
        initView();
        initPopupWindow();
    }

    private void initView() {
        parentView = layoutInflater.inflate(R.layout.choose_city_layout, null);
        wheelView = (MyWheelView) parentView.findViewById(R.id.province_wheel);
        cancelBtn = (TextView) parentView.findViewById(R.id.cancel_button);
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancel();
            }
        });
        confirmBtn = (TextView) parentView.findViewById(R.id.confirm_button);
        confirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirm();
            }
        });
        wheelView.setSoundEffectsEnabled(true);
        wheelView.setVisibleItems(7);
        wheelView.addChangingListener(this);
    }

    private void initPopupWindow() {
        popupWindow = new PopupWindow(parentView, WindowManager.LayoutParams.MATCH_PARENT, (int) (BaseTools.getWindowsHeight(context)) / 3);
        popupWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        popupWindow.setAnimationStyle(R.style.AnimBottom);
        // 设置窗体为透明效果
        ColorDrawable cd = new ColorDrawable(0x000000);
        popupWindow.setBackgroundDrawable(cd);
        popupWindow.setOutsideTouchable(false);
        popupWindow.setFocusable(true);
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            public void onDismiss() {
                layoutParams.alpha = 1.0f;
                context.getWindow().setAttributes(layoutParams);
                popupWindow.dismiss();
            }
        });
    }

    private void bindData() {
        wheelView.setViewAdapter(new ProvinceWheelAdapter(context, province));
    }

    @Override
    public void onChanged(MyWheelView wheel, int oldValue, int newValue) {

    }

    public void show(View v) {
        layoutParams.alpha = 0.8f;
        context.getWindow().setAttributes(layoutParams);
        popupWindow.showAtLocation(v, Gravity.BOTTOM, 0, 0);
    }

    public void setProvince(List<WheelInfo> province) {
        this.province = province;
        bindData();
    }

    public void confirm() {
        if (onAddressChangeListener != null) {
            int provinceIndex = wheelView.getCurrentItem();
            String common = null;
            if (province != null && province.size() > provinceIndex) {
                WheelInfo str = province.get(provinceIndex);
                common = str.common;
            }
            onAddressChangeListener.onAddressChange(common);
        }
        cancel();
    }

    public void cancel() {
        popupWindow.dismiss();
    }

    public void setOnAddressChangeListener(OnAddressChangeListener onAddressChangeListener) {
        this.onAddressChangeListener = onAddressChangeListener;
    }
}