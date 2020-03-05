package com.ayst.sample.items.androidx;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.ayst.androidx.IKeyInterceptService;
import com.ayst.androidx.ILog2fileService;
import com.ayst.androidx.IModemService;
import com.ayst.androidx.IOtgService;
import com.ayst.androidx.IWatchdogService;

public class AndroidXPresenter {
    private static final String TAG = "AndroidXPresenter";

    private static final String ANDROIDX_PACKAGE_NAME = "com.ayst.androidx";
    private static final String ACTION_MODEM_SERVICE = "com.ayst.androidx.MODEM_SERVICE";
    private static final String ACTION_WATCHDOG_SERVICE = "com.ayst.androidx.WATCHDOG_SERVICE";
    private static final String ACTION_OTG_SERVICE = "com.ayst.androidx.OTG_SERVICE";
    private static final String ACTION_KEY_INTERCEPT_SERVICE = "com.ayst.androidx.KEY_INTERCEPT_SERVICE";
    private static final String ACTION_LOG2FILE_SERVICE = "com.ayst.androidx.LOG2FILE_SERVICE";

    private Context mContext;
    private IAndroidXView mAndroidXView;
    private IModemService mModemService;
    private IWatchdogService mWatchdogService;
    private IOtgService mOtgService;
    private IKeyInterceptService mIKeyInterceptService;
    private ILog2fileService mLog2fileService;

    public AndroidXPresenter(Context context, IAndroidXView view) {
        mContext = context;
        mAndroidXView = view;
    }

    public void start() {
        Intent intent = new Intent();
        intent.setPackage(ANDROIDX_PACKAGE_NAME);
        intent.setAction(ACTION_MODEM_SERVICE);
        mContext.bindService(intent, mModemServiceConnection, Context.BIND_AUTO_CREATE);

        Intent intent1 = new Intent();
        intent1.setPackage(ANDROIDX_PACKAGE_NAME);
        intent1.setAction(ACTION_WATCHDOG_SERVICE);
        mContext.bindService(intent1, mWatchdogServiceConnection, Context.BIND_AUTO_CREATE);

        Intent intent2 = new Intent();
        intent2.setPackage(ANDROIDX_PACKAGE_NAME);
        intent2.setAction(ACTION_OTG_SERVICE);
        mContext.bindService(intent2, mOtgServiceConnection, Context.BIND_AUTO_CREATE);

        Intent intent3 = new Intent();
        intent3.setPackage(ANDROIDX_PACKAGE_NAME);
        intent3.setAction(ACTION_KEY_INTERCEPT_SERVICE);
        mContext.bindService(intent3, mKeyInterceptServiceConnection, Context.BIND_AUTO_CREATE);

        Intent intent4 = new Intent();
        intent4.setPackage(ANDROIDX_PACKAGE_NAME);
        intent4.setAction(ACTION_LOG2FILE_SERVICE);
        mContext.bindService(intent4, mLog2fileServiceConnection, Context.BIND_AUTO_CREATE);
    }

    public void stop() {
        mContext.unbindService(mModemServiceConnection);
        mContext.unbindService(mWatchdogServiceConnection);
        mContext.unbindService(mOtgServiceConnection);
        mContext.unbindService(mKeyInterceptServiceConnection);
    }

    private ServiceConnection mModemServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "IModemService, onServiceConnected...");

            mModemService = IModemService.Stub.asInterface(service);
            try {
                mAndroidXView.updateAndroidX4GState(mModemService.keepLiveIsOpen());
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mModemService = null;
            Log.d(TAG, "IModemService, onServiceDisconnected...");
        }
    };

    private ServiceConnection mWatchdogServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "IWatchdogService, onServiceConnected...");

            mWatchdogService = IWatchdogService.Stub.asInterface(service);
            try {
                mAndroidXView.updateAndroidXWatchdogState(mWatchdogService.watchdogIsOpen());
                mAndroidXView.updateAndroidXWatchdogTimeout(mWatchdogService.getWatchdogTimeout());
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "IWatchdogService, onServiceDisconnected...");
            mWatchdogService = null;
        }
    };

    private ServiceConnection mOtgServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "IOtgService, onServiceConnected...");

            mOtgService = IOtgService.Stub.asInterface(service);
            try {
                mAndroidXView.updateAndroidXOtgMode(mOtgService.getOtgMode());
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "IOtgService, onServiceDisconnected...");
            mOtgService = null;
        }
    };

    private ServiceConnection mKeyInterceptServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "IKeyInterceptService, onServiceConnected...");

            mIKeyInterceptService = IKeyInterceptService.Stub.asInterface(service);
            try {
                mAndroidXView.updateAndroidXKeyIntercept(mIKeyInterceptService.isOpen());
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "IKeyInterceptService, onServiceDisconnected...");
            mIKeyInterceptService = null;
        }
    };

    private ServiceConnection mLog2fileServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "ILog2fileService, onServiceConnected...");

            mLog2fileService = ILog2fileService.Stub.asInterface(service);
            try {
                mAndroidXView.updateAndroidXLog2file(mLog2fileService.isOpen());
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "ILog2fileService, onServiceDisconnected...");
            mLog2fileService = null;
        }
    };

    /**
     * 打开、关闭4G保活服务
     *
     * @param on
     */
    public void toggle4GKeepLive(boolean on) {
        if (null != mModemService) {
            try {
                if (on) {
                    mModemService.open4gKeepLive();
                } else {
                    mModemService.close4gKeepLive();
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 打开、关闭看门狗
     *
     * @param on
     */
    public void toggleWatchdog(boolean on) {
        if (null != mWatchdogService) {
            try {
                if (on) {
                    mWatchdogService.openWatchdog();
                } else {
                    mWatchdogService.closeWatchdog();
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 设置看门狗超时时长
     *
     * @param timeout
     */
    public void setWatchdogTimeout(int timeout) {
        if (null != mWatchdogService) {
            try {
                if (mWatchdogService.setWatchdogTimeout(timeout)) {
                    mAndroidXView.updateAndroidXWatchdogTimeout(timeout);
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 调otg口usb工作模式
     *
     * @param mode
     *      0：auto，由硬件决定
     *      1：host，usb模式
     *      2：device，otg调试模式
     */
    public void setOtgMode(String mode) {
        if (null != mOtgService) {
            try {
                if (!mOtgService.setOtgMode(mode)) {
                    mAndroidXView.updateAndroidXOtgMode(mOtgService.getOtgMode());
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 获取当前otg模式
     *
     * @return
     *      0：auto，由硬件决定
     *      1：host，usb模式
     *      2：device，otg调试模式
     */
    public String getOtgMode() {
        if (null != mOtgService) {
            try {
                return mOtgService.getOtgMode();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        return "0";
    }

    /**
     * 打开、关闭键值拦截
     *
     * @param on
     */
    public void toggleKeyIntercept(boolean on) {
        if (null != mIKeyInterceptService) {
            try {
                if (on) {
                    mIKeyInterceptService.openKeyIntercept();
                } else {
                    mIKeyInterceptService.closeKeyIntercept();
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 打开、关闭日志写入文件
     *
     * @param on
     */
    public void toggleLog2file(boolean on) {
        if (null != mLog2fileService) {
            try {
                if (on) {
                    mLog2fileService.openLog2file();
                } else {
                    mLog2fileService.closeLog2file();
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }
}
