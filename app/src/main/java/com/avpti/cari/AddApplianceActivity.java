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
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.avpti.cari.classes.Appliance;
import com.avpti.cari.classes.Common;
import com.avpti.cari.classes.Room;
import com.avpti.cari.classes.SessionManager;
import com.avpti.cari.services.BackgroundIntentService;
import com.avpti.cari.services.ConnectivityReceiver;

import java.util.ArrayList;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class AddApplianceActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    Spinner spin_room, spin_type;
    ArrayAdapter<String> adapterRoom, adapterType;
    ArrayList<String> arrayListRoom, arrayListType;
    ArrayList<String> arrayListId;
    SessionManager sessionManager;
    Button btnAdd;
    EditText edtName, edtNumber;
    int house_id, room_id;
    String type;
    AddApplianceActivity.CreateRoomReceiver receiver;
    private ActionBar actionBar;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_appliance);
        ConnectivityReceiver.registerConnectivityReceiver(this);

        initToolbar();
        //Get the house_id
        sessionManager = new SessionManager(this);
        house_id = sessionManager.getHouseId();

        //Assign the views
        btnAdd = findViewById(R.id.btn_appliance_add);
        edtName = findViewById(R.id.et_appliance_name);
        edtNumber = findViewById(R.id.et_appliance_no);

        //Create the Room Spinner
        spin_room = (Spinner) findViewById(R.id.et_appliance_room);
        arrayListRoom = new ArrayList<>();
        adapterRoom = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, arrayListRoom);
        spin_room.setAdapter(adapterRoom);

        //Loading the types
        spin_type = (Spinner) findViewById(R.id.et_appliance_type);
        arrayListId = new ArrayList<>();
        arrayListType = new ArrayList<>();
        arrayListType.add("Bulb");
        arrayListType.add("Fan");
        adapterType = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, arrayListType);
        spin_type.setAdapter(adapterType);

        //Register the listener
        spin_room.setOnItemSelectedListener(this);
        spin_type.setOnItemSelectedListener(this);

        registerCustomReceiver();
        //Load the current rooms
        startBackgroundIntentService(null);

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateData();
            }
        });
    }

    private void initToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setTitle("Add Appliance");
        //Tools.setSystemBarColor(this);
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
            Appliance appliance = new Appliance(name, number, type, new Room(room_id, house_id));
            startBackgroundIntentService(appliance);
        }
    }

    private void registerCustomReceiver() {
        receiver = new AddApplianceActivity.CreateRoomReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BackgroundIntentService.CARI_ACTION_APPLIANCE_CREATE);
        registerReceiver(receiver, intentFilter);
    }

    public void startBackgroundIntentService(Appliance appliance) {
        Intent intent = new Intent();
        intent.setClass(this, BackgroundIntentService.class);
        intent.setAction(BackgroundIntentService.CARI_ACTION_APPLIANCE_CREATE);
        if (appliance == null) {
            intent.putExtra("house_id", house_id);
        } else {
            intent.putExtra("appliance", appliance);
        }
        startService(intent);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

        switch (parent.getId()) {
            case R.id.et_appliance_room:
                room_id = Integer.parseInt(arrayListId.get(position));
                break;
            case R.id.et_appliance_type:
                if (position == 0) type = "bulb";
                else type = "fan";
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    //resetting data fuunction
    public void resetData() {
        edtNumber.setText("");
        edtName.setText("");
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

    class CreateRoomReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            arrayListRoom.clear();
            arrayListId.clear();
            //get the response of the server
            String res[] = intent.getStringArrayExtra(BackgroundIntentService.CARI_RPI);
            //checking if insert success or failed
            if (res[0].equals("41")) {
                int index = 2;
                for (int i = 1; i <= (res.length - 1) / 3; i++) {
                    arrayListRoom.add(res[index]);
                    arrayListId.add(res[index - 1] + "");
                    index += 3;
                }
                adapterRoom.notifyDataSetChanged();
            } else if (res[0].equals("35")) {
                edtName.setText("");
                edtNumber.setText("");
                resetData();
                Common.showAlertMessageAndFinish(AddApplianceActivity.this, "Add Appliance", "Appliance Added Succesfully");

            } else {
                arrayListRoom.clear();
                arrayListId.clear();
                arrayListType.clear();
                Common.showAlertMessageAndFinish(AddApplianceActivity.this, "Add Appliance", "Failed To Add Appliance");

            }
        }
    }
}
