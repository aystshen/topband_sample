package com.ayst.sample.items.upgrade;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.ayst.romupgrade.IRomUpgradeService;

public class RomUpgradePresenter {
    private static final String TAG = "RomUpgradePresenter";

    private static final String UPGRADE_PACKAGE_NAME = "com.ayst.romupgrade";
    private static final String ACTION_UPGRADE_SERVICE = "com.ayst.romupgrade.UPGRADE_SERVICE";

    private Context mContext;
    private IRomUpgradeService mRomUpgradeService;

    public RomUpgradePresenter(Context context) {
        mContext = context;
    }

    public void start() {
        Intent intent = new Intent();
        intent.setPackage(UPGRADE_PACKAGE_NAME);
        intent.setAction(ACTION_UPGRADE_SERVICE);
        mContext.bindService(intent, mRomUpgradeServiceConnection, Context.BIND_AUTO_CREATE);
    }

    public void stop() {
        mContext.unbindService(mRomUpgradeServiceConnection);
    }

    private ServiceConnection mRomUpgradeServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "IRomUpgradeService, onServiceConnected...");
            mRomUpgradeService = IRomUpgradeService.Stub.asInterface(service);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "IRomUpgradeService, onServiceDisconnected...");
            mRomUpgradeService = null;
        }
    };

    /**
     * 检查升级
     */
    public void checkUpdate() {
        if (null != mRomUpgradeService) {
            try {
                mRomUpgradeService.checkUpdate();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 安装升级
     *
     * @param packagePath ota升级包
     * @return
     */
    public boolean installPackage(String packagePath) {
        if (null != mRomUpgradeService) {
            try {
                return mRomUpgradeService.installPackage(packagePath);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        return false;
    }

    /**
     * 验证升级包
     *
     * @param packagePath ota升级包
     * @return
     */
    public boolean verifyPackage(String packagePath) {
        if (null != mRomUpgradeService) {
            try {
                return mRomUpgradeService.verifyPackage(packagePath);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        return false;
    }

    /**
     * 删除升级包
     *
     * @param packagePath ota升级包
     */
    public void deletePackage(String packagePath) {
        if (null != mRomUpgradeService) {
            try {
                mRomUpgradeService.deletePackage(packagePath);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }
}
