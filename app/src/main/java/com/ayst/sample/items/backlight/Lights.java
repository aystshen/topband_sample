package com.ayst.sample.items.backlight;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.IBinder;
import android.os.ILightsService;
import android.os.RemoteException;
import android.util.Log;

import java.lang.reflect.Method;

/**
 * Created by Administrator on 2018/11/6.
 */

public class Lights {
    private ILightsService mLightsService;

    @SuppressLint("WrongConstant")
    public Lights(Context context) {
        Method method = null;
        try {
            method = Class.forName("android.os.ServiceManager").getMethod("getService", String.class);
            IBinder binder = (IBinder) method.invoke(null, new Object[]{"lights"});
            mLightsService = ILightsService.Stub.asInterface(binder);
            if (mLightsService == null) {
                Log.i("Lights", "mLightsService is null");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @param type
     *     BACKLIGHT=0,
     * 	   BACKLIGHT_EXT=1,
     *     KEYBOARD=2,
     *     BUTTONS=3,
     *     BATTERY=4,
     *     NOTIFICATIONS=5,
     *     ATTENTION=6,
     *     BLUETOOTH=7,
     *     WIFI=8,
     * @param brightness 0~255
     * @return
     */
    public boolean setBrightness(int type, int brightness) {
        if (null != mLightsService) {
            try {
                return mLightsService.setBrightness(type, brightness);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return false;
    }
}
