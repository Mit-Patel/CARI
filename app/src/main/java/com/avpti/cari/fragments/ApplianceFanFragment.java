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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.avpti.cari.ApplianceActivity;
import com.avpti.cari.FragmentRefreshListener;
import com.avpti.cari.R;
import com.avpti.cari.adapters.ArrayAdapterFan;
import com.avpti.cari.classes.Communication;
import com.avpti.cari.classes.Fan;
import com.avpti.cari.classes.Room;
import com.avpti.cari.classes.SessionManager;
import com.avpti.cari.services.BackgroundIntentService;

import java.util.ArrayList;

public class ApplianceFanFragment extends Fragment {
    ArrayList<Fan> arrayList;
    SessionManager sessionManager;
    ArrayAdapterFan arrayAdapter;
    ViewPager layout;
    ListView lvLight;
    View parent_view;
    ViewGroup rootView;
    ApplianceFanReceiver receiver;
    OnApplianceNotFound callback;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        sessionManager = new SessionManager(getActivity().getApplicationContext());

        rootView = (ViewGroup) inflater.inflate(R.layout.fragment_fan, null);

        lvLight = rootView.findViewById(R.id.lv_fan);
        layout = rootView.findViewById(R.id.viewPager);

        arrayList = new ArrayList<>();
        arrayAdapter = new ArrayAdapterFan(this.getActivity(), arrayList, ApplianceActivity.user_id != -1);
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
        receiver = new ApplianceFanReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BackgroundIntentService.CARI_ACTION_APPLIANCES_FAN);

        getActivity().registerReceiver(receiver, intentFilter);
    }

    public void startBackgroundIntentService(String type, String userId) {
        Intent intent = new Intent();
        intent.setClass(getContext(), BackgroundIntentService.class);
        intent.setAction(BackgroundIntentService.CARI_ACTION_APPLIANCES_FAN);
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
            cm.sendData("12;" + ApplianceActivity.room_id + ";" + ApplianceActivity.user_id);

            //receiving the response data from server
            String res[] = cm.getMessage().split(";");

            //get all data if operation success
            if (res[0].equals("23")) {
                int index = 1;
                for (int i = 1; i <= ((res.length - 1) / 5); i++) {
                    Log.d("Here", res[i]);
                    arrayList.add(new Fan(Integer.parseInt(res[index]), res[index + 1], res[index + 2], (Integer.parseInt(res[index + 3]) == 1), Integer.parseInt(res[index + 4]), new Room(ApplianceActivity.room_id)));
                    index += 5;
                }
            } else {
                arrayList.clear();
            }

            //notify the adapter to change the data
            arrayAdapter.notifyDataSetChanged();
        } else if (ApplianceActivity.user_id != -1)
            startBackgroundIntentService("fan", String.valueOf(ApplianceActivity.user_id));
        else
            startBackgroundIntentService("fan", String.valueOf(sessionManager.getUserId()));
    }

    public void setOnApplianceNotFound(OnApplianceNotFound callback) {
        this.callback = callback;
    }

    public interface OnApplianceNotFound {
        void disableTab(int index);
    }

    class ApplianceFanReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            arrayList.clear();

            String res[] = intent.getStringArrayExtra(BackgroundIntentService.CARI_RPI);

            //get all data if operation success
            if (res[0].equals("13")) {
                if (lvLight.getVisibility() == View.INVISIBLE) {
                    lvLight.setVisibility(View.VISIBLE);
                    TextView tv = rootView.findViewById(R.id.txtNoFanFound);
                    tv.setVisibility(View.INVISIBLE);
                }
                int index = 1;
                for (int i = 1; i <= ((res.length - 1) / 6); i++) {
                    Log.d("Here", res[i]);
                    arrayList.add(new Fan(Integer.parseInt(res[index]), res[index + 1], res[index + 2], (Integer.parseInt(res[index + 4]) == 1), Integer.parseInt(res[index + 5]), new Room(ApplianceActivity.room_id)));
                    index += 6;
                }
            } else {
                lvLight.setVisibility(View.INVISIBLE);
                TextView tv = rootView.findViewById(R.id.txtNoFanFound);
                tv.setVisibility(View.VISIBLE);
                //Toast.makeText(context, "There are no fans in this room", Toast.LENGTH_SHORT).show();
//                callback.disableTab(1);
                arrayList.clear();
            }

            //notify the adapter to change the data
            arrayAdapter.notifyDataSetChanged();
        }
    }
}
