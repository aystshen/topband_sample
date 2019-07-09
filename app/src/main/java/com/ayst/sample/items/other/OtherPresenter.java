package com.ayst.sample.items.other;

import android.content.Context;
import android.util.Log;

import com.ayst.utils.AppUtil;

import cn.trinea.android.common.util.ShellUtils;

public class OtherPresenter {
    private static final String TAG = "OtherPresenter";

    private Context mContext;
    private IOtherView mOtherView;

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
}
