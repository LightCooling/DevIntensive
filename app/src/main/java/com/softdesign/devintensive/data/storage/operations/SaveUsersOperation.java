package com.softdesign.devintensive.data.storage.operations;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.redmadrobot.chronos.ChronosOperation;
import com.redmadrobot.chronos.ChronosOperationResult;
import com.softdesign.devintensive.data.managers.DataManager;
import com.softdesign.devintensive.data.network.res.UserListRes;
import com.softdesign.devintensive.data.network.res.UserModelRes;
import com.softdesign.devintensive.data.storage.models.Repository;
import com.softdesign.devintensive.data.storage.models.User;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class SaveUsersOperation extends ChronosOperation<Void> {
    private List<UserListRes.UserData> mUserData;

    public SaveUsersOperation(List<UserListRes.UserData> userData) {
        mUserData = userData;
    }

    @Nullable
    @Override
    public Void run() {
        List<Repository> allRepositories = new ArrayList<>();
        List<User> allUsers = new ArrayList<>();

        // create storage objects from network data
        for (UserListRes.UserData userRes : mUserData) {
            allRepositories.addAll(getRepoListFromUserRes(userRes));
            allUsers.add(new User(userRes));
        }

        // set list positions in order by rating
        Collections.sort(allUsers, new Comparator<User>() {
            @Override
            public int compare(User user, User t1) {
                if (user.getRating() > t1.getRating()) {
                    return -1;
                } else if (user.getRating() < t1.getRating()) {
                    return 1;
                }
                return 0;
            }
        });
        for (int i = 0; i < allUsers.size(); i++) {
            allUsers.get(i).setListPosition(i);
        }

        // write all result data to db
        DataManager.getInstance().getDaoSession()
                .getRepositoryDao().insertOrReplaceInTx(allRepositories);
        DataManager.getInstance().getDaoSession()
                .getUserDao().insertOrReplaceInTx(allUsers);
        return null;
    }

    private List<Repository> getRepoListFromUserRes(UserListRes.UserData userData) {
        final String userId = userData.getId();

        List<Repository> repositories = new ArrayList<>();
        for (UserModelRes.Repo repositoryRes : userData.getRepositories().getRepo()) {
            repositories.add(new Repository(repositoryRes, userId));
        }
        return repositories;
    }


    @NonNull
    @Override
    public Class<? extends ChronosOperationResult<Void>> getResultClass() {
        return Result.class;
    }

    public static final class Result extends ChronosOperationResult<Void> {
    }
}
