package com.softdesign.devintensive.data.events;

import com.softdesign.devintensive.data.network.res.UserModelRes;

public class LoginEvent {
    private boolean mSuccess;
    private int mResponseCode;
    private UserModelRes mResponse;
    private Throwable mError;

    public LoginEvent(boolean success, int responseCode, UserModelRes response) {
        mSuccess = success;
        mResponseCode = responseCode;
        mResponse = response;
    }

    public LoginEvent(boolean success, Throwable error) {
        mSuccess = success;
        mError = error;
    }

    public boolean isSuccess() {
        return mSuccess;
    }

    public int getResponseCode() {
        return mResponseCode;
    }

    public UserModelRes getResponse() {
        return mResponse;
    }

    public Throwable getError() {
        return mError;
    }
}
