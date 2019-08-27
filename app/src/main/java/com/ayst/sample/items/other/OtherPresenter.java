package com.ayst.sample.items.other;

import android.app.Activity;
import android.content.Context;
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

    public void root() {
        ShellUtils.CommandResult result = ShellUtils.execCommand("ls -l", true);

        Log.i(TAG, "execute, success message: " + result.successMsg + ", error message: " + result.errorMsg);

        mOtherView.updateRootResult(result.errorMsg.isEmpty());
    }

    public boolean silentInstall(String path) {
        return SilentInstall.install(mContext, path);
    }

    public void reboot() {
        AppUtil.reboot(mContext);
    }

    public void shutdown() {
        AppUtil.shutdown(mContext);
    }

    public void screenOn() {
        if (null == mPowerManager) {
            mPowerManager = ((PowerManager) mContext.getSystemService(Context.POWER_SERVICE));
            mWakeLock = mPowerManager.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK
                    | PowerManager.ON_AFTER_RELEASE, "Sample:WakeLock");
        }
        mWakeLock.acquire();
    }

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
            // 全屏显示，隐藏状态栏和导航栏，拉出状态栏和导航栏显示一会儿后消失
            activity.getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        } else {
            // 全屏显示，隐藏状态栏
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
}
