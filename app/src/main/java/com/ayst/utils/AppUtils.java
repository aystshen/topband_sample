package com.ayst.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Locale;
import java.util.UUID;

/**
 * Created by ayst.shen@foxmail.com on 2016/4/6.
 */
public class AppUtils {
    private final static String TAG = "AppUtils";

    private static final String KEY_IS_FIRST = "is_first_run";

    // Application version
    private static String mVersionName = "";
    private static int mVersionCode = -1;

    // Hardware version
    private static String mHwVersionName = "";
    private static int mHwVersionCode = -1;

    // Firmware version
    private static String mSwVersionName = "";
    private static int mSwVersionCode = -1;

    // Product
    private static String mProduceName = "";
    private static String mProduceId = "";

    // MAC
    private static String mEth0Mac = "";
    private static String mWifiMac = "";
    private static String mMac = "";
    private static String mMacNoColon = "";
    public static String mImei = "";

    // Screen
    private static int mScreenWidth = -1;
    private static int mScreenHeight = -1;

    // Storage
    private static String sRootDir = "";

    /**
     * Is first run
     * @param context
     * @return
     */
    public static boolean isFirstRun(Context context) {
        boolean isFirst = SPUtils.getInstance(context).getData(KEY_IS_FIRST, true);
        if (isFirst) {
            SPUtils.getInstance(context).saveData(KEY_IS_FIRST, false);
        }
        return isFirst;
    }

    /**
     * Get application version name
     * @param context Context
     * @return version name
     */
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

    /**
     * Get application version code
     * @param context Context
     * @return version code
     */
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

    /**
     * Get product name
     * @return product name
     */
    public static String getProductName() {
        if (TextUtils.isEmpty(mProduceName)) {
            mProduceName = AppUtils.getProperty("ro.product.model", "");
        }
        return mProduceName;
    }

    /**
     * Get product id
     * @return product id
     */
    public static String getProductId() {
        if (TextUtils.isEmpty(mProduceId)) {
            mProduceId = AppUtils.getProperty("ro.topband.product.id", "");
        }
        return mProduceId;
    }

    /**
     * Get hardware version code
     * @return version code
     */
    public static int getHwVersionCode() {
        if (-1 == mHwVersionCode) {
            int versionCode = 0;
            String value = getHwVersionName();
            if (!TextUtils.isEmpty(value)) {
                String[] arr = value.split("-");
                if (arr.length > 0) {
                    String str = arr[0].replace(".", "");
                    mHwVersionCode = Integer.parseInt(str);
                }
            }
        }
        return mHwVersionCode;
    }

    /**
     * Get hardware version name
     * @return version name
     */
    public static String getHwVersionName() {
        if (TextUtils.isEmpty(mHwVersionName)) {
            mHwVersionName = AppUtils.getProperty("ro.topband.hw.version", "");
        }
        return mHwVersionName;
    }

    /**
     * Get firmware version code
     * @return version code
     */
    public static int getSwVersionCode() {
        if (-1 == mSwVersionCode) {
            String value = AppUtils.getProperty("ro.topband.sw.versioncode", "0");
            mSwVersionCode = Integer.parseInt(value);
        }
        return mSwVersionCode;
    }

    /**
     * Get firmware version name
     * @return version name
     */
    public static String getSwVersionName() {
        if (TextUtils.isEmpty(mSwVersionName)) {
            mSwVersionName = AppUtils.getProperty("ro.topband.sw.version", "");
        }
        return mSwVersionName;
    }

    /**
     * Get Android version
     * @return version
     */
    public static String getAndroidVersion() {
        return AppUtils.getProperty("ro.build.version.release", "");
    }

    /**
     * Get serial number
     * @return serial number
     */
    public static String getProductSN() {
        String sn = AppUtils.getProperty("ro.serialno", "");
        if (TextUtils.isEmpty(sn)) {
            sn = "unknown";
        }

        return sn;
    }

    /**
     * Get current country
     * @return country
     */
    public static String getCountry() {
        return Locale.getDefault().getCountry();
    }

    /**
     * Get current language
     * @return language
     */
    public static String getLanguage() {
        return Locale.getDefault().getLanguage();
    }

    /**
     * Whether the network is connected
     * @param context Context
     * @return true/false
     */
    public static boolean isConnNetWork(Context context) {
        ConnectivityManager conManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = conManager.getActiveNetworkInfo();
        return ((networkInfo != null) && networkInfo.isConnected());
    }

