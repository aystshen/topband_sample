package com.ayst.item;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.SystemMcu;

/**
 * Created by Administrator on 2018/11/6.
 */

public class McuTest {
    private SystemMcu mMcuService;

    @SuppressLint("WrongConstant")
    public McuTest(Context context) {
        mMcuService = (SystemMcu) context.getSystemService("mcu");
    }

    /**
     * Heartbeat
     */
    public void heartbeat() {
        if (null != mMcuService) {
            mMcuService.heartbeat();
        }
    }

    /**
     * Set the boot countdown
     * @param time (unit: second)
     * @return <0：error
     */
    public int setUptime(int time) {
        if (null != mMcuService) {
            return mMcuService.setUptime(time);
        }
        return -1;
    }

    /**
     * Enable watchdog
     * @return <0：error
     */
    public int openWatchdog() {
        if (null != mMcuService) {
            return mMcuService.openWatchdog();
        }
        return -1;
    }

    /**
     * Disable watchdog
     * @return <0：error
     */
    public int closeWatchdog() {
        if (null != mMcuService) {
            return mMcuService.closeWatchdog();
        }
        return -1;
    }

    /**
     * Set watchdog over time duration
     * @param duration (unit: second)
     * @return <0：error
     */
    public int setWatchdogDuration(int duration) {
        if (null != mMcuService) {
            return mMcuService.setWatchdogDuration(duration);
        }
        return -1;
    }
}
