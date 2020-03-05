package com.ayst.sample.items.androidx;

public interface IAndroidXView {
    public void updateAndroidX4GState(boolean enable);
    public void updateAndroidXWatchdogState(boolean enable);
    public void updateAndroidXWatchdogTimeout(int timeout);
    public void updateAndroidXOtgMode(String mode);
    public void updateAndroidXKeyIntercept(boolean enable);
    public void updateAndroidXLog2file(boolean enable);
}
