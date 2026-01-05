package com.jtkj.demo.device;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.jtkj.demo.CoolLED;
import com.jtkj.demo.R;
import com.jtkj.demo.base.BaseFragment;
import com.jtkj.demo.databinding.DeviceFragmentBinding;
import com.jtkj.library.commom.logger.CLog;
import com.jtkj.library.fastble.data.BleDevice;
import com.jtkj.library.fastble.data.BleScanState;
import com.jtkj.library.fastble.scan.BleScanner;
import com.jtkj.library.infrastructure.eventbus.EventAgent;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

public class DeviceFragment extends BaseFragment implements
        AdapterView.OnItemClickListener,
        SwipeRefreshLayout.OnRefreshListener,
        DeviceAdapter.DeviceClickListener,
        View.OnClickListener {
    private static final String TAG = DeviceFragment.class.getSimpleName();
    DeviceAdapter mAdapter;
    MainActivity mMainActivity;
    DeviceFragmentBinding mBinding;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mMainActivity = (MainActivity) getActivity();
        EventAgent.register(this);
        CLog.i(TAG, "onAttach");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        EventAgent.unregister(this);
        mMainActivity = null;
        CLog.i(TAG, "onDetach");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        CLog.i(TAG, "onCreateView");
        mBinding = DeviceFragmentBinding.inflate(inflater, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        CLog.i(TAG, "onViewCreated");
        mBinding.pullRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_bright, android.R.color.holo_green_light, android.R.color.holo_orange_light, android.R.color.holo_red_light);
        mBinding.pullRefreshLayout.setOnRefreshListener(this);
        mAdapter = new DeviceAdapter(getActivity());
        mAdapter.setDeviceClickListener(this);
        mBinding.lv.setAdapter(mAdapter);
        mBinding.lv.setOnItemClickListener(this);

        mBinding.backBtn.setOnClickListener(this);
        mBinding.searchBtn.setOnClickListener(this);
        mBinding.contactUsBtn.setOnClickListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        CLog.i(TAG, "onResume");
        if (null != mBinding.pullRefreshLayout) {
            mBinding.pullRefreshLayout.setOnRefreshListener(this);
            mBinding.pullRefreshLayout.setEnabled(true);
            mBinding.pullRefreshLayout.setRefreshing(true);
        }
        onRefresh();
    }

    @Override
    public void onPause() {
        super.onPause();
        CLog.i(TAG, "onPause");
        if (null != mBinding.pullRefreshLayout) {
            mBinding.pullRefreshLayout.setRefreshing(false);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }


    @Override
    public void onRefresh() {
        CLog.i(TAG, "onRefresh");
        if (BleScanner.getInstance().getScanState() == BleScanState.STATE_IDLE) {
            DeviceManager.getInstance().startSearchDevice();

            List<DeviceManager.BleDeviceItem> devices = new ArrayList<>();
            for (DeviceManager.BleDeviceItem device : mAdapter.getDataSet()) {
                int state = mAdapter.getDeviceState(device.deviceMac);
                if (state != DeviceAdapter.CONNECT_SUCCESS && state != DeviceAdapter.START_CONNECT
                        && state != DeviceAdapter.PASSWORD_VERIFY_SUCCESS && state != DeviceAdapter.PASSWORD_VERIFY_FAILURE) {
                    devices.add(device);
                }
            }
            if (devices.size() > 0) {
                mAdapter.removeDevices(devices);
            }
        } else {
            if (null != mBinding.pullRefreshLayout) {
                mBinding.pullRefreshLayout.setRefreshing(false);
            }
        }
    }

    private void checkIsEmpty() {
        if (mAdapter.isEmpty()) {
            mBinding.emptyLayout.setVisibility(View.VISIBLE);
            mBinding.pullRefreshLayout.setVisibility(View.GONE);
        } else {
            mBinding.emptyLayout.setVisibility(View.GONE);
            mBinding.pullRefreshLayout.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onDisconnectClick(DeviceManager.BleDeviceItem bleDevice) {
        DeviceManager.getInstance().disConnectDevice(bleDevice.bleDevice);
        CLog.i(TAG, "onDisconnectClick device: " + bleDevice.deviceMac);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        DeviceManager.BleDeviceItem device = mAdapter.getItem(position);
        DeviceManager.getInstance().connectDevice(device.bleDevice);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.back_btn:
                mMainActivity.switchDeviceFragment();
                break;
            case R.id.search_btn:
                if (null != mBinding.pullRefreshLayout) {
                    mBinding.pullRefreshLayout.setRefreshing(true);
                }
                onRefresh();
                break;
            case R.id.contact_us_btn:
                break;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(final DeviceManager.BleDeviceConnectEvent event) {
        CLog.i(TAG, "onEvent(final BleService.BleDeviceConnectEvent event)>>>" + event.toString());
        switch (event.status) {
            case DeviceManager.BleDeviceConnectEvent.ACTIVE_DISCONNECT:
//                CoolLED.toast(CoolLED.getInstance().string(R.string.active_disconnect));
//                break;
            case DeviceManager.BleDeviceConnectEvent.NOT_ACTIVE_DISCONNECT:
                CoolLED.toast(CoolLED.getInstance().string(R.string.not_active_disconnect));
                BleDevice bleDevice = event.bleDevice;
                CLog.i(TAG, "addBleDevice>>>mac>>>" + bleDevice.getMac() + ">>>name>>>" + bleDevice.getName() + ">>>id>>>" + DeviceManager.getDeviceId(bleDevice) + ">>>disconnect");
                CLog.i(TAG, "connectDevice>>>ACTIVE_DISCONNECTORNOT_ACTIVE_DISCONNECT>>>addresses>>>" + event.bleDevice.getMac() + ">>>name>>> " + event.bleDevice.getName() + " >>>id>>>" + DeviceManager.getDeviceId(event.bleDevice));
                mAdapter.setDeviceState(event.bleDevice.getMac(), DeviceAdapter.CONNECT_FAILURE);
                mAdapter.notifyDataSetChanged();
                DeviceManager.getInstance().startSearchDevice();
                break;
            case DeviceManager.BleDeviceConnectEvent.CONNECT_FAILURE:
                mAdapter.setDeviceState(event.bleDevice.getMac(), DeviceAdapter.CONNECT_FAILURE);
                mAdapter.notifyDataSetChanged();
                CLog.i(TAG, "connectDevice>>>onConnectFail2>>>addresses>>>" + event.bleDevice.getMac() + ">>>name>>>" + event.bleDevice.getName() + " >>>id>>>" + DeviceManager.getDeviceId(event.bleDevice));
                showTipsDialog(R.string.connect_device_failed, true, R.string.contact_us, R.string.cancel,
                        null,
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        });
                break;
            case DeviceManager.BleDeviceConnectEvent.CONNECT_SUCCESS:
                CLog.i(TAG, "DeviceManager.BleDeviceConnectEvent.CONNECT_SUCCESS>>>bleDevice.getMac()>>>" + event.bleDevice.getMac());
                mAdapter.setDeviceState(event.bleDevice.getMac(), DeviceAdapter.CONNECT_SUCCESS);
                break;
            case DeviceManager.BleDeviceConnectEvent.START_CONNECT:
                CLog.i(TAG, "DeviceManager.BleDeviceConnectEvent.START_CONNECT>>>bleDevice.getMac()>>>" + event.bleDevice.mDeviceMac);
                mAdapter.setDeviceState(event.bleDevice.getMac(), DeviceAdapter.START_CONNECT);
                break;
            case DeviceManager.BleDeviceConnectEvent.PASSWORD_VERIFY_SUCCESS:
                CLog.i(TAG, "connectDevice>>>PASSWORD_VERIFY_SUCCESS>>>addresses>>>" + event.bleDevice.getMac() + ">>>name>>> " + event.bleDevice.getName() + " >>>id>>>" + DeviceManager.getDeviceId(event.bleDevice));
                mAdapter.setDeviceState(event.bleDevice.getMac(), DeviceAdapter.PASSWORD_VERIFY_SUCCESS);
                break;
            case DeviceManager.BleDeviceConnectEvent.PASSWORD_VERIFY_FAILURE:
                CLog.i(TAG, "connectDevice>>>PASSWORD_VERIFY_FAILURE>>>addresses>>>" + event.bleDevice.getMac() + ">>>name>>> " + event.bleDevice.getName() + " >>>id>>>" + DeviceManager.getDeviceId(event.bleDevice));
                mAdapter.setDeviceState(event.bleDevice.getMac(), DeviceAdapter.PASSWORD_VERIFY_FAILURE);
                break;
        }

        DeviceManager.getInstance().setBleDeviceItems(mAdapter.getDataSet());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(final DeviceManager.SearchDeviceEvent event) {
        CLog.i(TAG, "onEvent(final BleService.SearchDeviceEvent event)>>>" + event.toString());
        switch (event.status) {
            case DeviceManager.SearchDeviceEvent.SCAN_FINISHED:
                if (null != mBinding.pullRefreshLayout) {
                    mBinding.pullRefreshLayout.setRefreshing(false);
                }
                checkIsEmpty();
                CLog.i(TAG, "onEvent(final BleService.SearchDeviceEvent event)>>>3>>>" + event.toString());
                break;
            case DeviceManager.SearchDeviceEvent.ON_LE_SCAN:
                break;
            case DeviceManager.SearchDeviceEvent.SCANNING:
                if (null != mBinding.pullRefreshLayout) {
                    mBinding.pullRefreshLayout.setRefreshing(false);
                }
                if (TextUtils.isEmpty(event.bleDevice.getName())) {
                    return;
                }
                mAdapter.addDevice(DeviceManager.getBleDeviceItem(event.bleDevice));
                DeviceManager.getInstance().setBleDeviceItems(mAdapter.getDataSet());
                checkIsEmpty();
                CLog.i(TAG, "onEvent(final BleService.SearchDeviceEvent event)>>>1>>>" + event.toString());
                break;
            case DeviceManager.SearchDeviceEvent.START_SEARCH:
                CLog.i(TAG, "onEvent(final BleService.SearchDeviceEvent event)>>>2>>>" + event.toString());
                break;
        }
    }

}