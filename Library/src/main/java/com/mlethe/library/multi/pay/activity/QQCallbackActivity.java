package com.mlethe.library.multi.pay.activity;

import android.app.Activity;
import android.os.Bundle;

import com.mlethe.library.multi.pay.MultiMedia;
import com.mlethe.library.multi.pay.MultiPay;

/**
 * QQ支付回调
 *
 * @author Mlethe
 */
public abstract class QQCallbackActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MultiPay.getInstance().handleIntent(MultiMedia.QQ_PAY, getIntent());
        finish();
    }
}
