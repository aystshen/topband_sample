package com.ayst.sample;

import android.app.Dialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
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
import android.widget.Toast;
import android.widget.ToggleButton;

import com.ayst.item.CameraTest;
import com.ayst.item.GpioTest;
import com.ayst.item.McuTest;
import com.ayst.item.ShellTest;
import com.ayst.item.SilentInstall;
import com.ayst.item.TimingPowerTest;
import com.ayst.usb.OnDataChangeListener;
import com.ayst.usb.OnUsbConnectChangeListener;
import com.ayst.usb.UsbTransferServer;
import com.ayst.utils.AppUtil;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import java.util.ArrayList;
import java.util.HashMap;

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
    @BindView(R.id.btn_shutdown)
    Button mShutdownBtn;
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

    //Handler message
    private final static int MSG_USB_DATA_UPDATE = 1001;

    private GpioTest mGpioTest;
    private McuTest mMcuTest;
    private CameraTest mCameraTest;
    private TimingPowerTest mTimingPowerTest;
    private SensorManager mSensorManager;
    private SensorEventListener mAccSensorEventListener;
    private SensorEventListener mGyroSensorEventListener;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_USB_DATA_UPDATE:
                    Bundle bundle = msg.getData();
                    byte[] data = bundle.getByteArray("data");
                    int len = bundle.getInt("len");

                    break;
            }
        }
    };

    private UsbManager mUsbManager = null;
    private ArrayAdapter<String> mUsbDevicesAdapter = null;
    private ArrayList<UsbDevice> mUsbDevices = new ArrayList<>();
    private UsbDevice mCurUsbDevice = null;
    private UsbTransferServer mUsbService = null;
    ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "onServiceDisconnected");
            mUsbService = null;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "onServiceConnected");
            mUsbService = ((UsbTransferServer.UsbBinder) service).getService();
            initUsbServiceListener(mUsbService);
        }
    };

    private void initUsbServiceListener(UsbTransferServer usbService) {
        if (usbService != null) {
            Log.d(TAG, "initUsbServiceListener...");
            usbService.setOnDataChangeListener(new OnDataChangeListener() {
                @Override
                public void onDataChange(byte[] data, int len) {
                    Message msg = mHandler.obtainMessage(MSG_USB_DATA_UPDATE);
                    Bundle bundle = new Bundle();
                    bundle.putByteArray("data", data);
                    bundle.putInt("len", len);
                    msg.setData(bundle);
                    mHandler.sendMessage(msg);
                }
            });

            usbService.setOnUsbConnectChangeListener(new OnUsbConnectChangeListener() {
                @Override
                public void onUsbConnectChange(int status) {
                    switch (status) {
                        case UsbTransferServer.CONNECTED:
                            mUsbDataTv.setText("Connected");
                            break;
                        case UsbTransferServer.DISCONNECTED:
                            mUsbDataTv.setText("Disconnect");
                            break;
                        case UsbTransferServer.CONNECT_FAIL:
                            mUsbDataTv.setText("Connect fail");
                            break;
                    }
                }
            });
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_flexbox);
        ButterKnife.bind(this);

        mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);

        mGpioTest = new GpioTest(this);
        mMcuTest = new McuTest(this);
        mCameraTest = new CameraTest(this, mCameraLayout);
        mTimingPowerTest = new TimingPowerTest(this);

        initView();
        initSensors();
        getUsbDevices();

        Intent intent = new Intent().setClass(this, UsbTransferServer.class);
        bindService(intent, conn, Context.BIND_AUTO_CREATE);

        IntentFilter filter = new IntentFilter();
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        registerReceiver(mBroadcastReceiver, filter);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(conn);
        unregisterReceiver(mBroadcastReceiver);
        if (mSensorManager != null) {
            mSensorManager.unregisterListener(mAccSensorEventListener);
            mSensorManager.unregisterListener(mGyroSensorEventListener);
        }
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
                    mPowerOnTimeTv.setText("--:--");
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
                    mPowerOffTimeTv.setText("--:--");
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
                    mRebootTimeTv.setText("--:--");
                    mTimingPowerTest.stopAlarm(TimingPowerTest.ACTION_TIMED_REBOOT);
                }
            }
        });

        mUsbDevicesAdapter = new ArrayAdapter<>(this, R.layout.spinner_item);
        mUsbDeviceSpn.setAdapter(mUsbDevicesAdapter);
        mUsbDeviceSpn.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position < mUsbDevices.size()) {
                    mCurUsbDevice = mUsbDevices.get(position);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        mListenUsbBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    if (null != mUsbService && null != mCurUsbDevice) {
                        mUsbService.preConnect(mCurUsbDevice);
                    }
                } else {
                    if (null != mUsbService) {
                        mUsbService.disconnect();
                    }
                }
            }
        });
    }

    private void initSensors() {
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (null != mSensorManager) {
            Sensor accSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            if (accSensor != null) {
                mAccSensorEventListener = new SensorEventListener() {
                    @Override
                    public void onSensorChanged(SensorEvent event) {
                        float x = event.values[0];
                        float y = event.values[1];
                        float z = event.values[2];
                        mAccXTv.setText(x + "");
                        mAccYTv.setText(y + "");
                        mAccZTv.setText(z + "");
                    }

                    @Override
                    public void onAccuracyChanged(Sensor sensor, int accuracy) {

                    }
                };
                mSensorManager.registerListener(mAccSensorEventListener, accSensor, SensorManager.SENSOR_DELAY_NORMAL);
            }

            Sensor gyroSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
            if (gyroSensor != null) {
                mGyroSensorEventListener = new SensorEventListener() {
                    @Override
                    public void onSensorChanged(SensorEvent event) {
                        float x = event.values[0];
                        float y = event.values[1];
                        float z = event.values[2];
                        mGyroXTv.setText(x + "");
                        mGyroYTv.setText(y + "");
                        mGyroZTv.setText(z + "");
                    }

                    @Override
                    public void onAccuracyChanged(Sensor sensor, int accuracy) {

                    }
                };
                mSensorManager.registerListener(mGyroSensorEventListener, gyroSensor, SensorManager.SENSOR_DELAY_NORMAL);
            }
        }
    }

    @OnClick({R.id.btn_root_test, R.id.btn_silent_install, R.id.btn_reboot, R.id.btn_shutdown,
            R.id.btn_gpio, R.id.btn_set_watchdog_time, R.id.btn_heartbeat, R.id.btn_listen_usb})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_root_test:
                if (ShellTest.rootTest()) {
                    Toast.makeText(this, "success", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "fail", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.btn_silent_install:
                SilentInstall.install(this, "");
                break;
            case R.id.btn_reboot:
                AppUtil.reboot(this);
                break;
            case R.id.btn_shutdown:
                AppUtil.powerOff(this);
                break;
            case R.id.btn_gpio:
                Log.i(TAG, "onViewClicked, btn_gpio");
                if (mGpioOutputRdo.isChecked()) {
                    mGpioTest.gpioWrite(mGpioSpn.getSelectedItemPosition(), mGpioBtn.isChecked() ? 1 : 0);
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
                if (!isSensorActive) {
                    isSensorActive = true;
                    mSensorBtn.setChecked(true);
                }
                mHandler.removeCallbacks(mSensorClose);
                mHandler.postDelayed(mSensorClose, 3000);
            }
        } else if (mCurGpio >= 0 && event.getKeyCode() == GPIO_KEY_CODE[mCurGpio]) {
            mGpioBtn.setChecked(event.getAction() == KeyEvent.ACTION_UP);
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
        builder.setTitle("Set watchdog timeout (in seconds)");
        builder.setView(editText);
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
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

    private void getUsbDevices() {
        mUsbDevices.clear();
        HashMap<String, UsbDevice> deviceList = mUsbManager.getDeviceList();
        mUsbDevices.addAll(deviceList.values());

        if (!mUsbDevices.isEmpty()) {
            int curDeviceIndex = -1;
            String[] spinnerItems = new String[mUsbDevices.size()];
            for (int i = 0; i < mUsbDevices.size(); i++) {
                UsbDevice device = mUsbDevices.get(i);
                spinnerItems[i] = device.getManufacturerName()
                        + " " + device.getProductName()
                        + "(" + device.getProductId()
                        + "/" + device.getVendorId() + ")";

                if (null != mCurUsbDevice) {
                    if (mCurUsbDevice.getProductId() == device.getProductId()
                            && mCurUsbDevice.getVendorId() == device.getVendorId()) {
                        curDeviceIndex = i;
                    }
                }

                Log.d(TAG, "getUsbDevices, " + device.toString());
            }
            mUsbDevicesAdapter.clear();
            mUsbDevicesAdapter.addAll(spinnerItems);
            mUsbDevicesAdapter.notifyDataSetChanged();

            if (curDeviceIndex < 0) {
                mCurUsbDevice = mUsbDevices.get(0);
                mUsbDeviceSpn.setSelection(0);
            } else {
                mUsbDeviceSpn.setSelection(curDeviceIndex);
            }
        } else {
            mCurUsbDevice = null;
        }
    }

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public synchronized void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)
                    || UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {

                UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                Log.d(TAG, "mBroadcastReceiver: usb device venderid=" + device.getVendorId() + "productid=" + device.getProductId());

                getUsbDevices();
            }
        }
    };
}
