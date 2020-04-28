package com.avpti.cari;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.avpti.cari.classes.Common;
import com.avpti.cari.classes.Communication;
import com.avpti.cari.services.BackgroundServerConnection;
import com.avpti.cari.services.ConnectivityReceiver;

import java.net.InetAddress;
import java.net.UnknownHostException;


public class SplashScreenActivity extends AppCompatActivity {
    ConnectivityReceiver br;
    WifiManager wm;
    InetAddress inetAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        if (checkWifiConnectivity() == 1) {
            BackgroundServerConnection bgService = new BackgroundServerConnection(getApplicationContext());
            Intent bgIntent = new Intent(this, bgService.getClass());

            if (!Common.isMyServiceRunning(this, bgService.getClass())) {
                startService(bgIntent);
            }

            Handler h = new Handler();
            h.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent(SplashScreenActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                }
            }, 2000);
        }
    }


    public int checkWifiConnectivity() {
        Communication communication = new Communication();

        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiCheck = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        if (wifiCheck.isConnected()) {
            wm = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);

            inetAddress = intToInetAddress(wm.getDhcpInfo().serverAddress);
            String host = inetAddress.getHostAddress();
            if (!host.equals(communication.getHost())) {
                //Toast.makeText(getApplicationContext(), "Please connect wifi to RPi3", Toast.LENGTH_SHORT).show();
                Log.i("RPI_WIFI", "Some other wifi is connected!");
                Common.showAlertMessageAndFinish(this, "Error", "Please connect to Wi-Fi: RPi3");
                return 0;
            } else {
                Log.i("RPI_WIFI", "RPI wifi connected successfully!");
                return 1;
            }
        } else {
            //Toast.makeText(getApplicationContext(), "Wifi is not connected", Toast.LENGTH_SHORT).show();
            Log.i("WIFI", "WiFi not connected or turned on!");

            Common.showAlertMessageAndFinish(this, "Error", "Please connect to Wi-Fi");
            return 0;
        }
    }

    public InetAddress intToInetAddress(int hostAddress) {
        byte[] addressBytes = {(byte) (0xff & hostAddress),
                (byte) (0xff & (hostAddress >> 8)),
                (byte) (0xff & (hostAddress >> 16)),
                (byte) (0xff & (hostAddress >> 24))};

        try {
            return InetAddress.getByAddress(addressBytes);
        } catch (UnknownHostException e) {
            throw new AssertionError();
        }
    }


}
