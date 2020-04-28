package com.avpti.cari;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.widget.ListView;

import com.avpti.cari.adapters.ArrayAdapterUser;
import com.avpti.cari.classes.Common;
import com.avpti.cari.classes.SessionManager;
import com.avpti.cari.classes.User;
import com.avpti.cari.services.BackgroundIntentService;
import com.avpti.cari.services.ConnectivityReceiver;

import java.util.ArrayList;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

public class AllUsersActivity extends AppCompatActivity {
    //Objects for views
    ListView listUsers;
    SwipeRefreshLayout swipeRefreshLayout;
    //List and adapter objects for the list
    ArrayList<User> arrayList;
    ArrayAdapterUser arrayAdapter;
    SessionManager sessionManager;
    RemoveUserReceiver receiver;
    private ActionBar actionBar;
    private Toolbar toolbar;
    //sessionManager manager object

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_users);
        initToolbar();
        ConnectivityReceiver.registerConnectivityReceiver(this);

        //assigning views to objects
        listUsers = findViewById(R.id.list_users);
        swipeRefreshLayout = findViewById(R.id.layout_swipe_refresh_user);

        //getting the sessionManager manager of the app
        sessionManager = new SessionManager(getApplicationContext());

        //creating a array list
        arrayList = new ArrayList<>();

        //initializing the adapter
        arrayAdapter = new ArrayAdapterUser(this, arrayList);

        //load the data into list
        loadListData();

        //and set the adapter to the list
        listUsers.setAdapter(arrayAdapter);

        registerCustomReceiver();
        //setting on refresh listener and reload the users list
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //stopping the refreshing view
                swipeRefreshLayout.setRefreshing(false);
                //calling the function for loading the data
                loadListData();
            }
        });

    }
    //receiver

    private void initToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setTitle("All Users");
        //Tools.setSystemBarColor(this);
    }

    private void loadListData() {
        //sending the login data to the server
        startBackgroundIntentService();
    }

    protected void onResume() {
        super.onResume();
        registerCustomReceiver();

        swipeRefreshLayout.setRefreshing(true);
        loadListData();
        swipeRefreshLayout.setRefreshing(false);
    }

    private void registerCustomReceiver() {
        receiver = new RemoveUserReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BackgroundIntentService.CARI_ACTION_USER_DELETE_GET);
        registerReceiver(receiver, intentFilter);
    }

    public void startBackgroundIntentService() {
        Intent intent = new Intent();
        intent.setClass(this, BackgroundIntentService.class);
        intent.setAction(BackgroundIntentService.CARI_ACTION_USER_DELETE_GET);
        intent.putExtra("house_id", sessionManager.getHouseId());
        startService(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);

    }


    class RemoveUserReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            arrayList.clear();

            //get the response of the server
            String res[] = intent.getStringArrayExtra(BackgroundIntentService.CARI_RPI);
            //get all data if operation success
            if (res[0].equals("7")) {
                int index = 1;
                for (int i = 1; i <= ((res.length - 1) / 3); i++) {
                    arrayList.add(new User(Integer.parseInt(res[index]), res[index + 1], res[index + 2]));
                    index += 3;
                }
            } else {
                Common.showAlertMessageAndFinish(AllUsersActivity.this, "No data found", "No users found in your house");
            }

            //notify the adapter to change the data
            arrayAdapter.notifyDataSetChanged();
        }
    }


}
