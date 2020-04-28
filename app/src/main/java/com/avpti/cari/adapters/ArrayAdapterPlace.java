package com.avpti.cari.adapters;


import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.avpti.cari.PrivilegesDenyPlacesActivity;
import com.avpti.cari.R;
import com.avpti.cari.classes.Common;
import com.avpti.cari.classes.Communication;
import com.avpti.cari.classes.Room;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


public class ArrayAdapterPlace extends ArrayAdapter<Room> {

    Context context;

    private ArrayList<Room> list;
int isAllow;
    public ArrayAdapterPlace(@NonNull Context context, @NonNull ArrayList<Room> data, int isAllow) {
        super(context, R.layout.priv_rooms_list_item, data);
        this.isAllow = isAllow;
        this.list = data;
        this.context = context;
    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        final Room room = getItem(position);

        ViewHolder viewHolder;
        viewHolder = new ViewHolder();

        LayoutInflater inflater = LayoutInflater.from(getContext());
        convertView = inflater.inflate(R.layout.priv_rooms_list_item, parent, false);

        viewHolder.tvRoomId = convertView.findViewById(R.id.room_id);
        viewHolder.tvRoomNo = convertView.findViewById(R.id.room_no);
        viewHolder.tvRoomName = convertView.findViewById(R.id.room_name);
        viewHolder.btnGrantRoom = convertView.findViewById(R.id.btnGrantRoom);
        viewHolder.btnGrantRoom.setText("Deny whole room");
        if (isAllow == 1)
            viewHolder.btnGrantRoom.setText("Allow Whole room");

        viewHolder.btnGrantRoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isAllow == 1) {
                    if (allowWholeRoom(room.getRoom_id()).equals("25")) {
                        list.remove(position);
                        notifyDataSetChanged();
                        Common.showAlertMessage(context, "Success", "Room allowed to the user!");

                        if (list.size() == 0) {
                            Common.showAlertMessageAndFinish(context, "Success", "All the rooms allowed to the user!");
//                            ((Activity) context).finish();
                        }
                    }
                }else{
                    if (denyWholeRoom(room.getRoom_id()).equals("17")) {
                        list.remove(position);
                        notifyDataSetChanged();
                        Common.showAlertMessage(context, "Success", "Room has been denied to the user!");

                        if (list.size() == 0) {
                            Common.showAlertMessageAndFinish(context, "Success", "All the rooms has been denied to the user!");
//                            ((Activity) context).finish();
                        }
                    }
                }

            }
        });


        viewHolder.tvRoomId.setText(room.getRoom_id() + "");
        viewHolder.tvRoomNo.setText(room.getRoom_no());
        viewHolder.tvRoomName.setText(room.getRoom_name());
        return convertView;
    }

    public String denyWholeRoom(int id) {
        Communication cm = new Communication();
        cm.sendData("9;" + PrivilegesDenyPlacesActivity.user_id + ";" + id + ";null;1");
        String recv[] = cm.getMessage().split(";");
        return recv[0];
    }

    public String allowWholeRoom(int id) {
        Communication cm = new Communication();
        cm.sendData("13;" + id + ";" + PrivilegesDenyPlacesActivity.user_id);
        String recv[] = cm.getMessage().split(";");
        return recv[0];
    }

    private static class ViewHolder {
        TextView tvRoomNo, tvRoomName, tvRoomId;
        Button btnGrantRoom;
    }
}
