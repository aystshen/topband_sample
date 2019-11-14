package com.ayst.sample.items.androidx;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.ayst.androidx.IModemService;
import com.ayst.androidx.IWatchdogService;

public class AndroidXPresenter {
    private static final String TAG = "AndroidXPresenter";

    private static final String ANDROIDX_PACKAGE_NAME = "com.ayst.androidx";
    private static final String ACTION_MODEM_SERVICE = "com.ayst.androidx.MODEM_SERVICE";
    private static final String ACTION_WATCHDOG_SERVICE = "com.ayst.androidx.WATCHDOG_SERVICE";

    private Context mContext;
    private IAndroidXView mAndroidXView;
    private IModemService mModemService;
    private IWatchdogService mWatchdogService;

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
    }

    public void stop() {
        mContext.unbindService(mModemServiceConnection);
        mContext.unbindService(mWatchdogServiceConnection);
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
}
