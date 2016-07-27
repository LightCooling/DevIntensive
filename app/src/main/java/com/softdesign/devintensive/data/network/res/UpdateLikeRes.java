package com.softdesign.devintensive.data.network.res;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class UpdateLikeRes {

    @SerializedName("success")
    @Expose
    private boolean success;
    @SerializedName("data")
    @Expose
    private UserModelRes.ProfileValues profileValues;

    public UserModelRes.ProfileValues getProfileValues() {
        return profileValues;
    }
}
