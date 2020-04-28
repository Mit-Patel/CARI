package com.avpti.cari.adapters;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;

import com.avpti.cari.ApplianceActivity;
import com.avpti.cari.DeleteAppliance;
import com.avpti.cari.R;
import com.avpti.cari.UpdateLightActivity;
import com.avpti.cari.classes.Common;
import com.avpti.cari.classes.Communication;
import com.avpti.cari.classes.Light;
import com.avpti.cari.services.BackgroundIntentService;

import java.util.ArrayList;


public class ArrayAdapterLight extends ArrayAdapter<Light> implements View.OnClickListener {


    Context context;
    ViewHolder viewHolder;
    ArrayList<Light> list;
    boolean isPriv;
    ListView listView;
    ApplianceReceiver receiver;
    private Light light;
    static int btn_pos = 0;

    public ArrayAdapterLight(@NonNull Context context, @NonNull ArrayList<Light> data,ListView view) {
        super(context, R.layout.appliance_light_list_item, data);
        this.list = data;
        this.context = context;
        isPriv = false;
        listView = view;
        registerCustomReceiver();
    }

    public ArrayAdapterLight(@NonNull Context context, @NonNull ArrayList<Light> data, boolean isPriv,ListView view) {
        super(context, isPriv ? R.layout.priv_appliance_light_list_item : R.layout.appliance_light_list_item, data);
        this.list = data;
        this.context = context;
        this.isPriv = isPriv;
        listView = view;
        registerCustomReceiver();
    }

