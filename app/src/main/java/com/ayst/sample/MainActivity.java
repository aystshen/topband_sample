package com.ayst.sample;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.ayst.item.CameraTest;
import com.ayst.item.GpioTest;
import com.ayst.item.McuTest;
import com.ayst.item.ShellTest;
import com.ayst.item.SilentInstall;
import com.ayst.item.SystemAction;
import com.ayst.item.TimingPowerTest;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

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
    @BindView(R.id.btn_switch_watchdog)
    ToggleButton mWatchdogBtn;
    @BindView(R.id.btn_set_watchdog_time)
    Button mSetWatchdogDurationBtn;
    @BindView(R.id.btn_heartbeat)
    Button mHeartbeatBtn;
    @BindView(R.id.btn_set_power_on_time)
    ToggleButton mTimingPowerOnBtn;
    @BindView(R.id.btn_set_power_off_time)
    ToggleButton mTimingPowerOffBtn;
    @BindView(R.id.btn_set_reboot)
    ToggleButton mTimingRebootBtn;
    @BindView(R.id.tv_watchdog_time)
    TextView mWatchdogTimeTv;
    @BindView(R.id.tv_power_on_time)
    TextView mPowerOnTimeTv;
    @BindView(R.id.tv_power_off_time)
    TextView mPowerOffTimeTv;
    @BindView(R.id.tv_reboot_time)
    TextView mRebootTimeTv;

    private boolean isSensorActive = false;
    private int mCurGpio = -1;
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

    private static final int TYPE_POWER_ON = 0;
    private static final int TYPE_POWER_OFF = 1;
    private static final int TYPE_REBOOT = 2;
    private int mTimePickerType = TYPE_POWER_ON;

    private GpioTest mGpioTest;
    private McuTest mMcuTest;
    private CameraTest mCameraTest;
    private TimingPowerTest mTimingPowerTest;

    private Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        mHandler = new Handler(getMainLooper());
        mGpioTest = new GpioTest(this);
        mMcuTest = new McuTest(this);
        mCameraTest = new CameraTest(this, mCameraLayout);
        mTimingPowerTest = new TimingPowerTest(this);

        initView();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mTimingPowerTest.registerReceiver();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mTimingPowerTest.unRegisterReceiver();
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

        mWatchdogBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    mMcuTest.openWatchdog();
                } else {
                    mMcuTest.closeWatchdog();
                }
            }
        });

        mTimingPowerOnBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    showTimePikerDialog(TYPE_POWER_ON);
                } else {
                    mPowerOnTimeTv.setText("");
                    mTimingPowerTest.setUptime(0);
                }
            }
        });

        mTimingPowerOffBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    showTimePikerDialog(TYPE_POWER_OFF);
                } else {
                    mPowerOffTimeTv.setText("");
                    mTimingPowerTest.stopAlarm(TimingPowerTest.ACTION_TIMED_POWEROFF);
                }
            }
        });

        mTimingRebootBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    showTimePikerDialog(TYPE_REBOOT);
                } else {
                    mRebootTimeTv.setText("");
                    mTimingPowerTest.stopAlarm(TimingPowerTest.ACTION_TIMED_REBOOT);
                }
            }
        });
    }

    @OnClick({R.id.btn_root_test, R.id.btn_silent_install, R.id.btn_reboot, R.id.btn_start_app,
            R.id.btn_gpio, R.id.btn_set_watchdog_time, R.id.btn_heartbeat})
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
            case R.id.btn_set_watchdog_time:
                showWatchdogDurationPickerDialog();
                break;
            case R.id.btn_heartbeat:
                mMcuTest.heartbeat();
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

    Runnable mReadGpioRunnable = new Runnable() {
        @Override
        public void run() {
            int value = mGpioTest.gpioRead(mCurGpio);
            mGpioBtn.setChecked(value > 0);
            mHandler.postDelayed(this, 1000);
        }
    };

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        if (b) {
            switch (compoundButton.getId()) {
                case R.id.rdo_gpio_input:
                    mGpioTest.gpioUnregKeyEvent(mCurGpio);
                    mGpioTest.gpioDirection(mCurGpio, 0, 1);
                    mHandler.removeCallbacks(mReadGpioRunnable);
                    mHandler.postDelayed(mReadGpioRunnable, 1000);
                    break;
                case R.id.rdo_gpio_output:
                    mHandler.removeCallbacks(mReadGpioRunnable);
                    mGpioTest.gpioUnregKeyEvent(mCurGpio);
                    mGpioTest.gpioDirection(mCurGpio, 1, 0);
                    break;
                case R.id.rdo_gpio_key:
                    mHandler.removeCallbacks(mReadGpioRunnable);
                    mGpioTest.gpioRegKeyEvent(mCurGpio);
                    break;
            }
        }
    }

    private void showTimePikerDialog(final int type) {
        TimePickerDialog tpd = TimePickerDialog.newInstance(new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePickerDialog view, int hourOfDay, int minute, int second) {
                String showStr = String.format("%02d", hourOfDay) + ":" + String.format("%02d", minute) + ":" + String.format("%02d", second);
                if (TYPE_POWER_ON == type) {
                    mPowerOnTimeTv.setText(showStr);
                    mTimingPowerTest.startAlarm(TimingPowerTest.ACTION_TIMED_POWERON, hourOfDay, minute, second);
                } else if (TYPE_POWER_OFF == type) {
                    mPowerOffTimeTv.setText(showStr);
                    mTimingPowerTest.startAlarm(TimingPowerTest.ACTION_TIMED_POWEROFF, hourOfDay, minute, second);
                } else if (TYPE_REBOOT == type) {
                    mRebootTimeTv.setText(showStr);
                    mTimingPowerTest.startAlarm(TimingPowerTest.ACTION_TIMED_REBOOT, hourOfDay, minute, second);
                }
            }
        }, true);
        tpd.show(getFragmentManager(), "timepickerdialog");
    }

    private void showWatchdogDurationPickerDialog() {
        final EditText editText = new EditText(this);
        editText.setInputType(InputType.TYPE_CLASS_NUMBER);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("设置看门狗超时（单位：秒）");
        builder.setView(editText);
        builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String str = editText.getText().toString();
                if (!TextUtils.isEmpty(str)) {
                    mWatchdogTimeTv.setText(str);
                    mMcuTest.setWatchdogDuration(Integer.parseInt(str));
                }
                dialog.dismiss();
            }
        });
        final Dialog dialog = builder.create();
        dialog.show();
    }
}
