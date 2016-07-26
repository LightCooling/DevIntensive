package com.softdesign.devintensive.data.events;

import java.util.List;

public class UpdateLikeEvent {
    private int position;
    private List<String> likesBy;

    public UpdateLikeEvent(int position, List<String> likesBy) {
        this.position = position;
        this.likesBy = likesBy;
    }

    public int getPosition() {
        return position;
    }

    public List<String> getLikesBy() {
        return likesBy;
    }
}

