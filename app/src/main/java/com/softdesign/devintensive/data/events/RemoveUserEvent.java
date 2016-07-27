package com.softdesign.devintensive.data.events;

import com.softdesign.devintensive.data.storage.models.User;

public class RemoveUserEvent {
    private User mUserToRemove;

    public RemoveUserEvent(User userToRemove) {
        mUserToRemove = userToRemove;
    }

    public User getUserToRemove() {
        return mUserToRemove;
    }
}
