package com.avpti.cari.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import com.avpti.cari.classes.Common;

public class ConnectivityReceiver extends BroadcastReceiver {

    public static ConnectivityReceiver registerConnectivityReceiver(Context context) {
        ConnectivityReceiver receiver = new ConnectivityReceiver();
        IntentFilter filter = new IntentFilter(BackgroundServerConnection.SERVER_CONNECTION);
        receiver = new ConnectivityReceiver();
        context.registerReceiver(receiver, filter);
        return receiver;
    }

    @Override
    public void onReceive(final Context context, Intent intent) {
        int data = intent.getIntExtra("service", -1);
        String message = intent.getStringExtra("message");

        if (data == 1) {
            context.startService(new Intent(context, BackgroundServerConnection.class));
        } else {
            Common.showAlertMessageAndExit(context, "Connection Failed", message);
//            context.unregisterReceiver(this);
        }
    }

    public static void unregisterConnectivityReceiver(Context context,ConnectivityReceiver receiver){
        context.unregisterReceiver(receiver);
    }

}