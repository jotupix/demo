package com.jtkj.demo.device;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;

import com.jtkj.demo.BuildConfig;
import com.jtkj.demo.CoolLED;
import com.jtkj.demo.device.utils.Utils;
import com.jtkj.jotupix.core.JProtocol;
import com.jtkj.jotupix.core.JotuPix;
import com.jtkj.library.commom.logger.CLog;
import com.jtkj.library.fastble.BleManager;
import com.jtkj.library.fastble.callback.BleGattCallback;
import com.jtkj.library.fastble.callback.BleMtuChangedCallback;
import com.jtkj.library.fastble.callback.BleNotifyCallback;
import com.jtkj.library.fastble.callback.BleScanCallback;
import com.jtkj.library.fastble.callback.BleWriteCallback;
import com.jtkj.library.fastble.data.BleDevice;
import com.jtkj.library.fastble.exception.BleException;
import com.jtkj.library.fastble.scan.BleScanRuleConfig;
import com.jtkj.library.infrastructure.eventbus.EventAgent;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/***
 * DeviceManager implements JSend interface
 * it receive the data from the JotuPix module that send to the ble device
 * the function  of int send(byte[] data, int length); you can send the data by your own
 */
public class DeviceManager implements JProtocol.JSend {
    private static final String TAG = DeviceManager.class.getSimpleName();
    private static final String UUID_CHARACTER = "0000fff1-0000-1000-8000-00805f9b34fb";
    public static final String UUID_SERVICE = "0000fff0-0000-1000-8000-00805f9b34fb";
    private static final int INTERNAL_BETWEEN_TWO_PACKAGE = 15;
    private boolean sendNextWhenLastSuccess = false;
    private static final int BLE_MTU_MAX_SIZE = 247; //Maximum MTU size
    private static final int BLE_MTU_MIN_SIZE = 23; // Minimum MTU size
    //Although the MTU size is set to 247, for compatibility reasons, the packet size for writing feature values should not exceed 180.
    private static final int BLE_WRITE_CHAR_MAX_SIZE = 180;//The maximum value of the subcontracted writing feature.
    private static final int BLE_WRITE_CHAR_MIN_SIZE = 20; //Minimum value of subcontracted writing feature value


    public static final String Cool_LED_UX = "CoolLEDUX";

    public static final String DEVICE_COLUMN_KEY = "DEVICE_COLUMN_KEY";
    public static final String DEVICE_NAME_KEY = "DEVICE_NAME_KEY";
    public static final String DEVICE_ROW_KEY = "DEVICE_ROW_KEY";

    public static int DEVICE_COLUMN = 64;
    public static int DEVICE_ROW = 16;

    public static String DEVICE_NAME;

    private static final int MSG_SEND_MESSAGE = 1000;
    private final static int BLE_OPERATION_INTERNAL = 1000;


    public static final int SET_BLE_NOTIFY_AFTER_CONNECTED = 500;

    private DeviceHandler mHandler = new DeviceHandler(this);

    private BlockingQueue<byte[]> mDataByteList = new LinkedBlockingQueue<>();

    private Map<String, BleDevice> mBleDeviceMap = new HashMap<>();
    private Map<String, Long> mDeviceConnectTimeMap = new HashMap<>();

    public String mDeviceInfo;

    public static volatile DeviceManager mInstance;

    JotuPix mJotuPix;

    public static DeviceManager getInstance() {
        if (null == mInstance) {
            synchronized (DeviceManager.class) {
                if (null == mInstance) {
                    mInstance = new DeviceManager();
                }
            }
        }
        return mInstance;
    }

    public DeviceManager() {
        init();
    }

    public void init() {
        CLog.i(TAG, "init");
        /***
         * init the JotusPix instance here  ,the DeviceManager  implements JProtocol.JSend interface
         *
         */
        mJotuPix = new JotuPix();
        mJotuPix.init(this);
        initBleManager();
        DEVICE_COLUMN = CoolLED.getInstance().readInt(DEVICE_COLUMN_KEY, 48);
        DEVICE_ROW = CoolLED.getInstance().readInt(DEVICE_ROW_KEY, 12);
        DEVICE_NAME = CoolLED.getInstance().readString(DEVICE_NAME_KEY, null);
    }

