package com.mlethe.library.multi.pay;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;

import com.alipay.sdk.app.AuthTask;
import com.alipay.sdk.app.H5PayCallback;
import com.alipay.sdk.app.PayTask;
import com.alipay.sdk.util.H5PayResultModel;
import com.mlethe.library.multi.pay.entity.AliPayResult;
import com.mlethe.library.multi.pay.entity.AuthResult;
import com.mlethe.library.multi.pay.listener.OnPayActionListener;
import com.tencent.mm.opensdk.modelpay.PayReq;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mobileqq.openpay.api.IOpenApi;
import com.tencent.mobileqq.openpay.data.pay.PayApi;
import com.unionpay.UPPayAssistEx;

import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.Map;

/**
 * 支付
 *
 * @author Mlethe
 * @date 2021/5/19
 */
public class PayAction {
    /**
     * 支付方式
     */
    private MultiMedia mMultiMedia;
    /**
     * 支付监听
     */
    private OnPayActionListener mPayListener;
    /**
     * 微信支付商户号
     */
    private String partnerId;
    /**
     * 微信支付预支付交易会话ID
     */
    private String prepayId;
    /**
     * 微信支付、QQ钱包随机字符串
     */
    private String nonceStr;
    /**
     * 微信支付、QQ钱包时间戳
     */
    private long timeStamp;
    /**
     * 微信支付扩展字段
     */
    private String packageValue;
    /**
     * 微信支付、QQ钱包签名
     */
    private String sign;
    /**
     * 微信支付其他参数
     */
    private String extData;
    /**
     * 支付宝订单信息
     */
    private String orderInfo;
    /**
     * 支付宝账户授权业务信息
     */
    private String authInfo;
    /**
     * 支付宝二维码链接（监听事件无效）
     */
    private String qrcode;
    /**
     * 支付宝H5支付链接（WebViewClient重写shouldOverrideUrlLoading()方法）
     */
    private String url;
    /**
     * QQ钱包支付序号,用于标识此次支付
     */
    private String serialNumber;
    /**
     * QQ钱包支付结果回调给urlscheme为callbackScheme的activity
     */
    private String callbackScheme;
    /**
     * QQ钱包手Q公众帐号id.参与支付签名
     */
    private String pubAcc;
    /**
     * QQ钱包支付完成页面，展示给用户的提示语：提醒关注公众帐号
     */
    private String pubAccHint;
    /**
     * QQ钱包支付生成的token_id
     */
    private String tokenId;
    /**
     * QQ钱包签名类型，使用的加密方式，默认为"HMAC-SHA1"
     */
    private String signType = "HMAC-SHA1";
    /**
     * QQ钱包商户号
     */
    private String bargainorId;
    /**
     * 云闪付交易流水号
     */
    private String tn;

    public PayAction() {
    }

    /**
     * 支付方式
     *
     * @param media
     * @return
     */
    public PayAction setPlatform(MultiMedia media) {
        this.mMultiMedia = media;
        return this;
    }

    /**
     * 支付回调
     *
     * @param listener
     * @return
     */
    public PayAction setCallback(OnPayActionListener listener) {
        this.mPayListener = listener;
        return this;
    }

    /**
     * 微信支付、QQ钱包商户号
     *
     * @param partnerId 商户号
     * @return
     */
    public PayAction setPartnerId(String partnerId) {
        this.partnerId = partnerId;
        return this;
    }

    /**
     * 微信支付预支付交易会话ID、QQ钱包支付序号,用于标识此次支付
     *
     * @param prepayId 支付序号
     * @return
     */
    public PayAction setPrepayId(String prepayId) {
        this.prepayId = prepayId;
        return this;
    }

    /**
     * 微信支付、QQ钱包随机字符串
     *
     * @param nonceStr 随机字符串
     * @return
     */
    public PayAction setNonceStr(String nonceStr) {
        this.nonceStr = nonceStr;
        return this;
    }

