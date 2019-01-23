package com.ayst.sample;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.ToggleButton;

import com.ayst.item.CameraTest;
import com.ayst.item.GpioTest;
import com.ayst.item.ShellTest;
import com.ayst.item.SilentInstall;
import com.ayst.item.SystemAction;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class MainActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener {
    private static final String TAG = "Sample";

    @BindView(R.id.btn_root_test)
    Button mRootTestBtn;
    @BindView(R.id.btn_silent_install)
    Button mSilentInstallBtn;
    @BindView(R.id.btn_reboot)
    Button mRebootBtn;
    @BindView(R.id.btn_start_app)
    Button mStartAppBtn;
    @BindView(R.id.btn_gpio)
    ToggleButton mGpioBtn;
    @BindView(R.id.btn_sensor)
    ToggleButton mSensorBtn;
    @BindView(R.id.btn_camera)
    ToggleButton mCameraBtn;
    @BindView(R.id.layout_camera)
    LinearLayout mCameraLayout;
    @BindView(R.id.spn_gpio)
    Spinner mGpioSpn;
    @BindView(R.id.rdo_gpio_input)
    RadioButton mGpioInputRdo;
    @BindView(R.id.rdo_gpio_output)
    RadioButton mGpioOutputRdo;
    @BindView(R.id.rdo_gpio_key)
    RadioButton mGpioKeyRdo;

    private boolean isSensorActive = false;
    private int mCurGpio = -1;
    private static final int[] GPIO_KEY_CODE = {
            KeyEvent.KEYCODE_GPIO_0,
            KeyEvent.KEYCODE_GPIO_1,
            KeyEvent.KEYCODE_GPIO_2,
            KeyEvent.KEYCODE_GPIO_3,
            KeyEvent.KEYCODE_GPIO_4,
            KeyEvent.KEYCODE_GPIO_5,
            KeyEvent.KEYCODE_GPIO_6,
            KeyEvent.KEYCODE_GPIO_7,
            KeyEvent.KEYCODE_GPIO_8,
            KeyEvent.KEYCODE_GPIO_9};

    private GpioTest mGpioTest;
    private CameraTest mCameraTest;

    private Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        mHandler = new Handler(getMainLooper());
        mGpioTest = new GpioTest(this);
        mCameraTest = new CameraTest(this, mCameraLayout);

        initView();
    }

    private void initView() {
        mCameraBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    mCameraTest.start();
                } else {
                    mCameraTest.stop();
                }
            }
        });

        if (mGpioTest.gpioGetNumber() > 0) {
            String[] spinnerItems = new String[mGpioTest.gpioGetNumber()];
            for (int i = 0; i < mGpioTest.gpioGetNumber(); i++) {
                spinnerItems[i] = "GPIO_" + i;
            }
            ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this,
                    R.layout.spinner_item, spinnerItems);
            mGpioSpn.setAdapter(spinnerAdapter);

            mCurGpio = mGpioSpn.getSelectedItemPosition();
            mGpioSpn.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    mCurGpio = i;
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });

            mGpioOutputRdo.setOnCheckedChangeListener(this);
            mGpioInputRdo.setOnCheckedChangeListener(this);
            mGpioKeyRdo.setOnCheckedChangeListener(this);
        } else {
            mGpioBtn.setEnabled(false);
        }
    }

    @OnClick({R.id.btn_root_test, R.id.btn_silent_install, R.id.btn_reboot, R.id.btn_start_app, R.id.btn_gpio})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_root_test:
                ShellTest.rootTest();
                break;
            case R.id.btn_silent_install:
                SilentInstall.install(this, "");
                break;
            case R.id.btn_reboot:
                SystemAction.reboot(this, 5000);
                break;
            case R.id.btn_start_app:
                SystemAction.startApp(this, "com.android.settings", 5000);
                break;
            case R.id.btn_gpio:
                Log.i(TAG, "onViewClicked, btn_gpio");
                if (mGpioOutputRdo.isChecked()) {
                    mGpioTest.gpioWrite(mGpioSpn.getSelectedItemPosition(), mGpioBtn.isChecked() ? 0 : 1);
                } else {
                    mGpioBtn.setChecked(!mGpioBtn.isChecked());
                }
                break;
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        Log.i(TAG, "dispatchKeyEvent, keyCode=" + event.getKeyCode() + " action=" + event.getAction());
        if (event.getKeyCode() == KeyEvent.KEYCODE_F10) {
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                mHandler.removeCallbacks(mSensorClose);
                if (!isSensorActive) {
                    isSensorActive = true;
                    mSensorBtn.setChecked(true);
                    mHandler.postDelayed(mSensorClose, 3000);
                }
            }
        } else if (mCurGpio >= 0 && event.getKeyCode() == GPIO_KEY_CODE[mCurGpio]) {
            mGpioBtn.setChecked(event.getAction() == KeyEvent.ACTION_DOWN);
        }

        return super.dispatchKeyEvent(event);
    }

    Runnable mSensorClose = new Runnable() {
        @Override
        public void run() {
            isSensorActive = false;
            mSensorBtn.setChecked(false);
        }
    };

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        if (b) {
            switch (compoundButton.getId()) {
                case R.id.rdo_gpio_input:
                    mGpioTest.gpioUnregKeyEvent(mCurGpio);
                    mGpioTest.gpioDirection(mCurGpio, 0, 1);
                    break;
                case R.id.rdo_gpio_output:
                    mGpioTest.gpioUnregKeyEvent(mCurGpio);
                    mGpioTest.gpioDirection(mCurGpio, 1, 0);
                    break;
                case R.id.rdo_gpio_key:
                    mGpioTest.gpioRegKeyEvent(mCurGpio);
                    break;
            }
        }
    }
}
