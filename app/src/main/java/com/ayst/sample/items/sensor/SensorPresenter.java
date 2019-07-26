package com.ayst.sample.items.sensor;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class SensorPresenter {
    private static final String TAG = "SensorPresenter";

    private Context mContext;
    private ISensorView mSensorView;
    private SensorManager mSensorManager;
    private SensorEventListener mAccSensorEventListener;
    private SensorEventListener mGyroSensorEventListener;
    private SensorEventListener mAdcSensorEventListener;
    private SensorEventListener mHumanSensorEventListener;

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
            }

            // Gyroscope Sensor
            Sensor gyroSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
            if (gyroSensor != null) {
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
            }

            // Adc Sensor
            Sensor adcSensor = mSensorManager.getDefaultSensor(26); // 26: Sensor.TYPE_ADC
            if (adcSensor != null) {
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
            }

            // Human Sensor
            Sensor humanSensor = mSensorManager.getDefaultSensor(27); // 27: Sensor.TYPE_HUMAN
            if (humanSensor != null) {
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
