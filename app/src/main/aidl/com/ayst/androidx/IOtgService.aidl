// IOtgService.aidl
package com.ayst.androidx;

// Declare any non-default types here with import statements

interface IOtgService {
    boolean setOtgMode(String mode);
    boolean setOtgModeExt(String mode, boolean save);
    String getOtgMode();
}
