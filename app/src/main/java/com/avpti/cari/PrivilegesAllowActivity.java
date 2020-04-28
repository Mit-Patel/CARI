package com.avpti.cari;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.avpti.cari.adapters.ArrayAdapterUser;
import com.avpti.cari.classes.Common;
import com.avpti.cari.classes.Communication;
import com.avpti.cari.classes.SessionManager;
import com.avpti.cari.classes.User;
import com.avpti.cari.services.ConnectivityReceiver;

import java.util.ArrayList;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class PrivilegesAllowActivity extends AppCompatActivity {

    Communication cm;
    SessionManager session;
    ArrayList<User> arrayList;
    ArrayAdapterUser arrayAdapter;
    ListView listView;
    private ActionBar actionBar;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privileges_deny);
        ConnectivityReceiver.registerConnectivityReceiver(this);
        initToolbar();

        listView = findViewById(R.id.lst_user);

        session = new SessionManager(this);
        cm = new Communication();
        arrayList = new ArrayList<>();

        arrayAdapter = new ArrayAdapterUser(this, arrayList, false);

        loadUserData();

        listView.setAdapter(arrayAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                User user = arrayList.get(position);
                Intent intent = new Intent(PrivilegesAllowActivity.this, PrivilegesDenyPlacesActivity.class);
                intent.putExtra("UID", user.getUser_id());
                intent.putExtra("is_allow", 1);
                startActivity(intent);
            }
        });
    }

    private void initToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setTitle("Allow Appliances");
        //Tools.setSystemBarColor(this);
    }

    void loadUserData() {
        int hid = session.getHouseId();
        cm.sendData("24;" + hid);
        String res[] = cm.getMessage().split(";");
        //get all data if operation success
        if (res[0].equals("47")) {
            int index = 1;
            for (int i = 1; i <= ((res.length - 1) / 3); i++) {
                arrayList.add(new User(Integer.parseInt(res[index]), res[index + 1], res[index + 2]));
                index += 3;
            }
        } else {
            Common.showAlertMessageAndFinish(this, "No data found", "No user denied");
        }

        //notify the adapter to change the data
        arrayAdapter.notifyDataSetChanged();
    }
}
