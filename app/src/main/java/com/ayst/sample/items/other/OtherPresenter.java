package com.ayst.sample.items.other;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.PowerManager;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.ayst.utils.AppUtils;

import java.lang.reflect.Method;

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
        activeAdmin(context);
    }

    public void start() {
        registerPackageChangeBroadcast();
    }

    public void stop() {
        unregisterPackageChangeBroadcast();
    }

    public void openTcpAdb() {
        AppUtils.setProperty("service.adb.tcp.port", "5555");

        ShellUtils.CommandResult result1 = ShellUtils.execCommand("stop adbd", true);
        ShellUtils.CommandResult result2 = ShellUtils.execCommand("start adbd", true);

        mOtherView.updateTcpAdbResult(result1.errorMsg.isEmpty() && result2.errorMsg.isEmpty());
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
        AppUtils.reboot(mContext);
    }

    /**
     * 关机
     */
    public void shutdown() {
        AppUtils.shutdown(mContext);
    }

    /**
     * 恢复出厂设置
     */
    public void factoryReset() {
        Intent intent = new Intent("android.intent.action.MASTER_CLEAR");
        intent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
        intent.putExtra("android.intent.extra.REASON", "MasterClearConfirm");
        mContext.sendBroadcast(intent);
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

        Toast.makeText(mContext, "The screen has been turned on!",
                Toast.LENGTH_SHORT).show();
    }

    /**
     * 灭屏
     * 需要到设置->安全性和位置信息->设备管理应用中勾选Sample应用
     */
    public void screenOff() {
        if (null == mPowerManager) {
            mPowerManager = ((PowerManager) mContext.getSystemService(Context.POWER_SERVICE));
            mWakeLock = mPowerManager.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK
                    | PowerManager.ON_AFTER_RELEASE, "Sample:WakeLock");
        }
        mWakeLock.release();

        DevicePolicyManager policyManager = (DevicePolicyManager) mContext.getSystemService(Context.DEVICE_POLICY_SERVICE);
        ComponentName adminReceiver = new ComponentName(mContext, AdminReceiver.class);
        boolean admin = policyManager.isAdminActive(adminReceiver);
        if (admin) {
            policyManager.lockNow();
        } else {
            Toast.makeText(mContext, "No device admin permissions",
                    Toast.LENGTH_SHORT).show();
        }
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

    @SuppressLint("PrivateApi")
    private static void activeAdmin(Context context) {
        ComponentName adminReceiver = new ComponentName(context, AdminReceiver.class);
        try {
            DevicePolicyManager dpm = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
            Method setActiveAdmin = dpm.getClass().getDeclaredMethod("setActiveAdmin", ComponentName.class, boolean.class);
            setActiveAdmin.setAccessible(true);
            setActiveAdmin.invoke(dpm, adminReceiver, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void registerPackageChangeBroadcast() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
        intentFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        intentFilter.addAction(Intent.ACTION_PACKAGE_REPLACED);
        intentFilter.addDataScheme("package");
        mContext.registerReceiver(mPackageChangeReceiver, intentFilter);
    }

    private void unregisterPackageChangeBroadcast() {
        if (null != mPackageChangeReceiver) {
            mContext.unregisterReceiver(mPackageChangeReceiver);
        }
    }

    private BroadcastReceiver mPackageChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.i(TAG, "Package change receive action: " + action);
        }
    };
}
