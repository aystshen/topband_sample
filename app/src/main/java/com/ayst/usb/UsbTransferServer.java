package com.ayst.usb;

import android.app.PendingIntent;
import android.app.Service;
import android.content.*;
import android.hardware.usb.*;
import android.os.*;
import android.os.Handler;
import android.util.Log;

/**
 * Created by ayst on 2014/10/25.
 */
public class UsbTransferServer extends Service {
    private static final String TAG = "UsbTransferServer";

    public final static Object USB_LOCK = new Object(); // USB同步
    private static final String ACTION_USB_PERMISSION = "com.ayst.USB_PERMISSION";

    public final static int CONNECTED = 1;
    public final static int DISCONNECTED = 2;
    public final static int CONNECT_FAIL = 3;

    private static final int USB_VENDOR_ID = 1155;
    private static final int USB_PRODUCT_ID = 22336;
    private static final int USB_IN_DATA_LEN = 5000;

    private UsbDevice mCurDevice;
    private UsbManager mUsbManager;
    private UsbInterface mUsbInterface;
    private UsbEndpoint mUsbInEndpoint;
    private UsbEndpoint mUsbOutEndpoint;
    private UsbDeviceConnection mUsbDeviceConnection;
    private boolean isConnected = false;
    private byte[] mDeviceInData;
    private byte[] mDeviceOutData;

    //数据监听回调
    private OnUsbConnectChangeListener mOnUsbConnectChangeListener;
    private OnDataChangeListener mOnDataChangeListener;

