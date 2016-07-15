package com.softdesign.devintensive.data.network.res;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class UserListRes {
    @SerializedName("success")
    @Expose
    public boolean success;
    @SerializedName("data")
    @Expose
    public List<Datum> data = new ArrayList<Datum>();

    public List<Datum> getData() {
        return data;
    }

    public class Datum {

        @SerializedName("_id")
        @Expose
        public String id;
        @SerializedName("first_name")
        @Expose
        public String firstName;
        @SerializedName("second_name")
        @Expose
        public String secondName;
        @SerializedName("__v")
        @Expose
        public int v;
        @SerializedName("repositories")
        @Expose
        public UserModelRes.Repositories repositories;
        @SerializedName("profileValues")
        @Expose
        public UserModelRes.ProfileValues profileValues;
        @SerializedName("publicInfo")
        @Expose
        public UserModelRes.PublicInfo publicInfo;
        @SerializedName("specialization")
        @Expose
        public String specialization;
        @SerializedName("updated")
        @Expose
        public String updated;

        public String getFullName() {
            return firstName + " " + secondName;
        }

        public UserModelRes.Repositories getRepositories() {
            return repositories;
        }

        public UserModelRes.ProfileValues getProfileValues() {
            return profileValues;
        }

        public UserModelRes.PublicInfo getPublicInfo() {
            return publicInfo;
        }
    }
}
