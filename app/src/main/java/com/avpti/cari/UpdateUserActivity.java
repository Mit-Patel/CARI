package com.avpti.cari;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.avpti.cari.classes.SessionManager;
import com.avpti.cari.classes.User;
import com.avpti.cari.services.BackgroundIntentService;
import com.avpti.cari.services.ConnectivityReceiver;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class UpdateUserActivity extends AppCompatActivity {
    //views objetcs
    EditText etFirstName, etLastName, etEmail, etUsername, etPassword;
    Button btnUpdate;
    //sessionManager manager object
    SessionManager sessionManager;
    int user_id;
    UpdateUserReceiver receiver;
    private ActionBar actionBar;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_user);
        initToolbar();

        ConnectivityReceiver.registerConnectivityReceiver(this);

        //assigning views to objects
        etFirstName = findViewById(R.id.et_first_name);
        etLastName = findViewById(R.id.et_last_name);
        etEmail = findViewById(R.id.et_email);
        etUsername = findViewById(R.id.et_username);
        etPassword = findViewById(R.id.et_password);
        btnUpdate = findViewById(R.id.btn_update);

        //get the sessionManager manager for the app
        sessionManager = new SessionManager(this);

        //set the user id of the current user
        user_id = sessionManager.getUserId();

        registerCustomReceiver();

        startBackgroundIntentService(null);

        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateData();
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
        actionBar.setTitle("Update Profile");
        //Tools.setSystemBarColor(this);
    }

    public void validateData() {

        //clearing the errors
        etFirstName.setError(null);
        etLastName.setError(null);
        etEmail.setError(null);
        etUsername.setError(null);
        etPassword.setError(null);

        //getting the data from views

        String fname = etFirstName.getText().toString().trim();
        String lname = etLastName.getText().toString().trim();
        String username = etUsername.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        //other variables
        boolean cancel = false;
        View focusView = null;

        if (TextUtils.isEmpty(email)) {
            etEmail.setError(getString(R.string.error_field_required));
            focusView = etEmail;
            cancel = true;
        } else if (!isEmailValid(email)) {
            etEmail.setError(getString(R.string.error_invalid_email));
            focusView = etEmail;
            cancel = true;
        }

        if (TextUtils.isEmpty(fname)) {
            etFirstName.setError(getString(R.string.error_field_required));
            focusView = etFirstName;
            cancel = true;
        }
        if (TextUtils.isEmpty(lname)) {
            etLastName.setError(getString(R.string.error_field_required));
            focusView = etLastName;
            cancel = true;
        }
        //error if password is empty
        if (TextUtils.isEmpty(password)) {
            etPassword.setError(getString(R.string.error_field_required));
            focusView = etPassword;
            cancel = true;
        } else if (!isPasswordValid(password)) {
            etPassword.setError(getString(R.string.error_invalid_password));
            focusView = etPassword;
            cancel = true;
        }

        //error if username is empty
        if (TextUtils.isEmpty(username)) {
            etUsername.setError(getString(R.string.error_field_required));
            focusView = etUsername;
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
            user.setUser_id(user_id);

            startBackgroundIntentService(user);

        }
    }

    private boolean isPasswordValid(String password) {
        return password.length() > 4;
    }

    private boolean isEmailValid(CharSequence email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public void resetData() {
        etFirstName.setText("");
        etLastName.setText("");
        etPassword.setText("");
        etUsername.setText("");
        etEmail.setText("");
        etFirstName.requestFocus();
    }

    private void registerCustomReceiver() {
        receiver = new UpdateUserReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BackgroundIntentService.CARI_ACTION_USER_UPDATE);

        registerReceiver(receiver, intentFilter);
    }

    public void startBackgroundIntentService(User user) {
        Intent intent = new Intent();
        intent.setClass(this, BackgroundIntentService.class);
        intent.setAction(BackgroundIntentService.CARI_ACTION_USER_UPDATE);
        if (user == null) {
            intent.putExtra("user_id", user_id);
        } else {
            intent.putExtra("user", user);
        }
        startService(intent);
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

    class UpdateUserReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            //get the response of the server
            String res[] = intent.getStringArrayExtra(BackgroundIntentService.CARI_RPI);

            //getting the data and set to views
            if (res[0].equals("4")) {
                etFirstName.setText(res[1]);
                etLastName.setText(res[2]);
                etUsername.setText(res[3]);
                etPassword.setText(res[4]);
                etEmail.setText(res[5]);

            } else if (res[0].equals("5")) {

                Toast.makeText(UpdateUserActivity.this, "User updated!", Toast.LENGTH_LONG).show();
                resetData();
            } else {
                Toast.makeText(UpdateUserActivity.this, "An error has occurred! Try again later", Toast.LENGTH_LONG).show();
            }
        }
    }
}
