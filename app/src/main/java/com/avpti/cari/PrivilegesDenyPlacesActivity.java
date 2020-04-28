package com.avpti.cari;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.avpti.cari.adapters.ArrayAdapterPlace;
import com.avpti.cari.classes.Common;
import com.avpti.cari.classes.Communication;
import com.avpti.cari.classes.Room;
import com.avpti.cari.classes.SessionManager;
import com.avpti.cari.services.ConnectivityReceiver;

import java.util.ArrayList;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class PrivilegesDenyPlacesActivity extends AppCompatActivity {
    public static int user_id;
    SessionManager sessionManager;
    //List and adapter of rooms
    ArrayList<Room> arrayList;
    ArrayAdapterPlace arrayAdapter;
    //views object
    ListView listUsers;
    Common common = new Common();
     int isAllow = 0;
    private ActionBar actionBar;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privileges_deny_places);
        ConnectivityReceiver.registerConnectivityReceiver(this);

        initToolbar();
        sessionManager = new SessionManager(this);
        listUsers = findViewById(R.id.list_rooms);
        arrayList = new ArrayList<>();

        Intent intent = getIntent();
        user_id = intent.getIntExtra("UID", -1);
        isAllow = intent.getIntExtra("is_allow", -1);


        arrayAdapter = new ArrayAdapterPlace(this, arrayList,isAllow);

        loadListData();

        listUsers.setAdapter(arrayAdapter);
        listUsers.requestFocus();
        listUsers.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Room room = arrayList.get(position);
                Intent in = new Intent(PrivilegesDenyPlacesActivity.this, ApplianceActivity.class);
                in.putExtra("ROOM_ID", room.getRoom_id());
                in.putExtra("USER_ID", user_id);
                if (isAllow == 1) in.putExtra("is_allow", 1);
                startActivity(in);
            }
        });
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

    private void initToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setTitle("Rooms & Places");
        //Tools.setSystemBarColor(this);
    }

    private void loadListData() {
        //clearing the previous list data
        arrayList.clear();

        //getting the data from server and load it to the list
        //sending data to server
        //creating object to communicate with server
        Communication cm = new Communication();

        //sending the login data to the server
        if (isAllow == 1)
            cm.sendData("10;" + user_id);
        else
            cm.sendData("6;" + sessionManager.getHouseId() + ";" + user_id);

        //receiving the response data from server
        String res[] = cm.getMessage().split(";");
        //get all data if operation success
        if (res[0].equals("11") || res[0].equals("19")) {
            int index = 1;
            for (int i = 1; i <= ((res.length - 1) / 4); i++) {
                arrayList.add(new Room(Integer.parseInt(res[index]), res[index + 1], res[index + 2], res[index + 3]));
                index += 4;
            }
        } else {
            Common.showAlertMessageAndFinish(PrivilegesDenyPlacesActivity.this, "404", "No rooms found!");
        }
        //notify the adapter to change the data
        arrayAdapter.notifyDataSetChanged();
    }
}
