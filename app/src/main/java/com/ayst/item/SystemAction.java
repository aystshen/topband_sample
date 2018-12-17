package com.ayst.item;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

/**
 * Created by Administrator on 2018/11/6.
 */

public class SystemAction {

    public static void startApp(Context context, String packageName, int delay) {
        Intent intent = new Intent("com.android.action.COMMAND");
        intent.putExtra("command", 1002); // 启动APP
        intent.putExtra("delay", delay); // 延时
        Bundle bundle = new Bundle();
        bundle.putString("package_name", packageName); // 指定需要启动的APP包名
        intent.putExtras(bundle);
        context.sendBroadcast(intent);
    }

    public static void reboot(Context context, int delay) {
        Intent intent = new Intent("com.android.action.COMMAND");
        intent.putExtra("command", 1001); // 重启
        intent.putExtra("delay", delay); // 延时
        context.sendBroadcast(intent);
    }
}
