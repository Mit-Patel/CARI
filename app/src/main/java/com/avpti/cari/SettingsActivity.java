package com.avpti.cari;

import android.os.Bundle;
import android.widget.CompoundButton;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;

import com.avpti.cari.classes.Common;
import com.avpti.cari.classes.Communication;
import com.avpti.cari.classes.User;
import com.avpti.cari.services.ConnectivityReceiver;

public class SettingsActivity extends AppCompatActivity {
    SwitchCompat switchMotion;
    private ActionBar actionBar;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        ConnectivityReceiver.registerConnectivityReceiver(this);
        initToolbar();

        switchMotion = findViewById(R.id.switch_motion);

        Communication communication = new Communication();
        communication.sendData("28;");
        String res[] = communication.getMessage().split(";");

        if (res[0].equals("1")) {
            switchMotion.setChecked(true);
        } else {
            switchMotion.setChecked(false);
        }

        switchMotion.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Communication communication = new Communication();
                communication.sendData("27;" + (isChecked ? 1 : 0));
            }
        });
    }


    private void initToolbar() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setTitle("Settings");
        //Tools.setSystemBarColor(this);
    }
}
