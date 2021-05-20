package com.mlethe.library.multi.pay.entity;

import android.text.TextUtils;

import com.mlethe.library.multi.pay.MultiMedia;
import com.mlethe.library.multi.pay.Platform;

import org.json.JSONObject;

public class AppIdPlatform implements Platform {
    public String appId = null;
    public String appkey = null;
    public String redirectUrl = null;
    public String fileProvider = null;
    public String agentId = null;
    public String schema = null;
    private MultiMedia media;

    public AppIdPlatform(MultiMedia media) {
        this.media = media;
    }

    public MultiMedia getName() {
        return this.media;
    }

    public void parse(JSONObject var1) {
    }

    public boolean isConfigured() {
        return !TextUtils.isEmpty(this.appId) && !TextUtils.isEmpty(this.appkey);
    }

    public String getAppId() {
        return this.appId;
    }

    public String getAppSecret() {
        return this.appkey;
    }
}
