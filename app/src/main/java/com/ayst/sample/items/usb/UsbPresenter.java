package com.ayst.sample.items.usb;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.ArrayAdapter;

import com.ayst.sample.R;
import com.ayst.usb.OnDataChangeListener;
import com.ayst.usb.OnUsbConnectChangeListener;
import com.ayst.usb.UsbTransferServer;

import java.util.ArrayList;
import java.util.HashMap;

public class UsbPresenter {
    private static final String TAG = "UsbPresenter";

    //Handler message
    private final static int MSG_USB_DATA_UPDATE = 1001;

    private Context mContext;
    private IUsbView mUsbView;

    private UsbManager mUsbManager = null;
    private ArrayAdapter<String> mUsbDevicesAdapter;
    private ArrayList<UsbDevice> mUsbDevices = new ArrayList<>();
    private UsbDevice mCurUsbDevice = null;
    private UsbTransferServer mUsbService = null;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_USB_DATA_UPDATE:
                    Bundle bundle = msg.getData();
                    byte[] data = bundle.getByteArray("data");
                    int len = bundle.getInt("len");
                    mUsbView.updateUsbData(data.toString());
                    break;
            }
        }
    };

    ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "onServiceDisconnected");
            mUsbService = null;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "onServiceConnected");
            mUsbService = ((UsbTransferServer.UsbBinder) service).getService();
            initUsbServiceListener(mUsbService);
        }
    };

    private void initUsbServiceListener(UsbTransferServer usbService) {
        if (usbService != null) {
            Log.d(TAG, "initUsbServiceListener...");
            usbService.setOnDataChangeListener(new OnDataChangeListener() {
                @Override
                public void onDataChange(byte[] data, int len) {
                    Message msg = mHandler.obtainMessage(MSG_USB_DATA_UPDATE);
                    Bundle bundle = new Bundle();
                    bundle.putByteArray("data", data);
                    bundle.putInt("len", len);
                    msg.setData(bundle);
                    mHandler.sendMessage(msg);
                }
            });

            usbService.setOnUsbConnectChangeListener(new OnUsbConnectChangeListener() {
                @Override
                public void onUsbConnectChange(int status) {
                    switch (status) {
                        case UsbTransferServer.CONNECTED:
                            mUsbView.updateUsbData("Connected");
                            break;
                        case UsbTransferServer.DISCONNECTED:
                            mUsbView.updateUsbData("Disconnect");
                            break;
                        case UsbTransferServer.CONNECT_FAIL:
                            mUsbView.updateUsbData("Connect fail");
                            break;
                    }
                }
            });
        }
    }

    public UsbPresenter(Context context, IUsbView view){
        mContext = context;
        mUsbView = view;
        mUsbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
        mUsbDevicesAdapter = new ArrayAdapter<>(context, R.layout.spinner_item);
    }

    public void start() {
        Intent intent = new Intent().setClass(mContext, UsbTransferServer.class);
        mContext.bindService(intent, conn, Context.BIND_AUTO_CREATE);

        IntentFilter filter = new IntentFilter();
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        mContext.registerReceiver(mBroadcastReceiver, filter);

        getUsbDevices();
    }

    public void stop() {
        mContext.unbindService(conn);
        mContext.unregisterReceiver(mBroadcastReceiver);
    }

    public ArrayAdapter<String> getAdapter() {
        return mUsbDevicesAdapter;
    }

    public void setSelectedDevice(int index) {
        if (index < mUsbDevices.size()) {
            mCurUsbDevice = mUsbDevices.get(index);
        }
    }

    public void connect() {
        if (null != mUsbService && null != mCurUsbDevice) {
            mUsbService.preConnect(mCurUsbDevice);
        }
    }

    public void disconnect() {
        if (null != mUsbService) {
            mUsbService.disconnect();
        }
    }

    private void getUsbDevices() {
        mUsbDevices.clear();
        HashMap<String, UsbDevice> deviceList = mUsbManager.getDeviceList();
        mUsbDevices.addAll(deviceList.values());

        if (!mUsbDevices.isEmpty()) {
            int curDeviceIndex = -1;
            String[] spinnerItems = new String[mUsbDevices.size()];
            for (int i = 0; i < mUsbDevices.size(); i++) {
                UsbDevice device = mUsbDevices.get(i);
                spinnerItems[i] = device.getManufacturerName()
                        + " " + device.getProductName()
                        + "(" + device.getProductId()
                        + "/" + device.getVendorId() + ")";

                if (null != mCurUsbDevice) {
                    if (mCurUsbDevice.getProductId() == device.getProductId()
                            && mCurUsbDevice.getVendorId() == device.getVendorId()) {
                        curDeviceIndex = i;
                    }
                }

                Log.d(TAG, "getUsbDevices, " + device.toString());
            }
            mUsbDevicesAdapter.clear();
            mUsbDevicesAdapter.addAll(spinnerItems);
            mUsbDevicesAdapter.notifyDataSetChanged();

            if (curDeviceIndex < 0) {
                mCurUsbDevice = mUsbDevices.get(0);
                mUsbView.updateUsbDeviceList(0);
            } else {
                mUsbView.updateUsbDeviceList(curDeviceIndex);
            }
        } else {
            mCurUsbDevice = null;
        }
    }

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public synchronized void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)
                    || UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {

                UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                Log.d(TAG, "mBroadcastReceiver: usb device venderid=" + device.getVendorId() + "productid=" + device.getProductId());

                getUsbDevices();
            }
        }
    };
}
