package com.jtkj.library.infrastructure.base;

import androidx.fragment.app.Fragment;


/**
 * Created by yfxiong on 2017/2/9.
 * Copyright  yfxiong www.jotus-tech.com. All Rights Reserved.
 */
public abstract class AnalyticsFragment extends Fragment {
    @Override
    public void onResume() {
        super.onResume();
//		MobclickAgent.onPageStart(getClass().getSimpleName());
    }

    @Override
    public void onPause() {
        super.onPause();
//		MobclickAgent.onPageEnd(getClass().getSimpleName());
    }
}
