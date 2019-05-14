package com.ayst.item;

import android.util.Log;

import cn.trinea.android.common.util.ShellUtils;

/**
 * Created by Administrator on 2018/11/6.
 */

public class ShellTest {
    private static final String TAG = "ShellTest";

    public static boolean rootTest() {
        ShellUtils.CommandResult result = ShellUtils.execCommand("ls -l", true);

        Log.i(TAG, "execute, success message: " + result.successMsg + ", error message: " + result.errorMsg);

        return result.errorMsg.isEmpty();
    }
}
