package com.avpti.cari;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.avpti.cari.adapters.PlacesCardRecyclerViewAdapter;
import com.avpti.cari.classes.Common;
import com.avpti.cari.classes.Room;
import com.avpti.cari.classes.SessionManager;
import com.avpti.cari.classes.ViewAnimation;
import com.avpti.cari.services.BackgroundIntentService;
import com.avpti.cari.services.ConnectivityReceiver;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

public class PlacesActivity extends AppCompatActivity {

    FloatingActionButton fab_mic;
    FloatingActionButton fab_call;
    FloatingActionButton fab_add;
    private RecyclerView recyclerView;
    private ArrayList<Room> arrayList;
    private RoomReceiver receiver;
    private ConnectivityReceiver connectivityReceiver;
    private SwipeRefreshLayout swipeRefreshLayout;
    private SessionManager sessionManager;
    private PlacesCardRecyclerViewAdapter recyclerViewAdapter;
    private ActionBar actionBar;
    private Toolbar toolbar;
    private View parent_view;
    private View back_drop;
    private boolean rotate = false;
    private View lyt_mic;
    private View lyt_call;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cari_nav_drawer);

        initToolbar();
        initNavigationMenu();

        parent_view = findViewById(android.R.id.content);
        back_drop = findViewById(R.id.back_drop);

        fab_mic = (FloatingActionButton) findViewById(R.id.fab_mic);
        fab_call = (FloatingActionButton) findViewById(R.id.fab_call);
        fab_add = (FloatingActionButton) findViewById(R.id.fab_add);
        lyt_mic = findViewById(R.id.lyt_mic);
        lyt_call = findViewById(R.id.lyt_call);
        ViewAnimation.initShowOut(lyt_mic);
        ViewAnimation.initShowOut(lyt_call);
        back_drop.setVisibility(View.GONE);

        fab_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleFabMode(v);
            }
        });

        back_drop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleFabMode(fab_add);
            }
        });

        fab_mic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PlacesActivity.this, AddRoomActivity.class);
                startActivity(intent);
            }
        });

        fab_call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PlacesActivity.this, AddApplianceActivity.class);
                startActivity(intent);
            }
        });

        sessionManager = new SessionManager(getApplicationContext());

        if (sessionManager.isAdmin() == 0) {
            //normal user
            NavigationView navView = findViewById(R.id.nav_view);
            Menu menu = navView.getMenu();
            menu.removeItem(R.id.nav_deny_user);
            menu.removeItem(R.id.nav_allow_user);
            menu.removeItem(R.id.nav_add_user);
            menu.removeItem(R.id.nav_all_users);
        }

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2, RecyclerView.VERTICAL, false));
        arrayList = new ArrayList<>();
        recyclerViewAdapter = new PlacesCardRecyclerViewAdapter(arrayList, new PlacesCardRecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Room item) {
                Intent intent = new Intent(PlacesActivity.this, ApplianceActivity.class);
                intent.putExtra("ROOM_ID", item.getRoom_id());
                startActivity(intent);
            }
        });

        loadListData();

        recyclerView.setAdapter(recyclerViewAdapter);

        //refresh listener
        swipeRefreshLayout = findViewById(R.id.places_swipe_refresh);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //stop the animation
                swipeRefreshLayout.setRefreshing(false);

                //load the data into list
                loadListData();
            }
        });


        registerCustomReceiver();
        connectivityReceiver = ConnectivityReceiver.registerConnectivityReceiver(this);

    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        swipeRefreshLayout.setRefreshing(true);
        registerCustomReceiver();
        loadListData();
        swipeRefreshLayout.setRefreshing(false);
        if (rotate) toggleFabMode(fab_add);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        swipeRefreshLayout.setRefreshing(true);
        registerCustomReceiver();
        loadListData();
        swipeRefreshLayout.setRefreshing(false);
        if (rotate) toggleFabMode(fab_add);

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if (rotate) toggleFabMode(fab_add);
            else super.onBackPressed();
        }
    }

    private void toggleFabMode(View v) {
        rotate = ViewAnimation.rotateFab(v, !rotate);
        if (rotate) {
            ViewAnimation.showIn(lyt_mic);
            ViewAnimation.showIn(lyt_call);
            back_drop.setVisibility(View.VISIBLE);
        } else {
            ViewAnimation.showOut(lyt_mic);
            ViewAnimation.showOut(lyt_call);
            back_drop.setVisibility(View.GONE);
        }
    }

    private void loadListData() {
        startBackgroundIntentService(String.valueOf(sessionManager.getHouseId()), String.valueOf(sessionManager.getUserId()));
    }

    private void registerCustomReceiver() {
        receiver = new RoomReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BackgroundIntentService.CARI_ACTION_ROOMS);

        registerReceiver(receiver, intentFilter);
    }

    public void startBackgroundIntentService(String houseId, String userId) {
        Intent intent = new Intent();
        intent.setClass(this, BackgroundIntentService.class);
        intent.setAction(BackgroundIntentService.CARI_ACTION_ROOMS);
        intent.putExtra("house_id", houseId);
        intent.putExtra("user_id", userId);
        startService(intent);
    }

    private void initToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setTitle("Rooms & Places");
        //Tools.setSystemBarColor(this);
    }

    private void initNavigationMenu() {
        NavigationView nav_view = findViewById(R.id.nav_view);
        final DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }
        };

        drawer.addDrawerListener(toggle);
        toggle.syncState();
        nav_view.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(final MenuItem item) {
                // Handle navigation view item clicks here.
                int id = item.getItemId();

                if (id == R.id.nav_places) {
                    item.setChecked(true);
                } else if (id == R.id.nav_deny_user) {
                    startActivity(new Intent(getApplicationContext(), PrivilegesDenyActivity.class));
                } else if (id == R.id.nav_allow_user) {
                    startActivity(new Intent(getApplicationContext(), PrivilegesAllowActivity.class));
                } else if (id == R.id.nav_add_user) {
                    startActivity(new Intent(getApplicationContext(), AddUserActivity.class));
                } else if (id == R.id.nav_all_users) {
                    startActivity(new Intent(getApplicationContext(), AllUsersActivity.class));
                } else if (id == R.id.nav_profile_update) {
                    startActivity(new Intent(getApplicationContext(), UpdateUserActivity.class));
                } else if (id == R.id.nav_feedback) {
                    startActivity(new Intent(getApplicationContext(), FeedbackActivity.class));
                } else if (id == R.id.nav_about_us) {
                    startActivity(new Intent(getApplicationContext(), AboutUsActivity.class));
                } else if (id == R.id.nav_settings) {
                    startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
                } else if (id == R.id.nav_logout) {
                    attemptLogout();
                } else {
                    item.setChecked(true);
                }

                actionBar.setTitle(item.getTitle());
                drawer.closeDrawers();
                return true;
            }
        });

        // open drawer at start
        //drawer.openDrawer(GravityCompat.START);
    }

    private void attemptLogout() {
        sessionManager.logoutUser();
        finish();
    }

    class RoomReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(BackgroundIntentService.CARI_ACTION_ROOMS)) {
                arrayList.clear();
                //get the response of the server
                String res[] = intent.getStringArrayExtra(BackgroundIntentService.CARI_RPI);

                //get all data if operation success
                if (res[0].equals("11")) {
                    if(recyclerView.getVisibility() == View.INVISIBLE) {
                        recyclerView.setVisibility(View.VISIBLE);
                        TextView tv = findViewById(R.id.txtNoRoomsFound);
                        tv.setVisibility(View.INVISIBLE);
                    }
                    int index = 1;
                    for (int i = 1; i <= ((res.length - 1) / 4); i++) {
                        arrayList.add(new Room(Integer.parseInt(res[index]), res[index + 1], res[index + 2], res[index + 3]));
                        index += 4;
                    }
                } else {
                    arrayList.clear();
                    recyclerView.setVisibility(View.INVISIBLE);
                    TextView tv = findViewById(R.id.txtNoRoomsFound);
                    tv.setVisibility(View.VISIBLE);
                    return;
                }
                //notify the adapter to change the data
                recyclerViewAdapter.notifyDataSetChanged();
            }
        }
    }
}
