package com.jtkj.demo.device;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;

import com.jtkj.demo.R;
import com.jtkj.demo.base.BaseFragment;
import com.jtkj.demo.databinding.MainFragmentBinding;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

public class MainFragment extends BaseFragment {

    private static final int BASIC = 1;
    private static final int TEXT = 2;
    private static final int ANIMATION = 3;
    private static final int SETTINGS = 4;

    private int mDevicePageType = BASIC;

    MainFragmentBinding mBinding;
    MainActivity mMainActivity;
    BasicFragment mBasicFragment;
    TextFragment mTextFragment;
    AnimationFragment mAnimationFragment;
    SetsFragment mSetsFragment;

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
        mBinding = MainFragmentBinding.inflate(inflater, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        showFragment(BASIC);
        mBinding.deviceIbt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMainActivity.switchDeviceFragment();
            }
        });

        mBinding.menuGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.basic_btn:
                        showFragment(BASIC);
                        break;
                    case R.id.text_btn:
                        showFragment(TEXT);
                        break;
                    case R.id.animation_btn:
                        showFragment(ANIMATION);
                        break;
                    case R.id.sets_btn:
                        showFragment(SETTINGS);
                        break;
                }
            }
        });
    }

    private void showFragment(int page) {
        mDevicePageType = page;
        FragmentManager fm = getChildFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        mBasicFragment = (BasicFragment) fm.findFragmentByTag(BasicFragment.class.getSimpleName());
        mTextFragment = (TextFragment) fm.findFragmentByTag(TextFragment.class.getSimpleName());
        mAnimationFragment = (AnimationFragment) fm.findFragmentByTag(AnimationFragment.class.getSimpleName());
        mSetsFragment = (SetsFragment) fm.findFragmentByTag(SetsFragment.class.getSimpleName());

        if (mBasicFragment != null) {
            ft.hide(mBasicFragment);
        }

        if (mTextFragment != null) {
            ft.hide(mTextFragment);
        }

        if (mAnimationFragment != null) {
            ft.hide(mAnimationFragment);
        }

        if (mSetsFragment != null) {
            ft.hide(mSetsFragment);
        }

        switch (page) {
            case BASIC:
                if (mBasicFragment == null) {
                    mBasicFragment = new BasicFragment();
                    ft.add(R.id.content_layout, mBasicFragment, BasicFragment.class.getSimpleName());
                } else {
                    ft.show(mBasicFragment);
                }
                break;
            case TEXT:
                if (mTextFragment == null) {
                    mTextFragment = new TextFragment();
                    ft.add(R.id.content_layout, mTextFragment, TextFragment.class.getSimpleName());
                } else {
                    ft.show(mTextFragment);
                }
                break;
            case ANIMATION:
                if (mAnimationFragment == null) {
                    mAnimationFragment = new AnimationFragment();
                    ft.add(R.id.content_layout, mAnimationFragment, AnimationFragment.class.getSimpleName());
                } else {
                    ft.show(mAnimationFragment);
                }
                break;
            case SETTINGS:
                if (mSetsFragment == null) {
                    mSetsFragment = new SetsFragment();
                    ft.add(R.id.content_layout, mSetsFragment, SetsFragment.class.getSimpleName());
                } else {
                    ft.show(mSetsFragment);
                }
                break;
            default:
                break;
        }
        ft.commitAllowingStateLoss();
    }

}
