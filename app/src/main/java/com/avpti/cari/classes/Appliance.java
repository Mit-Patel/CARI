package com.avpti.cari.classes;

import java.io.Serializable;

public class Appliance implements Serializable {
    private int id;
    private String number,name,type;
    private boolean status;
    private Room room;

    public Appliance(int id, String number, String name, boolean status, Room room) {
        this.id = id;
        this.number = number;
        this.name = name;
        this.status = status;
        this.room = room;
    }

    public Appliance(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public Appliance(int id, String number, String name, String type, boolean status, Room room) {
        this.id = id;
        this.number = number;
        this.name = name;
        this.type = type;
        this.status = status;
        this.room = room;
    }

    public Appliance(String name, String number, String type, Room room) {
        this.name = name;
        this.number = number;
        this.type = type;
        this.room = room;
    }
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }
}
