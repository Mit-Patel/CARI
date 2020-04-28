package com.avpti.cari;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ProgressBar;

import com.avpti.cari.classes.Common;
import com.avpti.cari.classes.SessionManager;
import com.avpti.cari.services.BackgroundIntentService;
import com.avpti.cari.services.ConnectivityReceiver;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import androidx.appcompat.app.AppCompatActivity;

//User LOGIN Form
public class LoginActivity extends AppCompatActivity {

    //Textbox for getting username and password values
    TextInputLayout textInputLayoutUsername, textInputLayoutPassword;
    TextInputEditText etUsername, etPassword;
    //button for login
    FloatingActionButton btnLogin;
    //Receiver for background service
    LoginReceiver receiver;
    ConnectivityReceiver connectivityReceiver;
    private ProgressBar progress_bar;
    //sessionManager manager for the application
    private SessionManager sessionManager = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        connectivityReceiver = ConnectivityReceiver.registerConnectivityReceiver(this);

        //logging out the user
        Intent intent = getIntent();
        if (intent.getIntExtra("user_logout", 0) == 1) {
            Common.showAlertMessage(this, "Logout Successful", "You have been successfully logged out!");
        }

        //initializing the and sessionManager manager
        sessionManager = new SessionManager(getApplicationContext());

        //checking if sessionManager is already set to automatically login
        if (sessionManager.isLoggedIn()) {
            //create an intent and start the activity
            Intent i = new Intent(this, PlacesActivity.class);
            startActivity(i);

            //destroy login activity
            finish();
        }

        //assigning the views to objects
        textInputLayoutUsername = findViewById(R.id.text_layout_username);
        textInputLayoutPassword = findViewById(R.id.text_layout_password);
        etUsername = findViewById(R.id.et_username);
        etPassword = findViewById(R.id.et_password);
        btnLogin = findViewById(R.id.btn_login);
        progress_bar = findViewById(R.id.progress_bar);


        registerCustomReceiver();

        //on click listener for login button
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progress_bar.setVisibility(View.VISIBLE);
                btnLogin.setAlpha(0f);
                attemptLogin();
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

    private void registerCustomReceiver() {
        receiver = new LoginReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BackgroundIntentService.CARI_ACTION_LOGIN);

        registerReceiver(receiver, intentFilter);
    }

    public void startBackgroundIntentService(String username, String password) {
        Intent intent = new Intent();
        intent.setClass(this, BackgroundIntentService.class);
        intent.setAction(BackgroundIntentService.CARI_ACTION_LOGIN);
        intent.putExtra("username", username);
        intent.putExtra("password", password);
        startService(intent);
    }

    private void attemptLogin() {
        //clearing the errors
        textInputLayoutUsername.setError(null);
        textInputLayoutPassword.setError(null);

        //getting the data from views
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        //other variables
        boolean cancel = false;
        View focusView = null;

        //error if password is empty
        if (TextUtils.isEmpty(password)) {
            textInputLayoutPassword.setError(getString(R.string.error_field_required));
            focusView = etPassword;
            cancel = true;
        }

        //error if password is less than 4 digits
        else if (!isPasswordValid(password)) {
            textInputLayoutPassword.setError(getString(R.string.error_invalid_password));
            focusView = etPassword;
            cancel = true;
        }

        //error if username is empty
        if (TextUtils.isEmpty(username)) {
            textInputLayoutUsername.setError(getString(R.string.error_field_required));
            focusView = etUsername;
            cancel = true;
        }

        //show error or login the user
        if (cancel) {
            focusView.requestFocus();
            progress_bar.setVisibility(View.GONE);
            btnLogin.setAlpha(1f);
        } else {
            performLogin();
        }
    }

    private void performLogin() {
        //Common.startProgressDialog(this, null, "Logging In");
        startBackgroundIntentService(etUsername.getText().toString(), etPassword.getText().toString());
    }

    //password validation
    private boolean isPasswordValid(String password) {
        return password.length() >= 8;
    }

    //resets text in views
    public void resetData() {
        etUsername.setText("");
        etPassword.setText("");
        etUsername.requestFocus();
    }

    class LoginReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(BackgroundIntentService.CARI_ACTION_LOGIN)) {
                //get the response of the servser
                String res[] = intent.getStringArrayExtra(BackgroundIntentService.CARI_RPI);

                //check the response and [perform tasks
                if (res[0].equals("0")) {

                    //set the sessionManager variables
                    sessionManager.createLoginSession(Integer.parseInt(res[1]), Integer.parseInt(res[2]), Integer.parseInt(res[3]));
                    sessionManager.setName(res[4], res[5]);
                    sessionManager.setUsername(res[6]);

                    progress_bar.setVisibility(View.GONE);
                    btnLogin.setAlpha(1f);

                    //create an intent and start the activity
                    Intent i = new Intent(LoginActivity.this, PlacesActivity.class);
                    startActivity(i);

                    //close the login activity
                    finish();

                } else {
                    //show the error message
                    progress_bar.setVisibility(View.GONE);
                    btnLogin.setAlpha(1f);
                    Common.showAlertMessage(LoginActivity.this, "Invalid Login", "Invalid Username or Password!");
                    //clearing the edittexts
                    resetData();
                }
            }
        }
    }
}