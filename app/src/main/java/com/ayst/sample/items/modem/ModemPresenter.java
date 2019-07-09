package com.ayst.sample.items.modem;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

public class ModemPresenter {
    private static final String TAG = "ModemPresenter";

    private Context mContext;
    private IModemView mModemView;
    private Modem mModem;

    private Handler mHandler;

    public ModemPresenter(Context context, IModemView view) {
        mContext = context;
        mModemView = view;
        mModem = new Modem(context);
        mHandler = new Handler(Looper.getMainLooper());
    }

    public void start() {
        mHandler.postDelayed(mCheckStatusRunnable, 1000);
    }

    public void stop() {
        mHandler.removeCallbacks(mCheckStatusRunnable);
    }

    public void powerOn(){
        mModem.powerOn();
    }

    public void powerOff() {
        mModem.powerOff();
    }

    public void reset() {
        mModem.reset();
    }

    public void wakeup() {
        mModem.wakeup();
    }

    public void sleep() {
        mModem.sleep();
    }

    private Runnable mCheckStatusRunnable = new Runnable() {
        @Override
        public void run() {
            mModemView.updateModemStatus(mModem.isPowerOn(), mModem.isWakeup());
            mHandler.postDelayed(this, 1000);
        }
    };
}
