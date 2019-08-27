package com.ayst.sample.items.backlight;

import android.content.Context;

import com.ayst.sample.items.camera.CameraPresenter;

public class BacklightPresenter {

    private Context mContext;
    private Lights mLights;

    public BacklightPresenter(Context context) {
        mContext = context;
        mLights = new Lights(context);
    }

    public void setMainBrightness(int brightness) {
        mLights.setBrightness(0, brightness);
    }

    public void setSubBrightness(int brightness) {
        mLights.setBrightness(1, brightness);
    }
}
