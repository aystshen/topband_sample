package com.ayst.sample.items.androidx;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import com.ayst.sample.items.watchdog.Mcu;
import com.ayst.utils.AppUtil;

public class AndroidXPresenter {
    private static final String TAG = "AndroidXPresenter";

    private static final String ANDROIDX_PACKAGE_NAME = "com.ayst.androidx";
    private static final String RECEIVER_PERMISSION = "com.ayst.androidx.permission.SELF_BROADCAST";

    private static final String ACTION_RUN_ALL = "com.topband.androidx.ACTION_RUN_ALL";
    private static final String ACTION_4G_KEEP_LIVE = "com.topband.androidx.ACTION_4G_KEEP_LIVE";
    private static final String ACTION_WATCHDOG = "com.topband.androidx.ACTION_WATCHDOG";

    private static final String EXTRA_ACTION = "action";
    private static final String EXTRA_ACTION_OPEN = "open";
    private static final String EXTRA_ACTION_CLOSE = "close";
    private static final String EXTRA_ACTION_CONFIG = "config";

    private Context mContext;
    private IAndroidXView mAndroidXView;
    private Mcu mMcu;

    public AndroidXPresenter(Context context, IAndroidXView view) {
        mContext = context;
        mAndroidXView = view;
        mMcu = new Mcu(context);
    }

    public void start() {
        mAndroidXView.updateAndroidX4GState(TextUtils.equals("1",
                AppUtil.getProperty("persist.androidx.4g_keep_live", "0")));
        mAndroidXView.updateAndroidXWatchdogState(mMcu.watchdogIsOpen());
        mAndroidXView.updateAndroidXWatchdogTimeout(mMcu.getWatchdogDuration());
    }

    public void stop() {
    }

    /**
     * 打开、关闭4G保活服务
     * @param on
     */
    public void toggle4GKeepLive(boolean on) {
        Intent intent = new Intent();
        intent.setAction(ACTION_4G_KEEP_LIVE);
        intent.putExtra(EXTRA_ACTION, on ? EXTRA_ACTION_OPEN : EXTRA_ACTION_CLOSE);
        intent.setPackage(ANDROIDX_PACKAGE_NAME);
        mContext.sendBroadcast(intent, RECEIVER_PERMISSION);
    }

    /**
     * 打开、关闭看门狗
     * @param on
     */
    public void toggleWatchdog(boolean on) {
        Intent intent = new Intent();
        intent.setAction(ACTION_WATCHDOG);
        intent.putExtra(EXTRA_ACTION, on ? EXTRA_ACTION_OPEN : EXTRA_ACTION_CLOSE);
        intent.setPackage(ANDROIDX_PACKAGE_NAME);
        mContext.sendBroadcast(intent, RECEIVER_PERMISSION);
    }

    /**
     * 设置看门狗超时时长
     * @param timeout
     */
    public void setWatchdogTimeout(int timeout) {
        Intent intent = new Intent();
        intent.setAction(ACTION_WATCHDOG);
        intent.putExtra(EXTRA_ACTION, EXTRA_ACTION_CONFIG);
        Bundle bundle = new Bundle();
        bundle.putInt("timeout", timeout);
        intent.putExtras(bundle);
        intent.setPackage(ANDROIDX_PACKAGE_NAME);
        mContext.sendBroadcast(intent, RECEIVER_PERMISSION);

        mAndroidXView.updateAndroidXWatchdogTimeout(timeout);
    }
}
