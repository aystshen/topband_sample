package com.ayst.sample.items.camera;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.serenegiant.usb.Size;
import com.serenegiant.usb.USBMonitor;
import com.serenegiant.usb.UVCCamera;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by Administrator on 2018/12/17.
 */

public class UVCPresenter {
    private static final String TAG = "UVCPresenter";

    private boolean isHD = true;
    private Context mContext;
    private ICameraView mCameraView;
    private USBMonitor mUSBMonitor;
    private final Object mSync = new Object();
    private Map<String, CameraItem> mCameras = new HashMap<>();

    public UVCPresenter(Context context, ICameraView view) {
        mContext = context;
        mCameraView = view;
    }

    @SuppressLint("CheckResult")
    public void start(boolean isHD) {
        this.isHD = isHD;

        // 构建UVCCamera相关类
        mUSBMonitor = new USBMonitor(mContext, mOnDeviceConnectListener);
        mUSBMonitor.register();
    }

    private final USBMonitor.OnDeviceConnectListener mOnDeviceConnectListener =
            new USBMonitor.OnDeviceConnectListener() {

                @Override
                public void onAttach(UsbDevice device) {
                    Log.i(TAG, "USBMonitor, onAttach " + String.format("%x", device.getProductId()));
                    mUSBMonitor.requestPermission(device);
                }

                @Override
                public void onDettach(UsbDevice device) {
                    Log.i(TAG, "USBMonitor, onDettach " + String.format("%x", device.getProductId()));
                }

                @Override
                public void onConnect(UsbDevice device, USBMonitor.UsbControlBlock ctrlBlock, boolean createNew) {
                    Log.i(TAG, "USBMonitor, onConnect " + String.format("%x", device.getProductId()));
                    open(getCameraTag(device), ctrlBlock);
                }

                @Override
                public void onDisconnect(UsbDevice device, USBMonitor.UsbControlBlock ctrlBlock) {
                    Log.i(TAG, "USBMonitor, onDisconnect " + String.format("%x", device.getProductId()));
                    release(getCameraTag(device));
                }

                @Override
                public void onCancel(UsbDevice device) {

                }
            };

    private void release(String tag) {
        synchronized (mSync) {
            Log.i(TAG, "release, " + tag);

            /* USB Camera断开时，执行下面close或destroy操作会阻塞，
             * 影响重连，因此注释掉
             */
//            CameraItem item = mCameras.get(tag);
//            if (item != null) {
//                if (item.camera != null) {
//                    try {
//                        item.camera.stopPreview();
//                        item.camera.setStatusCallback(null);
//                        item.camera.setButtonCallback(null);
//                        item.camera.close();
//                        if (null != item.camera) {
//                            item.camera.destroy();
//                        }
//                    } catch (final Exception e) {
//                        e.printStackTrace();
//                    }
//                    item.camera = null;
//                }
//            }
            CameraItem item = mCameras.get(tag);
            if (item != null) {
                mCameraView.removeCameraView(item.surface);
                mCameras.remove(tag);
            }
        }
    }

    private void open(String tag, USBMonitor.UsbControlBlock ctrlBlock) {
        synchronized (mSync) {
            Log.i(TAG, "open...");
            try {
                final UVCCamera camera = new UVCCamera();
                camera.open(ctrlBlock);

                List<Size> sizes = camera.getSupportedSizeList();
                if (sizes != null && !sizes.isEmpty()) {
                    Collections.sort(sizes, new SizeComparator());
                    Log.i(TAG, "open, supported size: " + sizes.toString());
                    Size previewSize = sizes.get(isHD ? sizes.size() - 1 : 0);
                    try {
                        camera.setPreviewSize(previewSize.width,
                                previewSize.height,
                                UVCCamera.FRAME_FORMAT_MJPEG);
                    } catch (final IllegalArgumentException e) {
                        // fallback to YUV mode
                        try {
                            camera.setPreviewSize(previewSize.width,
                                    previewSize.height,
                                    UVCCamera.DEFAULT_PREVIEW_MODE);
                        } catch (final IllegalArgumentException e1) {
                            Log.e(TAG, "open, setPreviewSize failed!");
                            camera.destroy();
                            return;
                        }
                    }
                } else {
                    Log.e(TAG, "open, Supported size is null!");
                    camera.destroy();
                    return;
                }

                SurfaceView surface = new SurfaceView(mContext);
                surface.getHolder().addCallback(new SurfaceHolder.Callback() {
                    @Override
                    public void surfaceCreated(SurfaceHolder surfaceHolder) {
                        // Set preview surface
                        camera.setPreviewDisplay(surfaceHolder);
                        camera.startPreview();
                        Size previewSize = camera.getPreviewSize();
                        Log.i(TAG, "surfaceCreated, preview size: "
                                + previewSize.width + "x" + previewSize.height);
                    }

                    @Override
                    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

                    }

                    @Override
                    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

                    }
                });

                ((Activity) mContext).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mCameraView.addCameraView(surface);
                    }
                });

                CameraItem cameraItem = new CameraItem();
                cameraItem.camera = camera;
                cameraItem.surface = surface;
                mCameras.put(tag, cameraItem);
            } catch (UnsupportedOperationException e) {
                Log.e(TAG, "open camera one failed: \n" + e.getMessage());
            }
        }
    }

    public void stop() {
        List<String> tags = new ArrayList<>(mCameras.keySet());
        for (String tag : tags) {
            release(tag);
        }

        synchronized (mSync) {
            if (mUSBMonitor != null) {
                mUSBMonitor.unregister();
            }
        }

        mCameras.clear();
        mCameraView.removeCameraViewAll();
    }

    private String getCameraTag(UsbDevice device) {
        return String.format("%x:%x", device.getVendorId(), device.getProductId());
    }

    private class CameraItem {
        private UVCCamera camera;
        private SurfaceView surface;
    }

    private class SizeComparator implements Comparator<Size> {

        @Override
        public int compare(Size size1, Size size2) {
            return (size1.width - size2.width) == 0 ? (size1.height - size2.height) : (size1.width - size2.width);
        }
    }
}
