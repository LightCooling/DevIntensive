package com.softdesign.devintensive.data.storage.operations;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.redmadrobot.chronos.ChronosOperation;
import com.redmadrobot.chronos.ChronosOperationResult;
import com.softdesign.devintensive.data.managers.DataManager;
import com.softdesign.devintensive.data.storage.models.User;
import com.softdesign.devintensive.utils.StringUtils;

import java.util.List;

public class ShowLikesOperation extends ChronosOperation<List<User>>{
    private String mRemoteId;

    public ShowLikesOperation(String remoteId) {
        mRemoteId = remoteId;
    }

    @Nullable
    @Override
    public List<User> run() {
        User user = DataManager.getInstance().getUser(mRemoteId);
        return DataManager.getInstance().getUsersByIds(StringUtils.strToList(user.getLikesBy()));
    }

    @NonNull
    @Override
    public Class<? extends ChronosOperationResult<List<User>>> getResultClass() {
        return Result.class;
    }

    public final static class Result extends ChronosOperationResult<List<User>> {}
}
