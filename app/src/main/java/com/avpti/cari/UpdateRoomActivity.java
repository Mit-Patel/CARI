package com.avpti.cari;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.avpti.cari.classes.Room;
import com.avpti.cari.classes.SessionManager;
import com.avpti.cari.services.BackgroundIntentService;
import com.avpti.cari.services.ConnectivityReceiver;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class UpdateRoomActivity extends AppCompatActivity {
    //View objects
    EditText edtName, edtNum;
    Button btnUpdate;
    Spinner spType;

    //sessionManager manager object
    SessionManager sessionManager;
    int house_id;
    //intent passed
    Room room;
    //recevier
    UpdateRoomActivity.CreateRoomReceiver receiver;
    private ActionBar actionBar;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_room);
        ConnectivityReceiver.registerConnectivityReceiver(this);
        initToolbar();
        //assigning views to objects
        edtName = findViewById(R.id.edtName);
        edtNum = findViewById(R.id.edtNumber);
        spType = findViewById(R.id.update_room_roomType);
        spType.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, new String[]{"Living Room", "Kitchen", "Bed Room"}));

        btnUpdate = findViewById(R.id.btnUpdate);

        //Getting the house id
        sessionManager = new SessionManager(this);
        house_id = sessionManager.getHouseId();

        //Loading the initial data
        init();

        registerCustomReceiver();

        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateData();
            }
        });
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
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setTitle("Update Room");
        //Tools.setSystemBarColor(this);
    }

    private void init() {
        Intent intent = getIntent();
        room = (Room) intent.getSerializableExtra("room");

        edtName.setText(room.getRoom_name());
        edtNum.setText(room.getRoom_no());

        switch (room.getRoom_type()) {
            case "living":
                spType.setSelection(0);
                break;
            case "kitchen":
                spType.setSelection(1);
                break;
            case "bed":
                spType.setSelection(2);
                break;
        }
    }

    public void validateData() {
        //clearing the errors
        edtName.setError(null);
        edtNum.setError(null);
        String number = edtNum.getText().toString().trim();
        String name = edtName.getText().toString().trim();
        String type = room.getRoom_type();
        switch (spType.getSelectedItemPosition()) {
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

        //other variables
        boolean cancel = false;
        View focusView = null;

        if (TextUtils.isEmpty(name)) {
            edtName.setError(getString(R.string.error_field_required));
            focusView = edtName;
            cancel = true;
        }
        if (TextUtils.isEmpty(number)) {
            edtNum.setError(getString(R.string.error_field_required));
            focusView = edtName;
            cancel = true;
        }
        //show error or login the user
        if (cancel) {
            focusView.requestFocus();
        } else {
            //sending data to server
            startBackgroundIntentService(new Room(room.getRoom_id(), name, number, type));
        }
    }

    //resetting data fuunction
    public void resetData() {
        edtNum.setText("");
        edtName.setText("");
        spType.setSelection(0);
    }

    private void registerCustomReceiver() {
        receiver = new UpdateRoomActivity.CreateRoomReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BackgroundIntentService.CARI_ACTION_ROOM_UPDATE);

        registerReceiver(receiver, intentFilter);
    }

    public void startBackgroundIntentService(Room room) {
        Intent intent = new Intent();
        intent.setClass(this, BackgroundIntentService.class);
        intent.setAction(BackgroundIntentService.CARI_ACTION_ROOM_UPDATE);
        intent.putExtra("room", room);
        startService(intent);
    }

    class CreateRoomReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            //get the response of the server
            String res[] = intent.getStringArrayExtra(BackgroundIntentService.CARI_RPI);
            //checking if insert success or failed
            if (res[0].equals("33")) {
                Toast.makeText(UpdateRoomActivity.this, "Room Updated Successfully!", Toast.LENGTH_LONG).show();
                finish();

            } else {
                Toast.makeText(UpdateRoomActivity.this, "Room failed to Update!", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

}
