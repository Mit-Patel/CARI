package com.avpti.cari;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.avpti.cari.classes.Common;
import com.avpti.cari.classes.SessionManager;
import com.avpti.cari.services.BackgroundIntentService;

import static com.avpti.cari.classes.Common.promptForResult;

public class DeleteAppliance {
    int appliance_id;
    Context context;
    SessionManager sessionManager;
    DeleteRoomReceiver receiver;

    public DeleteAppliance(Context context, int appliance_id) {
        this.context = context;
        this.appliance_id = appliance_id;

        sessionManager = new SessionManager(context);
        registerCustomReceiver();
    }

    
    public void delete() {
        promptForResult(context, "Delete Appliance", "Are you sure to delete this appliance?","yes","no",true, new Common.PromptRunnable() {
            // put whatever code you want to run after user enters a result
            public void run() {
                if (this.getResult()) {
                    startBackgroundIntentService();
                }
            }
        });
    }

    private void registerCustomReceiver() {
        receiver = new DeleteRoomReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BackgroundIntentService.CARI_ACTION_APPLIANCE_DELETE);

        context.registerReceiver(receiver, intentFilter);
    }

    public void startBackgroundIntentService() {
        Intent intent = new Intent();
        intent.setClass(context, BackgroundIntentService.class);
        intent.setAction(BackgroundIntentService.CARI_ACTION_APPLIANCE_DELETE);
        intent.putExtra("appliance_id", appliance_id);
        intent.putExtra("house_id", sessionManager.getHouseId());
        context.startService(intent);
    }

    class DeleteRoomReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            //get the response of the server
            String res[] = intent.getStringArrayExtra(BackgroundIntentService.CARI_RPI);
            //checking if insert success or failed
            if (res[0].equals("37")) {
                Common.showAlertMessage(context, "Delete Appliance", "Appliance Deleted Successfully");
            } else {
                Common.showAlertMessage(context, "Delete Appliance", "Failed To Delete Appliance");
            }
        }
    }
}

