package com.avpti.cari.classes;

import java.io.Serializable;

public class User implements Serializable {
    private String fname,lname,username,password,email;
    private long mobile_no;
    private int house_id,user_id,is_admin;

    public User(String username, String password){
        this.username = username;
        this.password = password;
    }

    public User(String fname, String lname, String username, String password, int houseId, String email, long mobile_no) {
        this.fname = fname;
        this.lname = lname;
        this.username = username;
        this.password = password;
        this.house_id = houseId;
        this.email = email;
        this.mobile_no = mobile_no;
    }

    public User(int user_id,String fname, String lname, String username, String password, String email, long mobile_no ) {
        this.fname = fname;
        this.lname = lname;
        this.username = username;
        this.password = password;
        this.email = email;
        this.mobile_no = mobile_no;
        this.user_id = user_id;
    }

    public User(int user_id,String fname, String lname) {
        this.fname = fname;
        this.lname = lname;
        this.user_id = user_id;
    }

    public User() {

    }

    public String getFname() {
        return fname;
    }

    public String getLname() {
        return lname;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getEmail() {
        return email;
    }

    public long getMobile_no() {
        return mobile_no;
    }

    public int getHouse_id() {
        return house_id;
    }

    public int getUser_id() {
        return user_id;
    }

    public void setFname(String fname) {
        this.fname = fname;
    }

    public void setLname(String lname) {
        this.lname = lname;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setMobile_no(long mobile_no) {
        this.mobile_no = mobile_no;
    }

    public void setHouse_id(int house_id) {
        this.house_id = house_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public void setIs_admin(int is_admin) {
        this.is_admin = is_admin;
    }

    public int getIsAdmin() {
        return is_admin;
    }
}