    /**
     * 微信支付、QQ钱包时间戳
     *
     * @param timeStamp 时间戳
     * @return
     */
    public PayAction setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
        return this;
    }

    /**
     * 微信支付扩展字段
     *
     * @param packageValue
     * @return
     */
    public PayAction setPackageValue(String packageValue) {
        this.packageValue = packageValue;
        return this;
    }

    /**
     * 微信支付、QQ钱包签名
     *
     * @param sign 签名
     * @return
     */
    public PayAction setSign(String sign) {
        this.sign = sign;
        return this;
    }

    /**
     * 微信支付其他参数
     *
     * @param extData
     * @return
     */
    public PayAction setExtData(String extData) {
        this.extData = extData;
        return this;
    }

    /**
     * 支付宝订单信息
     *
     * @param orderInfo
     */
    public PayAction setOrderInfo(String orderInfo) {
        this.orderInfo = orderInfo;
        return this;
    }

    /**
     * 支付宝账户授权业务信息
     *
     * @param authInfo
     * @return
     */
    public PayAction setAuthInfo(String authInfo) {
        this.authInfo = authInfo;
        return this;
    }

    /**
     * 支付宝二维码链接
     *
     * @param qrcode
     * @return
     */
    public PayAction setQrcode(String qrcode) {
        this.qrcode = qrcode;
        return this;
    }

    /**
     * 支付宝H5支付链接（WebViewClient重写shouldOverrideUrlLoading()方法）
     *
     * @param url
     */
    public PayAction setUrl(String url) {
        this.url = url;
        return this;
    }

    /**
     * QQ钱包支付序号,用于标识此次支付
     *
     * @param serialNumber
     * @return
     */
    public PayAction setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
        return this;
    }

    /**
     * QQ钱包支付结果回调给urlscheme为callbackScheme的activity
     *
     * @param callbackScheme
     * @return
     */
    public PayAction setCallbackScheme(String callbackScheme) {
        this.callbackScheme = callbackScheme;
        return this;
    }

    /**
     * QQ钱包手Q公众帐号id.参与支付签名
     *
     * @param pubAcc
     * @return
     */
    public PayAction setPubAcc(String pubAcc) {
        this.pubAcc = pubAcc;
        return this;
    }

    /**
     * QQ钱包支付完成页面，展示给用户的提示语：提醒关注公众帐号
     *
     * @param pubAccHint
     * @return
     */
    public PayAction setPubAccHint(String pubAccHint) {
        this.pubAccHint = pubAccHint;
        return this;
    }

    /**
     * QQ钱包支付生成的token_id
     *
     * @param tokenId
     * @return
     */
    public PayAction setTokenId(String tokenId) {
        this.tokenId = tokenId;
        return this;
    }

    /**
     * QQ钱包签名类型，使用的加密方式，默认为"HMAC-SHA1"
     *
     * @param signType
     * @return
     */
    public PayAction setSignType(String signType) {
        this.signType = signType;
        return this;
    }

    /**
     * QQ钱包商户号
     *
     * @param bargainorId
     */
    public PayAction setBargainorId(String bargainorId) {
        this.bargainorId = bargainorId;
        return this;
    }

    /**
     * 云闪付交易流水号
     *
     * @param tn
     * @return
     */
    public PayAction setTn(String tn) {
        this.tn = tn;
        return this;
    }

    /**
     * 支付
     *
     * @param activity
     * @return 支付宝H5是否拦截 true 已拦截
     */
    public boolean pay(Activity activity) {
        if (MultiMedia.WECHAT == mMultiMedia) {
            // 微信
            IWXAPI api = MultiPay.getInstance().getIWXAPI();
            if (api == null) {
                if (mPayListener != null) {
                    mPayListener.onFailure(mMultiMedia, -1001);
                }
            } else {
                Platform platForm = MultiPay.getInstance().getPlatForm(mMultiMedia);
                MultiPay.getInstance().setOnPayListener(mPayListener);
                PayReq request = new PayReq();
                request.appId = platForm.getAppId();
                request.partnerId = partnerId;
                request.prepayId = prepayId;
                request.packageValue = packageValue;
                request.nonceStr = nonceStr;
                request.timeStamp = String.valueOf(timeStamp);
                request.sign = sign;
                request.extData = extData;
                api.sendReq(request);
            }
            release();
        } else if (MultiMedia.ALIPAY == mMultiMedia) {
            // 支付宝
            if (orderInfo != null && !orderInfo.isEmpty()) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        PayTask alipay = new PayTask(activity);
                        Map<String, String> result = alipay.payV2(orderInfo, true);
                        final AliPayResult payResult = new AliPayResult(result);
                        int status = payResult.getResultStatus();
                        if (status == 9000) {
                            // 订单支付成功
                            JSONObject jsonObject = new JSONObject();
                            try {
                                jsonObject.put("memo", payResult.getMemo());
                                jsonObject.put("result", payResult.getResult());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            if (mPayListener != null) {
                                mPayListener.onComplete(mMultiMedia, jsonObject);
                            }
                        } else {
                            /**
                             * 8000：正在处理中，支付结果未知（有可能已经支付成功），请查询商户订单列表中订单的支付状态
                             * 4000：订单支付失败
                             * 5000：重复请求
                             * 6001：用户中途取消
                             * 6002：网络连接出错
                             * 6004：支付结果未知（有可能已经支付成功），请查询商户订单列表中订单的支付状态
                             * 其它支付错误
                             */
                            if (mPayListener != null) {
                                mPayListener.onFailure(mMultiMedia, status);
                            }
                        }
                        release();
                    }
                }).start();
            } else if (qrcode != null && !qrcode.isEmpty()) {
                // 支付宝二维码链接支付
                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    Uri uri = Uri.parse("alipays://platformapi/startapp?saId=10000007&qrcode=" + URLEncoder.encode(qrcode, "UTF-8"));
                    intent.setData(uri);
                    activity.startActivity(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                    if (mPayListener != null) {
                        mPayListener.onFailure(mMultiMedia, -1002);
                    }
                } finally {
                    release();
                }
            } else if (url != null && !url.isEmpty()) {
                // H5唤起支付宝支付
                PayTask payTask = new PayTask(activity);
                return payTask.payInterceptorWithUrl(url, true, new H5PayCallback() {
                    @Override
                    public void onPayResult(H5PayResultModel result) {
                        int code = Integer.parseInt(result.getResultCode());
                        if (code == 9000) {
                            // 订单支付成功
                            JSONObject jsonObject = new JSONObject();
                            try {
                                jsonObject.put("returnUrl", result.getReturnUrl());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            if (mPayListener != null) {
                                mPayListener.onComplete(mMultiMedia, jsonObject);
                            }
                        } else {
                            /**
                             * 8000：正在处理中，支付结果未知（有可能已经支付成功），请查询商户订单列表中订单的支付状态
                             * 4000：订单支付失败
                             * 5000：重复请求
                             * 6001：用户中途取消
                             * 6002：网络连接出错
                             * 6004：支付结果未知（有可能已经支付成功），请查询商户订单列表中订单的支付状态
                             * 其它支付错误
                             */
                            if (mPayListener != null) {
                                mPayListener.onFailure(mMultiMedia, code);
                            }
                        }
                        release();
                    }
                });
            } else if (authInfo != null && !authInfo.isEmpty()) {
                // 支付宝账户授权业务信息
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        AuthTask alipay = new AuthTask(activity);
                        Map<String, String> result = alipay.authV2(orderInfo, true);
                        final AuthResult authResult = new AuthResult(result, true);
                        int status = authResult.getResultStatus();
                        int code = authResult.getResultCode();
                        if (status == 9000 && code == 200) {
                            // 订单支付成功
                            JSONObject jsonObject = new JSONObject();
                            try {
                                jsonObject.put("memo", authResult.getMemo());
                                jsonObject.put("result", authResult.getResult());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            if (mPayListener != null) {
                                mPayListener.onComplete(mMultiMedia, jsonObject);
                            }
                        } else {
                            if (mPayListener != null) {
                                mPayListener.onFailure(mMultiMedia, code);
                            }
                        }
                        release();
                    }
                }).start();
            } else {
                if (mPayListener != null) {
                    mPayListener.onFailure(mMultiMedia, -1003);
                }
                release();
            }
        } else if (MultiMedia.QQ_PAY == mMultiMedia) {
            // QQ钱包
            IOpenApi api = MultiPay.getInstance().getIOpenApi();
            if (api == null) {
                if (mPayListener != null) {
                    mPayListener.onFailure(mMultiMedia, -1001);
                }
            } else {
                Platform platForm = MultiPay.getInstance().getPlatForm(mMultiMedia);
                PayApi request = new PayApi();
                request.appId = platForm.getAppId();
                request.serialNumber = serialNumber;
                request.callbackScheme = callbackScheme;
                request.tokenId = tokenId;
                request.pubAcc = pubAcc;
                request.pubAccHint = pubAccHint;
                request.nonce = nonceStr;
                request.timeStamp = timeStamp;
                request.bargainorId = bargainorId;
                request.sig = sign;
                request.sigType = signType;
                if (request.checkParams()) {
                    MultiPay.getInstance().setOnPayListener(mPayListener);
                    api.execApi(request);
                } else {
                    if (mPayListener != null) {
                        mPayListener.onFailure(mMultiMedia, -1004);
                    }
                }
            }
            release();
        } else if (MultiMedia.UNION_PAY == mMultiMedia) {
            // 云闪付
            if (tn != null && !tn.isEmpty()) {
                MultiPay.getInstance().setMultiMedia(mMultiMedia);
                MultiPay.getInstance().setOnPayListener(mPayListener);
                UPPayAssistEx.startPay(activity, null, null, tn, "00");
            } else {
                if (mPayListener != null) {
                    mPayListener.onFailure(mMultiMedia, -1005);
                }
            }
            release();
        }
        return false;
    }

    private void release() {
        mMultiMedia = null;
        mPayListener = null;
    }
}
