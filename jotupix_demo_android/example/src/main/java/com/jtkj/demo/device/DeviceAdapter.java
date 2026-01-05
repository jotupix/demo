package com.jtkj.demo.device;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.jtkj.demo.R;
import com.jtkj.demo.adapter.BaseAdapter;
import com.jtkj.demo.databinding.DeviceItemBinding;
import com.jtkj.library.commom.logger.CLog;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DeviceAdapter extends BaseAdapter<DeviceManager.BleDeviceItem> {
    private static final String TAG = DeviceAdapter.class.getSimpleName();

    public interface DeviceClickListener {
        void onDisconnectClick(DeviceManager.BleDeviceItem bleDevice);
    }

    public static final int START_CONNECT = 1;
    public static final int CONNECT_SUCCESS = 2;
    public static final int CONNECT_FAILURE = 3;
    public static final int ACTIVE_DISCONNECT = 4;
    public static final int PASSWORD_VERIFY_SUCCESS = 5;
    public static final int PASSWORD_VERIFY_FAILURE = 6;
    public static final int DEFAULT_UN_CONNECT = 7;

    public DeviceClickListener mListener;

    public void setDeviceClickListener(DeviceClickListener listener) {
        mListener = listener;
    }

    public Map<String, Integer> mDeviceStateMap = new HashMap<>();

    public DeviceAdapter(Context ctx) {
        super(ctx);
    }

    public boolean contains(DeviceManager.BleDeviceItem bleDevice) {
        for (DeviceManager.BleDeviceItem device : getDataSet()) {
            if (device.deviceMac.equalsIgnoreCase(bleDevice.deviceMac)) {
                return true;
            }
        }
        return false;
    }

    public void setDeviceState(String deviceAddress, int flag) {
        mDeviceStateMap.put(deviceAddress, flag);
        notifyDataSetChanged();
    }

    public void addDevice(DeviceManager.BleDeviceItem bleDevice) {
        if (!contains(bleDevice)) {
            addData(bleDevice);
            notifyDataSetChanged();
        } else {
            int index = getDevicePosition(bleDevice);
            if (index >= 0) {
                CLog.i(TAG, "addBleDevice>>>" + bleDevice.deviceMac + ">>>name>>>" + bleDevice.deviceName + ">>>id>>>" + DeviceManager.getDeviceId(bleDevice.scanRecord));
                setItem(index, bleDevice);
                notifyDataSetChanged();
            }
        }
    }

    public int getDevicePosition(DeviceManager.BleDeviceItem bleDevice) {
        int index = -1;
        for (int i = 0; i < getCount(); i++) {
            DeviceManager.BleDeviceItem item = getItem(i);
            if (!TextUtils.isEmpty(item.deviceMac)) {
                if (item.deviceMac.equalsIgnoreCase(bleDevice.deviceMac)) {
                    index = i;
                    break;
                }
            }
        }
        return index;
    }

    public int getDeviceState(String deviceAddress) {
        if (mDeviceStateMap.containsKey(deviceAddress)) {
            return mDeviceStateMap.get(deviceAddress);
        } else {
            return DEFAULT_UN_CONNECT;
        }
    }

    public void removeDevice(String address) {
        DeviceManager.BleDeviceItem bleDevice = null;
        for (DeviceManager.BleDeviceItem device : getDataSet()) {
            if (device.deviceMac.equalsIgnoreCase(address)) {
                bleDevice = device;
                break;
            }
        }

        if (null != bleDevice) {
            mDeviceStateMap.remove(bleDevice.deviceMac);
            remove(bleDevice);
        }
        notifyDataSetChanged();
    }

    public void removeDevices(List<DeviceManager.BleDeviceItem> devices) {
        for (DeviceManager.BleDeviceItem device : devices) {
            removeDevice(device.deviceMac);
        }
    }

    public void show(ImageView view) {
        view.clearAnimation();
        Animation loadAnimation = AnimationUtils.loadAnimation(mContext, R.anim.connecting);
        view.startAnimation(loadAnimation);
    }

    public void hide(ImageView view) {
        view.clearAnimation();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        DeviceManager.BleDeviceItem device = getItem(position);
        String deviceName;
        DeviceItemBinding binding;
        if (convertView == null) {
            binding = DeviceItemBinding.inflate(LayoutInflater.from(parent.getContext()), null, false);
            convertView = binding.getRoot();
            convertView.setTag(binding);
        } else {
            binding = (DeviceItemBinding) convertView.getTag();
        }
        if (!mDeviceStateMap.containsKey(device.deviceMac)) {
            mDeviceStateMap.put(device.deviceMac, DEFAULT_UN_CONNECT);
        }
        int flag = mDeviceStateMap.get(device.deviceMac);
        switch (flag) {
            case DEFAULT_UN_CONNECT:
            case CONNECT_FAILURE:
            case ACTIVE_DISCONNECT:
                hide(binding.connectingImg);
                binding.connectingImg.setVisibility(View.INVISIBLE);
                binding.connectSuccessImg.setVisibility(View.INVISIBLE);
                binding.disconnectImg.setVisibility(View.INVISIBLE);
                binding.passwordIv.setVisibility(View.INVISIBLE);
                break;
            case START_CONNECT:
                binding.connectingImg.setVisibility(View.VISIBLE);
                binding.connectSuccessImg.setVisibility(View.INVISIBLE);
                binding.disconnectImg.setVisibility(View.INVISIBLE);
                binding.passwordIv.setVisibility(View.INVISIBLE);
                show(binding.connectingImg);
                break;
            case CONNECT_SUCCESS:
                hide(binding.connectingImg);
                binding.connectingImg.setVisibility(View.INVISIBLE);
                binding.connectSuccessImg.setVisibility(View.VISIBLE);
                binding.disconnectImg.setVisibility(View.VISIBLE);
                binding.passwordIv.setVisibility(View.INVISIBLE);
                break;
            case PASSWORD_VERIFY_SUCCESS:
                binding.connectingImg.setVisibility(View.INVISIBLE);
                binding.connectSuccessImg.setVisibility(View.VISIBLE);
                binding.disconnectImg.setVisibility(View.VISIBLE);
                binding.passwordIv.setVisibility(View.VISIBLE);
//                if (DeviceManager.getInstance().isDevicePasswordDefault(device.bleDevice)) {
//                    binding.passwordIv.setImageResource(R.drawable.ic_password_default);
//                } else {
//                    binding.passwordIv.setImageResource(R.drawable.ic_password_locked);
//                }
                break;
            case PASSWORD_VERIFY_FAILURE:
                binding.connectingImg.setVisibility(View.INVISIBLE);
                binding.connectSuccessImg.setVisibility(View.VISIBLE);
                binding.disconnectImg.setVisibility(View.VISIBLE);
                binding.passwordIv.setVisibility(View.INVISIBLE);
                break;
        }

        try {
            deviceName = (device.deviceName + "-" + DeviceManager.getDeviceId(device.scanRecord));
            binding.deviceNameTv.setText(deviceName);
            int row = DeviceManager.getDeviceRow(device.scanRecord);
            int column = DeviceManager.getDeviceColumn(device.scanRecord);
            binding.deviceAddressTv.setText("Pixel:" + row + "x" + column);

            binding.signalIv.setImageResource(getSignalImage(device.rssi));
            binding.disconnectImg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (null != mListener) {
                        mListener.onDisconnectClick(device);
                    }
                }
            });

        } catch (Exception e) {
            CLog.i(TAG, "getView>>>" + e.getMessage());
        }
        return convertView;
    }

    public int getSignalImage(int rssi) {
        if (rssi >= -60 && rssi <= 0) {
            return R.drawable.ic_signal_four;
        } else if (rssi >= -70 && rssi < -60) {
            return R.drawable.ic_signal_three;
        } else if (rssi >= -80 && rssi < -70) {
            return R.drawable.ic_signal_two;
        } else if (rssi < -80) {
            return R.drawable.ic_signal_one;
        }
        return R.drawable.ic_signal_one;
    }
}
