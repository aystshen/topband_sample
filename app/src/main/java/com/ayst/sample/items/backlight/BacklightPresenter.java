package com.ayst.sample.items.backlight;

import android.content.Context;
import android.provider.Settings;

public class BacklightPresenter {

    private Context mContext;
    private Lights mLights;

    public BacklightPresenter(Context context) {
        mContext = context;
        mLights = new Lights(context);
    }

    public void setMainBrightness(int brightness) {
        //mLights.setBrightness(0, brightness);
        saveMainBrightness(brightness);
    }

    public void setSubBrightness(int brightness) {
        //mLights.setBrightness(1, brightness);
        saveSubBrightness(brightness);
    }

    public void saveMainBrightness(int brightness) {
        try {
            Settings.System.putInt(mContext.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, brightness);
        } catch (Exception localException) {
            localException.printStackTrace();
        }
    }

    public void saveSubBrightness(int brightness) {
        try {
            /**
             * Settings.System.SCREEN_BRIGHTNESS_EXT = "screen_brightness_ext"
             */
            Settings.System.putInt(mContext.getContentResolver(), "screen_brightness_ext", brightness);
        } catch (Exception localException) {
            localException.printStackTrace();
        }
    }
}
