package com.softdesign.devintensive.data.storage.operations;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.redmadrobot.chronos.ChronosOperation;
import com.redmadrobot.chronos.ChronosOperationResult;
import com.softdesign.devintensive.data.managers.DataManager;
import com.softdesign.devintensive.data.storage.models.Repository;
import com.softdesign.devintensive.data.storage.models.RepositoryDao;
import com.softdesign.devintensive.data.storage.models.User;

import java.util.List;

public class RemoveUserOperation extends ChronosOperation<Void> {
    private User mUserToRemove;

    public RemoveUserOperation(User userToRemove) {
        mUserToRemove = userToRemove;
    }

    @Nullable
    @Override
    public Void run() {
        RepositoryDao repositoryDao = DataManager.getInstance().getDaoSession().getRepositoryDao();
        List<Repository> repsToRemove = repositoryDao.queryBuilder()
                .where(RepositoryDao.Properties.UserRemoteId.eq(mUserToRemove.getRemoteId()))
                .list();
        repositoryDao.deleteInTx(repsToRemove);
        DataManager.getInstance().getDaoSession().getUserDao().delete(mUserToRemove);
        return null;
    }

    @NonNull
    @Override
    public Class<? extends ChronosOperationResult<Void>> getResultClass() {
        return Result.class;
    }

    public static final class Result extends ChronosOperationResult<Void> {}
}
