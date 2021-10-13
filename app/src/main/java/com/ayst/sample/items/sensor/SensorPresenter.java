package com.ayst.sample.items.sensor;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.util.Log;

public class SensorPresenter {
    private static final String TAG = "SensorPresenter";

    private Context mContext;
    private ISensorView mSensorView;
    private SensorManager mSensorManager;
    private SensorEventListener mAccSensorEventListener;
    private SensorEventListener mGyroSensorEventListener;
    private SensorEventListener mAdcSensorEventListener;
    private SensorEventListener mHumanSensorEventListener;
    private SensorEventListener mLightSensorEventListener;
    private SensorEventListener mProximitySensorEventListener;

    public SensorPresenter(Context context, ISensorView view) {
        mContext = context;
        mSensorView = view;
        mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
    }

    public void start() {
        if (null != mSensorManager) {
            // Accelerometer Sensor
            Sensor accSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            if (accSensor != null) {
                Log.i(TAG, "accSensor>>>>");
                mAccSensorEventListener = new SensorEventListener() {
                    @Override
                    public void onSensorChanged(SensorEvent event) {
                        float x = event.values[0];
                        float y = event.values[1];
                        float z = event.values[2];
                        mSensorView.updateAccSensorData(x, y, z);
                    }

                    @Override
                    public void onAccuracyChanged(Sensor sensor, int accuracy) {

                    }
                };
                mSensorManager.registerListener(mAccSensorEventListener, accSensor, SensorManager.SENSOR_DELAY_NORMAL);
            } else {
                Log.e(TAG, "accSensor is null");
            }

            // Gyroscope Sensor
            Sensor gyroSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
            if (gyroSensor != null) {
                Log.i(TAG, "gyroSensor>>>>");
                mGyroSensorEventListener = new SensorEventListener() {
                    @Override
                    public void onSensorChanged(SensorEvent event) {
                        float x = event.values[0];
                        float y = event.values[1];
                        float z = event.values[2];
                        mSensorView.updateGyroSensorData(x, y, z);
                    }

                    @Override
                    public void onAccuracyChanged(Sensor sensor, int accuracy) {

                    }
                };
                mSensorManager.registerListener(mGyroSensorEventListener, gyroSensor, SensorManager.SENSOR_DELAY_NORMAL);
            } else {
                Log.e(TAG, "gyroSensor is null");
            }

            // Adc Sensor
            Sensor adcSensor = mSensorManager.getDefaultSensor(38); // 26: Sensor.TYPE_ADC
            if (adcSensor != null) {
                Log.i(TAG, "adcSensor>>>>");
                mAdcSensorEventListener = new SensorEventListener() {
                    @Override
                    public void onSensorChanged(SensorEvent event) {
                        float voltage = event.values[0];
                        mSensorView.updateAdcSensorData(voltage);
                    }

                    @Override
                    public void onAccuracyChanged(Sensor sensor, int accuracy) {

                    }
                };
                mSensorManager.registerListener(mAdcSensorEventListener, adcSensor, SensorManager.SENSOR_DELAY_NORMAL);
            } else {
                Log.e(TAG, "adcSensor is null");
            }

            // Human Sensor
            // 27: Sensor.TYPE_HUMAN for Android 5.1
            // 36: Sensor.TYPE_HUMAN for Android 8.1
            // 37: Sensor.TYPE_HUMAN for Android 11.0
            int humanSensorType = 36;
            if (Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP_MR1) { // Android 5.1
                humanSensorType = 27;
            } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.O_MR1) { // Android 8.1
                humanSensorType = 36;
            } else if (Build.VERSION.SDK_INT == 30) { // Android 11
                humanSensorType = 37;
            }
            Sensor humanSensor = mSensorManager.getDefaultSensor(humanSensorType);
            if (humanSensor != null) {
                Log.i(TAG, "humanSensor>>>>");
                mHumanSensorEventListener = new SensorEventListener() {
                    @Override
                    public void onSensorChanged(SensorEvent event) {
                        float human = event.values[0];
                        mSensorView.updateHumanSensorData(human);
                    }

                    @Override
                    public void onAccuracyChanged(Sensor sensor, int accuracy) {

                    }
                };
                mSensorManager.registerListener(mHumanSensorEventListener, humanSensor, SensorManager.SENSOR_DELAY_NORMAL);
            } else {
                Log.e(TAG, "humanSensor is null");
            }

            // Light Sensor
            Sensor lightSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
            if (lightSensor != null) {
                Log.i(TAG, "lightSensor>>>>");
                mLightSensorEventListener = new SensorEventListener() {
                    @Override
                    public void onSensorChanged(SensorEvent event) {
                        float light = event.values[0];
                        mSensorView.updateLightSensorData(light);
                    }

                    @Override
                    public void onAccuracyChanged(Sensor sensor, int accuracy) {

                    }
                };
                mSensorManager.registerListener(mLightSensorEventListener, lightSensor, SensorManager.SENSOR_DELAY_NORMAL);
            } else {
                Log.e(TAG, "lightSensor is null");
            }

            // Proximity Sensor
            Sensor proximitySensor = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
            if (proximitySensor != null) {
                Log.i(TAG, "proximitySensor>>>>");
                mProximitySensorEventListener = new SensorEventListener() {
                    @Override
                    public void onSensorChanged(SensorEvent event) {
                        float proximity = event.values[0];
                        mSensorView.updateProximitySensorData(proximity);
                    }

                    @Override
                    public void onAccuracyChanged(Sensor sensor, int accuracy) {

                    }
                };
                mSensorManager.registerListener(mProximitySensorEventListener, proximitySensor, SensorManager.SENSOR_DELAY_NORMAL);
            } else {
                Log.e(TAG, "proximitySensor is null");
            }
        }
    }

    public void stop() {
        if (null != mSensorManager) {
            if (null != mAccSensorEventListener) {
                mSensorManager.unregisterListener(mAccSensorEventListener);
            }
            if (null != mGyroSensorEventListener) {
                mSensorManager.unregisterListener(mGyroSensorEventListener);
            }
            if (null != mAdcSensorEventListener) {
                mSensorManager.unregisterListener(mAdcSensorEventListener);
            }
        }
    }
}