    private void initBleManager() {
        BleManager.getInstance()
                .enableLog(BuildConfig.DEBUG)
                .setReConnectCount(5, 1000)
                .setConnectOverTime(5000)
                .setOperateTimeout(5000);
        UUID[] serviceUUIDs = new UUID[1];
        serviceUUIDs[0] = UUID.fromString(UUID_SERVICE);
        String[] names = new String[1];
        names[0] = Cool_LED_UX;
        BleScanRuleConfig scanRuleConfig = new BleScanRuleConfig.Builder()
                .setServiceUuids(serviceUUIDs)
                .setDeviceName(false, names)
                .setAutoConnect(false)
                .setScanTimeOut(3000)
                .build();
        BleManager.getInstance().initScanRule(scanRuleConfig);
    }

    public JotuPix getJotuPix() {
        return mJotuPix;
    }

    public void release() {
        CLog.i(TAG, "release");
        exitToClearData();
        mHandler.removeCallbacksAndMessages(null);
        mInstance = null;
        BleManager.getInstance().destroy();
    }

    private void exitToClearData() {
        mBleDeviceMap.clear();
        mDataByteList.clear();
    }

    private static class DeviceHandler extends Handler {
        WeakReference<DeviceManager> mService;

        public DeviceHandler(DeviceManager service) {
            super(Looper.myLooper());
            mService = new WeakReference<>(service);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            DeviceManager deviceManager = mService.get();
            if (null != deviceManager) {
                deviceManager.processMessage(msg);
            }
        }
    }

    private void getDeviceInfo(BleDevice device) {
        String deviceName;
        deviceName = device.getName();
        DEVICE_NAME = deviceName;
        if (!TextUtils.isEmpty(deviceName)) {
            CLog.i(TAG, "getDeviceInfo>>>deviceName>>>" + deviceName);
            CoolLED.getInstance().savePreference(DEVICE_NAME_KEY, DEVICE_NAME);
            if (deviceName.equalsIgnoreCase(DeviceManager.Cool_LED_UX)) {
                DEVICE_ROW = getDeviceRow(device);
                DEVICE_COLUMN = getDeviceColumn(device);
                CLog.i(TAG, "getDeviceInfo>>>DEVICE_ROW>>>" + DEVICE_ROW);
                CLog.i(TAG, "getDeviceInfo>>>DEVICE_COLUMN>>>" + DEVICE_COLUMN);
                CoolLED.getInstance().savePreference(DEVICE_ROW_KEY, DEVICE_ROW);
                CoolLED.getInstance().savePreference(DEVICE_COLUMN_KEY, DEVICE_COLUMN);
                mDeviceInfo = deviceName + DEVICE_ROW + "*" + DEVICE_COLUMN;
            }
        }
    }


    List<DeviceManager.BleDeviceItem> mBleDeviceItems;

    public synchronized List<DeviceManager.BleDeviceItem> getBleDeviceItems() {
        List<DeviceManager.BleDeviceItem> connectedDevices = new ArrayList<>();
        if (null != mBleDeviceItems && mBleDeviceItems.size() > 0) {
            for (DeviceManager.BleDeviceItem item : mBleDeviceItems) {
                if (isDeviceConnected(item.bleDevice)) {
                    connectedDevices.add(item);
                }
            }
        }
        return connectedDevices;
    }


    public void setBleDeviceItems(List<DeviceManager.BleDeviceItem> bleDeviceItems) {
        mBleDeviceItems = new ArrayList<>();
        mBleDeviceItems.addAll(bleDeviceItems);
    }


