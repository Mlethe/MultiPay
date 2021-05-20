package com.mlethe.library.multi.pay.entity;

import android.text.TextUtils;

import java.util.Map;

/**
 * 支付宝支付返回结果
 *
 * @author Mlethe
 */
public class AliPayResult {
    /**
     * 判断resultStatus 为9000则代表支付成功
     */
    private int resultStatus;
    private String result;
    private String memo;

    public AliPayResult(Map<String, String> rawResult) {
        if (rawResult == null) {
            return;
        }

        for (String key : rawResult.keySet()) {
            if (TextUtils.equals(key, "resultStatus")) {
                String val = rawResult.get(key);
                if (val == null) {
                    resultStatus = -1;
                } else {
                    try {
                        resultStatus = Integer.valueOf(val);
                    } catch (Exception e) {
                        e.printStackTrace();
                        resultStatus = -1;
                    }
                }
            } else if (TextUtils.equals(key, "result")) {
                result = rawResult.get(key);
            } else if (TextUtils.equals(key, "memo")) {
                memo = rawResult.get(key);
            }
        }
    }

    @Override
    public String toString() {
        return "resultStatus={" + resultStatus + "};memo={" + memo
                + "};result={" + result + "}";
    }

    /**
     * @return the resultStatus
     */
    public int getResultStatus() {
        return resultStatus;
    }

    /**
     * @return the memo
     */
    public String getMemo() {
        return memo;
    }

    /**
     * @return the result
     */
    public String getResult() {
        return result;
    }
}
