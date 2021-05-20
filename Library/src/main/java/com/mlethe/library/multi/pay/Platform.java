package com.mlethe.library.multi.pay;

import org.json.JSONObject;

public interface Platform {
    MultiMedia getName();

    void parse(JSONObject jsonObject);

    boolean isConfigured();

    String getAppId();

    String getAppSecret();
}
