package com.mx.hb.moon.entity;

/**
 * Created by Kiven on 16/8/19.
 */
public class WXLoginEvent {
    public final String message;
    public final String code;

    public WXLoginEvent(String message, String code) {
        this.message = message;
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public String getCode() {
        return code;
    }
}
