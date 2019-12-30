package com.ayst.sample.items.gps;

import android.content.Context;
import android.location.Criteria;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import java.util.Iterator;

public class GpsPresenter {
    private IGpsView mGpsView;
    private LocationManager mLocationManager;
    private GpsStatus.Listener mStatusListener;
    private LocationListener mLocationListener;

    public GpsPresenter(Context context, IGpsView view) {
        mGpsView = view;
        mLocationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
    }

    public void start() {
        mGpsView.updateGpsEnableStatus(mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER));

        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE); // 定位精度: 最高
        criteria.setAltitudeRequired(false); // 海拔信息：不需要
        criteria.setBearingRequired(false); // 方位信息: 不需要
        criteria.setCostAllowed(true); // 是否允许付费
        criteria.setPowerRequirement(Criteria.POWER_LOW); // 耗电量: 低功耗
        String provider = mLocationManager.getBestProvider(criteria, true); // 获取GPS信息

        mStatusListener = new GpsStatus.Listener() {
            @Override
            public void onGpsStatusChanged(int event) {
                switch (event) {
                    case GpsStatus.GPS_EVENT_SATELLITE_STATUS: {
                        GpsStatus status = mLocationManager.getGpsStatus(null);
                        int maxSatellites = status.getMaxSatellites();
                        Iterator<GpsSatellite> iters = status.getSatellites().iterator();
                        int count = 0;
                        while (iters.hasNext() && count <= maxSatellites) {
                            GpsSatellite s = iters.next();
                            count++;
                        }
                        mGpsView.updateSatellitesCount(count);
                    }
                }
            }
        };
        mLocationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                mGpsView.updateLocation(location);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };
        mLocationManager.requestLocationUpdates(provider, 1000, 0, mLocationListener);
        mLocationManager.addGpsStatusListener(mStatusListener);
    }

    public void stop() {
        mLocationManager.removeGpsStatusListener(mStatusListener);
        mLocationManager.removeUpdates(mLocationListener);
    }
}
