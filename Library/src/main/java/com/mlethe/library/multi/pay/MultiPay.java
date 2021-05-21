package com.mlethe.library.multi.pay;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.mlethe.library.multi.pay.entity.AppIdPlatform;
import com.mlethe.library.multi.pay.listener.OnPayActionListener;
import com.tencent.mm.opensdk.constants.ConstantsAPI;
import com.tencent.mm.opensdk.modelbase.BaseReq;
import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.tencent.mm.opensdk.modelpay.PayResp;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;
import com.tencent.mobileqq.openpay.api.IOpenApi;
import com.tencent.mobileqq.openpay.api.IOpenApiListener;
import com.tencent.mobileqq.openpay.api.OpenApiFactory;
import com.tencent.mobileqq.openpay.constants.OpenConstants;
import com.tencent.mobileqq.openpay.data.base.BaseResponse;
import com.tencent.mobileqq.openpay.data.pay.PayResponse;
import com.unionpay.UPPayAssistEx;

import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

/**
 * 第三方登录、支付和分享工具管理类
 *
 * @author Mlethe
 * @date 2021/5/19
 */
public final class MultiPay {

    private final Map<MultiMedia, Platform> configs = new HashMap();

    private Context mContext;

    private IWXAPI mWxapi;

    private IOpenApi mOpenApi;

    private MultiMedia mMultiMedia;

    private OnPayActionListener mPayListener;

    private MultiPay() {
        configs.put(MultiMedia.QQ_PAY, new AppIdPlatform(MultiMedia.QQ_PAY));
        configs.put(MultiMedia.WECHAT, new AppIdPlatform(MultiMedia.WECHAT));
        configs.put(MultiMedia.ALIPAY, new AppIdPlatform(MultiMedia.ALIPAY));
    }

    private static final class Holder {
        private static final MultiPay INSTANCE = new MultiPay();
    }

    public static MultiPay getInstance() {
        return Holder.INSTANCE;
    }

    /**
     * 微信
     *
     * @param appid
     * @param secret
     * @return
     */
    public MultiPay setWechat(String appid, String secret) {
        AppIdPlatform platForm = (AppIdPlatform) getPlatForm(MultiMedia.WECHAT);
        platForm.appId = appid;
        platForm.appkey = secret;
        return this;
    }

    /**
     * QQ支付
     *
     * @param appid
     * @param secret
     * @return
     */
    public MultiPay setQQPay(String appid, String secret) {
        AppIdPlatform platForm = (AppIdPlatform) getPlatForm(MultiMedia.QQ_PAY);
        platForm.appId = appid;
        platForm.appkey = secret;
        return this;
    }

    /**
     * 支付宝（授权登录、分享）
     *
     * @param appid
     * @param secret
     * @return
     */
    public MultiPay setAlipay(String appid, String secret) {
        AppIdPlatform platForm = (AppIdPlatform) getPlatForm(MultiMedia.ALIPAY);
        platForm.appId = appid;
        platForm.appkey = secret;
        return this;
    }

    /**
     * 初始化
     *
     * @param context
     */
    public void init(Context context) {
        this.mContext = context.getApplicationContext();
        Platform platForm = getPlatForm(MultiMedia.WECHAT);
        if (platForm != null && platForm.isConfigured()) {
            String appId = platForm.getAppId();
            mWxapi = WXAPIFactory.createWXAPI(mContext, appId, false);
            mWxapi.registerApp(appId);
        }
        platForm = getPlatForm(MultiMedia.QQ_PAY);
        if (platForm != null && platForm.isConfigured()) {
            mOpenApi = OpenApiFactory.getInstance(mContext, platForm.getAppId());
        }
    }

    /**
     * 是否支持支付
     *
     * @param media
     * @return
     */
    public boolean enablePay(MultiMedia media) {
        if (MultiMedia.WECHAT == media) {
            if (mWxapi != null) {
                return mWxapi.isWXAppInstalled();
            }
        } else if (MultiMedia.QQ_PAY == media) {
            if (mOpenApi != null) {
                return mOpenApi.isMobileQQSupportApi(OpenConstants.API_NAME_PAY);
            }
        } else if (MultiMedia.ALIPAY == media) {
            return true;
        } else if (MultiMedia.UNION_PAY == media) {
            return !UPPayAssistEx.checkWalletInstalled(mContext);
        }
        return false;
    }

    protected Platform getPlatForm(MultiMedia media) {
        return configs.get(media);
    }

    /**
     * 设置方式
     *
     * @param media
     */
    protected void setMultiMedia(MultiMedia media) {
        this.mMultiMedia = media;
    }

    /**
     * 设置支付监听
     *
     * @param listener
     */
    protected void setOnPayListener(OnPayActionListener listener) {
        this.mPayListener = listener;
    }

    /**
     * 获取微信对象
     *
     * @return
     */
    protected IWXAPI getIWXAPI() {
        return mWxapi;
    }

    /**
     * 获取QQ对象
     *
     * @return
     */
    protected IOpenApi getIOpenApi() {
        return mOpenApi;
    }

    /**
     * 结果处理
     *
     * @param intent
     */
    public void handleIntent(MultiMedia media, Intent intent) {
        if (MultiMedia.WECHAT == media) {
            IWXAPI api = getIWXAPI();
            if (api != null) {
                api.handleIntent(intent, new EventHandler());
            }
        } else if (MultiMedia.QQ_PAY == media) {
            IOpenApi api = getIOpenApi();
            if (api != null) {
                api.handleIntent(intent, new OpenApiListener());
            }
        }
    }

