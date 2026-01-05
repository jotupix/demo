package com.jtkj.library.fastble.data;


import android.bluetooth.BluetoothDevice;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.Arrays;


public class BleDevice implements Parcelable {

    private BluetoothDevice mDevice;
    private byte[] mScanRecord;
    private int mRssi;
    private long mTimestampNanos;
    public String mDeviceName;
    public String mDeviceMac;
    public String mDeviceKey;

    public BleDevice(BluetoothDevice device) {
        mDevice = device;
        mDeviceName = device.getName();
        mDeviceMac = device.getAddress();
        mDeviceKey = (mDeviceName + mDeviceMac);
    }

    public BleDevice(BluetoothDevice device, int rssi, byte[] scanRecord, long timestampNanos) {
        mDevice = device;
        mDeviceName = device.getName();
        mDeviceMac = device.getAddress();
        mDeviceKey = (mDeviceName + mDeviceMac);
        mScanRecord = scanRecord;
        mRssi = rssi;
        mTimestampNanos = timestampNanos;
    }

    protected BleDevice(Parcel in) {
        mDevice = in.readParcelable(BluetoothDevice.class.getClassLoader());
        mScanRecord = in.createByteArray();
        mRssi = in.readInt();
        mTimestampNanos = in.readLong();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(mDevice, flags);
        dest.writeByteArray(mScanRecord);
        dest.writeInt(mRssi);
        dest.writeLong(mTimestampNanos);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<BleDevice> CREATOR = new Creator<BleDevice>() {
        @Override
        public BleDevice createFromParcel(Parcel in) {
            return new BleDevice(in);
        }

        @Override
        public BleDevice[] newArray(int size) {
            return new BleDevice[size];
        }
    };

    public String getName() {
        return mDeviceName;
    }

    public String getMac() {
        return mDeviceMac;
    }

    public String getKey() {
        return mDeviceKey;
    }

    public BluetoothDevice getDevice() {
        return mDevice;
    }

    public void setDevice(BluetoothDevice device) {
        this.mDevice = device;
    }

    public byte[] getScanRecord() {
        return mScanRecord;
    }

    public void setScanRecord(byte[] scanRecord) {
        this.mScanRecord = scanRecord;
    }

    public int getRssi() {
        return mRssi;
    }

    public void setRssi(int rssi) {
        this.mRssi = rssi;
    }

    public long getTimestampNanos() {
        return mTimestampNanos;
    }

    public void setTimestampNanos(long timestampNanos) {
        this.mTimestampNanos = timestampNanos;
    }

    @Override
    public String toString() {
        return "BleDevice{" +
                "mDevice=" + mDevice +
                ", deviceMac='" + mDeviceMac + '\'' +
                ", deviceName='" + mDeviceName + '\'' +
                ", deviceKey='" + mDeviceKey + '\'' +
                ", mScanRecord=" + Arrays.toString(mScanRecord) +
                ", mRssi=" + mRssi +
                ", mTimestampNanos=" + mTimestampNanos +
                '}';
    }
}