    /**
     * Whether WiFi is connected
     * @param context Context
     * @return true/false
     */
    public static boolean isWifiConnected(Context context) {
        ConnectivityManager conManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiNetworkInfo = conManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return ((wifiNetworkInfo != null) && wifiNetworkInfo.isConnected());
    }

    /**
     * Get Ethernet MAC
     * @param context
     * @return
     */
    public static String getEth0Mac(Context context) {
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
                mEth0Mac = strBuf.toString().replaceAll("\r|\n", "");
                reader.close();
            } catch (IOException ex) {
                Log.w(TAG, "eth0 mac not exist");
            }
        }
        return mEth0Mac;
    }

    /**
     * Get WiFi MAC
     * @param context
     * @return
     */
    @SuppressLint("HardwareIds")
    public static String getWifiMac(Context context) {
        if (TextUtils.isEmpty(mWifiMac)) {
            WifiManager wifiManager = (WifiManager) context.getApplicationContext()
                    .getSystemService(Context.WIFI_SERVICE);
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            mWifiMac = wifiInfo.getMacAddress();
        }
        return mWifiMac;
    }

    /**
     * Get MAC, get the Ethernet MAC first, then get the WiFi MAC if it is empty.
     * @param context
     * @return
     */
    public static String getMac(Context context) {
        if (TextUtils.isEmpty(mMac)) {
            mMac = getEth0Mac(context);
            if (TextUtils.isEmpty(mMac)) {
                mMac = getWifiMac(context);
            }
        }
        return mMac;
    }

    /**
     * Get the MAC with the colon removed
     * @param context
     * @return
     */
    public static String getMacNoColon(Context context) {
        if (TextUtils.isEmpty(mMacNoColon)) {
            String mac = getMac(context);
            if (!TextUtils.isEmpty(mac)) {
                mMacNoColon = mac.replace(":", "");
            }
        }
        return mMacNoColon;
    }

    /**
     * Get screen width
     * @param context Activity
     * @return screen width
     */
    public static int getScreenWidth(Activity context) {
        if (-1 == mScreenWidth) {
            mScreenWidth = context.getWindowManager().getDefaultDisplay().getWidth();
        }
        return mScreenWidth;
    }

    /**
     * Get screen height
     * @param context Activity
     * @return screen height
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

    /**
     * Get the root storage path
     * @param context Context
     * @return path
     */
    public static String getRootDir(Context context) {
        if (sRootDir.isEmpty()) {
            File sdcardDir = null;
            try {
                if (isExternalStorageMounted()) {
                    sdcardDir = Environment.getExternalStorageDirectory();
                    Log.i(TAG, "Environment.MEDIA_MOUNTED :" + sdcardDir.getAbsolutePath()
                            + " R:" + sdcardDir.canRead() + " W:" + sdcardDir.canWrite());

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
            sRootDir = Environment.getDownloadCacheDirectory().getAbsolutePath();
        }
        return sRootDir;
    }

    /**
     * Get relative storage path
     * @param context Context
     * @param dirName relative path
     * @return full path
     */
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
     * Get property
     * @param key property key
     * @param defaultValue default value
     * @return property value
     */
    @SuppressLint("PrivateApi")
    public static String getProperty(String key, String defaultValue) {
        String value = defaultValue;
        try {
            Class<?> c = Class.forName("android.os.SystemProperties");
            Method get = c.getMethod("get", String.class, String.class);
            value = (String) (get.invoke(c, key, defaultValue));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            return value;
        }
    }

    /**
     * Set property
     * @param key property key
     * @param value property value
     */
    @SuppressLint("PrivateApi")
    public static void setProperty(String key, String value) {
        try {
            Class<?> c = Class.forName("android.os.SystemProperties");
            Method set = c.getMethod("set", String.class, String.class);
            set.invoke(c, key, value);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Get UUID
     * @return UUID
     */
    public static String getUUID() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * reboot
     * @param context
     */
    public static void reboot(Context context) {
        Intent intent = new Intent(Intent.ACTION_REBOOT);
        intent.putExtra("nowait", 1);
        intent.putExtra("interval", 1);
        intent.putExtra("window", 0);
        context.sendBroadcast(intent);
    }

    /**
     * shutdown
     * @param context
     */
    public static void shutdown(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Intent intent = new Intent("com.android.internal.intent.action.REQUEST_SHUTDOWN");
            intent.putExtra("android.intent.extra.KEY_CONFIRM", false);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } else {
            Intent intent = new Intent("android.intent.action.ACTION_REQUEST_SHUTDOWN");
            intent.putExtra("android.intent.extra.KEY_CONFIRM", false);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }
    }
}
