package com.ayst.item;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.SystemMcu;
import android.text.TextUtils;
import android.util.Log;

import java.util.Calendar;

/**
 * Created by Administrator on 2019/1/25.
 */

public class TimingPowerTest {
    private static final String TAG = "TimingPowerTest";

    public static final long MILLIS_OF_DAY = (24 * 60 * 60 * 1000);

    public static final String ACTION_TIMED_POWEROFF = "com.ayst.sample.timed_power_off";
    public static final String ACTION_TIMED_POWERON = "com.ayst.sample.timed_power_on";
    public static final String ACTION_TIMED_REBOOT = "com.ayst.sample.timed_reboot";

    private Context mContext;
    private SystemMcu mMcuService;
    private AlarmManager mAlarmManager;

    @SuppressLint("WrongConstant")
    public TimingPowerTest(Context context) {
        mContext = context;
        mMcuService = (SystemMcu) context.getSystemService("mcu");
    }

    public void registerReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_TIMED_POWEROFF);
        filter.addAction(ACTION_TIMED_POWERON);
        filter.addAction(ACTION_TIMED_REBOOT);
        mContext.registerReceiver(mAlarmReceiver, filter);
    }

    public void unRegisterReceiver() {
        mContext.unregisterReceiver(mAlarmReceiver);
    }

    public void startAlarm(String action, int hourOfDay, int minute, int second) {
        if (TextUtils.isEmpty(action)) {
            return;
        }

        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(System.currentTimeMillis());
        int curHour = cal.get(Calendar.HOUR_OF_DAY);
        int curMin = cal.get(Calendar.MINUTE);
        int curSec = cal.get(Calendar.SECOND);

        long curMillis = (((curHour * 60) + curMin) * 60 + curSec) * 1000;
        long timingMillis = (((hourOfDay * 60) + minute) * 60 + second) * 1000;
        long differMillis = timingMillis - curMillis;

        long alarmMillis = 0;
        if (differMillis > 0) {
            Log.i(TAG, "setAlarm, action: " + action + ", " +
                    "today->" +
                    String.format("%02d", hourOfDay) + ":"
                    + String.format("%02d", minute) + ":"
                    + String.format("%02d", second));

            alarmMillis = System.currentTimeMillis() + differMillis;
        } else {
            Log.i(TAG, "setAlarm, action: " + action + ", " +
                    "tomorrow->" +
                    String.format("%02d", hourOfDay) + ":"
                    + String.format("%02d", minute) + ":"
                    + String.format("%02d", second));

            alarmMillis = System.currentTimeMillis() + differMillis + MILLIS_OF_DAY;
        }

        if (TextUtils.equals(ACTION_TIMED_POWERON, action)) {
            setUptime((int) (alarmMillis / 1000));
        } else {
            setAlarm(action, alarmMillis);
        }
    }


    private void setAlarm(String action, long time) {
        stopAlarm(action);
        if (null == mAlarmManager) {
            mAlarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
        }
        PendingIntent intent = PendingIntent.getBroadcast(mContext, 0, new Intent(action), 0);

        mAlarmManager.setExact(AlarmManager.RTC, time, intent);
    }

    public void stopAlarm(String action) {
        PendingIntent intent = PendingIntent.getBroadcast(mContext, 0, new Intent(action), 0);

        if (null == mAlarmManager) {
            mAlarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
        }
        mAlarmManager.cancel(intent);
    }


    /**
     * Set the boot countdown
     *
     * @param time (unit: second)
     * @return <0：error
     */
    public int setUptime(int time) {
        if (null != mMcuService) {
            return mMcuService.setUptime(time);
        }
        return -1;
    }

    private void reboot() {
        Intent intent = new Intent(Intent.ACTION_REBOOT);
        intent.putExtra("nowait", 1);
        intent.putExtra("interval", 1);
        intent.putExtra("window", 0);
        mContext.sendBroadcast(intent);
    }

    private void powerOff() {
        Intent intent = new Intent("com.android.internal.intent.action.REQUEST_SHUTDOWN");
        intent.putExtra("android.intent.extra.KEY_CONFIRM", false);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(intent);
    }

    private BroadcastReceiver mAlarmReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.i(TAG, "Alarm receiver action: " + action);
            if (TextUtils.equals(ACTION_TIMED_POWEROFF, action)) {
                powerOff();
            } else if (TextUtils.equals(ACTION_TIMED_REBOOT, action)) {
                reboot();
            }
        }
    };
}
