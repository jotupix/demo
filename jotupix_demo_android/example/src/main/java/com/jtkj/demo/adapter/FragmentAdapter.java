package com.jtkj.demo.adapter;

import android.view.ViewGroup;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentTransaction;

public class FragmentAdapter extends FragmentPagerAdapter {

    private List<Fragment> menuFragments;
    private FragmentManager fm;

    public FragmentAdapter(FragmentManager fm, List<Fragment> fragments) {
        this(fm);
        this.fm = fm;
        this.menuFragments = fragments;
    }

    public FragmentAdapter(FragmentManager fm) {
        super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
    }

    @Override
    public Fragment getItem(int arg0) {
        return menuFragments.get(arg0);
    }

    @Override
    public int getCount() {
        return menuFragments.size();
    }

    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
    }

    public void setFragments(List<Fragment> fragments) {
        if (this.menuFragments != null) {
            FragmentTransaction ft = fm.beginTransaction();
            for (Fragment f : this.menuFragments) {
                ft.remove(f);
            }
            ft.commit();
            fm.executePendingTransactions();
        }
        this.menuFragments = fragments;
        notifyDataSetChanged();
    }

}