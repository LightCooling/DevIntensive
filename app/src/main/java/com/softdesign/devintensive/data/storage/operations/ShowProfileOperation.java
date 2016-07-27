package com.softdesign.devintensive.data.storage.operations;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.redmadrobot.chronos.ChronosOperation;
import com.redmadrobot.chronos.ChronosOperationResult;
import com.softdesign.devintensive.data.managers.DataManager;
import com.softdesign.devintensive.data.storage.models.User;

public class ShowProfileOperation extends ChronosOperation<User> {
    private String mRemoteId;

    public ShowProfileOperation(String remoteId) {
        mRemoteId = remoteId;
    }

    @Nullable
    @Override
    public User run() {
        return DataManager.getInstance().getUser(mRemoteId);
    }

    @NonNull
    @Override
    public Class<? extends ChronosOperationResult<User>> getResultClass() {
        return Result.class;
    }

    public final static class Result extends ChronosOperationResult<User> {}
}
