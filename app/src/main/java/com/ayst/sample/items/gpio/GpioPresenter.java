package com.ayst.sample.items.gpio;

import android.content.Context;
import android.os.Handler;
import android.view.KeyEvent;

public class GpioPresenter {
    private static final String TAG = "GpioPresenter";

    private Context mContext;
    private IGpioView mGpioView;
    private int mSelectedGpio;
    private Gpio mGpio;
    private Mode mMode;

    private Handler mHandler;

    private static final int[] GPIO_KEY_CODE = {
            275, //KeyEvent.KEYCODE_GPIO_0,
            276, //KeyEvent.KEYCODE_GPIO_1,
            277, //KeyEvent.KEYCODE_GPIO_2,
            278, //KeyEvent.KEYCODE_GPIO_3,
            279, //KeyEvent.KEYCODE_GPIO_4,
            280, //KeyEvent.KEYCODE_GPIO_5,
            281, //KeyEvent.KEYCODE_GPIO_6,
            282, //KeyEvent.KEYCODE_GPIO_7,
            283, //KeyEvent.KEYCODE_GPIO_8,
            284  //KeyEvent.KEYCODE_GPIO_9
    };

    public enum Mode {
        INPUT,
        OUTPUT,
        KEY
    }

    public GpioPresenter(Context context, IGpioView view) {
        mContext = context;
        mGpioView = view;
        mGpio = new Gpio(context);
        mHandler = new Handler(context.getMainLooper());
    }

    public int getNumber() {
        return mGpio.gpioGetNumber();
    }

    public void setSelected(int gpio) {
        mSelectedGpio = gpio;
    }

    public void setMode(Mode mode) {
        switch (mode) {
            case INPUT:
                mMode = Mode.INPUT;
                mGpio.gpioUnregKeyEvent(mSelectedGpio);
                mGpio.gpioDirection(mSelectedGpio, 0, 1);
                mHandler.removeCallbacks(mReadGpioRunnable);
                mHandler.postDelayed(mReadGpioRunnable, 1000);
                break;
            case OUTPUT:
                mMode = Mode.OUTPUT;
                mHandler.removeCallbacks(mReadGpioRunnable);
                mGpio.gpioUnregKeyEvent(mSelectedGpio);
                mGpio.gpioDirection(mSelectedGpio, 1, 0);
                break;
            case KEY:
                mMode = Mode.KEY;
                mHandler.removeCallbacks(mReadGpioRunnable);
                mGpio.gpioRegKeyEvent(mSelectedGpio);
                break;
        }
    }

    public Mode getMode() {
        return mMode;
    }

    public void write(int value) {
        mGpio.gpioWrite(mSelectedGpio, value);
    }

    public void checkKeyEvent(KeyEvent event) {
        if (mSelectedGpio >= 0 && mSelectedGpio < GPIO_KEY_CODE.length
                && event.getKeyCode() == GPIO_KEY_CODE[mSelectedGpio]) {
            mGpioView.updateGpioStatus(event.getAction() == KeyEvent.ACTION_UP);
        }
    }

    private Runnable mReadGpioRunnable = new Runnable() {
        @Override
        public void run() {
            int value = mGpio.gpioRead(mSelectedGpio);
            mGpioView.updateGpioStatus(value > 0);
            mHandler.postDelayed(this, 1000);
        }
    };
}