    public boolean isDeviceConnectedSuccess() {
        List<DeviceManager.BleDeviceItem> bleDeviceItems = getBleDeviceItems();
        if (null != bleDeviceItems && bleDeviceItems.size() > 0) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isDeviceConnected(BleDevice bleDevice) {
        return BleManager.getInstance().isConnected(bleDevice);
    }

    public void disConnectDevice(BleDevice bleDevice) {
        CLog.i(TAG, "disConnectDevice bleDevice: " + bleDevice.toString());
        if (BleManager.getInstance().isConnected(bleDevice)) {
            BleManager.getInstance().disconnect(bleDevice);
        }
        deleteDeviceDisconnectOrConnectFailure(bleDevice);
    }

    private void deleteDeviceDisconnectOrConnectFailure(BleDevice bleDevice) {
        if (mBleDeviceMap.containsKey(bleDevice.getMac())) {
            mBleDeviceMap.remove(bleDevice.getMac());
        }

        long currentTime = System.currentTimeMillis();
        mDeviceConnectTimeMap.put(bleDevice.getMac(), currentTime);
    }

    private void saveDeviceConnectSuccess(BleDevice bleDevice) {
        mBleDeviceMap.put(bleDevice.getMac(), bleDevice);
        if (mDeviceConnectTimeMap.containsKey(bleDevice.getMac())) {
            mDeviceConnectTimeMap.remove(bleDevice.getMac());
        }
        long currentTime = System.currentTimeMillis();
        mDeviceConnectTimeMap.put(bleDevice.getMac(), currentTime);

    }

    public synchronized void connectDevice(final BleDevice bleDevice) {
        long lastConnectTime = 0;
        if (mDeviceConnectTimeMap.containsKey(bleDevice.getMac())) {
            lastConnectTime = mDeviceConnectTimeMap.get(bleDevice.getMac());
        }

        long currentTime = System.currentTimeMillis();
        if ((currentTime - lastConnectTime) >= BLE_OPERATION_INTERNAL) {
            CoolLED.getInstance().getExecutorService().submit(new Runnable() {
                @Override
                public void run() {
                    BleManager.getInstance().connect(bleDevice, new BleGattCallback() {
                        @Override
                        public void onStartConnect() {
                            CLog.i(TAG, "connectDevice>>>onStartConnect>>" + "addresses>>" + bleDevice.getMac() + ">>>name>>>" + bleDevice.getName() + ">>>>id>>>" + getDeviceId(bleDevice));
                            EventAgent.post(new BleDeviceConnectEvent(bleDevice, BleDeviceConnectEvent.START_CONNECT));
                        }

                        @Override
                        public void onConnectFail(BleDevice bleDevice, BleException exception) {
                            CLog.i(TAG, "connectDevice>>>onConnectFail>>" + "addresses>>" + bleDevice.getMac() + ">>>name>>>" + bleDevice.getName() + ">>>>id>>>" + getDeviceId(bleDevice));

                            deleteDeviceDisconnectOrConnectFailure(bleDevice);

                            EventAgent.post(new BleDeviceConnectEvent(bleDevice, BleDeviceConnectEvent.CONNECT_FAILURE));
                        }

                        @Override
                        public void onConnectSuccess(final BleDevice bleDevice, BluetoothGatt gatt, int status) {
                            CLog.i(TAG, "connectDevice>>>onConnectSuccess>>" + "addresses>>" + bleDevice.getMac() + ">>>name>>>" + bleDevice.getName() + ">>>>id>>>" + getDeviceId(bleDevice));
                            saveDeviceConnectSuccess(bleDevice);

                            getDeviceInfo(bleDevice);

                            setMtu(bleDevice);

                            EventAgent.post(new BleDeviceConnectEvent(bleDevice, BleDeviceConnectEvent.CONNECT_SUCCESS));
                            mHandler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    CLog.i(TAG, "connectDevice>>>start>>setBleResponse>>" + "addresses>>" + bleDevice.getMac() + ">>>name>>>" + bleDevice.getName() + ">>>>id>>>" + getDeviceId(bleDevice));
                                    setBleResponse(bleDevice, UUID_SERVICE, UUID_CHARACTER);
                                }
                            }, SET_BLE_NOTIFY_AFTER_CONNECTED);

                        }

                        @Override
                        public void onDisConnected(boolean isActiveDisConnected, BleDevice bleDevice, BluetoothGatt gatt, int status) {
                            CLog.i(TAG, "connectDevice>>>onDisConnected>>>addresses>>>" + bleDevice.getMac() + ">>>name>>> " + bleDevice.getName() + " >>>id>>>" + getDeviceId(bleDevice) + ">>>isActiveDisConnected>>>" + isActiveDisConnected);
                            if (isActiveDisConnected) {
                                EventAgent.post(new BleDeviceConnectEvent(bleDevice, BleDeviceConnectEvent.ACTIVE_DISCONNECT));
                            } else {
                                EventAgent.post(new BleDeviceConnectEvent(bleDevice, BleDeviceConnectEvent.NOT_ACTIVE_DISCONNECT));
                            }
                        }
                    });
                }
            });
        } else {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    connectDevice(bleDevice);
                }
            }, BLE_OPERATION_INTERNAL);
        }
    }


    public void startSearchDevice() {
        CLog.i(TAG, "startSearchDevice");
        CoolLED.getInstance().getExecutorService().submit(new Runnable() {
            @Override
            public void run() {
                BleManager.getInstance().scan(new BleScanCallback() {
                    @Override
                    public void onScanStarted(boolean success) {
                        CLog.i(TAG, "startSearchDevice onScanStarted>>>>>" + success);
                        EventAgent.post(new SearchDeviceEvent(null, SearchDeviceEvent.START_SEARCH));
                    }

                    @Override
                    public void onLeScan(BleDevice bleDevice) {
//                        SearchDeviceEvent event = new SearchDeviceEvent(bleDevice, SearchDeviceEvent.ON_LE_SCAN);
//                        CLog.i(TAG, "startSearchDevice onLeScan>>>>>" + event);
//                        EventAgent.post(event);
                    }

                    @Override
                    public void onScanning(BleDevice bleDevice) {
                        SearchDeviceEvent event = new SearchDeviceEvent(bleDevice, SearchDeviceEvent.SCANNING);
                        CLog.i(TAG, "startSearchDevice onScanning>>>>>" + event);
                        EventAgent.post(event);
                    }

                    @Override
                    public void onScanFinished(List<BleDevice> scanResultList) {
                        SearchDeviceEvent event = new SearchDeviceEvent(null, SearchDeviceEvent.SCAN_FINISHED);
                        CLog.i(TAG, "startSearchDevice onScanFinished>>>>>" + event);
                        EventAgent.post(event);
                    }
                });
            }
        });
    }

    private void setMtu(final BleDevice bleDevice) {
        CoolLED.getInstance().getExecutorService().submit(new Runnable() {
            @Override
            public void run() {
                if (null != bleDevice.getName()) {
                    if (bleDevice.getName().equalsIgnoreCase(DeviceManager.Cool_LED_UX)) {
                        BleManager.getInstance().setMtu(bleDevice, BLE_MTU_MAX_SIZE, new BleMtuChangedCallback() {

                            @Override
                            public void onSetMTUFailure(BleException exception) {
                                BleManager.getInstance().setSplitWriteNum(BLE_WRITE_CHAR_MIN_SIZE);
                                CLog.i(TAG, "onSetMTUFailure>>>" + exception.getDescription());
                            }

                            @Override
                            public void onMtuChanged(int mtu) {
                                BleManager.getInstance().setSplitWriteNum(BLE_WRITE_CHAR_MAX_SIZE);
                                CLog.i(TAG, "onMtuChanged>>>" + mtu);
                            }
                        });
                    } else {
                        BleManager.getInstance().setSplitWriteNum(BLE_WRITE_CHAR_MIN_SIZE);
                    }
                } else {
                    BleManager.getInstance().setSplitWriteNum(BLE_WRITE_CHAR_MIN_SIZE);
                }
            }
        });
    }


    private void setBleResponse(BleDevice bleDevice, String serviceUUID, String characterUUID) {
        CLog.i(TAG, "setBleResponse");
        CoolLED.getInstance().getExecutorService().submit(new Runnable() {
            @Override
            public void run() {
                BleManager.getInstance().notify(bleDevice, serviceUUID, characterUUID, new BleNotifyCallback() {
                    @Override
                    public void onNotifySuccess() {
                        CLog.i(TAG, "connectDevice>>>onNotifySuccess>>>" + "addresses>>" + bleDevice.getMac() + ">>>name>>>" + bleDevice.getName() + ">>>>id>>>" + getDeviceId(bleDevice));
                    }

                    @Override
                    public void onNotifyFailure(BleException exception) {
                        CLog.i(TAG, "connectDevice>>>onNotifyFailure>>>" + exception);
                    }

                    @Override
                    public void onCharacteristicChanged(byte[] data) {
                        DeviceManager.getInstance().getJotuPix().parseRecvData(data, data.length);
                        CLog.i(TAG, "onCharacteristicChanged>>> " + bleDevice.getMac() + "Data>>>" + Utils.bytesToHexString(data));
                    }
                });
            }
        });
    }

    /***
     *  send the data to the ble device
     *  you can send the by your own
     * @param data Pointer to the data buffer to send.
     * @param length Length of the data in bytes.
     * @return
     */
    @Override
    public int send(byte[] data, int length) {
        addDataBytes(data);
        return 0;
    }

    private void addDataBytes(byte[] data) {
        boolean isEmpty = false;
        if (mDataByteList.isEmpty()) {
            isEmpty = true;
        }
        mDataByteList.offer(data);
        if (isEmpty) {
            sendDataToDevice();
        }
    }

    private void sendDataToDevice() {
        if (null != mDataByteList && mDataByteList.size() > 0) {
            try {
                byte[] bytes = mDataByteList.take();
                for (Map.Entry<String, BleDevice> entry : mBleDeviceMap.entrySet()) {
                    BleManager.getInstance().write(entry.getValue(), UUID_SERVICE, UUID_CHARACTER, bytes, true, sendNextWhenLastSuccess, INTERNAL_BETWEEN_TWO_PACKAGE, new BleWriteCallback() {
                        @Override
                        public void onWriteSuccess(int current, int total, byte[] justWrite) {
                            CLog.i(TAG, "sendingData>>>onWriteSuccess>>>>address" + entry.getKey() + "write success, current: " + current + " total: " + total);
                        }

                        @Override
                        public void onWriteFailure(BleException exception) {
                            CLog.i(TAG, "sendingData>>onWriteFailure>>>>address" + entry.getKey() + "exception>>>" + exception.toString());
                        }
                    });
                }

                if (mDataByteList.size() > 0) {
                    mHandler.sendEmptyMessage(MSG_SEND_MESSAGE);
                    CLog.i(TAG, "sendingData continue");
                } else {
                    CLog.i(TAG, "sendingData end");
                }
            } catch (Exception e) {
                CLog.i(TAG, "sendingData>>>DataSendingThread Exception e>>>" + e.getLocalizedMessage());
                CoolLED.reportError(e);
                if (mDataByteList.size() > 0) {
                    mHandler.sendEmptyMessage(MSG_SEND_MESSAGE);
                    CLog.i(TAG, "sendingData continue");
                } else {
                    CLog.i(TAG, "sendingData end");
                }
            }
        }
    }


    private void processMessage(Message msg) {
        switch (msg.what) {
            case MSG_SEND_MESSAGE:
                sendDataToDevice();
                break;
        }
    }

    public static class BleDeviceConnectEvent {
        public static final int START_CONNECT = 1;
        public static final int CONNECT_SUCCESS = 2;
        public static final int CONNECT_FAILURE = 3;
        public static final int ACTIVE_DISCONNECT = 4;
        public static final int PASSWORD_VERIFY_SUCCESS = 5;
        public static final int PASSWORD_VERIFY_FAILURE = 6;
        public static final int NOT_ACTIVE_DISCONNECT = 7;
        public BleDevice bleDevice;
        public int status;

        public BleDeviceConnectEvent(BleDevice bleDevice, int status) {
            this.bleDevice = bleDevice;
            this.status = status;
        }

        @Override
        public String toString() {
            if (null != bleDevice) {
                return "SearchDeviceEvent{" + "bleDevice=" + bleDevice.getName() + ">>>>" + bleDevice.getMac() + ">>>" + bleDevice.getRssi() + ">>>>" + bleDevice.getScanRecord() + ">>>status>>>" + status + '}';
            } else {
                return "SearchDeviceEvent{" + "status>>>" + status + '}';
            }
        }
    }

    public static class SearchDeviceEvent {
        public static final int START_SEARCH = 1;
        public static final int ON_LE_SCAN = 2;
        public static final int SCANNING = 3;
        public static final int SCAN_FINISHED = 4;
        public BleDevice bleDevice;
        public int status;

        public SearchDeviceEvent(BleDevice bleDevice, int status) {
            this.bleDevice = bleDevice;
            this.status = status;
        }

        @Override
        public String toString() {
            if (null != bleDevice) {
                return "SearchDeviceEvent{" + "bleDevice>>>name>>>" + bleDevice.getName() + ">>>>id>>>" + getDeviceId(bleDevice) + ">>>mac>>" + bleDevice.getMac() + ">>>rssi>>>" + bleDevice.getRssi() + ">>>>" + "status>>>" + status + '}';
            } else {
                return "SearchDeviceEvent{" + "status>>>" + status + '}';
            }
        }
    }


    public static class BleDeviceItem {
        public String deviceName;
        public String deviceMac;
        public byte[] scanRecord;
        public int rssi;
        public BleDevice bleDevice;
        public BluetoothDevice bluetoothDevice;

        @Override
        public String toString() {
            return "BleDeviceItem{" + "deviceName='" + deviceName + '\'' + ", deviceMac='" + deviceMac + '\'' + ", scanRecord=" + Arrays.toString(scanRecord) + ", rssi=" + rssi + '}';
        }
    }

    public static int getDeviceColumn(BleDevice device) {
        int column = Integer.parseInt(Utils.getHexStringForInt(0xFF & device.getScanRecord()[18]) + Utils.getHexStringForInt(0xFF & device.getScanRecord()[19]), 16);
        return column;
    }

    public static int getDeviceColumn(byte[] mScanRecord) {
        int column = Integer.parseInt(Utils.getHexStringForInt(0xFF & mScanRecord[18]) + Utils.getHexStringForInt(0xFF & mScanRecord[19]), 16);
        return column;
    }

    public static int getDeviceRow(BleDevice device) {
        int row = Integer.parseInt(Utils.getHexStringForInt(0xFF & device.getScanRecord()[17]), 16);
        return row;
    }

    public static int getDeviceRow(byte[] mScanRecord) {
        int row = Integer.parseInt(Utils.getHexStringForInt(0xFF & mScanRecord[17]), 16);
        return row;
    }

    public static int getDeviceColorTye(BleDevice device) {
        int type = Integer.parseInt(Utils.getHexStringForInt(0xFF & device.getScanRecord()[20]), 16);
        return type;
    }

    public static String getDeviceId(BleDevice device) {
        String id = Utils.getHexStringForInt(0xFF & device.getScanRecord()[10]).toUpperCase() + Utils.getHexStringForInt(0xFF & device.getScanRecord()[9]).toUpperCase();
        return id;
    }

    public static String getDeviceId(byte[] mScanRecord) {
        String id = Utils.getHexStringForInt(0xFF & mScanRecord[10]).toUpperCase() + Utils.getHexStringForInt(0xFF & mScanRecord[9]).toUpperCase();
        return id;
    }

    public static int getDeviceVersion(BleDevice device) {
        int version = Integer.parseInt(Utils.getHexStringForInt(0xFF & device.getScanRecord()[21]), 20);
        return version;
    }

    public static BleDeviceItem getBleDeviceItem(BleDevice bleDevice) {
        BleDeviceItem item = new BleDeviceItem();
        item.deviceMac = bleDevice.getMac();
        item.deviceName = bleDevice.getName();
        item.rssi = bleDevice.getRssi();
        item.scanRecord = bleDevice.getScanRecord();
        item.bleDevice = bleDevice;
        item.bluetoothDevice = bleDevice.getDevice();
        return item;
    }

}