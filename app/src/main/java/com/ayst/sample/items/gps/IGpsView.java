package com.ayst.sample.items.gps;

import android.location.GpsStatus;
import android.location.Location;

public interface IGpsView {
    void updateGpsEnableStatus(Boolean status);
    void updateLocation(Location location);
    void updateSatellitesCount(int count);
}
