package com.avpti.cari.classes;

import java.io.Serializable;

public class Light extends Appliance implements Serializable {

    public Light(int id, String lightNo, String lightName, boolean status, Room room) {
        super(id, lightNo, lightName, status, room);
    }
    public Light(int id, String lightNo, String lightName, String type,boolean status, Room room) {
        super(id, lightNo, lightName, type,status, room);
    }
}
