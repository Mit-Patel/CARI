package com.avpti.cari.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.avpti.cari.ApplianceActivity;
import com.avpti.cari.FragmentRefreshListener;
import com.avpti.cari.R;
import com.avpti.cari.adapters.ArrayAdapterLight;
import com.avpti.cari.classes.Communication;
import com.avpti.cari.classes.Light;
import com.avpti.cari.classes.Room;
import com.avpti.cari.classes.SessionManager;
import com.avpti.cari.services.BackgroundIntentService;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class ApplianceLightFragment extends Fragment {
    ArrayList<Light> arrayList;
    SessionManager sessionManager;
    ArrayAdapterLight arrayAdapter;

    //Receiver for background service
    ApplianceLightReceiver receiver;
    //ViewGroup viewGroup = null;
    ViewGroup rootView;
    ApplianceFanFragment.OnApplianceNotFound callback;
    ListView lvLight;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        sessionManager = new SessionManager(getActivity().getApplicationContext());

        rootView = (ViewGroup) inflater.inflate(R.layout.fragment_light, null);

        lvLight = rootView.findViewById(R.id.lv_light);

        arrayList = new ArrayList<>();
        arrayAdapter = new ArrayAdapterLight(this.getActivity(), arrayList, ApplianceActivity.user_id != -1,lvLight);

        loadListData();

        lvLight.setAdapter(arrayAdapter);

        registerCustomReceiver();
        ((ApplianceActivity) getActivity()).setFragmentRefreshListener(new FragmentRefreshListener() {
            @Override
            public void onRefresh() {
                loadListData();
            }
        });
        return rootView;
    }

    private void registerCustomReceiver() {
        receiver = new ApplianceLightReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BackgroundIntentService.CARI_ACTION_APPLIANCES_BULB);

        getActivity().registerReceiver(receiver, intentFilter);
    }

    public void startBackgroundIntentService(String type, String userId) {
        Intent intent = new Intent();
        intent.setClass(getContext(), BackgroundIntentService.class);
        intent.setAction(BackgroundIntentService.CARI_ACTION_APPLIANCES_BULB);
        intent.putExtra("appliance_type", type);
        intent.putExtra("room_id", ApplianceActivity.room_id);
        intent.putExtra("user_id", userId);
        getActivity().startService(intent);
    }

    private void loadListData() {
        if (ApplianceActivity.is_allow == 1 && ApplianceActivity.user_id != -1) {
            //clearing the previous list data
            arrayList.clear();
            //creating object to communicate with server
            Communication cm = new Communication();

            //sending the login data to the server
            cm.sendData("11;" + ApplianceActivity.room_id + ";" + ApplianceActivity.user_id);

            //receiving the response data from server
            String res[] = cm.getMessage().split(";");

            //get all data if operation success
            if (res[0].equals("21")) {
                int index = 1;
                for (int i = 1; i <= ((res.length - 1) / 4); i++) {
                    arrayList.add(new Light(Integer.parseInt(res[index]), res[index + 1], res[index + 2], (Integer.parseInt(res[index + 3]) == 1), new Room(ApplianceActivity.room_id)));
                    index += 4;
                }
            } else {
                arrayList.clear();
            }

            //notify the adapter to change the data
            arrayAdapter.notifyDataSetChanged();
        } else if (ApplianceActivity.user_id != -1)
            startBackgroundIntentService("bulb", String.valueOf(ApplianceActivity.user_id));
        else
            startBackgroundIntentService("bulb", String.valueOf(sessionManager.getUserId()));
    }

    public void setOnApplianceNotFound(ApplianceFanFragment.OnApplianceNotFound callback) {
        this.callback = callback;
    }

    class ApplianceLightReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            arrayList.clear();

            String res[] = intent.getStringArrayExtra(BackgroundIntentService.CARI_RPI);

            //get all data if operation success
            if (res[0].equals("13")) {
                if(lvLight.getVisibility() == View.INVISIBLE) {
                    lvLight.setVisibility(View.VISIBLE);
                    TextView tv = rootView.findViewById(R.id.txtNoLightFound);
                    tv.setVisibility(View.INVISIBLE);
                }
                int index = 1;
                for (int i = 1; i <= ((res.length - 1) / 6); i++) {

                    arrayList.add(new Light(Integer.parseInt(res[index]), res[index + 2], res[index + 1], res[index + 3], (Integer.parseInt(res[index + 4]) == 1), new Room(ApplianceActivity.room_id)));
                    index += 6;
                }
            } else {
                //Toast.makeText(context, "There are no lights in this room", Toast.LENGTH_SHORT).show();
                lvLight.setVisibility(View.INVISIBLE);
                TextView tv = rootView.findViewById(R.id.txtNoLightFound);
                tv.setVisibility(View.VISIBLE);
                arrayList.clear();
//                callback.disableTab(0);
            }

            //notify the adapter to change the data
            arrayAdapter.notifyDataSetChanged();
        }
    }

}
