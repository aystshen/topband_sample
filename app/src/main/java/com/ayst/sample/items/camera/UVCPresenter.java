package com.ayst.sample.items.camera;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

import com.ayst.sample.R;
import com.serenegiant.usb.DeviceFilter;
import com.serenegiant.usb.Size;
import com.serenegiant.usb.USBMonitor;
import com.serenegiant.usb.UVCCamera;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by Administrator on 2018/12/17.
 */

public class UVCPresenter {
    private static final String TAG = "UVCPresenter";

    private Context mContext;
    private ICameraView mCameraView;
    private USBMonitor mUSBMonitor;
    private List<UsbDevice> mUsbDeviceList;
    private final Object mSync = new Object();
    private Map<String, CameraItem> mCameras = new HashMap<>();

    public UVCPresenter(Context context, ICameraView view) {
        mContext = context;
        mCameraView = view;
    }

    @SuppressLint("CheckResult")
    public void start() {
        // 构建UVCCamera相关类
        mUSBMonitor = new USBMonitor(mContext, mOnDeviceConnectListener);
        final List<DeviceFilter> filter = DeviceFilter.getDeviceFilters(mContext, R.xml.device_filter);
        mUsbDeviceList = mUSBMonitor.getDeviceList(filter.get(0));

        if (!mUsbDeviceList.isEmpty()) {
            synchronized (mSync) {
                if (mUSBMonitor != null) {
                    mUSBMonitor.register();

                    for (int i = 0; i < mUsbDeviceList.size(); i++) {
                        // 摄像头请求权限
                        UsbDevice device = mUsbDeviceList.get(i);
                        Observable.timer(i * 1000, TimeUnit.MILLISECONDS)
                                .subscribeOn(Schedulers.io())
                                .subscribe(new Consumer<Long>() {
                                    @Override
                                    public void accept(Long aLong) throws Exception {
                                        Log.i(TAG, "onStart, requestPermission "
                                                + String.format("%x:%x", device.getVendorId(),
                                                device.getProductId()));
                                        mUSBMonitor.requestPermission(device);
                                    }
                                });
                    }
                }
            }
        } else {
            Log.e(TAG, "start, mUsbDeviceList is null.");
            Toast.makeText(mContext, "No UVC camera!", Toast.LENGTH_SHORT).show();
        }
    }

    private final USBMonitor.OnDeviceConnectListener mOnDeviceConnectListener =
            new USBMonitor.OnDeviceConnectListener() {

                @Override
                public void onAttach(UsbDevice device) {
                }

                @Override
                public void onDettach(UsbDevice device) {
                }

                @Override
                public void onConnect(UsbDevice device, USBMonitor.UsbControlBlock ctrlBlock, boolean createNew) {
                    Log.i(TAG, "USBMonitor, onConnect " + String.format("%x", device.getProductId()));
                    release(getCameraTag(device));
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

            CameraItem item = mCameras.get(tag);
            if (item != null) {
                if (item.camera != null) {
                    try {
                        item.camera.stopPreview();
                        item.camera.setStatusCallback(null);
                        item.camera.setButtonCallback(null);
                        item.camera.close();
                        if (null != item.camera) {
                            item.camera.destroy();
                        }
                    } catch (final Exception e) {
                        e.printStackTrace();
                    }
                    item.camera = null;
                }
            }
            mCameras.remove(tag);
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
                    try {
                        camera.setPreviewSize(sizes.get(sizes.size() - 1).width,
                                sizes.get(sizes.size() - 1).height,
                                UVCCamera.FRAME_FORMAT_MJPEG);
                    } catch (final IllegalArgumentException e) {
                        // fallback to YUV mode
                        try {
                            camera.setPreviewSize(sizes.get(sizes.size() - 1).width,
                                    sizes.get(sizes.size() - 1).height,
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
        mCameraView.removeCameraView();
    }

    private String getCameraTag(UsbDevice device) {
        return String.format("%x:%x", device.getVendorId(), device.getProductId());
    }

    private class CameraItem {
        private UVCCamera camera;
        private SurfaceView surface;
    }
}
