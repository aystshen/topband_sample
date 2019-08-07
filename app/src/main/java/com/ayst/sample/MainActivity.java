package com.ayst.sample;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.SurfaceView;
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

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.ayst.sample.items.camera.CameraPresenter;
import com.ayst.sample.items.camera.ICameraView;
import com.ayst.sample.items.gpio.GpioPresenter;
import com.ayst.sample.items.gpio.IGpioView;
import com.ayst.sample.items.modem.IModemView;
import com.ayst.sample.items.modem.ModemPresenter;
import com.ayst.sample.items.other.IOtherView;
import com.ayst.sample.items.other.OtherPresenter;
import com.ayst.sample.items.resumebyalarm.ResumeByAlarmPresenter;
import com.ayst.sample.items.sensor.ISensorView;
import com.ayst.sample.items.sensor.SensorPresenter;
import com.ayst.sample.items.usb.IUsbView;
import com.ayst.sample.items.usb.UsbPresenter;
import com.ayst.sample.items.watchdog.IWatchdogView;
import com.ayst.sample.items.watchdog.WatchdogPresenter;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class MainActivity extends AppCompatActivity implements
        CompoundButton.OnCheckedChangeListener, IOtherView,
        ICameraView, ISensorView, IGpioView, IWatchdogView,
        IModemView, IUsbView {
    private static final String TAG = "Sample";

    @BindView(R.id.btn_root_test)
    Button mRootTestBtn;
    @BindView(R.id.btn_silent_install)
    Button mSilentInstallBtn;
    @BindView(R.id.btn_reboot)
    Button mRebootBtn;
    @BindView(R.id.btn_shutdown)
    Button mShutdownBtn;
    @BindView(R.id.btn_gpio)
    ToggleButton mGpioBtn;
    @BindView(R.id.btn_camera)
    ToggleButton mCameraBtn;
    @BindView(R.id.tv_camera_info)
    TextView mCameraInfoTv;
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
    @BindView(R.id.spn_usb)
    Spinner mUsbDeviceSpn;
    @BindView(R.id.btn_listen_usb)
    ToggleButton mListenUsbBtn;
    @BindView(R.id.tv_usb_data)
    TextView mUsbDataTv;
    @BindView(R.id.tv_acc_x)
    TextView mAccXTv;
    @BindView(R.id.tv_acc_y)
    TextView mAccYTv;
    @BindView(R.id.tv_acc_z)
    TextView mAccZTv;
    @BindView(R.id.tv_gyro_x)
    TextView mGyroXTv;
    @BindView(R.id.tv_gyro_y)
    TextView mGyroYTv;
    @BindView(R.id.tv_gyro_z)
    TextView mGyroZTv;
    @BindView(R.id.tv_voltage)
    TextView mVoltageTv;
    @BindView(R.id.btn_modem_power)
    ToggleButton mModemPowerBtn;
    @BindView(R.id.btn_modem_wakeup)
    ToggleButton mModemWakeupBtn;
    @BindView(R.id.btn_modem_reset)
    Button mModemResetBtn;
    @BindView(R.id.tv_root_result)
    TextView mRootResultTv;
    @BindView(R.id.tv_human)
    TextView mHumanTv;
    @BindView(R.id.btn_screen)
    ToggleButton mScreenBtn;

    private static final int TYPE_POWER_ON = 0;
    private static final int TYPE_POWER_OFF = 1;
    private static final int TYPE_REBOOT = 2;

    private int mTimePickerType = TYPE_POWER_ON;

    private OtherPresenter mOtherPresenter;
    private CameraPresenter mCameraPresenter;
    private SensorPresenter mSensorPresenter;
    private GpioPresenter mGpioPresenter;
    private WatchdogPresenter mWatchdogPresenter;
    private ResumeByAlarmPresenter mResumeByAlarmPresenter;
    private ModemPresenter mModemPresenter;
    private UsbPresenter mUsbPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate");
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main_flexbox);
        ButterKnife.bind(this);

        mOtherPresenter = new OtherPresenter(this, this);
        mCameraPresenter = new CameraPresenter(this, this);
        mSensorPresenter = new SensorPresenter(this, this);
        mGpioPresenter = new GpioPresenter(this, this);
        mWatchdogPresenter = new WatchdogPresenter(this, this);
        mResumeByAlarmPresenter = new ResumeByAlarmPresenter(this);
        mModemPresenter = new ModemPresenter(this, this);
        mUsbPresenter = new UsbPresenter(this, this);

        initView();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mSensorPresenter.start();
        mResumeByAlarmPresenter.start();
        mModemPresenter.start();
        mUsbPresenter.start();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mSensorPresenter.stop();
        mResumeByAlarmPresenter.stop();
        mModemPresenter.stop();
        mUsbPresenter.stop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void initView() {
        mCameraBtn.setOnCheckedChangeListener(this);
        mWatchdogBtn.setOnCheckedChangeListener(this);
        mTimingPowerOnBtn.setOnCheckedChangeListener(this);
        mTimingPowerOffBtn.setOnCheckedChangeListener(this);
        mTimingRebootBtn.setOnCheckedChangeListener(this);
        mScreenBtn.setOnCheckedChangeListener(this);

        // init gpio
        if (mGpioPresenter.getNumber() > 0) {
            String[] spinnerItems = new String[mGpioPresenter.getNumber()];
            for (int i = 0; i < mGpioPresenter.getNumber(); i++) {
                spinnerItems[i] = "GPIO_" + i;
            }
            ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this,
                    R.layout.spinner_item, spinnerItems);
            mGpioSpn.setAdapter(spinnerAdapter);

            mGpioPresenter.setSelected(mGpioSpn.getSelectedItemPosition());
            mGpioSpn.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                    mGpioPresenter.setSelected(position);
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

        // init usb
        mUsbDeviceSpn.setAdapter(mUsbPresenter.getAdapter());
        mUsbDeviceSpn.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mUsbPresenter.setSelectedDevice(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        mListenUsbBtn.setOnCheckedChangeListener(this);
    }

    @OnClick({R.id.btn_root_test, R.id.btn_silent_install, R.id.btn_reboot, R.id.btn_shutdown,
            R.id.btn_gpio, R.id.btn_set_watchdog_time, R.id.btn_heartbeat, R.id.btn_modem_reset})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_root_test:
                mOtherPresenter.root();
                break;
            case R.id.btn_silent_install:
                mOtherPresenter.silentInstall("");
                break;
            case R.id.btn_reboot:
                mOtherPresenter.reboot();
                break;
            case R.id.btn_shutdown:
                mOtherPresenter.shutdown();
                break;
            case R.id.btn_gpio:
                if (mGpioPresenter.getMode() == GpioPresenter.Mode.OUTPUT) {
                    mGpioPresenter.write(mGpioBtn.isChecked() ? 1 : 0);
                } else {
                    mGpioBtn.setChecked(!mGpioBtn.isChecked());
                }
                break;
            case R.id.btn_set_watchdog_time:
                showWatchdogDurationPickerDialog();
                break;
            case R.id.btn_heartbeat:
                mWatchdogPresenter.sendHeartbeat();
                break;
            case R.id.btn_modem_reset:
                mModemPresenter.reset();
                break;
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        switch (compoundButton.getId()) {
            case R.id.btn_camera:
                if (b) {
                    mCameraPresenter.start();
                } else {
                    mCameraPresenter.stop();
                }
                break;
            case R.id.rdo_gpio_input:
                if (b) {
                    mGpioPresenter.setMode(GpioPresenter.Mode.INPUT);
                }
                break;
            case R.id.rdo_gpio_output:
                if (b) {
                    mGpioPresenter.setMode(GpioPresenter.Mode.OUTPUT);
                }
                break;
            case R.id.rdo_gpio_key:
                if (b) {
                    mGpioPresenter.setMode(GpioPresenter.Mode.KEY);
                }
                break;
            case R.id.btn_switch_watchdog:
                if (b) {
                    mWatchdogPresenter.openWatchdog();
                } else {
                    mWatchdogPresenter.closeWatchdog();
                }
                break;
            case R.id.btn_set_power_on_time:
                if (b) {
                    showTimePikerDialog(TYPE_POWER_ON);
                } else {
                    mPowerOnTimeTv.setText("--:--");
                    mResumeByAlarmPresenter.setUptime(0);
                }
                break;
            case R.id.btn_set_power_off_time:
                if (b) {
                    showTimePikerDialog(TYPE_POWER_OFF);
                } else {
                    mPowerOffTimeTv.setText("--:--");
                    mResumeByAlarmPresenter.stopAlarm(ResumeByAlarmPresenter.ACTION_TIMED_POWEROFF);
                }
                break;
            case R.id.btn_set_reboot:
                if (b) {
                    showTimePikerDialog(TYPE_REBOOT);
                } else {
                    mRebootTimeTv.setText("--:--");
                    mResumeByAlarmPresenter.stopAlarm(ResumeByAlarmPresenter.ACTION_TIMED_REBOOT);
                }
                break;
            case R.id.btn_listen_usb:
                if (b) {
                    mUsbPresenter.connect();
                } else {
                    mUsbPresenter.disconnect();
                }
                break;
            case R.id.btn_screen:
                if (b) {
                    mOtherPresenter.screenOn();
                } else {
                    mOtherPresenter.screenOff();
                }
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        Log.i(TAG, "dispatchKeyEvent, keyCode=" + event.getKeyCode() + " action=" + event.getAction());
        if (mGpioPresenter.getMode() == GpioPresenter.Mode.KEY) {
            mGpioPresenter.checkKeyEvent(event);
        }

        return super.dispatchKeyEvent(event);
    }

    private void showTimePikerDialog(final int type) {
        TimePickerDialog tpd = TimePickerDialog.newInstance(new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePickerDialog view, int hourOfDay, int minute, int second) {
                String showStr = String.format("%02d", hourOfDay) + ":" + String.format("%02d", minute) + ":" + String.format("%02d", second);
                if (TYPE_POWER_ON == type) {
                    mPowerOnTimeTv.setText(showStr);
                    mResumeByAlarmPresenter.startAlarm(ResumeByAlarmPresenter.ACTION_TIMED_POWERON, hourOfDay, minute, second);
                } else if (TYPE_POWER_OFF == type) {
                    mPowerOffTimeTv.setText(showStr);
                    mResumeByAlarmPresenter.startAlarm(ResumeByAlarmPresenter.ACTION_TIMED_POWEROFF, hourOfDay, minute, second);
                } else if (TYPE_REBOOT == type) {
                    mRebootTimeTv.setText(showStr);
                    mResumeByAlarmPresenter.startAlarm(ResumeByAlarmPresenter.ACTION_TIMED_REBOOT, hourOfDay, minute, second);
                }
            }
        }, true);
        tpd.show(getFragmentManager(), "timepickerdialog");
    }

    private void showWatchdogDurationPickerDialog() {
        final EditText editText = new EditText(this);
        editText.setInputType(InputType.TYPE_CLASS_NUMBER);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Set watchdog timeout (in seconds)");
        builder.setView(editText);
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String str = editText.getText().toString();
                if (!TextUtils.isEmpty(str)) {
                    mWatchdogTimeTv.setText(str + " seconds");
                    mWatchdogPresenter.setTimeout(Integer.parseInt(str));
                }
                dialog.dismiss();
            }
        });
        final Dialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public void updateRootResult(boolean result) {
        mRootResultTv.setText(result ? "success" : "failed");
    }

    @Override
    public void removeCameraView() {
        mCameraLayout.removeAllViews();
    }

    @Override
    public void addCameraView(SurfaceView surface) {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(300, 225);
        mCameraLayout.addView(surface, layoutParams);
    }

    @Override
    public void updateCameraInfo(String info) {
        mCameraInfoTv.setText(info);
    }

    @Override
    public void updateAccSensorData(float x, float y, float z) {
        mAccXTv.setText(x + "");
        mAccYTv.setText(y + "");
        mAccZTv.setText(z + "");
    }

    @Override
    public void updateGyroSensorData(float x, float y, float z) {
        mGyroXTv.setText(x + "");
        mGyroYTv.setText(y + "");
        mGyroZTv.setText(z + "");
    }

    @Override
    public void updateAdcSensorData(float value) {
        mVoltageTv.setText(value + "");
    }

    @Override
    public void updateHumanSensorData(float value) {
        mHumanTv.setText(value + "");
    }

    @Override
    public void updateGpioStatus(boolean level) {
        mGpioBtn.setChecked(level);
    }

    @Override
    public void updateCountdown(int countdown) {
        mWatchdogTimeTv.setText(countdown + " seconds");
    }

    @Override
    public void updateModemStatus(boolean isPowerOn, boolean isWakeup) {
        mModemPowerBtn.setChecked(isPowerOn);
        mModemWakeupBtn.setChecked(isWakeup);
    }

    @Override
    public void updateUsbData(String data) {
        mUsbDataTv.append(data + " ");
    }

    @Override
    public void updateUsbDeviceList(int position) {
        mUsbDeviceSpn.setSelection(position);
    }
}
