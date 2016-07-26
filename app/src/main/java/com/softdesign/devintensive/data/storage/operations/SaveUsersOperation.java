package com.softdesign.devintensive.data.storage.operations;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.redmadrobot.chronos.ChronosOperation;
import com.redmadrobot.chronos.ChronosOperationResult;
import com.softdesign.devintensive.data.managers.DataManager;
import com.softdesign.devintensive.data.storage.models.Repository;
import com.softdesign.devintensive.data.storage.models.User;

import java.util.List;

public class SaveUsersOperation extends ChronosOperation<Void> {
    public static final int MODE_INSERT_REPLACE = 111222333;
    public static final int MODE_UPDATE = 444555666;
    private List<User> mUsers;
    private List<Repository> mRepositories;
    private int mMode;

    public SaveUsersOperation(List<User> users, List<Repository> repositories, int mode) {
        mUsers = users;
        mRepositories = repositories;
        mMode = mode;
    }

    public SaveUsersOperation(List<User> users, int mode) {
        this(users, null, mode);
    }

    @Nullable
    @Override
    public Void run() {
        switch (mMode) {
            case MODE_INSERT_REPLACE:
                DataManager.getInstance().getDaoSession()
                        .getUserDao().insertOrReplaceInTx(mUsers);
                if (mRepositories != null) {
                    DataManager.getInstance().getDaoSession()
                            .getRepositoryDao().insertOrReplaceInTx(mRepositories);
                }
                break;
            case MODE_UPDATE:
                DataManager.getInstance().getDaoSession()
                        .getUserDao().updateInTx(mUsers);
                if (mRepositories != null) {
                    DataManager.getInstance().getDaoSession()
                            .getRepositoryDao().insertOrReplaceInTx(mRepositories);
                }
        }
        return null;
    }

    @NonNull
    @Override
    public Class<? extends ChronosOperationResult<Void>> getResultClass() {
        return Result.class;
    }

    public static final class Result extends ChronosOperationResult<Void> {
    }
}
