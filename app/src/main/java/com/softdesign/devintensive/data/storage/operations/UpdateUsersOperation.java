package com.softdesign.devintensive.data.storage.operations;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.redmadrobot.chronos.ChronosOperation;
import com.redmadrobot.chronos.ChronosOperationResult;
import com.softdesign.devintensive.data.managers.DataManager;
import com.softdesign.devintensive.data.storage.models.User;

import java.util.List;

public class UpdateUsersOperation extends ChronosOperation<Void> {
    private List<User> mUsersToUpdate;

    public UpdateUsersOperation(List<User> usersToUpdate) {
        mUsersToUpdate = usersToUpdate;
    }

    @Nullable
    @Override
    public Void run() {
        DataManager.getInstance().getDaoSession().getUserDao().updateInTx(mUsersToUpdate);
        return null;
    }

    @NonNull
    @Override
    public Class<? extends ChronosOperationResult<Void>> getResultClass() {
        return Result.class;
    }

    public static final class Result extends ChronosOperationResult<Void> {}
}
