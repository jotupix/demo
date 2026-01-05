package com.jtkj.demo.device;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jtkj.demo.base.BaseFragment;
import com.jtkj.demo.databinding.SetsFragmentBinding;
import com.jtkj.demo.base.NoticeDialog;
import com.jtkj.library.commom.logger.CLog;
import com.jtkj.jotupix.core.JInfo;
import com.jtkj.jotupix.core.JotuPix;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class SetsFragment extends BaseFragment {
    private static final String TAG = SetsFragment.class.getSimpleName();

    SetsFragmentBinding mBinding;

    MainActivity mMainActivity;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mMainActivity = (MainActivity) getActivity();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mMainActivity = null;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = SetsFragmentBinding.inflate(inflater, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mBinding.deviceInfoLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DeviceManager.getInstance().getJotuPix().getDevInfo(new JotuPix.JGetDevInfoCallback() {
                    @Override
                    public void onEvent(JInfo devInfo) {
                        CLog.i(TAG, "deviceInfo>>>" + devInfo.toString());

                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                String deviceInfo="";
                                deviceInfo+="devName:"+devInfo.devName+"\n";
                                deviceInfo+="devId:"+devInfo.devId+"\n";
                                deviceInfo+="devWidth:"+devInfo.devWidth+"\n";
                                deviceInfo+="devHeight:"+devInfo.devHeight+"\n";
                                deviceInfo+="switchStatus:"+devInfo.switchStatus+"\n";
                                deviceInfo+="brightness:"+devInfo.brightness+"\n";
                                deviceInfo+="flip:"+devInfo.flip+"\n";
                                deviceInfo+="version:"+devInfo.version+"\n";

                                NoticeDialog.showDialog(getActivity(), deviceInfo);
                            }
                        });
                    }
                });
            }
        });
    }
}
