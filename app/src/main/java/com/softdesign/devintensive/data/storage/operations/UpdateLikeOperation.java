package com.softdesign.devintensive.data.storage.operations;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.redmadrobot.chronos.ChronosOperation;
import com.redmadrobot.chronos.ChronosOperationResult;
import com.softdesign.devintensive.data.events.UpdateLikeEvent;
import com.softdesign.devintensive.data.managers.DataManager;
import com.softdesign.devintensive.data.network.res.UserModelRes;
import com.softdesign.devintensive.data.storage.models.User;
import com.softdesign.devintensive.utils.StringUtils;

import java.util.List;

public class UpdateLikeOperation extends ChronosOperation<List<String>> {
    private int position;
    private String mRemoteId;
    private UserModelRes.ProfileValues mProfileValues;

    public UpdateLikeOperation(int position, String remoteId, UserModelRes.ProfileValues profileValues) {
        this.position = position;
        mRemoteId = remoteId;
        mProfileValues = profileValues;
    }

    public UpdateLikeOperation(String remoteId, UserModelRes.ProfileValues values) {
        this(-1, remoteId, values);
    }

    @Nullable
    @Override
    public List<String> run() {
        User user = DataManager.getInstance().getUser(mRemoteId);
        user.setLikes(mProfileValues.getLikes());
        user.setLikesBy(StringUtils.listToStr(mProfileValues.getLikesBy()));
        DataManager.getInstance().getDaoSession().getUserDao().update(user);
        DataManager.getInstance().getBus().post(new UpdateLikeEvent(position, mProfileValues.getLikesBy()));
        return mProfileValues.getLikesBy();
    }

    @NonNull
    @Override
    public Class<? extends ChronosOperationResult<List<String>>> getResultClass() {
        return Result.class;
    }

    public final static class Result extends ChronosOperationResult<List<String>> {
    }
}