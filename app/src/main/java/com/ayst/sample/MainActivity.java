package com.ayst.sample;

import android.app.Dialog;
import android.content.DialogInterface;
import android.media.MediaMetadata;
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
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.ayst.sample.items.a2dpsink.A2dpSinkPresenter;
import com.ayst.sample.items.a2dpsink.IA2dpSinkView;
import com.ayst.sample.items.androidx.AndroidXPresenter;
import com.ayst.sample.items.androidx.IAndroidXView;
import com.ayst.sample.items.backlight.BacklightPresenter;
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
        IModemView, IUsbView, IA2dpSinkView, SeekBar.OnSeekBarChangeListener, IAndroidXView {
    private static final String TAG = "Sample";

    private static final int TYPE_POWER_ON = 0;
    private static final int TYPE_POWER_OFF = 1;
    private static final int TYPE_REBOOT = 2;
    private int mTimePickerType = TYPE_POWER_ON;

    @BindView(R.id.btn_root_test)
    Button mRootTestBtn;
    @BindView(R.id.btn_silent_install)
    Button mSilentInstallBtn;
    @BindView(R.id.btn_reboot)
    Button mRebootBtn;
    @BindView(R.id.btn_shutdown)
    Button mShutdownBtn;
    @BindView(R.id.btn_factory_reset)
    Button mFactoryResetBtn;
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
    @BindView(R.id.tv_a2dpsink_media_info)
    TextView mA2dpSinkMediaInfoTv;
    @BindView(R.id.seekbar_main_backlight)
    SeekBar mMainBacklightSeekbar;
    @BindView(R.id.seekbar_sub_backlight)
    SeekBar mSubBacklightSeekbar;
    @BindView(R.id.btn_keycode)
    ToggleButton mKeycodeBtn;
    @BindView(R.id.btn_fullscreen)
    ToggleButton mFullscreenBtn;
    @BindView(R.id.btn_systembar)
    ToggleButton mSystembarBtn;
    @BindView(R.id.btn_androidx_4g)
    ToggleButton mAndroidx4gBtn;
    @BindView(R.id.btn_androidx_watchdog)
    ToggleButton mAndroidxWatchdogBtn;
    @BindView(R.id.btn_androidx_watchdog_timeout)
    Button mAndroidxWatchdogTimeoutBtn;

    private OtherPresenter mOtherPresenter;
    private CameraPresenter mCameraPresenter;
    private SensorPresenter mSensorPresenter;
    private GpioPresenter mGpioPresenter;
    private WatchdogPresenter mWatchdogPresenter;
    private ResumeByAlarmPresenter mResumeByAlarmPresenter;
    private ModemPresenter mModemPresenter;
    private UsbPresenter mUsbPresenter;
    private A2dpSinkPresenter mA2dpSinkPresenter;
    private BacklightPresenter mBacklightPresenter;
    private AndroidXPresenter mAndroidXPresenter;

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
        mA2dpSinkPresenter = new A2dpSinkPresenter(this, this);
        mBacklightPresenter = new BacklightPresenter(this);
        mAndroidXPresenter = new AndroidXPresenter(this, this);

        initView();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mSensorPresenter.start();
        mResumeByAlarmPresenter.start();
        mModemPresenter.start();
        mUsbPresenter.start();
        mA2dpSinkPresenter.start();
        mWatchdogPresenter.start();
        mAndroidXPresenter.start();
        mOtherPresenter.start();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mSensorPresenter.stop();
        mResumeByAlarmPresenter.stop();
        mModemPresenter.stop();
        mUsbPresenter.stop();
        mA2dpSinkPresenter.stop();
        mWatchdogPresenter.stop();
        mAndroidXPresenter.stop();
        mOtherPresenter.stop();
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
        mFullscreenBtn.setOnCheckedChangeListener(this);
        mSystembarBtn.setOnCheckedChangeListener(this);
        mMainBacklightSeekbar.setOnSeekBarChangeListener(this);
        mSubBacklightSeekbar.setOnSeekBarChangeListener(this);

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
            R.id.btn_gpio, R.id.btn_set_watchdog_time, R.id.btn_heartbeat, R.id.btn_modem_reset,
            R.id.btn_switch_watchdog, R.id.btn_androidx_4g, R.id.btn_androidx_watchdog,
            R.id.btn_androidx_watchdog_timeout, R.id.btn_factory_reset})
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
            case R.id.btn_factory_reset:
                mOtherPresenter.factoryReset();
                break;
            case R.id.btn_gpio:
                if (mGpioPresenter.getMode() == GpioPresenter.Mode.OUTPUT) {
                    mGpioPresenter.write(mGpioBtn.isChecked() ? 1 : 0);
                } else {
                    mGpioBtn.setChecked(!mGpioBtn.isChecked());
                }
                break;
            case R.id.btn_set_watchdog_time:
                showWatchdogDurationPickerDialog(false);
                break;
            case R.id.btn_heartbeat:
                mWatchdogPresenter.sendHeartbeat();
                break;
            case R.id.btn_modem_reset:
                mModemPresenter.reset();
                break;
            case R.id.btn_switch_watchdog:
                if (mWatchdogBtn.isChecked()) {
                    mWatchdogPresenter.openWatchdog();
                } else {
                    mWatchdogPresenter.closeWatchdog();
                }
                break;
            case R.id.btn_androidx_4g:
                mAndroidXPresenter.toggle4GKeepLive(mAndroidx4gBtn.isChecked());
                break;
            case R.id.btn_androidx_watchdog:
                mAndroidXPresenter.toggleWatchdog(mAndroidxWatchdogBtn.isChecked());
                break;
            case R.id.btn_androidx_watchdog_timeout:
                showWatchdogDurationPickerDialog(true);
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
//                if (b) {
//                    mWatchdogPresenter.openWatchdog();
//                } else {
//                    mWatchdogPresenter.closeWatchdog();
//                }
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
                break;
            case R.id.btn_fullscreen:
                if (b) {
                    mOtherPresenter.fullScreen(this);
                } else {
                    mOtherPresenter.exitFullScreen(this);
                }
                break;
            case R.id.btn_systembar:
                if (b) {
                    mOtherPresenter.hideSystemBar();
                } else {
                    mOtherPresenter.showSystemBar();
                }
                break;
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        switch (seekBar.getId()) {
            case R.id.seekbar_main_backlight:
                mBacklightPresenter.setMainBrightness(seekBar.getProgress());
                break;
            case R.id.seekbar_sub_backlight:
                mBacklightPresenter.setSubBrightness(seekBar.getProgress());
                break;
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        Log.i(TAG, "dispatchKeyEvent, keyCode=" + event.getKeyCode() + " action=" + event.getAction());
        if (mGpioPresenter.getMode() == GpioPresenter.Mode.KEY) {
            mGpioPresenter.checkKeyEvent(event);
        }

        mKeycodeBtn.setTextOff("KEY: " + event.getKeyCode());
        mKeycodeBtn.setTextOn("KEY: " + event.getKeyCode());
        mKeycodeBtn.setChecked(event.getAction() == KeyEvent.ACTION_DOWN);

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

    private void showWatchdogDurationPickerDialog(final boolean androidx) {
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
                    if (androidx) {
                        mAndroidXPresenter.setWatchdogTimeout(Integer.parseInt(str));
                    } else {
                        mWatchdogTimeTv.setText(str + " seconds");
                        mWatchdogPresenter.setTimeout(Integer.parseInt(str));
                    }
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
    public void updateWatchdogState(boolean isOpen, int duration) {
        mWatchdogBtn.setChecked(isOpen);
        mSetWatchdogDurationBtn.setText("Set the timeout: " + duration);
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

    @Override
    public void updateA2dpSinkMediaInfo(MediaMetadata info) {
        StringBuilder sb = new StringBuilder();
        sb.append("title: ").append(info.getString(MediaMetadata.METADATA_KEY_TITLE)).append("\n");
        sb.append("artist: ").append(info.getString(MediaMetadata.METADATA_KEY_ARTIST)).append("\n");
        sb.append("album: ").append(info.getString(MediaMetadata.METADATA_KEY_ALBUM)).append("\n");
        sb.append("author: ").append(info.getString(MediaMetadata.METADATA_KEY_AUTHOR)).append("\n");
        sb.append("writer: ").append(info.getString(MediaMetadata.METADATA_KEY_WRITER)).append("\n");
        sb.append("composer: ").append(info.getString(MediaMetadata.METADATA_KEY_COMPOSER)).append("\n");
        sb.append("compilation: ").append(info.getString(MediaMetadata.METADATA_KEY_COMPILATION)).append("\n");
        sb.append("date: ").append(info.getString(MediaMetadata.METADATA_KEY_DATE)).append("\n");
        sb.append("genre: ").append(info.getString(MediaMetadata.METADATA_KEY_GENRE)).append("\n");
        sb.append("album artist: ").append(info.getString(MediaMetadata.METADATA_KEY_ALBUM_ARTIST)).append("\n");
        sb.append("display title: ").append(info.getString(MediaMetadata.METADATA_KEY_DISPLAY_TITLE)).append("\n");
        sb.append("sub title: ").append(info.getString(MediaMetadata.METADATA_KEY_DISPLAY_SUBTITLE)).append("\n");
        sb.append("description: ").append(info.getString(MediaMetadata.METADATA_KEY_DISPLAY_DESCRIPTION)).append("\n");

        mA2dpSinkMediaInfoTv.setText(sb.toString());
    }

    @Override
    public void updateAndroidX4GState(boolean enable) {
        mAndroidx4gBtn.setChecked(enable);
    }

    @Override
    public void updateAndroidXWatchdogState(boolean enable) {
        mAndroidxWatchdogBtn.setChecked(enable);
    }

    @Override
    public void updateAndroidXWatchdogTimeout(int timeout) {
        mAndroidxWatchdogTimeoutBtn.setText("Watchdog Timeout: " + timeout + " s");
    }
}
