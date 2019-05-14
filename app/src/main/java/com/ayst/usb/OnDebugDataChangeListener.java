package com.ayst.usb;

/**
 * Created by ayst on 2014/11/7.
 */
public interface OnDebugDataChangeListener {
    void onDebugRtDataChange(int len);
    void onDebugData1Change();
    void onDebugData2Change();
}
