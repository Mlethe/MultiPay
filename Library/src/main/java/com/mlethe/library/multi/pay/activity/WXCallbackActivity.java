package com.mlethe.library.multi.pay.activity;

import android.app.Activity;
import android.os.Bundle;

import com.mlethe.library.multi.pay.MultiMedia;
import com.mlethe.library.multi.pay.MultiPay;

/**
 * 微信支付、授权、分享回调
 *
 * @author Mlethe
 */
public abstract class WXCallbackActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MultiPay.getInstance().handleIntent(MultiMedia.WECHAT, getIntent());
        finish();
    }
}
