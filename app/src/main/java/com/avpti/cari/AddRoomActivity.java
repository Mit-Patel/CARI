package com.avpti.cari;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import com.avpti.cari.classes.Common;
import com.avpti.cari.classes.Room;
import com.avpti.cari.classes.SessionManager;
import com.avpti.cari.services.BackgroundIntentService;
import com.avpti.cari.services.ConnectivityReceiver;

import java.util.ArrayList;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class AddRoomActivity extends AppCompatActivity {
    SessionManager session;
    int hid;
    EditText edtNumber, edtName;
    CreateRoomReceiver receiver;
    Spinner spType;
    String type;
    ArrayAdapter<String> adapterType;
    ArrayList<String> arrayListType;
    private ActionBar actionBar;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_room);
        ConnectivityReceiver.registerConnectivityReceiver(this);

        initToolbar();

        edtNumber = findViewById(R.id.et_room_no);
        edtName = findViewById(R.id.et_room_name);
        spType = findViewById(R.id.et_room_type);

        arrayListType = new ArrayList<>();
        arrayListType.add("Living Room");
        arrayListType.add("Kitchen");
        arrayListType.add("Bed Room");

        adapterType = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, arrayListType);
        spType.setAdapter(adapterType);

        spType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        type = "living";
                        break;
                    case 1:
                        type = "kitchen";
                        break;
                    case 2:
                        type = "bed";
                        break;
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        session = new SessionManager(this);
        hid = session.getHouseId();

        registerCustomReceiver();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);

    }

    @Override
    protected void onResume() {
        super.onResume();
        registerCustomReceiver();
    }

    private void initToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setTitle("Add Room");
        //Tools.setSystemBarColor(this);
    }

    public void add(View v) {
        validateData();
    }

    public void validateData() {
        //clearing the errors
        edtName.setError(null);
        edtNumber.setError(null);
        String number = edtNumber.getText().toString().trim();
        String name = edtName.getText().toString().trim();

        //other variables
        boolean cancel = false;
        View focusView = null;

        if (TextUtils.isEmpty(name)) {
            edtName.setError(getString(R.string.error_field_required));
            focusView = edtName;
            cancel = true;
        }
        if (TextUtils.isEmpty(number)) {
            edtNumber.setError(getString(R.string.error_field_required));
            focusView = edtName;
            cancel = true;
        }
        //show error or login the user
        if (cancel) {
            focusView.requestFocus();
        } else {
            //sending data to server

            Room room = new Room(name, number, hid, type);
            startBackgroundIntentService(room);
        }
    }

    //resetting data fuunction
    public void resetData() {
        edtNumber.setText("");
        edtName.setText("");
    }

    private void registerCustomReceiver() {
        receiver = new CreateRoomReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BackgroundIntentService.CARI_ACTION_ROOM_CREATE);

        registerReceiver(receiver, intentFilter);
    }

    public void startBackgroundIntentService(Room room) {
        Intent intent = new Intent();
        intent.setClass(this, BackgroundIntentService.class);
        intent.setAction(BackgroundIntentService.CARI_ACTION_ROOM_CREATE);
        intent.putExtra("room", room);
        startService(intent);
    }

    class CreateRoomReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            //get the response of the server
            String res[] = intent.getStringArrayExtra(BackgroundIntentService.CARI_RPI);
            //checking if insert success or failed
            if (res[0].equals("29")) {
                resetData();
                Common.showAlertMessageAndFinish(AddRoomActivity.this, "Add Room", "Room Added Succesfully");
            } else {
                Common.showAlertMessageAndFinish(AddRoomActivity.this, "Add Room", "Failed To Add Room");
            }
        }
    }
}
