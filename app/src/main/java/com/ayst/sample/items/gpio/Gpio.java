package com.ayst.sample.items.gpio;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.IBinder;
import android.os.IGpioService;
import android.os.RemoteException;

import java.lang.reflect.Method;

/**
 * Created by Administrator on 2018/11/6.
 */

public class Gpio {
    private IGpioService mGpioService;

    @SuppressLint("WrongConstant")
    public Gpio(Context context) {
        Method method = null;
        try {
            method = Class.forName("android.os.ServiceManager").getMethod("getService", String.class);
            IBinder binder = (IBinder) method.invoke(null, new Object[]{"gpio"});
            mGpioService = IGpioService.Stub.asInterface(binder);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * GPIO write
     *
     * @param gpio  0~Number
     * @param value 0: Low 1: High
     */
    public void gpioWrite(int gpio, int value) {
        if (null != mGpioService) {
            try {
                mGpioService.gpioWrite(gpio, value);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * GPIO read
     *
     * @param gpio 0~Number
     * @return 0: Low 1: High other：error
     */
    public int gpioRead(int gpio) {
        if (null != mGpioService) {
            try {
                return mGpioService.gpioRead(gpio);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return -1;
    }

    /**
     * GPIO direction
     *
     * @param gpio      0~Number
     * @param direction 0: input 1: output
     * @param value     0: Low 1: High
     */
    public void gpioDirection(int gpio, int direction, int value) {
        if (null != mGpioService) {
            try {
                mGpioService.gpioDirection(gpio, direction, value);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * GPIO register key event
     *
     * @param gpio 0~Number
     */
    public void gpioRegKeyEvent(int gpio) {
        if (null != mGpioService) {
            try {
                mGpioService.gpioRegKeyEvent(gpio);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * GPIO unregister key event
     *
     * @param gpio 0~Number
     */
    public void gpioUnregKeyEvent(int gpio) {
        if (null != mGpioService) {
            try {
                mGpioService.gpioUnregKeyEvent(gpio);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Get GPIO number
     *
     * @return <0: error other: GPIO number
     */
    public int gpioGetNumber() {
        if (null != mGpioService) {
            try {
                return mGpioService.gpioGetNumber();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return -1;
    }
}
