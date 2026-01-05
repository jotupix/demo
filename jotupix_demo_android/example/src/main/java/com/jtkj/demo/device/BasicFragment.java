package com.jtkj.demo.device;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.SeekBar;

import com.jtkj.demo.base.BaseFragment;
import com.jtkj.demo.databinding.BasicFragmentBinding;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class BasicFragment extends BaseFragment {
    BasicFragmentBinding mBinding;

    MainActivity mMainActivity;

    List<ChooseDialog.ChooseItem> mRotateData;
    ChooseDialog.ChooseItem mChooseItem = new ChooseDialog.ChooseItem("No Flip", 0, 0);

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mMainActivity = (MainActivity) getActivity();
//        EventAgent.register(this);
    }

    @Override
    public void onDetach() {
        super.onDetach();
//        EventAgent.unregister(this);
        mMainActivity = null;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = BasicFragmentBinding.inflate(inflater, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mBinding.switchCb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    DeviceManager.getInstance().getJotuPix().sendSwitchStatus(1);
                } else {
                    DeviceManager.getInstance().getJotuPix().sendSwitchStatus(0);
                }
            }
        });

        mBinding.rotateTv.setText(mChooseItem.name);
        mRotateData = new ArrayList<>();
        mRotateData.add(new ChooseDialog.ChooseItem("No Flip", 0, 0));
        mRotateData.add(new ChooseDialog.ChooseItem("XY Flip", 1, 1));
        mRotateData.add(new ChooseDialog.ChooseItem("X Flip", 2, 2));
        mRotateData.add(new ChooseDialog.ChooseItem("Y Flip", 3, 3));

        mBinding.rotateLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChooseDialog.showDialog(mMainActivity, mRotateData, mChooseItem, new ChooseDialog.ChooseListener() {
                    @Override
                    public void onItemChoose(ChooseDialog.ChooseItem item, int index) {
                        mChooseItem = item;
                        mBinding.rotateTv.setText(mChooseItem.name);
                        DeviceManager.getInstance().getJotuPix().sendScreenFlip(mChooseItem.value);
                    }
                });
            }
        });

        mBinding.brightSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                DeviceManager.getInstance().getJotuPix().sendBrightness(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

    }
}
