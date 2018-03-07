package com.mx.hb.moon.entity;

/**
 * Created by Kiven on 16/8/31.
 */
public class AlipayEvent {
    public final String alipayAccount;
    public final String alipayName;

    public AlipayEvent(String alipayAccount, String alipayName) {
        this.alipayAccount = alipayAccount;
        this.alipayName = alipayName;
    }

    public String getAlipayAccount() {
        return alipayAccount;
    }

    public String getAlipayName() {
        return alipayName;
    }
}
