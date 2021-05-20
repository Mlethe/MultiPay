package com.mlethe.library.multi.demo;

import android.app.Application;

import com.mlethe.library.multi.pay.MultiPay;

/**
 * @author Mlethe
 * @date 2021/5/20
 */
public class DemoApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        MultiPay.getInstance()
                .init(this);
    }
}
