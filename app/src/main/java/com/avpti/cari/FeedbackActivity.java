package com.avpti.cari;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.avpti.cari.services.ConnectivityReceiver;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class FeedbackActivity extends AppCompatActivity {
    EditText txtSubject, txtFeedback;
    Button btnSend;
    private ActionBar actionBar;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);
        ConnectivityReceiver.registerConnectivityReceiver(this);
        initToolbar();
        //views in control
        txtSubject = findViewById(R.id.text_subject);
        txtFeedback = findViewById(R.id.text_feedback);
        btnSend = findViewById(R.id.btn_send);

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptMail();
            }
        });
    }

    private void initToolbar() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setTitle("Feedback");
        //Tools.setSystemBarColor(this);
    }

    private void attemptMail() {
        //clearing the errors
        txtSubject.setError(null);
        txtFeedback.setError(null);

        //getting the data from views
        String subject = txtSubject.getText().toString().trim();
        String feedback = txtFeedback.getText().toString().trim();

        //other variables
        boolean cancel = false;
        View focusView = null;

        //error if password is empty
        if (TextUtils.isEmpty(feedback)) {
            txtFeedback.setError(getString(R.string.error_field_required));
            focusView = txtFeedback;
            cancel = true;
        }

        //error if username is empty
        if (TextUtils.isEmpty(subject)) {
            txtSubject.setError(getString(R.string.error_field_required));
            focusView = txtSubject;
            cancel = true;
        }

        //show error or login the user
        if (cancel) {
            focusView.requestFocus();
        } else {
            String data = "mailto:patelmit2012@gmail.com" +
                    "?subject=" + Uri.encode(subject) +
                    "&body=" + Uri.encode(feedback);
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(Uri.parse(data));

            startActivity(intent);

        }
    }

}
