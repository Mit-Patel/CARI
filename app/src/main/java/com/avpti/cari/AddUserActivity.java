package com.avpti.cari;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.avpti.cari.classes.SessionManager;
import com.avpti.cari.classes.User;
import com.avpti.cari.services.BackgroundIntentService;
import com.avpti.cari.services.ConnectivityReceiver;

import java.util.HashMap;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class AddUserActivity extends AppCompatActivity {

    //constant for house id
    private static int HOUSE_ID;
    //Session manager
    SessionManager sessionManager;
    EditText txt_fname, txt_lname, txt_email, txt_username, txt_password;
    CreateUserReceiver receiver;
    private ActionBar actionBar;
    private Toolbar toolbar;
    //UI references

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_user);
        ConnectivityReceiver.registerConnectivityReceiver(this);

        initToolbar();

        //getting sessionManager data
        sessionManager = new SessionManager(getApplicationContext());

        //views in control
        txt_fname = findViewById(R.id.txt_first_name);
        txt_lname = findViewById(R.id.txt_last_name);
        txt_email = findViewById(R.id.txt_email);
        txt_username = findViewById(R.id.txt_username);
        txt_password = findViewById(R.id.txt_password);

        //setting house_id
        HashMap<String, Integer> hashMap = sessionManager.getUserData();
        HOUSE_ID = hashMap.get(SessionManager.KEY_HOUSE_ID);

        registerCustomReceiver();
    }
    //Receiver

    private void initToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setTitle("Add User");
        //Tools.setSystemBarColor(this);
    }

    //creating user
    public void createClick(View view) {
        //validating data and create user
        validateData();
    }

    private boolean isPasswordValid(String password) {
        return password.length() > 4;
    }

    private boolean isEmailValid(CharSequence email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    //validate data function
    public void validateData() {

        //clearing the errors
        txt_fname.setError(null);
        txt_lname.setError(null);
        txt_username.setError(null);
        txt_password.setError(null);
        txt_email.setError(null);


        //getting the data from views

        String fname = txt_fname.getText().toString().trim();
        String lname = txt_lname.getText().toString().trim();
        String username = txt_username.getText().toString().trim();
        String email = txt_email.getText().toString().trim();

        String password = txt_password.getText().toString().trim();

        //other variables
        boolean cancel = false;
        View focusView = null;

        if (TextUtils.isEmpty(email)) {
            txt_email.setError(getString(R.string.error_field_required));
            focusView = txt_email;
            cancel = true;
        } else if (!isEmailValid(email)) {
            txt_email.setError(getString(R.string.error_invalid_email));
            focusView = txt_email;
            cancel = true;
        }
        if (TextUtils.isEmpty(fname)) {
            txt_fname.setError(getString(R.string.error_field_required));
            focusView = txt_fname;
            cancel = true;
        }
        if (TextUtils.isEmpty(lname)) {
            txt_lname.setError(getString(R.string.error_field_required));
            focusView = txt_lname;
            cancel = true;
        }
        //error if password is empty
        if (TextUtils.isEmpty(password)) {
            txt_password.setError(getString(R.string.error_field_required));
            focusView = txt_password;
            cancel = true;
        } else if (!isPasswordValid(password)) {
            txt_password.setError(getString(R.string.error_invalid_password));
            focusView = txt_password;
            cancel = true;
        }

        //error if username is empty
        if (TextUtils.isEmpty(username)) {
            txt_username.setError(getString(R.string.error_field_required));
            focusView = txt_username;
            cancel = true;
        }

        //show error or login the user
        if (cancel) {
            focusView.requestFocus();
        } else {
            //sending data to server

            User user = new User();
            user.setFname(fname);
            user.setLname(lname);
            user.setUsername(username);
            user.setPassword(password);
            user.setEmail(email);
            user.setHouse_id(HOUSE_ID);

            startBackgroundIntentService(user);
        }
    }

    //resetting data fuunction
    public void resetData() {
        txt_fname.setText("");
        txt_lname.setText("");
        txt_email.setText("");
        txt_username.setText("");
        txt_password.setText("");
        txt_fname.requestFocus();
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

    private void registerCustomReceiver() {
        receiver = new CreateUserReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BackgroundIntentService.CARI_ACTION_USER_CREATE);

        registerReceiver(receiver, intentFilter);
    }

    public void startBackgroundIntentService(User user) {
        Intent intent = new Intent();
        intent.setClass(this, BackgroundIntentService.class);
        intent.setAction(BackgroundIntentService.CARI_ACTION_USER_CREATE);
        intent.putExtra("user", user);
        startService(intent);
    }

    class CreateUserReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            //get the response of the server
            String res[] = intent.getStringArrayExtra(BackgroundIntentService.CARI_RPI);
            //checking if insert success or failed
            if (res[0].equals("2")) {
                Toast.makeText(AddUserActivity.this, "User created!", Toast.LENGTH_LONG).show();
                resetData();
            } else {
                Toast.makeText(AddUserActivity.this, "User not created!", Toast.LENGTH_LONG).show();
            }
        }
    }

}
