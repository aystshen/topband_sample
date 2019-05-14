package com.ayst.usb;

import java.util.Date;

/**
 * Created by ayst on 2014/10/26.
 */
public interface OnDataChangeListener {
    void onDataChange(byte[] data, int len);
}
