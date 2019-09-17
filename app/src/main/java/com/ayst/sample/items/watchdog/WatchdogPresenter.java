package com.ayst.sample.items.watchdog;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;


public class WatchdogPresenter {
    private static final String TAG = "WatchdogPresenter";

    private Context mContext;
    private IWatchdogView mWatchdogView;
    private Mcu mMcu;

    private int mTimeout = 180; // 默认：3分钟
    private Handler mHandler;

    public WatchdogPresenter(Context context, IWatchdogView view) {
        mContext = context;
        mWatchdogView = view;
        mMcu = new Mcu(context);
        mHandler = new Handler(Looper.getMainLooper());
    }

    public void start() {
        mWatchdogView.updateWatchdogState(mMcu.watchdogIsOpen(), mMcu.getWatchdogDuration());
    }

    public void stop() {

    }

    public void openWatchdog() {
        mMcu.openWatchdog();
        mHandler.removeCallbacks(mCountdownRunnable);
        mHandler.postDelayed(mCountdownRunnable, 1000);
    }

    public void closeWatchdog() {
        mMcu.closeWatchdog();
        mHandler.removeCallbacks(mCountdownRunnable);
    }

    public void setTimeout(int timeout) {
        mTimeout = timeout;
        mMcu.setWatchdogDuration(timeout);
        if (mMcu.watchdogIsOpen()) {
            mHandler.removeCallbacks(mCountdownRunnable);
            mHandler.postDelayed(mCountdownRunnable, 1000);
        }
    }

    public void sendHeartbeat() {
        mMcu.heartbeat();
    }

    private Runnable mCountdownRunnable = new Runnable() {
        @Override
        public void run() {
            if (mTimeout > 0) {
                mTimeout--;
                mWatchdogView.updateCountdown(mTimeout);
            }
            mHandler.postDelayed(this, 1000);
        }
    };
}
