package com.avpti.cari;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.avpti.cari.classes.Common;
import com.avpti.cari.classes.SessionManager;
import com.avpti.cari.services.BackgroundIntentService;

import static com.avpti.cari.classes.Common.promptForResult;

public class DeleteRoom {
    int room_no;
    Context context;
    SessionManager sessionManager;
    DeleteRoomReceiver receiver;

    public DeleteRoom(Context context, int room_no) {
        this.context = context;
        this.room_no = room_no;

        sessionManager = new SessionManager(context);
        registerCustomReceiver();
    }

    public void delete() {
        promptForResult(context, "Delete Room", "Are you sure to delete this room?","yes","no",true, new Common.PromptRunnable() {
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
        intentFilter.addAction(BackgroundIntentService.CARI_ACTION_ROOM_DELETE);

        context.registerReceiver(receiver, intentFilter);
    }

    public void startBackgroundIntentService() {
        Intent intent = new Intent();
        intent.setClass(context, BackgroundIntentService.class);
        intent.setAction(BackgroundIntentService.CARI_ACTION_ROOM_DELETE);
        intent.putExtra("room_id", room_no);
        intent.putExtra("house_id", sessionManager.getHouseId());
        context.startService(intent);
    }

    class DeleteRoomReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            //get the response of the server
            String res[] = intent.getStringArrayExtra(BackgroundIntentService.CARI_RPI);
            //checking if insert success or failed
            if (res[0].equals("31")) {
                Common.showAlertMessage(context, "Delete Room", "Room Deleted Successfully");
            } else {
                Common.showAlertMessage(context, "Delete Room", "Failed To Delete Room");
            }
        }
    }
}