    public UsbTransferServer() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new UsbBinder();
    }

    public class UsbBinder extends Binder {
        public UsbTransferServer getService() {
            mOnUsbConnectChangeListener = null;
            mOnDataChangeListener = null;
            return UsbTransferServer.this;
        }
    }

    public void setOnUsbConnectChangeListener(OnUsbConnectChangeListener onUsbConnectChangeListener) {
        this.mOnUsbConnectChangeListener = onUsbConnectChangeListener;
    }

    public void setOnDataChangeListener(OnDataChangeListener onDataChangeListener) {
        this.mOnDataChangeListener = onDataChangeListener;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate...");

        mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);

        IntentFilter filter = new IntentFilter();
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(ACTION_USB_PERMISSION);
        registerReceiver(mUsbStateReceiver, filter);

        if (!mDeviceThread.isAlive()) {
            mDeviceThread.setPriority(Thread.MAX_PRIORITY);
            mDeviceThread.start();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy...");
        mHandler.removeCallbacks(mRunnable);
        if (mDeviceThread.isAlive()) {
            mDeviceThread.interrupt();
        }

        unregisterReceiver(mUsbStateReceiver);
    }

    private Handler mHandler = new Handler();
    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {

        }
    };

    private Thread mDeviceThread = new Thread() {
        @Override
        public void run() {
            do {
                if (isConnected) {
                    synchronized (USB_LOCK) {
                        if (isConnected) {
                            Log.d(TAG, "mDeviceThread, read data...");
                            int length = mUsbDeviceConnection.bulkTransfer(mUsbInEndpoint, mDeviceInData, mDeviceInData.length, 1000);
                            if (length > 0) {
                                Log.d(TAG, "mDeviceThread, read data: " + mDeviceInData.toString());
                                if (mOnDataChangeListener != null) {
                                    mOnDataChangeListener.onDataChange(mDeviceInData, length);
                                }
                                break;
                            }
                        }
                    }
                } else {
                    try {
                        sleep(500L);
                    } catch (InterruptedException e) {
                        Log.d(TAG, "mDeviceThread exit...");
                        break;
                    }
                }
            } while (!isInterrupted());
        }
    };

    public void preConnect(UsbDevice device) {
        if (null == device) {
            Log.e(TAG, "preConnect, device is null");
        }

        if(mUsbManager.hasPermission(device)) {
            Log.d(TAG, "preConnect, already have permission to connect directly");
            connect(device);
        } else {
            Log.d(TAG, "preConnect, request permission");
            PendingIntent permissionIntent = PendingIntent.getBroadcast(UsbTransferServer.this,
                    0, new Intent(ACTION_USB_PERMISSION), 0);
            mUsbManager.requestPermission(device, permissionIntent);
        }
    }

    private void connect(UsbDevice device) {
        if (connectDevice(device)) {
            if (mOnUsbConnectChangeListener != null) {
                mOnUsbConnectChangeListener.onUsbConnectChange(CONNECTED);
            }
        } else {
            if (mOnUsbConnectChangeListener != null) {
                mOnUsbConnectChangeListener.onUsbConnectChange(CONNECT_FAIL);
            }
        }
    }

    private boolean connectDevice(UsbDevice device) {
        if (null == device) {
            Log.e(TAG, "connect, device is null");
            return false;
        }
        mCurDevice = device;

        synchronized (USB_LOCK) {
            Log.d(TAG, "Device Plugin");
            printDeviceInfo(device);

            mUsbInterface = null;
            mUsbInEndpoint = null;
            mUsbOutEndpoint = null;

            Log.d(TAG, "Search BULK ENDPOINT");
            Log.d(TAG, "InterfaceCount: " + device.getInterfaceCount());
            for (int ifIdx = 0; ifIdx < device.getInterfaceCount(); ifIdx++) {
                UsbInterface uif = device.getInterface(ifIdx);
                Log.d(TAG, "-Interface " + ifIdx + " :");
                for (int epIdx = 0; epIdx < uif.getEndpointCount(); epIdx++) {
                    UsbEndpoint uep = uif.getEndpoint(epIdx);
                    Log.d(TAG, "--endpoint " + epIdx);
                    Log.d(TAG, "---type: " + getEndPointType(uep.getType()));
                    Log.d(TAG, "---Direction: " + getEndPointDirection(uep.getDirection()));
                    Log.d(TAG, "---MaxPacketSize: " + uep.getMaxPacketSize());
                    Log.d(TAG, "---Interval: " + uep.getInterval());
                    Log.d(TAG, "---EndpointNumber: " + uep.getEndpointNumber());

                    if (uep.getType() == UsbConstants.USB_ENDPOINT_XFER_BULK) {
                        mUsbInterface = uif;
                        if (uep.getDirection() == UsbConstants.USB_DIR_IN) {
                            Log.d(TAG, "[FOUND] IN ENDPOINT");
                            mUsbInEndpoint = uep;
                        } else if (uep.getDirection() == UsbConstants.USB_DIR_OUT) {
                            Log.d(TAG, "[FOUND] OUT ENDPOINT");
                            mUsbOutEndpoint = uep;
                        }
                    }
                }
            }

            if ((mUsbInterface != null) && mUsbInEndpoint != null) {
                mUsbDeviceConnection = mUsbManager.openDevice(device);
                if (mUsbDeviceConnection != null) {
                    mUsbDeviceConnection.claimInterface(mUsbInterface, true);

                    mDeviceInData = new byte[mUsbInEndpoint.getMaxPacketSize()];
                    mDeviceOutData = new byte[mUsbOutEndpoint.getMaxPacketSize()];

                    isConnected = true;
                    Log.d(TAG, "connect, success");
                    return true;
                } else {
                    Log.e(TAG, "connect, mUsbDeviceConnection is null");
                }
            } else {
                Log.e(TAG, "connect, mUsbInterface or mUsbInEndpoint is null");
            }
        }

        return false;
    }

    public boolean disconnect() {
        synchronized (USB_LOCK) {
            isConnected = false;
            mDeviceInData = null;
            mDeviceOutData = null;

            if (mUsbDeviceConnection != null) {
                if (mUsbInterface != null) {
                    mUsbDeviceConnection.releaseInterface(mUsbInterface);
                }
                mUsbDeviceConnection.close();
            }
            if (mOnUsbConnectChangeListener != null) {
                mOnUsbConnectChangeListener.onUsbConnectChange(DISCONNECTED);
            }
            Log.d(TAG, "Device Pullout");
        }

        return true;
    }

    public void write(byte[] data) {
        if (isConnected) {
            if (mUsbOutEndpoint != null) {
                int length = mUsbDeviceConnection.bulkTransfer(mUsbOutEndpoint, data, data.length, 1000);
                Log.d(TAG, "Write Length: " + length);
            } else {
                Log.d(TAG, "Not found USB out endpoint");
            }
        } else {
            Log.d(TAG, "Write Error: Usb is not connected");
        }
    }

    private String getEndPointType(int type) {
        String typeString = "UNKNOW";
        switch (type) {
            case UsbConstants.USB_ENDPOINT_XFER_CONTROL:
                typeString = "Control";
                break;
            case UsbConstants.USB_ENDPOINT_XFER_ISOC:
                typeString = "ISOC";
                break;
            case UsbConstants.USB_ENDPOINT_XFER_BULK:
                typeString = "BULK";
                break;
            case UsbConstants.USB_ENDPOINT_XFER_INT:
                typeString = "INT";
                break;
        }
        return typeString;
    }

    private String getEndPointDirection(int dir) {
        String dirString = "UNKNOW";
        switch (dir) {
            case UsbConstants.USB_DIR_IN:
                dirString = "IN";
                break;
            case UsbConstants.USB_DIR_OUT:
                dirString = "OUT";
                break;
        }
        return dirString;
    }

    private void printDeviceInfo(UsbDevice device) {
        if (device == null) {
            return;
        }
        Log.d(TAG, "++++++++++++++++++++++++");
        Log.d(TAG, "Name: " + device.getDeviceName());
        Log.d(TAG, "InterfaceCount: " + device.getInterfaceCount());
        Log.d(TAG, "VendorId: " + device.getVendorId());
        Log.d(TAG, "ProductId: " + device.getProductId());
        Log.d(TAG, "Class: " + device.getClass());
        Log.d(TAG, "SubClass: " + device.getDeviceSubclass());
        Log.d(TAG, "Protocol: " + device.getDeviceProtocol());
    }

    private BroadcastReceiver mUsbStateReceiver = new BroadcastReceiver() {
        @Override
        public synchronized void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
            Log.d(TAG, "mUsbStateReceiver: venderid=" + device.getVendorId() + "productid=" + device.getProductId());

            if (null != mCurDevice
                    && mCurDevice.getVendorId() == device.getVendorId()
                    && mCurDevice.getProductId() == device.getProductId()) {
                if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
                    disconnect();
                } else if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
                    PendingIntent permissionIntent = PendingIntent.getBroadcast(UsbTransferServer.this,
                            0, new Intent(ACTION_USB_PERMISSION), 0);
                    mUsbManager.requestPermission(device, permissionIntent);
                } else if (ACTION_USB_PERMISSION.equals(action)) {
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        connect(device);
                    }
                }
            }
        }
    };
}
