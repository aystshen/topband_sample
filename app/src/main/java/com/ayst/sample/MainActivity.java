package com.ayst.sample;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ToggleButton;

import com.ayst.item.CameraTest;
import com.ayst.item.GpioTest;
import com.ayst.item.ShellTest;
import com.ayst.item.SilentInstall;
import com.ayst.item.SystemAction;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class MainActivity extends AppCompatActivity {
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

    private boolean isSensorActive = false;
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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.CAMERA}, 1);
            }
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

        mGpioBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                mGpioTest.gpioCtrl(2, b ? 1 : 0);
            }
        });
    }

    @OnClick({R.id.btn_root_test, R.id.btn_silent_install, R.id.btn_reboot, R.id.btn_start_app})
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
}
