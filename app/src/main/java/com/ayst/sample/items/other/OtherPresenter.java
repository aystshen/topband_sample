package com.ayst.sample.items.other;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.KeyguardManager;
import android.app.admin.DevicePolicyManager;
import android.app.backup.BackupManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Build;
import android.os.PowerManager;
import android.os.SystemClock;
import android.provider.Settings;
import android.speech.tts.TextToSpeech;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.ayst.utils.AppUtils;
import com.ayst.utils.InstallUtil;

import java.lang.reflect.Method;
import java.util.Locale;

import cn.trinea.android.common.util.ShellUtils;

public class OtherPresenter {
    private static final String TAG = "OtherPresenter";

    private Context mContext;
    private IOtherView mOtherView;
    private PowerManager mPowerManager;
    private TextToSpeech mSpeech;

    public OtherPresenter(Context context, IOtherView view) {
        mContext = context;
        mOtherView = view;
        activeAdmin(context);
        mSpeech = new TextToSpeech(mContext, new TTSListener());
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

        mOtherView.updateTcpAdbResult(
                result1.result >=0
                && result2.result >=0
                && TextUtils.isEmpty(result1.errorMsg)
                && TextUtils.isEmpty(result2.errorMsg));
    }

    /**
     * root权限测试
     */
    public void root() {
        ShellUtils.CommandResult result = ShellUtils.execCommand("ls -l", true);

        Log.i(TAG, "execute, success message: " + result.successMsg + ", error message: " + result.errorMsg);

        mOtherView.updateRootResult(result.result >=0
                && TextUtils.isEmpty(result.errorMsg));
    }

    /**
     * 静默安装
     *
     * @param path
     * @return
     */
    public boolean silentInstall(String path) {
        return InstallUtil.installSilent(path);
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
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
            Intent intent = new Intent("android.intent.action.MASTER_CLEAR");
            intent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
            intent.putExtra("android.intent.extra.REASON", "MasterClearConfirm");
            mContext.sendBroadcast(intent);
        } else {
            Intent intent = new Intent("android.intent.action.MASTER_CLEAR");
            intent.setPackage("android");
            mContext.sendBroadcast(intent);
        }
    }

    /**
     * 亮屏
     */
    public void screenOn() {
        if (null == mPowerManager) {
            mPowerManager = ((PowerManager) mContext.getSystemService(Context.POWER_SERVICE));
        }
        PowerManager.WakeLock wakeLock = mPowerManager.newWakeLock(
                PowerManager.ACQUIRE_CAUSES_WAKEUP
                        | PowerManager.SCREEN_DIM_WAKE_LOCK,
                "Sample:WakeLock");
        wakeLock.acquire();
        wakeLock.release();
    }

    /**
     * 灭屏
     * 需要到设置->安全性和位置信息->设备管理应用中勾选Sample应用
     */
    public void screenOff() {
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
     *
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
     *
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

    /**
     * 获取自动同步时间
     *
     * @return 0：关闭；1：打开；
     */
    public int getAutoTime() {
        return Settings.Global.getInt(mContext.getContentResolver(),
                Settings.Global.AUTO_TIME, 0);
    }

    /**
     * 设置自动同步时间
     *
     * @param value 0：关闭；1：打开；
     */
    public void setAutoTime(int value) {
        Settings.Global.putInt(mContext.getContentResolver(),
                Settings.Global.AUTO_TIME, value);
    }

    /**
     * 获取当前时间戳
     *
     * @return 时间戳
     */
    public long getTime() {
        return System.currentTimeMillis();
    }

    /**
     * 设置时间戳
     *
     * @param millis 时间戳
     */
    public void setTime(long millis) {
        SystemClock.setCurrentTimeMillis(millis);
    }

    /**
     * 修改系统语言
     * @param locale
     */
    public void setSystemLanguage(Locale locale) {
        if (locale != null) {
            try {
                Class classActivityManagerNative = Class.forName("android.app.ActivityManagerNative");
                Method getDefault = classActivityManagerNative.getDeclaredMethod("getDefault");
                Object objIActivityManager = getDefault.invoke(classActivityManagerNative);
                Class classIActivityManager = Class.forName("android.app.IActivityManager");
                Method getConfiguration = classIActivityManager.getDeclaredMethod("getConfiguration");
                Configuration config = (Configuration) getConfiguration.invoke(objIActivityManager);
                config.setLocale(locale);
                Class clzConfig = Class.forName("android.content.res.Configuration");
                java.lang.reflect.Field userSetLocale = clzConfig.getField("userSetLocale");
                userSetLocale.set(config, true);
                Class[] clzParams = {Configuration.class};
                Method updateConfiguration = classIActivityManager.getDeclaredMethod("updateConfiguration", clzParams);
                updateConfiguration.invoke(objIActivityManager, config);
                BackupManager.dataChanged("com.android.providers.settings");
            } catch (Exception e) {
                Log.d(TAG, "setSystemLanguage: " + e.getLocalizedMessage());
            }
        }
    }

    public void tts(String text) {
        Log.i(TAG, "tts, text: " + text);
        mSpeech.setSpeechRate(1.0f);
        mSpeech.setPitch(1.0f);
        mSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null);
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

    private class TTSListener implements TextToSpeech.OnInitListener {
        @Override
        public void onInit(int status) {
            // TODO Auto-generated method stub
            if (status == TextToSpeech.SUCCESS) {
                int supported = mSpeech.setLanguage(Locale.CHINESE);
                if (supported != TextToSpeech.LANG_AVAILABLE
                        && supported != TextToSpeech.LANG_COUNTRY_AVAILABLE) {
                    Log.e(TAG, "TTS onInit, not support language");
                }
                Log.i(TAG, "TTS onInit, success");
            } else {
                Log.e(TAG, "TTS onInit, failed");
            }
        }
    }
}
