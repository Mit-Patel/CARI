package com.avpti.cari.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.avpti.cari.classes.Communication;

import java.util.Timer;
import java.util.TimerTask;

import androidx.annotation.Nullable;

public class BackgroundServerConnection extends Service {
    public static final String SERVER_CONNECTION = "server";
    public static final String TAG = BackgroundServerConnection.class.getSimpleName();
    static int i = 0;
    public int counter = 0;
    Context context;
    long oldTime = 0;
    private Timer timer;
    private TimerTask timerTask;

    public BackgroundServerConnection() {
    }

    public BackgroundServerConnection(Context context) {
        this.context = context;

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        startTimer(intent);

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy");

        // send new broadcast when service is destroyed.
        // this broadcast restarts the service.
        Intent broadcastIntent = new Intent(SERVER_CONNECTION);

        broadcastIntent.putExtra("service", 1);
        broadcastIntent.putExtra("message", "");

        sendBroadcast(broadcastIntent);

        stoptimertask();
    }

    public void startTimer(Intent intent) {
        //set a new Timer
        timer = new Timer();

        //initialize the TimerTask's job
        initializeTimerTask(intent);

        //schedule the timer, to wake up every 1 second
        timer.schedule(timerTask, 0, 2000); //
    }

    /**
     * it sets the timer to print the counter every x seconds
     */
    public void initializeTimerTask(final Intent intent) {
        timerTask = new TimerTask() {
            public void run() {
                Communication cm = new Communication();

                String data = "99;";
                String result = cm.sendData2(data);
                String[] res = result.split(";");

                if (res[0].equals("-1")) {
                    Intent broadcastIntent = new Intent(SERVER_CONNECTION);

                    broadcastIntent.putExtra("service", 2);
                    broadcastIntent.putExtra("message", "You are not connected with server! Please check your Wi-Fi connection.");

                    sendBroadcast(broadcastIntent);
                }
                Log.i(TAG, result);
                Log.i(TAG, "in timer ++++  " + (counter++));
            }
        };
    }

    /**
     * not needed
     */
    public void stoptimertask() {
        //stop the timer, if it's not already null
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
