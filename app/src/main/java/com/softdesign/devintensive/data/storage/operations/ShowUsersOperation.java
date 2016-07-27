package com.softdesign.devintensive.data.storage.operations;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.redmadrobot.chronos.ChronosOperation;
import com.redmadrobot.chronos.ChronosOperationResult;
import com.softdesign.devintensive.data.managers.DataManager;
import com.softdesign.devintensive.data.storage.models.User;

import java.util.List;

public class ShowUsersOperation extends ChronosOperation<List<User>> {
    private String mQuery;

    public ShowUsersOperation(String query) {
        this.mQuery = query;
    }

    @Nullable
    @Override
    public List<User> run() {
        return DataManager.getInstance().getUserListByName(mQuery);
    }

    @NonNull
    @Override
    public Class<? extends ChronosOperationResult<List<User>>> getResultClass() {
        return Result.class;
    }

    public final static class Result extends ChronosOperationResult<List<User>> {}
}