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

    private WeakReference<MultiMedia> mMultiMedia;

    private WeakReference<OnPayActionListener> mPayListener;

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
        this.mMultiMedia = new WeakReference<>(media);
    }

    /**
     * 设置支付监听
     *
     * @param listener
     */
    protected void setOnPayListener(OnPayActionListener listener) {
        this.mPayListener = new WeakReference<>(listener);
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
        if (mMultiMedia == null) {
            if (mPayListener != null) {
                mPayListener.clear();
                mPayListener = null;
            }
            return;
        }
        MultiMedia media = mMultiMedia.get();
        if (MultiMedia.UNION_PAY == media) {
            // 云闪付
            if (mPayListener == null) {
                if (mMultiMedia != null) {
                    mMultiMedia.clear();
                    mMultiMedia = null;
                }
                return;
            }
            OnPayActionListener payListener = mPayListener.get();
            if (requestCode == 10) {
                // 支付
                if (resultCode != Activity.RESULT_OK) {
                    if (payListener != null) {
                        payListener.onFailure(media, -1006);
                        mPayListener.clear();
                        mPayListener = null;
                    }
                    if (mMultiMedia != null) {
                        mMultiMedia.clear();
                        mMultiMedia = null;
                    }
                    return;
                }
                /**
                 * 支付控件返回字符串:success、fail、cancel 分别代表支付成功，支付失败，支付取消
                 */
                String str = data.getStringExtra("pay_result");
                if ("success".equalsIgnoreCase(str)) {
                    JSONObject resultJson = new JSONObject();
                    if (payListener != null) {
                        payListener.onComplete(media, resultJson);
                    }
                } else if ("fail".equalsIgnoreCase(str)) {
                    if (payListener != null) {
                        payListener.onFailure(media, -1007);
                    }
                } else if ("cancel".equalsIgnoreCase(str)) {
                    if (payListener != null) {
                        payListener.onCancel(media);
                    }
                }
            }
        }
        if (mPayListener != null) {
            mPayListener.clear();
            mPayListener = null;
        }
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
                    /*if (mOauthListener == null) {
                        return;
                    }
                    if (errCode == BaseResp.ErrCode.ERR_OK) {
                        // 授权成功
                        SendAuth.Resp sendResp = (SendAuth.Resp) baseResp;
                        if (isGetUserInfo) {
                            isGetUserInfo = false;
                            getUserInfo(sendResp);
                        } else {
                            JSONObject jsonObject = new JSONObject();
                            jsonObject.put("code", getCode(sendResp));
                            mOauthListener.onComplete(Platform.WECHAT, jsonObject);
                            mOauthListener = null;
                        }
                    } else if (errCode == BaseResp.ErrCode.ERR_USER_CANCEL) {
                        // 授权取消
                        mOauthListener.onCancel(Platform.WECHAT);
                        mOauthListener = null;
                    } else if (errCode == BaseResp.ErrCode.ERR_AUTH_DENIED) {
                        // 权限验证失败
                        mOauthListener.onFail(Platform.WECHAT);
                        mOauthListener = null;
                    } else if (errCode == BaseResp.ErrCode.ERR_SENT_FAILED) {
                        // 授权失败
                        mOauthListener.onFail(Platform.WECHAT);
                        mOauthListener = null;
                    } else {
                        // 未知错误
                        mOauthListener.onFail(Platform.WECHAT);
                        mOauthListener = null;
                    }*/
                } else if (type == ConstantsAPI.COMMAND_SENDMESSAGE_TO_WX) {
                    // 分享
                    /*ShareActionListener listener = WechatShare.getShareActionListener();
                    if (listener == null) {
                        return;
                    }
                    if (errCode == BaseResp.ErrCode.ERR_OK) {
                        // 发送成功
                        listener.onComplete(Platform.WECHAT);
                    } else if (errCode == BaseResp.ErrCode.ERR_USER_CANCEL) {
                        // 发送取消
                        listener.onCancel(Platform.WECHAT);
                    } else if (errCode == BaseResp.ErrCode.ERR_AUTH_DENIED) {
                        // 权限验证失败
                        listener.onFail(Platform.WECHAT);
                    } else if (errCode == BaseResp.ErrCode.ERR_SENT_FAILED) {
                        // 发送失败
                        listener.onFail(Platform.WECHAT);
                    } else {
                        // 未知错误
                        listener.onFail(Platform.WECHAT);
                    }
                    WechatShare.setShareActionListener(null);*/
                } else if (type == ConstantsAPI.COMMAND_PAY_BY_WX) {
                    // 支付
                    if (mPayListener == null) {
                        return;
                    }
                    OnPayActionListener payListener = mPayListener.get();
                    if (errCode == BaseResp.ErrCode.ERR_OK) {
                        // 支付成功
                        PayResp payResp = (PayResp) baseResp;
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("extData", payResp.extData);
                        jsonObject.put("openId", payResp.openId);
                        jsonObject.put("returnKey", payResp.returnKey);
                        jsonObject.put("prepayId", payResp.prepayId);
                        payListener.onComplete(media, jsonObject);
                    } else if (errCode == BaseResp.ErrCode.ERR_USER_CANCEL) {
                        // 支付取消
                        payListener.onCancel(media);
                    } else if (errCode == BaseResp.ErrCode.ERR_AUTH_DENIED) {
                        // 权限验证失败
                        payListener.onFailure(media, errCode);
                    } else if (errCode == BaseResp.ErrCode.ERR_SENT_FAILED) {
                        // 授权失败
                        payListener.onFailure(media, errCode);
                    } else {
                        // 未知错误
                        payListener.onFailure(media, errCode);
                    }
                    mPayListener.clear();
                    mPayListener = null;
                }
            } catch (Exception e) {
                e.printStackTrace();
                if (mPayListener != null) {
                    mPayListener.clear();
                    mPayListener = null;
                }
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
                    if (mPayListener == null) {
                        return;
                    }
                    OnPayActionListener payListener = mPayListener.get();
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
                        payListener.onComplete(MultiMedia.QQ_PAY, jsonObject);
                    } else if (code == -1) {
                        // 用户取消
                        payListener.onCancel(MultiMedia.QQ_PAY);
                    } else if (code == -2) {
                        // 登录态超时
                        payListener.onFailure(MultiMedia.QQ_PAY, code);
                    } else if (code == -3) {
                        // 重复提交订单
                        payListener.onFailure(MultiMedia.QQ_PAY, code);
                    } else {
                        payListener.onFailure(MultiMedia.QQ_PAY, code);
                    }
                    mPayListener.clear();
                    mPayListener = null;
                }
            } catch (Exception e) {
                e.printStackTrace();
                if (mPayListener != null) {
                    mPayListener.clear();
                    mPayListener = null;
                }
            }
        }
    }
}
