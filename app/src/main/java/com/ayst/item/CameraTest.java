package com.ayst.item;

import android.content.Context;
import android.hardware.Camera;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.LinearLayout;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2018/12/17.
 */

public class CameraTest {
    private static final String TAG = "CameraTest";

    private Context mContext;
    private LinearLayout mLayout;
    private Handler mHandler;

    private List<CameraItem> mCameras = new ArrayList<>();

    public CameraTest(Context context, LinearLayout layout) {
        mContext = context;
        mLayout = layout;
        mHandler = new Handler(Looper.getMainLooper());
    }

    public void start() {
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
        mLayout.removeAllViews();
    }

    Runnable mCameraRunnable = new Runnable() {
        @Override
        public void run() {
            int cameraNumber = Camera.getNumberOfCameras();
            Log.i(TAG, "Camera num: " + cameraNumber);
            for (int i=mCameras.size(); i<cameraNumber; i++) {
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
                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(300, 225);
                    mLayout.addView(surface, layoutParams);
                }
            }

            mHandler.postDelayed(this, 5000);
        }
    };

    class CameraItem {
        public int id;
        public Camera camera;
        public SurfaceView surface;

        public void release() {
            if (null != camera) {
                Log.i(TAG, "Release camera, id:" + id);
                camera.stopPreview();
                camera.release();
                camera = null;
            }
        }
    }
}