    /**
     * activity 结果处理
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        MultiMedia media = mMultiMedia;
        try {
            if (MultiMedia.UNION_PAY == media) {
                // 云闪付
                if (requestCode == 10) {
                    // 支付
                    if (resultCode != Activity.RESULT_OK) {
                        if (mPayListener != null) {
                            mPayListener.onFailure(media, -1006);
                        }
                    } else {
                        /*
                         * 支付控件返回字符串:success、fail、cancel 分别代表支付成功，支付失败，支付取消
                         */
                        String str = data.getStringExtra("pay_result");
                        if ("success".equalsIgnoreCase(str)) {
                            JSONObject resultJson = new JSONObject();
                            if (mPayListener != null) {
                                mPayListener.onComplete(media, resultJson);
                            }
                        } else if ("fail".equalsIgnoreCase(str)) {
                            if (mPayListener != null) {
                                mPayListener.onFailure(media, -1007);
                            }
                        } else if ("cancel".equalsIgnoreCase(str)) {
                            if (mPayListener != null) {
                                mPayListener.onCancel(media);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            release();
        }
    }

    /**
     * 释放对象
     */
    public void release() {
        mMultiMedia = null;
        mPayListener = null;
    }

    /**
     * 微信回调处理类
     */
    private class EventHandler implements IWXAPIEventHandler {

        @Override
        public void onReq(BaseReq baseReq) {
            switch (baseReq.getType()) {
                case ConstantsAPI.COMMAND_GETMESSAGE_FROM_WX:
                    break;
                case ConstantsAPI.COMMAND_SHOWMESSAGE_FROM_WX:
                    break;
                default:
                    break;
            }
        }

        @Override
        public void onResp(BaseResp baseResp) {
            int type = baseResp.getType();
            MultiMedia media = MultiMedia.WECHAT;
            try {
                int errCode = baseResp.errCode;
                if (type == ConstantsAPI.COMMAND_SENDAUTH) {
                    // 授权登录
                } else if (type == ConstantsAPI.COMMAND_SENDMESSAGE_TO_WX) {
                    // 分享
                } else if (type == ConstantsAPI.COMMAND_PAY_BY_WX) {
                    // 支付
                    if (errCode == BaseResp.ErrCode.ERR_OK) {
                        // 支付成功
                        PayResp payResp = (PayResp) baseResp;
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("extData", payResp.extData);
                        jsonObject.put("openId", payResp.openId);
                        jsonObject.put("returnKey", payResp.returnKey);
                        jsonObject.put("prepayId", payResp.prepayId);
                        if (mPayListener != null) {
                            mPayListener.onComplete(media, jsonObject);
                        }
                    } else if (errCode == BaseResp.ErrCode.ERR_USER_CANCEL) {
                        // 支付取消
                        if (mPayListener != null) {
                            mPayListener.onCancel(media);
                        }
                    } else if (errCode == BaseResp.ErrCode.ERR_AUTH_DENIED) {
                        // 权限验证失败
                        if (mPayListener != null) {
                            mPayListener.onFailure(media, errCode);
                        }
                    } else if (errCode == BaseResp.ErrCode.ERR_SENT_FAILED) {
                        // 授权失败
                        if (mPayListener != null) {
                            mPayListener.onFailure(media, errCode);
                        }
                    } else {
                        // 未知错误
                        if (mPayListener != null) {
                            mPayListener.onFailure(media, errCode);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                release();
            }
        }
    }

    /**
     * QQ支付回调监听
     */
    private class OpenApiListener implements IOpenApiListener {

        @Override
        public void onOpenResponse(BaseResponse response) {
            try {
                if (response instanceof PayResponse) {
                    PayResponse payResponse = (PayResponse) response;
                    int code = payResponse.retCode;
                    if (payResponse.isSuccess()) {
                        // 支付成功
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("apiName", payResponse.apiName);
                        jsonObject.put("serialNumber", payResponse.serialNumber);
                        jsonObject.put("retMsg", payResponse.retMsg);
                        jsonObject.put("transactionId", payResponse.transactionId);
                        jsonObject.put("payTime", payResponse.payTime);
                        jsonObject.put("callbackUrl", payResponse.callbackUrl);
                        jsonObject.put("totalFee", payResponse.totalFee);
                        jsonObject.put("spData", payResponse.spData);
                        if (mPayListener != null) {
                            mPayListener.onComplete(MultiMedia.QQ_PAY, jsonObject);
                        }
                    } else if (code == -1) {
                        // 用户取消
                        if (mPayListener != null) {
                            mPayListener.onCancel(MultiMedia.QQ_PAY);
                        }
                    } else if (code == -2) {
                        // 登录态超时
                        if (mPayListener != null) {
                            mPayListener.onFailure(MultiMedia.QQ_PAY, code);
                        }
                    } else if (code == -3) {
                        // 重复提交订单
                        if (mPayListener != null) {
                            mPayListener.onFailure(MultiMedia.QQ_PAY, code);
                        }
                    } else {
                        if (mPayListener != null) {
                            mPayListener.onFailure(MultiMedia.QQ_PAY, code);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                release();
            }
        }
    }
}
