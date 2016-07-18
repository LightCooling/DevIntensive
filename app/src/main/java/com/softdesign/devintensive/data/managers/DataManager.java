package com.softdesign.devintensive.data.managers;

import com.softdesign.devintensive.data.network.RestService;
import com.softdesign.devintensive.data.network.ServiceGenerator;
import com.softdesign.devintensive.data.network.req.UserModelReq;
import com.softdesign.devintensive.data.network.res.UserListRes;
import com.softdesign.devintensive.data.network.res.UserModelRes;

import retrofit2.Call;

public class DataManager {
    private static DataManager INSTANCE = null;

    private PreferencesManager mPreferencesManager;
    private RestService mRestService;

    public DataManager() {
        this.mPreferencesManager = new PreferencesManager();
        this.mRestService = ServiceGenerator.createService(RestService.class);
    }

    public static DataManager getInstance() {
        if (INSTANCE == null)
            INSTANCE = new DataManager();
        return INSTANCE;
    }

    public PreferencesManager getPreferencesManager() {
        return mPreferencesManager;
    }


    //<editor-fold desc="=========== Network ===========">

    public Call<UserModelRes> loginUser(UserModelReq userModelReq) {
        return mRestService.loginUser(userModelReq);
    }

    public Call<UserListRes> getUserList() {
        return mRestService.getUserList();
    }
    //</editor-fold>
}
