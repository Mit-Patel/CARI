package com.avpti.cari.services;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;

import com.avpti.cari.classes.Appliance;
import com.avpti.cari.classes.Communication;
import com.avpti.cari.classes.Fan;
import com.avpti.cari.classes.Light;
import com.avpti.cari.classes.Room;
import com.avpti.cari.classes.User;

import androidx.annotation.Nullable;

public class BackgroundIntentService extends IntentService {
    public static final String CARI_ACTION_LOGIN = "0";
    public static final String CARI_ACTION_USER_CREATE = "1";
    public static final String CARI_ACTION_USER = "2";
    public static final String CARI_ACTION_USER_UPDATE = "3";
    public static final String CARI_ACTION_ALL_USERS = "4";
    public static final String CARI_ACTION_USER_DELETE = "5";
    public static final String CARI_ACTION_USER_DELETE_GET = "51";
    public static final String CARI_ACTION_ROOMS = "6";
    public static final String CARI_ACTION_APPLIANCES_BULB = "7";
    public static final String CARI_ACTION_APPLIANCES_FAN = "71";
    public static final String CARI_ACTION_ALL_APPLIANCES = "8";
    public static final String CARI_ACTION_GRANT_PRIVILEGE = "9";
    public static final String CARI_ACTION_ROOM_CREATE = "15";
    public static final String CARI_ACTION_ROOM_UPDATE = "17";
    public static final String CARI_ACTION_ROOM_DELETE = "171";
    public static final String CARI_ACTION_APPLIANCE_CREATE = "18";
    public static final String CARI_ACTION_APPLIANCE_UPDATE = "19";
    public static final String CARI_ACTION_APPLIANCE_DELETE = "20";
    public static final String CARI_ACTION_APPLIANCE_BULB_ON = "21";
    public static final String CARI_ACTION_APPLIANCE_FAN_ON = "22";
    public static final String CARI_RPI = "database";

