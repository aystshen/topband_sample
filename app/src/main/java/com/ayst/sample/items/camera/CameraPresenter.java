package com.ayst.sample.items.camera;

import android.content.Context;
import android.hardware.Camera;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.ayst.utils.AppUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by Administrator on 2018/12/17.
 */

public class CameraPresenter {
    private static final String TAG = "CameraPresenter";

    private Context mContext;
    private ICameraView mCameraView;
    private Handler mHandler;

    private boolean isHD = true;
    private List<CameraItem> mCameras = new ArrayList<>();

    public CameraPresenter(Context context, ICameraView view) {
        mContext = context;
        mCameraView = view;
        mHandler = new Handler(Looper.getMainLooper());
    }

    public void start(boolean isHD) {
        this.isHD = isHD;
        mHandler.removeCallbacks(mCameraRunnable);
        mHandler.postDelayed(mCameraRunnable, 100);
    }

    private boolean openCamera(int id, SurfaceView surface) {
        try {
            Log.i(TAG, "openCamera, id:" + id);
            Camera camera = Camera.open(id);

            CameraItem cameraItem = new CameraItem();
            cameraItem.id = id;
            cameraItem.camera = camera;
            cameraItem.surface = surface;
            mCameras.add(cameraItem);

            return true;
        } catch (Exception e) {
            Log.e(TAG, "Open camera:" + id + "failed: " + e.getMessage());
        }

        return false;
    }

    private void preview(int id) {
        for (CameraItem cameraItem : mCameras) {
            if (cameraItem.id == id) {
                try {
                    Camera.Parameters parameters = cameraItem.camera.getParameters();
                    List<Camera.Size> sizes = parameters.getSupportedPreviewSizes();
                    if (sizes != null && !sizes.isEmpty()) {
                        Collections.sort(sizes, new SizeComparator());
                        Camera.Size previewSize = sizes.get(isHD ? sizes.size()-1 : 0);
                        parameters.setPreviewSize(previewSize.width, previewSize.height);
                        cameraItem.camera.setParameters(parameters);
                        Log.i(TAG, "preview, preview size: " + previewSize.width
                                + "x" + previewSize.height);
                    }
                    cameraItem.camera.setPreviewDisplay(cameraItem.surface.getHolder());
                    cameraItem.camera.startPreview();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void stop() {
        mHandler.removeCallbacks(mCameraRunnable);
        for (CameraItem cameraItem : mCameras) {
            cameraItem.release();
        }
        mCameras.clear();
        mCameraView.removeCameraView();
    }

    private Runnable mCameraRunnable = new Runnable() {
        @Override
        public void run() {
            int cameraNumber = Camera.getNumberOfCameras();
            Log.i(TAG, "Camera num: " + cameraNumber);
            for (int i = mCameras.size(); i < cameraNumber; i++) {
                SurfaceView surface = new SurfaceView(mContext);
                if (openCamera(i, surface)) {
                    final int cameraId = i;
                    surface.getHolder().addCallback(new SurfaceHolder.Callback() {
                        @Override
                        public void surfaceCreated(SurfaceHolder surfaceHolder) {
                            preview(cameraId);
                        }

                        @Override
                        public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

                        }

                        @Override
                        public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

                        }
                    });
                    mCameraView.addCameraView(surface);
                }
            }

            StringBuilder sb = new StringBuilder();
            for (int i=0; i<cameraNumber; i++) {
                String pidvid = AppUtils.getProperty("topband.dev.video" + i, "");
                if (!TextUtils.isEmpty(pidvid)) {
                    sb.append("video" + i + "(" + pidvid + ") ");
                }
            }
            mCameraView.updateCameraInfo(sb.toString());

            mHandler.postDelayed(this, 5000);
        }
    };

    private class CameraItem {
        private int id;
        private Camera camera;
        private SurfaceView surface;

        private void release() {
            if (null != camera) {
                Log.i(TAG, "Release camera, id:" + id);
                camera.stopPreview();
                camera.release();
                camera = null;
            }
        }
    }

    private class SizeComparator implements Comparator<Camera.Size> {

        @Override
        public int compare(Camera.Size size1, Camera.Size size2) {
            return (size1.width-size2.width) == 0 ? (size1.height-size2.height) : (size1.width-size2.width);
        }
    }
}
