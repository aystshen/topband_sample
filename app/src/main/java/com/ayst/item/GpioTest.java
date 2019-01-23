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
     * GPIO write
     * @param gpio 0,1,2,3,4,5,6
     * @param value 0: Low 1: High
     */
    public void gpioWrite(int gpio, int value) {
        if (null != mGpioService) {
            mGpioService.gpioWrite(gpio, value);
        }
    }

    /**
     * GPIO read
     * @param gpio 0,1,2,3,4,5,6
     * @return 0: Low 1: High other：error
     */
    public int gpioRead(int gpio) {
        if (null != mGpioService) {
            return mGpioService.gpioRead(gpio);
        }
        return -1;
    }

    /**
     * GPIO direction
     * @param gpio 0,1,2,3,4,5,6
     * @param direction 0: input 1: output
     * @param value 0: Low 1: High
     */
    public void gpioDirection(int gpio, int direction, int value) {
        if (null != mGpioService) {
            mGpioService.gpioDirection(gpio, direction, value);
        }
    }

    /**
     * GPIO register key event
     * @param gpio 0,1,2,3,4,5,6
     */
    public void gpioRegKeyEvent(int gpio) {
        if (null != mGpioService) {
            mGpioService.gpioRegKeyEvent(gpio);
        }
    }

    /**
     * GPIO unregister key event
     * @param gpio 0,1,2,3,4,5,6
     */
    public void gpioUnregKeyEvent(int gpio) {
        if (null != mGpioService) {
            mGpioService.gpioUnregKeyEvent(gpio);
        }
    }

    /**
     * Get GPIO number
     * @return -1: error other: GPIO number
     */
    public int gpioGetNumber() {
        if (null != mGpioService) {
            return mGpioService.gpioGetNumber();
        }
        return -1;
    }
}