    public BackgroundIntentService() {
        super("CARIBackgroundIntentService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        //creating object to communicate with server
        Communication cm = new Communication();
        Intent i = new Intent();
        String res[] = null;
        String data = "", result = "";

        //gets data from incoming intent
        switch (intent.getAction()) {
            case CARI_ACTION_LOGIN:

                //sending the login data to the server
                String username = intent.getStringExtra("username");
                String password = intent.getStringExtra("password");

                data = "0;" + username + ";" + password;

                //receiving the response data from server
                result = cm.sendData2(data);
                res = result.split(";");

                //send data to receiver
                i.setAction(CARI_ACTION_LOGIN);

                break;
            case CARI_ACTION_ROOMS:
                //sending the login data to the server
                String houseId = intent.getStringExtra("house_id");
                String userId = intent.getStringExtra("user_id");
                data = "6;" + houseId + ";" + userId;

                //receiving the response data from server
                result = cm.sendData2(data);
                res = result.split(";");

                //send data to receiver
                i.setAction(CARI_ACTION_ROOMS);

                break;
            case CARI_ACTION_APPLIANCES_BULB:

                //sending the login data to the server
                String type = intent.getStringExtra("appliance_type");
                int roomId = intent.getIntExtra("room_id", -1);
                userId = intent.getStringExtra("user_id");


                data = "7;" + type + ";" + roomId + ";" + userId;

                //receiving the response data from server
                result = cm.sendData2(data);
                res = result.split(";");

                //send data to receiver
                i.setAction(CARI_ACTION_APPLIANCES_BULB);
                break;
            case CARI_ACTION_APPLIANCES_FAN:

                //sending the login data to the server
                type = intent.getStringExtra("appliance_type");
                roomId = intent.getIntExtra("room_id", -1);
                userId = intent.getStringExtra("user_id");


                data = "7;" + type + ";" + roomId + ";" + userId;

                //receiving the response data from server
                result = cm.sendData2(data);
                res = result.split(";");

                //send data to receiver
                i.setAction(CARI_ACTION_APPLIANCES_FAN);
                break;
            case CARI_ACTION_USER_CREATE:

                //sending the login data to the server
                User user = (User) intent.getSerializableExtra("user");

                data = "1;" + user.getFname() + ";" + user.getLname() + ";" + user.getUsername() + ";" + user.getPassword() + ";" + user.getEmail() + ";" + user.getHouse_id() + ";0";

                //receiving the response data from server
                result = cm.sendData2(data);
                res = result.split(";");

                //send data to receiver
                i.setAction(CARI_ACTION_USER_CREATE);
                break;
            case CARI_ACTION_USER_UPDATE:
                Bundle bundle = intent.getExtras();

                if (bundle.get("user_id") != null) {
                    int user_id = bundle.getInt("user_id");
                    data = "2;" + user_id;
                } else {
                    //sending the login data to the server
                    user = (User) bundle.getSerializable("user");

                    data = "3;" + user.getUser_id() + ";" + user.getFname() + ";" + user.getLname() + ";" + user.getUsername() + ";" + user.getPassword() + ";" + user.getEmail();
                }

                //receiving the response data from server
                result = cm.sendData2(data);
                res = result.split(";");

                //send data to receiver
                i.setAction(CARI_ACTION_USER_UPDATE);
                break;
            case CARI_ACTION_USER_DELETE_GET:

                //sending the login data to the server
                int house_id = intent.getIntExtra("house_id", -1);

                data = "4;" + house_id;

                //receiving the response data from server
                result = cm.sendData2(data);
                res = result.split(";");

                //send data to receiver
                i.setAction(CARI_ACTION_USER_DELETE_GET);
                break;
            case CARI_ACTION_USER_DELETE:

                //sending the login data to the server
                int user_id = intent.getIntExtra("user_id", -1);

                data = "5;" + user_id;

                //receiving the response data from server
                result = cm.sendData2(data);
                res = result.split(";");

                //send data to receiver
                i.setAction(CARI_ACTION_USER_DELETE);
                break;
            case CARI_ACTION_ROOM_CREATE:

                //sending the login data to the server
                Room room = (Room) intent.getSerializableExtra("room");
                data = "15;" + room.getRoom_name() + ";" + room.getRoom_no() + ";" + room.getHouse_id() + ";" + room.getRoom_type();

                //receiving the response data from server
                result = cm.sendData2(data);
                res = result.split(";");

                //send data to receiver
                i.setAction(CARI_ACTION_ROOM_CREATE);
                break;
            case CARI_ACTION_ROOM_UPDATE:

                //sending the login data to the server
                room = (Room) intent.getSerializableExtra("room");
                data = "17;" + room.getRoom_name() + ";" + room.getRoom_no() + ";" + room.getRoom_type() + ";" + room.getRoom_id();

                //receiving the response data from server
                result = cm.sendData2(data);
                res = result.split(";");

                //send data to receiver
                i.setAction(CARI_ACTION_ROOM_UPDATE);
                break;
            case CARI_ACTION_ROOM_DELETE:

                //sending the login data to the server
                int room_no = intent.getIntExtra("room_id", -1);
                house_id = intent.getIntExtra("house_id", -1);

                data = "16;" + room_no + ";" + house_id;

                //receiving the response data from server
                result = cm.sendData2(data);
                res = result.split(";");

                //send data to receiver
                i.setAction(CARI_ACTION_ROOM_DELETE);
                break;
            case CARI_ACTION_APPLIANCE_CREATE:
                bundle = intent.getExtras();

                if (bundle.get("house_id") != null) {
                    house_id = bundle.getInt("house_id");
                    data = "21;" + house_id;
                } else {
                    //sending the login data to the server
                    Appliance appliance = (Appliance) bundle.getSerializable("appliance");

                    data = "18;" + appliance.getName() + ";" + appliance.getNumber() + ";" + appliance.getRoom().getRoom_id() + ";" + appliance.getType() + ";" + appliance.getRoom().getHouse_id();
                }

                //receiving the response data from server
                result = cm.sendData2(data);
                res = result.split(";");

                //send data to receiver
                i.setAction(CARI_ACTION_APPLIANCE_CREATE);
                break;
            case CARI_ACTION_APPLIANCE_UPDATE:
                bundle = intent.getExtras();
                house_id = bundle.getInt("house_id");
                Light light;
                Fan fan;
                if (bundle.getInt("get_rooms") != 0) {
                    data = "21;" + house_id;
                } else if (bundle.get("light") != null) {
                    light = (Light) bundle.get("light");
                    data = "20;" + light.getName() + ";" + light.getNumber() + ";" + light.getRoom().getRoom_id() + ";" + light.getType() + ";" + light.getId() + ";" + house_id;
                } else {
                    fan = (Fan) bundle.get("fan");
                    data = "20;" + fan.getName() + ";" + fan.getNumber() + ";" + fan.getRoom().getRoom_id() + ";" + fan.getType() + ";" + fan.getId() + ";" + house_id;
                }
                //sending the login data to the server


                //receiving the response data from server
                result = cm.sendData2(data);
                res = result.split(";");

                //send data to receiver
                i.setAction(CARI_ACTION_APPLIANCE_UPDATE);
                break;
            case CARI_ACTION_APPLIANCE_DELETE:

                //sending the login data to the server
                int appliance_id = intent.getIntExtra("appliance_id", -1);
                house_id = intent.getIntExtra("house_id", -1);

                data = "19;" + appliance_id + ";" + house_id;

                //receiving the response data from server
                result = cm.sendData2(data);
                res = result.split(";");

                //send data to receiver
                i.setAction(CARI_ACTION_APPLIANCE_DELETE);
                break;
            case CARI_ACTION_APPLIANCE_BULB_ON:

                //sending the login data to the server
                appliance_id = intent.getIntExtra("appliance_id", -1);

                data = "25;" + appliance_id;

                //receiving the response data from server
                result = cm.sendData2(data);
                res = result.split(";");

                //send data to receiver
                i.setAction(CARI_ACTION_APPLIANCE_BULB_ON);
                break;
            case CARI_ACTION_APPLIANCE_FAN_ON:

                //sending the login data to the server
                appliance_id = intent.getIntExtra("appliance_id", -1);
                int speed = intent.getIntExtra("speed", -1);
                int state = intent.getIntExtra("state", -1);

                data = "26;" + appliance_id + ";" + state + ";" + speed;

                //receiving the response data from server
                result = cm.sendData2(data);
                res = result.split(";");

                //send data to receiver
                i.setAction(CARI_ACTION_APPLIANCE_FAN_ON);
                break;
        }
        i.putExtra(CARI_RPI, res);
        sendBroadcast(i);
    }
}
