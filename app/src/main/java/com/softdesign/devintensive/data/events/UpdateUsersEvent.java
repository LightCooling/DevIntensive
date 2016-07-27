package com.softdesign.devintensive.data.events;

import com.softdesign.devintensive.data.storage.models.User;

import java.util.List;

public class UpdateUsersEvent {
    private List<User> mUsersToUpdate;

    public UpdateUsersEvent(List<User> usersToUpdate) {
        mUsersToUpdate = usersToUpdate;
    }

    public List<User> getUsersToUpdate() {
        return mUsersToUpdate;
    }
}
