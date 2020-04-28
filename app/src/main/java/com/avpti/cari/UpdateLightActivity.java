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
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.avpti.cari.classes.Light;
import com.avpti.cari.classes.Room;
import com.avpti.cari.classes.SessionManager;
import com.avpti.cari.services.BackgroundIntentService;
import com.avpti.cari.services.ConnectivityReceiver;

import java.util.ArrayList;

public class UpdateLightActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    //View objects
    EditText edtName, edtNum;
    Button btnUpdate;
    Spinner spType;
    Spinner spin_room;
    ArrayAdapter<String> adapterRoom;
    ArrayList<String> arrayListRoom;
    ArrayList<String> arrayListId;

    //sessionManager manager object
    SessionManager sessionManager;
    int house_id;
    //intent passed
    Room room;
    Light light;

    //recevier
    UpdateLightActivity.CreateRoomReceiver receiver;
    private ActionBar actionBar;
    private Toolbar toolbar;
    private int room_id_pos;
    private int room_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_appliance);
        ConnectivityReceiver.registerConnectivityReceiver(this);
        initToolbar();
        //assigning views to objects
        edtName = findViewById(R.id.edtName);
        edtNum = findViewById(R.id.edtNumber);
        spType = findViewById(R.id.type);
        spType.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, new String[]{"Bulb", "Fan"}));
        arrayListId = new ArrayList<>();

        //Create the Room Spinner
        spin_room = findViewById(R.id.room);
        arrayListRoom = new ArrayList<>();
        adapterRoom = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, arrayListRoom);
        spin_room.setAdapter(adapterRoom);

        btnUpdate = findViewById(R.id.btnUpdate);

        //Getting the house id
        sessionManager = new SessionManager(this);
        house_id = sessionManager.getHouseId();
        spin_room.setOnItemSelectedListener(this);


        registerCustomReceiver();
        startBackgroundIntentService(null);

        //Loading the initial data
        init();

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
        actionBar.setTitle("Update Appliance");
        //Tools.setSystemBarColor(this);
    }

    private void init() {
        Intent intent = getIntent();
        light = (Light) intent.getSerializableExtra("light");
        room = light.getRoom();

        edtName.setText(light.getName());
        edtNum.setText(light.getNumber());

        switch (light.getType()) {
            case "bulb":
                spType.setSelection(0);
                break;
            case "fan":
                spType.setSelection(1);
                break;
        }

        spin_room.setSelection(room_id_pos);
    }

    public void validateData() {
        //clearing the errors
        edtName.setError(null);
        edtNum.setError(null);
        String number = edtNum.getText().toString().trim();
        String name = edtName.getText().toString().trim();
        String type = light.getType();
        int room_pos = spin_room.getSelectedItemPosition();

        switch (spType.getSelectedItemPosition()) {
            case 0:
                type = "bulb";
                break;
            case 1:
                type = "fan";
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
            startBackgroundIntentService(new Light(light.getId(), number, name, type, light.isStatus(), new Room(Integer.parseInt(arrayListId.get(room_pos)))));
        }
    }

    //resetting data fuunction
    public void resetData() {
        edtNum.setText("");
        edtName.setText("");
        spType.setSelection(0);
    }

    private void registerCustomReceiver() {
        receiver = new UpdateLightActivity.CreateRoomReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BackgroundIntentService.CARI_ACTION_APPLIANCE_UPDATE);

        registerReceiver(receiver, intentFilter);
    }

    public void startBackgroundIntentService(Light light) {
        Intent intent = new Intent();
        intent.setClass(this, BackgroundIntentService.class);
        intent.setAction(BackgroundIntentService.CARI_ACTION_APPLIANCE_UPDATE);
        intent.putExtra("house_id", house_id);
        if (light == null) {
            intent.putExtra("get_rooms", 1);
        } else {
            intent.putExtra("light", light);
        }
        startService(intent);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

        switch (parent.getId()) {
            case R.id.room:
                room_id = Integer.parseInt(arrayListId.get(position));
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

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
                    if (room.getRoom_id() == Integer.parseInt(res[index - 1])) {

                        spin_room.setSelection(arrayListId.size() - 1);
                    }
                    index += 3;
                }

                adapterRoom.notifyDataSetChanged();
            } else if (res[0].equals("39")) {
                Toast.makeText(UpdateLightActivity.this, "Appliance Updated Successfully!", Toast.LENGTH_LONG).show();
                finish();

            } else {
                Toast.makeText(UpdateLightActivity.this, "Appliance failed to Update!", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

}
