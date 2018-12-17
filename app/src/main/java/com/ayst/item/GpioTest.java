package com.ayst.item;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.SystemGpio;

/**
 * Created by Administrator on 2018/11/6.
 */

public class GpioTest {
    private SystemGpio mGpioService;

    @SuppressLint("WrongConstant")
    public GpioTest(Context context) {
        mGpioService = (SystemGpio) context.getSystemService("gpio");
    }

    /**
     * GPIO control
     * @param gpio 1,2,3,4
     * @param value 0: Low 1: High
     */
    public void gpioCtrl(int gpio, int value) {
        if (null != mGpioService) {
            mGpioService.gpioCtrl(gpio, value);
        }
    }
}
