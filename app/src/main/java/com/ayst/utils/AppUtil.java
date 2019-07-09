package com.ayst.utils;

import android.app.Activity;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Environment;
import android.os.Vibrator;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Method;

/**
 * Created by Administrator on 2016/4/6.
 */
public class AppUtil {
    private final static String TAG = "AppUtil";

    // APP版本号
    private static String mVersionName = "";
    private static int mVersionCode = -1;

    // MAC地址获取
    private static String mEth0Mac = "";
    private static String mWifiMac = "";

    // 屏幕宽高
    private static int mScreenWidth = -1;
    private static int mScreenHeight = -1;

    // 目录
    private static String sRootDir = "";

    public static String getVersionName(Context context) {
        if (TextUtils.isEmpty(mVersionName)) {
            try {
                PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
                mVersionName = info.versionName;
                mVersionCode = info.versionCode;
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }
        return mVersionName;
    }

    public static int getVersionCode(Context context) {
        if (-1 == mVersionCode) {
            try {
                PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
                mVersionName = info.versionName;
                mVersionCode = info.versionCode;
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }
        return mVersionCode;
    }

    public static boolean isConnNetWork(Context context) {
        ConnectivityManager conManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = conManager.getActiveNetworkInfo();
        return ((networkInfo != null) && networkInfo.isConnected());
    }

    public static boolean isWifiConnected(Context context) {
        ConnectivityManager conManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiNetworkInfo = conManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return ((wifiNetworkInfo != null) && wifiNetworkInfo.isConnected());
    }

    /**
     * 获取有线mac地址
     *
     * @return
     */
    public static String getEth0MacAddress(Context context) {
        if (TextUtils.isEmpty(mEth0Mac)) {
            try {
                int numRead = 0;
                char[] buf = new char[1024];
                StringBuffer strBuf = new StringBuffer(1000);
                BufferedReader reader = new BufferedReader(new FileReader("/sys/class/net/eth0/address"));
                while ((numRead = reader.read(buf)) != -1) {
                    String readData = String.valueOf(buf, 0, numRead);
                    strBuf.append(readData);
                }
                mEth0Mac = strBuf.toString();
                reader.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        Log.d(TAG, "getEth0MacAddress, mac=" + mEth0Mac);
        return mEth0Mac;
    }

    /**
     * 获取无线mac地址
     *
     * @param context
     * @return
     */
    public static String getWifiMacAddr(Context context) {
        if (TextUtils.isEmpty(mWifiMac)) {
            WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            mWifiMac = wifiInfo.getMacAddress();
        }
        Log.d(TAG, "getWifiMacAddr, mac=" + mWifiMac);
        return mWifiMac;
    }

    /**
     * 获取屏幕宽度
     *
     * @param context
     * @return
     */
    public static int getScreenWidth(Activity context) {
        if (-1 == mScreenWidth) {
            mScreenWidth = context.getWindowManager().getDefaultDisplay().getWidth();
        }
        return mScreenWidth;
    }

    /**
     * 获取屏幕高度
     *
     * @param context
     * @return
     */
    public static int getScreenHeight(Activity context) {
        if (-1 == mScreenHeight) {
            mScreenHeight = context.getWindowManager().getDefaultDisplay().getHeight();
        }
        return mScreenHeight;
    }

    private static boolean isExternalStorageMounted() {
        return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
    }

    public static String getRootDir(Context context) {
        if (sRootDir.isEmpty()) {
            File sdcardDir = null;
            try {
                if (isExternalStorageMounted()) {
                    sdcardDir = Environment.getExternalStorageDirectory();
                    Log.i(TAG, "Environment.MEDIA_MOUNTED :" + sdcardDir.getAbsolutePath() + " R:" + sdcardDir.canRead() + " W:" + sdcardDir.canWrite());
                    if (sdcardDir.canWrite()) {
                        String dir = sdcardDir.getAbsolutePath();
                        File file = new File(dir);
                        if (!file.exists()) {
                            Log.i(TAG, "getRootDir, dir not exist and make dir");
                            file.mkdirs();
                        }
                        sRootDir = dir;
                        return sRootDir;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            sRootDir = context.getFilesDir().getAbsolutePath();
        }
        return sRootDir;
    }

    public static String getDir(Context context, String dirName) {
        String dir = getRootDir(context) + File.separator + dirName;
        File file = new File(dir);
        if (!file.exists()) {
            Log.i(TAG, "getDir, dir not exist and make dir");
            file.mkdirs();
        }
        return dir;
    }

    /**
     * 获取系统属性
     *
     * @param key
     * @param defaultValue
     * @return
     */
    public static String getProperty(String key, String defaultValue) {
        String value = defaultValue;
        try {
            Class<?> c = Class.forName("android.os.SystemProperties");
            Method get = c.getMethod("get", String.class, String.class);
            value = (String) (get.invoke(c, key, "unknown"));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            return value;
        }
    }

    /**
     * 设置系统属性
     *
     * @param key
     * @param value
     */
    public static void setProperty(String key, String value) {
        try {
            Class<?> c = Class.forName("android.os.SystemProperties");
            Method set = c.getMethod("set", String.class, String.class);
            set.invoke(c, key, value);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void reboot(Context context) {
        Intent intent = new Intent(Intent.ACTION_REBOOT);
        intent.putExtra("nowait", 1);
        intent.putExtra("interval", 1);
        intent.putExtra("window", 0);
        context.sendBroadcast(intent);
    }

    public static void shutdown(Context context) {
        Intent intent = new Intent("com.android.internal.intent.action.REQUEST_SHUTDOWN");
        intent.putExtra("android.intent.extra.KEY_CONFIRM", false);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
}
