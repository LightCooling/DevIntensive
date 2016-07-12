package com.softdesign.devintensive.data.network.req;

public class UserModelReq {

    private String email;
    private String password;


    public UserModelReq(String email, String password) {
        this.password = password;
        this.email = email;
    }
}
