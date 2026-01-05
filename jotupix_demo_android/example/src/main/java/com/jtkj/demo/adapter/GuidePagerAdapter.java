package com.jtkj.demo.adapter;

import android.view.View;

import java.util.List;

import androidx.viewpager.widget.PagerAdapter;


public class GuidePagerAdapter extends PagerAdapter {

    private List<View> pageViews;

    public GuidePagerAdapter(List<View> pageViews) {
        this.pageViews = pageViews;
    }

    @Override
    public int getCount() {
        return pageViews.size();
    }

    @Override
    public boolean isViewFromObject(View arg0, Object arg1) {
        return arg0 == arg1;
    }


    @Override
    public int getItemPosition(Object object) {
        return super.getItemPosition(object);
    }

}