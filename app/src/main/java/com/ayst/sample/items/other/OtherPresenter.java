package com.ayst.sample.items.other;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.PowerManager;
import android.util.Log;
import android.view.View;

import com.ayst.utils.AppUtil;

import cn.trinea.android.common.util.ShellUtils;

public class OtherPresenter {
    private static final String TAG = "OtherPresenter";

    private Context mContext;
    private IOtherView mOtherView;
    private PowerManager mPowerManager;
    private PowerManager.WakeLock mWakeLock;

    public OtherPresenter(Context context, IOtherView view) {
        mContext = context;
        mOtherView = view;
    }

    /**
     * root权限测试
     */
    public void root() {
        ShellUtils.CommandResult result = ShellUtils.execCommand("ls -l", true);

        Log.i(TAG, "execute, success message: " + result.successMsg + ", error message: " + result.errorMsg);

        mOtherView.updateRootResult(result.errorMsg.isEmpty());
    }

    /**
     * 静默安装
     * @param path
     * @return
     */
    public boolean silentInstall(String path) {
        return SilentInstall.install(mContext, path);
    }

    /**
     * 重启
     */
    public void reboot() {
        AppUtil.reboot(mContext);
    }

    /**
     * 关机
     */
    public void shutdown() {
        AppUtil.shutdown(mContext);
    }

    /**
     * 亮屏
     */
    public void screenOn() {
        if (null == mPowerManager) {
            mPowerManager = ((PowerManager) mContext.getSystemService(Context.POWER_SERVICE));
            mWakeLock = mPowerManager.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK
                    | PowerManager.ON_AFTER_RELEASE, "Sample:WakeLock");
        }
        mWakeLock.acquire();
    }

    /**
     * 灭屏
     */
    public void screenOff() {
        if (null == mPowerManager) {
            mPowerManager = ((PowerManager) mContext.getSystemService(Context.POWER_SERVICE));
            mWakeLock = mPowerManager.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK
                    | PowerManager.ON_AFTER_RELEASE, "Sample:WakeLock");
        }
        mWakeLock.release();
    }

    /**
     * 全屏显示，隐藏状态栏与导航栏
     * @param activity
     */
    public void fullScreen(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            activity.getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        } else {
            activity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
        }
    }

    /**
     * 退出全屏，显示状态栏与导航栏
     * @param activity
     */
    public void exitFullScreen(Activity activity) {
        activity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
    }

    /**
     * 全局隐藏状态栏与导航栏
     */
    public void hideSystemBar() {
        Intent intent = new Intent();
        intent.setAction("android.intent.action.SYSTEM_BAR_HIDE");
        mContext.sendBroadcast(intent);
    }

    /**
     * 全局显示状态栏与导航栏
     */
    public void showSystemBar() {
        Intent intent = new Intent();
        intent.setAction("android.intent.action.SYSTEM_BAR_SHOW");
        mContext.sendBroadcast(intent);
    }
}
