package com.avpti.cari.classes;

import java.io.Serializable;

public class Fan extends Appliance implements Serializable {
    private int speed;

    public Fan(int id, String fanNo, String fanName, boolean status, int speed, Room room) {
        super(id, fanNo, fanName, status, room);
        this.speed = speed;
    }


    public int getSpeed() {
        return speed;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

}
