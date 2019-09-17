package com.ayst.sample.items.watchdog;

public interface IWatchdogView {
    void updateCountdown(int countdown);
    void updateWatchdogState(boolean isOpen, int duration);
}
