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
import com.avpti.cari.classes.Fan;
import com.avpti.cari.services.BackgroundIntentService;

import java.util.ArrayList;


public class ArrayAdapterFan extends ArrayAdapter<Fan> implements View.OnClickListener {

    static int i;
    ApplianceReceiver receiver;
    Context context;
    boolean isPriv;
    ViewHolder viewHolder;
    Fan fan;
    private ArrayList<Fan> list;

    public ArrayAdapterFan(@NonNull Context context, @NonNull ArrayList<Fan> data) {
        super(context, R.layout.appliance_fan_list_item, data);
        this.list = data;
        this.context = context;
        this.isPriv = false;
        registerCustomReceiver();
    }

    public ArrayAdapterFan(@NonNull Context context, @NonNull ArrayList<Fan> data, boolean isPriv) {
        super(context, isPriv ? R.layout.priv_appliance_fan_list_item : R.layout.appliance_fan_list_item, data);
        this.list = data;
        this.context = context;
        this.isPriv = isPriv;
        registerCustomReceiver();
    }

    @Override
    public void onClick(View v) {

    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        fan = getItem(position);


        if (convertView == null) {
            viewHolder = new ViewHolder();

            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(isPriv ? R.layout.priv_appliance_fan_list_item : R.layout.appliance_fan_list_item, parent, false);

            viewHolder.tvFanId = convertView.findViewById(R.id.fan_id);
            viewHolder.tvFanNo = convertView.findViewById(R.id.fan_no);
            viewHolder.tvFanName = convertView.findViewById(R.id.fan_name);
            if (isPriv) {
                viewHolder.btnDeny = convertView.findViewById(R.id.btn_delete_fan);
                if (ApplianceActivity.is_allow == 1) viewHolder.btnDeny.setText("Allow");
            } else {
                viewHolder.tbFanStatus = convertView.findViewById(R.id.btn_fan_switch);
                viewHolder.btnMore = convertView.findViewById(R.id.btn_more);
                viewHolder.txtSpeed = convertView.findViewById(R.id.txtSpeed);
                viewHolder.btnSpeedMin = convertView.findViewById(R.id.btnSpeedMin);
                viewHolder.btnSpeedMax = convertView.findViewById(R.id.btnSpeedMax);
            }
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        if (fan != null) {
            viewHolder.tvFanId.setText(fan.getId() + "");
            viewHolder.tvFanNo.setText(fan.getNumber());
            viewHolder.tvFanName.setText(fan.getName());
            if (isPriv) {
                viewHolder.btnDeny.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (ApplianceActivity.is_allow == 1) {
                            if (allowAppliance(fan.getId()).equals("27")) {
                                list.remove(position);
                                notifyDataSetChanged();
                                Common.showAlertMessage(context, "Success", "Appliance allowed to the user!");

                                if (list.size() == 0) {
                                    Common.showAlertMessageAndFinish(context, "Alert", "All the appliances of this rooms allowed to the user!");
//                                    ((Activity) context).finish();
                                }
                            }
                        } else {
                            if (denyAppliance(fan.getId()).equals("17")) {
                                list.remove(position);
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
                                        intent.putExtra("fan", fan);
                                        getContext().startActivity(intent);
                                        break;
                                    case R.id.option_delete:
                                        DeleteAppliance deleteLight = new DeleteAppliance(getContext(), fan.getId());
                                        deleteLight.delete();
                                        break;
                                }

                                return true;
                            }
                        });

                        popup.show();
                    }
                });

                if (!fan.isStatus()) {
                    viewHolder.tbFanStatus.setImageResource(R.drawable.ic_fan);
//                    i = 1;
                } else {
                    viewHolder.tbFanStatus.setImageResource(R.drawable.ic_fan_glow);
//                    i = 0;
                }

                viewHolder.tbFanStatus.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startBackgroundIntentService(fan, (fan.isStatus() ? 1 : 0), Integer.parseInt(viewHolder.txtSpeed.getText().toString()));
                        fan.setStatus(!fan.isStatus());
                    }
                });

                viewHolder.txtSpeed.setText(fan.getSpeed() + "");

                viewHolder.btnSpeedMin.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (fan.isStatus()) {
                            int s = Integer.parseInt(viewHolder.txtSpeed.getText().toString());

                            if (s > 0) {
                                viewHolder.txtSpeed.setText(--s + "");
                            }

                            startBackgroundIntentService(fan, 0, Integer.parseInt(viewHolder.txtSpeed.getText().toString()));
                        }
                    }
                });

                viewHolder.btnSpeedMax.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (fan.isStatus()) {
                            int s = Integer.parseInt(viewHolder.txtSpeed.getText().toString());

                            if (s < 5) {
                                viewHolder.txtSpeed.setText(++s + "");
                            }

                            startBackgroundIntentService(fan, 0, Integer.parseInt(viewHolder.txtSpeed.getText().toString()));
                        }
                    }
                });

                viewHolder.tbFanStatus.setTag(position);
            }
        }

        return convertView;
    }

    private void registerCustomReceiver() {
        receiver = new ApplianceReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BackgroundIntentService.CARI_ACTION_APPLIANCE_FAN_ON);
        context.registerReceiver(receiver, intentFilter);
    }

    public void startBackgroundIntentService(Fan fan, int state, int speed) {
        Intent intent = new Intent();
        intent.setClass(context, BackgroundIntentService.class);
        intent.setAction(BackgroundIntentService.CARI_ACTION_APPLIANCE_FAN_ON);
        intent.putExtra("appliance_id", fan.getId());
        intent.putExtra("state", state);
        intent.putExtra("speed", speed);
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
        TextView tvFanNo, tvFanName, tvFanId, txtSpeed;
        ImageButton tbFanStatus, btnSpeedMin, btnSpeedMax, btnMore;

        //        RatingBar fanSpeed;
        Button btnDeny;
    }

    class ApplianceReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            //get the response of the server
            String res[] = intent.getStringArrayExtra(BackgroundIntentService.CARI_RPI);

            //checking if insert success or failed
            if (res[0].equals("51")) {
                if (!res[1].equals("0")) {
                    viewHolder.tbFanStatus.setImageResource(R.drawable.ic_fan);
                } else {
                    viewHolder.tbFanStatus.setImageResource(R.drawable.ic_fan_glow);
                }
            } else if (res[0].equals("52")) {
                Toast.makeText(context, "An Error occured", Toast.LENGTH_SHORT).show();
                viewHolder.tbFanStatus.setImageResource(R.drawable.ic_fan);
            }
        }
    }
}
