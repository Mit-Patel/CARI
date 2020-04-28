package com.avpti.cari.adapters;

import android.content.Context;

import com.avpti.cari.fragments.ApplianceFanFragment;
import com.avpti.cari.fragments.ApplianceLightFragment;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

public class FragmentPagerAdapterAppliance extends FragmentPagerAdapter {

    int totalTabs;
    private Context context;

    public FragmentPagerAdapterAppliance(Context context, FragmentManager fm, int totalTabs) {
        super(fm);
        this.context = context;
        this.totalTabs = totalTabs;

    }


    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new ApplianceLightFragment();
            case 1:
                return new ApplianceFanFragment();
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return totalTabs;
    }
/*
    @Override
    public CharSequence removeFragment(int position) {
        mFragmentTitleList.remove(position);
        mFragmentList.remove(position);
        notifyDatasetChanged();
    }*/
}