    @Override
    public void onClick(View v) {

    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        light = getItem(position);
        light.setType(list.get(position).getType());
        final int pos = position;
        if (convertView == null) {
            viewHolder = new ViewHolder();

            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(isPriv ? R.layout.priv_appliance_light_list_item : R.layout.appliance_light_list_item, parent, false);

            viewHolder.tvLightId = convertView.findViewById(R.id.light_id);
            viewHolder.tvLightNo = convertView.findViewById(R.id.light_no);
            viewHolder.tvLightName = convertView.findViewById(R.id.light_name);
            if (isPriv) {
                viewHolder.btnDeny = convertView.findViewById(R.id.btn_delete_light);
                if (ApplianceActivity.is_allow == 1) viewHolder.btnDeny.setText("Allow");
            } else {
                viewHolder.tbLightStatus = convertView.findViewById(R.id.btn_light_switch);
                viewHolder.btnMore = convertView.findViewById(R.id.btn_more);
            }
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        if (light != null) {
            viewHolder.tvLightId.setText(light.getId() + "");
            viewHolder.tvLightNo.setText(light.getNumber());
            viewHolder.tvLightName.setText(light.getName());
            if (isPriv) {
                viewHolder.btnDeny.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (ApplianceActivity.is_allow == 1) {
                            if (allowAppliance(light.getId()).equals("27")) {
                                list.remove(pos);
                                notifyDataSetChanged();
                                Common.showAlertMessage(context, "Success", "Appliance allowed to the user!");

                                if (list.size() == 0) {
                                    Common.showAlertMessageAndFinish(context, "Alert", "All the appliances of this rooms allowed to the user!");
//                                    ((Activity) context).finish();
                                }
                            }
                        } else {
                            if (denyAppliance(light.getId()).equals("17")) {
                                list.remove(pos);
                                notifyDataSetChanged();
                                Common.showAlertMessage(context, "Success", "Appliance has been denied to the user!");

                                if (list.size() == 0) {
                                    Common.showAlertMessageAndFinish(context, "Alert", "All the appliances of this rooms has been denied to the user!");
//                                    ((Activity) context).finish();
                                }
                            }
                        }

                    }
                });
                viewHolder.btnDeny.setTag(position);
            } else {
                viewHolder.tbLightStatus.setTag(position);

                viewHolder.btnMore.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        //Creating the instance of PopupMenu
                        PopupMenu popup = new PopupMenu(getContext(), viewHolder.btnMore);

                        //Inflating the Popup using xml file
                        popup.getMenuInflater().inflate(R.menu.cari_places_card_options_menu, popup.getMenu());

                        //registering popup with OnMenuItemClickListener
                        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                            public boolean onMenuItemClick(MenuItem item) {
                                switch (item.getItemId()) {
                                    case R.id.option_edit:
                                        Intent intent = new Intent(getContext(), UpdateLightActivity.class);
                                        intent.putExtra("light", light);
                                        getContext().startActivity(intent);
                                        break;
                                    case R.id.option_delete:
                                        DeleteAppliance deleteLight = new DeleteAppliance(getContext(), light.getId());
                                        deleteLight.delete();
                                        break;
                                }

                                return true;
                            }
                        });

                        popup.show();
                    }
                });



                if (getItem(position).isStatus()) {
                    viewHolder.tbLightStatus.setImageResource(R.drawable.ic_bulb_unglow);
                } else {
                    viewHolder.tbLightStatus.setImageResource(R.drawable.ic_bulb_glow);
                }

                viewHolder.tbLightStatus.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Light l = getItem((Integer)v.getTag());
                        btn_pos = (Integer)v.getTag();
                        startBackgroundIntentService(l);
                    }
                });
            }

        }

        return convertView;
    }

    private void registerCustomReceiver() {
        receiver = new ApplianceReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BackgroundIntentService.CARI_ACTION_APPLIANCE_BULB_ON);
        context.registerReceiver(receiver, intentFilter);
    }

    public void startBackgroundIntentService(Light light) {
        Intent intent = new Intent();
        intent.setClass(context, BackgroundIntentService.class);
        intent.setAction(BackgroundIntentService.CARI_ACTION_APPLIANCE_BULB_ON);
        intent.putExtra("appliance_id", light.getId());
        context.startService(intent);
    }

    public String denyAppliance(int id) {
        Communication cm = new Communication();
        cm.sendData("9;" + ApplianceActivity.user_id + ";" + ApplianceActivity.room_id + ";" + id + ";0");
        String recv[] = cm.getMessage().split(";");
        return recv[0];
    }

    public String allowAppliance(int id) {
        Communication cm = new Communication();

        //sending the login data to the server
        cm.sendData("14;" + id + ";" + ApplianceActivity.room_id + ";" + ApplianceActivity.user_id);
        String res[] = cm.getMessage().split(";");
        return res[0];
    }

    private static class ViewHolder {
        TextView tvLightNo, tvLightName, tvLightId;
        ImageButton tbLightStatus, btnMore;
        Button btnDeny;
    }

    class ApplianceReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            //get the response of the server
            String res[] = intent.getStringArrayExtra(BackgroundIntentService.CARI_RPI);

            //checking if insert success or failed
            if (res[0].equals("49")) {
                if (res[1].equals("0")) {

                    ((ImageButton)listView.getChildAt(btn_pos).findViewById(R.id.btn_light_switch)).setImageResource(R.drawable.ic_bulb_unglow);

//                    ((ImageButton)getView((Integer)viewHolder.tbLightStatus.getTag(),null, listView)).setImageResource(R.drawable.ic_bulb_unglow);
                } else {
                    ((ImageButton)listView.getChildAt(btn_pos).findViewById(R.id.btn_light_switch)).setImageResource(R.drawable.ic_bulb_glow);

//                    ((ImageButton)getView((Integer)viewHolder.tbLightStatus.getTag(),null, listView)).setImageResource(R.drawable.ic_bulb_glow);
////                    viewHolder.tbLightStatus.setImageResource(R.drawable.ic_bulb_glow);
                }
            } else if (res[0].equals("50")) {
                Toast.makeText(context, "An Error occured", Toast.LENGTH_SHORT).show();
                viewHolder.tbLightStatus.setImageResource(R.drawable.ic_bulb_unglow);
            }
        }
    }
}
