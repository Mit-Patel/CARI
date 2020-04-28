package com.avpti.cari;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager.widget.ViewPager;

import com.avpti.cari.adapters.FragmentPagerAdapterAppliance;
import com.avpti.cari.fragments.ApplianceFanFragment;
import com.avpti.cari.fragments.ApplianceLightFragment;
import com.avpti.cari.services.ConnectivityReceiver;
import com.google.android.material.tabs.TabItem;
import com.google.android.material.tabs.TabLayout;

public class ApplianceActivity extends AppCompatActivity implements ApplianceFanFragment.OnApplianceNotFound {


    public static int room_id;
    public static int user_id = -1;
    public static int is_allow = 0;
    //objects of views
    TabLayout tabLayout;
    TabItem tabLight, tabFan;
    ViewPager viewPager;
    FragmentPagerAdapterAppliance adapterAppliance;
    SwipeRefreshLayout swipeRefreshLayout;
    private ActionBar actionBar;
    private Toolbar toolbar;
    private FragmentRefreshListener fragmentRefreshListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appliance);
        ConnectivityReceiver.registerConnectivityReceiver(this);
        initToolbar();

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        if (bundle.getInt("ROOM_ID") != 0)
            room_id = intent.getIntExtra("ROOM_ID", -1);
        if (bundle.getInt("USER_ID") != 0)
            user_id = intent.getIntExtra("USER_ID", -1);
        if (bundle.getInt("USER_ID") != 0)
            is_allow = intent.getIntExtra("is_allow", -1);

        //assigning view to object
        tabLayout = findViewById(R.id.tabLayout);
        tabLight = findViewById(R.id.tabLight);
        tabFan = findViewById(R.id.tabFan);
        viewPager = findViewById(R.id.viewPager);

        adapterAppliance = new FragmentPagerAdapterAppliance(this, getSupportFragmentManager(), tabLayout.getTabCount());

        viewPager.setAdapter(adapterAppliance);

        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());

            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
        //refresh listener
        swipeRefreshLayout = findViewById(R.id.srlAppliances);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefreshLayout.setRefreshing(false);
                if (getFragmentRefreshListener() != null) {
                    getFragmentRefreshListener().onRefresh();
                }
            }
        });


        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout) {
            @Override
            public void onPageScrollStateChanged(int state) {
                toggleRefreshing(state == ViewPager.SCROLL_STATE_IDLE);
            }
        });


    }

    public void toggleRefreshing(boolean enabled) {
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setEnabled(enabled);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        user_id = -1;
        is_allow = 0;
    }

    public FragmentRefreshListener getFragmentRefreshListener() {
        return fragmentRefreshListener;
    }

    public void setFragmentRefreshListener(FragmentRefreshListener fragmentRefreshListener) {
        this.fragmentRefreshListener = fragmentRefreshListener;
    }

    @Override
    protected void onResume() {
        super.onResume();
        swipeRefreshLayout.setRefreshing(true);
        if (getFragmentRefreshListener() != null) {
            getFragmentRefreshListener().onRefresh();
        }
        swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        swipeRefreshLayout.setRefreshing(true);
        if (getFragmentRefreshListener() != null) {
            getFragmentRefreshListener().onRefresh();
        }
        swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onAttachFragment(@NonNull Fragment fragment) {
        if (fragment instanceof ApplianceFanFragment) {
            ApplianceFanFragment fanFragment = (ApplianceFanFragment) fragment;
            fanFragment.setOnApplianceNotFound(this);
        } else if (fragment instanceof ApplianceLightFragment) {
            ApplianceLightFragment lightFragment = (ApplianceLightFragment) fragment;
            lightFragment.setOnApplianceNotFound(this);
        }
    }

    @Override
    public void disableTab(int index) {
        if (tabLayout.getTabCount() == 1) {
            Toast.makeText(this, "There are no appliances in this room", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        tabLayout.removeTabAt(index);
        adapterAppliance.notifyDataSetChanged();
    }

    private void initToolbar() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setTitle("Appliances");
        //Tools.setSystemBarColor(this);
    }

}
