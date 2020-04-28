package com.avpti.cari.classes;

import java.io.Serializable;

public class Room implements Serializable {
    private int room_id, house_id;
    private String room_name, room_no, room_type;

    public Room(String room_name, String room_no) {
        this.room_name = room_name;
        this.room_no = room_no;
    }

    public Room(String room_name, String room_no,int house_id,String type) {
        this.room_name = room_name;
        this.room_no = room_no;
        this.house_id = house_id;
        this.room_type = type;
    }
    public Room(int id, String room_name, String room_no, String type) {
        this.room_id = id;
        this.room_name = room_name;
        this.room_no = room_no;
        this.room_type = type;
    }

    public Room(int room_id, int house_id) {
        this.room_id = room_id;
        this.house_id = house_id;
    }

    public Room(int room_id) {
        this.room_id = room_id;
    }

    public Room(int id, String name) {
        this.room_id = id;
        this.room_name = name;
    }

    public int getHouse_id() {
        return house_id;
    }

    public void setHouse_id(int house_id) {
        this.house_id = house_id;
    }

    public String getRoom_type() {
        return room_type;
    }

    public void setRoom_type(String room_type) {
        this.room_type = room_type;
    }

    public int getRoom_id() {
        return room_id;
    }

    public void setRoom_id(int room_id) {
        this.room_id = room_id;
    }

    public String getRoom_name() {
        return room_name;
    }

    public void setRoom_name(String room_name) {
        this.room_name = room_name;
    }

    public String getRoom_no() {
        return room_no;
    }

    public void setRoom_no(String room_no) {
        this.room_no = room_no;
    }
}
