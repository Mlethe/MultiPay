package com.mlethe.library.multi.pay.listener;

import androidx.annotation.NonNull;

import com.mlethe.library.multi.pay.MultiMedia;

import org.json.JSONObject;

/**
 * 支付回调
 *
 * @author Mlethe
 */
public interface OnPayActionListener {
    /**
     * 完成
     *
     * @param media       类型
     * @param jsonObject 数据
     */
    void onComplete(@NonNull MultiMedia media, @NonNull JSONObject jsonObject);

    /**
     * 取消
     *
     * @param media 类型
     */
    void onCancel(@NonNull MultiMedia media);

    /**
     * 失败
     *
     * @param media 类型
     */
    void onFailure(@NonNull MultiMedia media, int code);
}
