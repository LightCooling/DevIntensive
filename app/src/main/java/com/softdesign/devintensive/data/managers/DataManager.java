package com.softdesign.devintensive.data.managers;

import android.content.Context;

import com.softdesign.devintensive.data.network.PicassoCache;
import com.softdesign.devintensive.data.network.RestService;
import com.softdesign.devintensive.data.network.ServiceGenerator;
import com.softdesign.devintensive.data.network.req.UserModelReq;
import com.softdesign.devintensive.data.network.res.UpdateLikeRes;
import com.softdesign.devintensive.data.network.res.UploadPhotoRes;
import com.softdesign.devintensive.data.network.res.UserListRes;
import com.softdesign.devintensive.data.network.res.UserModelRes;
import com.softdesign.devintensive.data.storage.models.DaoSession;
import com.softdesign.devintensive.data.storage.models.User;
import com.softdesign.devintensive.data.storage.models.UserDao;
import com.softdesign.devintensive.utils.DevIntensiveApplication;
import com.squareup.picasso.Picasso;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import okhttp3.MultipartBody;
import retrofit2.Call;

public class DataManager {
    private static DataManager INSTANCE = null;

    private Context mContext;
    private PreferencesManager mPreferencesManager;
    private RestService mRestService;
    private Picasso mPicasso;
    private DaoSession mDaoSession;
    private EventBus mBus;

    public DataManager() {
        this.mContext = DevIntensiveApplication.getContext();
        this.mPreferencesManager = new PreferencesManager();
        this.mRestService = ServiceGenerator.createService(RestService.class);
        this.mPicasso = new PicassoCache(mContext).getPicassoInstance();
        this.mDaoSession = DevIntensiveApplication.getDaoSession();
        this.mBus = new EventBus();
    }

    public static DataManager getInstance() {
        if (INSTANCE == null)
            INSTANCE = new DataManager();
        return INSTANCE;
    }

    public Picasso getPicasso() {
        return mPicasso;
    }

    public PreferencesManager getPreferencesManager() {
        return mPreferencesManager;
    }

    public EventBus getBus() {
        return mBus;
    }

    //<editor-fold desc="========== Network ==========">

    public Call<UserModelRes> loginUser(UserModelReq userModelReq) {
        return mRestService.loginUser(userModelReq);
    }

    public Call<UserListRes> getUserListFromNetwork() {
        return mRestService.getUserList();
    }

    public Call<UploadPhotoRes> uploadPhoto(MultipartBody.Part file) {
        return mRestService.uploadPhoto(getPreferencesManager().getUserId(), file);
    }

    public Call<UploadPhotoRes> uploadAvatar(MultipartBody.Part file) {
        return mRestService.uploadAvatar(getPreferencesManager().getUserId(), file);
    }

    public Call<UpdateLikeRes> likeUser(String userId) {
        return mRestService.likeUser(userId);
    }

    public Call<UpdateLikeRes> unlikeUser(String userId) {
        return mRestService.unlikeUser(userId);
    }

    //</editor-fold>

    // <editor-fold desc="========== Database ==========">

    public DaoSession getDaoSession() {
        return mDaoSession;
    }

    public List<User> getUserListFromDb() {
        List<User> userList = new ArrayList<>();
        try {
            userList = mDaoSession.queryBuilder(User.class)
                    .where(UserDao.Properties.Rating.gt(0))
                    .orderAsc(UserDao.Properties.ListPosition)
                    .list();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return userList;
    }

    public List<User> getUserListByName(String query) {
        if (query == null || query.isEmpty())
            return getUserListFromDb();
        List<User> userList = new ArrayList<>();
        try {
            userList = mDaoSession.queryBuilder(User.class)
                    .where(UserDao.Properties.Rating.gt(0), UserDao.Properties.SearchName.like("%" + query.toUpperCase() + "%"))
                    .orderAsc(UserDao.Properties.ListPosition)
                    .list();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return userList;
    }

    public User getUser(String remoteId) {
        return mDaoSession.getUserDao().queryBuilder().where(UserDao.Properties.RemoteId.eq(remoteId)).unique();
    }

    public List<User> getUsersByIds(List<String> remoteIds) {
        return mDaoSession.getUserDao().queryBuilder().where(UserDao.Properties.RemoteId.in(remoteIds)).list();
    }
    //</editor-fold>
}
