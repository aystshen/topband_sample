package com.ayst.sample.items.camera;

import android.view.SurfaceView;

public interface ICameraView {
    void removeCameraViewAll();
    void removeCameraView(SurfaceView surface);
    void addCameraView(SurfaceView surface);
    void updateCameraInfo(String info);
}
