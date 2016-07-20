package com.softdesign.devintensive.ui.adapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.redmadrobot.chronos.ChronosConnector;
import com.softdesign.devintensive.R;
import com.softdesign.devintensive.data.events.RemoveUserEvent;
import com.softdesign.devintensive.data.events.UpdateUsersEvent;
import com.softdesign.devintensive.data.managers.DataManager;
import com.softdesign.devintensive.data.storage.models.User;
import com.softdesign.devintensive.data.storage.operations.RemoveUserOperation;
import com.softdesign.devintensive.data.storage.operations.UpdateUsersOperation;
import com.softdesign.devintensive.ui.views.AspectRatioImageView;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.List;
import java.util.Queue;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.ViewHolder> {
    private static final String TAG = "UsersAdapter";

    ChronosConnector mChronosConnector;
    Context mContext;
    List<User> mUsers;
    Queue<User> pendingRemoval;
    Queue<Integer> pendingRemovalPosition;
    CoordinatorLayout mCoordinatorLayout;
    ViewHolder.CustomClickListener mListener;

    public UsersAdapter(List<User> users, CoordinatorLayout coordinatorLayout,
                        ViewHolder.CustomClickListener customClickListener) {
        mChronosConnector = new ChronosConnector();
        mUsers = users;
        pendingRemoval = new ArrayDeque<>();
        pendingRemovalPosition = new ArrayDeque<>();
        mCoordinatorLayout = coordinatorLayout;
        mListener = customClickListener;
    }

    @Override
    public UsersAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        View contentView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user_list, parent, false);
        return new ViewHolder(contentView, mListener);
    }

    @Override
    public void onBindViewHolder(final UsersAdapter.ViewHolder holder, int position) {
        final User user = mUsers.get(position);
        final String userPhoto = user.getPhoto();

        if (!userPhoto.isEmpty()) {
            DataManager.getInstance().getPicasso()
                    .load(user.getPhoto())
                    .error(holder.mDummy)
                    .placeholder(holder.mDummy)
                    .fit()
                    .centerCrop()
                    .networkPolicy(NetworkPolicy.OFFLINE)
                    .into(holder.userPhoto, new Callback() {
                        @Override
                        public void onSuccess() {
                            Log.d(TAG, "Cache: " + userPhoto);
                        }

                        @Override
                        public void onError() {
                            DataManager.getInstance().getPicasso()
                                    .load(user.getPhoto())
                                    .error(holder.mDummy)
                                    .placeholder(holder.mDummy)
                                    .fit()
                                    .centerCrop()
                                    .into(holder.userPhoto);
                        }
                    });
        }

        holder.mFullName.setText(user.getFullName());
        holder.mRating.setText(String.valueOf(user.getRating()));
        holder.mCodeLine.setText(String.valueOf(user.getCodeLines()));
        holder.mProjects.setText(String.valueOf(user.getProjects()));

        if (user.getBio() == null || user.getBio().isEmpty()) {
            holder.mBio.setVisibility(View.GONE);
        } else {
            holder.mBio.setVisibility(View.VISIBLE);
            holder.mBio.setText(user.getBio());
        }
    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }

    public void moveItem(int fromPosition, int toPosition) {
        Log.d(TAG, "from " + fromPosition + " to " + toPosition);
        User itemToMove = mUsers.get(fromPosition);
        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {
                mUsers.get(i).setListPosition(i+1);
                mUsers.get(i+1).setListPosition(i);
                Collections.swap(mUsers, i, i+1);
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                mUsers.get(i).setListPosition(i-1);
                mUsers.get(i-1).setListPosition(i);
                Collections.swap(mUsers, i, i-1);
            }
        }
        DataManager.getInstance().getBus().post(new UpdateUsersEvent(mUsers));
        notifyItemMoved(fromPosition, toPosition);
    }

    public void pendingRemoval(int position) {
        pendingRemoval.add(mUsers.get(position));
        pendingRemovalPosition.add(position);
        mUsers.remove(position);
        notifyItemRemoved(position);
        Snackbar.make(mCoordinatorLayout, "Пользователь удален", Snackbar.LENGTH_LONG)
                .setDuration(4000)
                .setAction("Отменить", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mUsers.add(pendingRemovalPosition.element(), pendingRemoval.remove());
                        notifyItemInserted(pendingRemovalPosition.remove());
                    }
                })
                .setCallback(new Snackbar.Callback() {
                    @Override
                    public void onDismissed(Snackbar snackbar, int event) {
                        pendingRemovalPosition.remove();
                        User user = pendingRemoval.remove();
                        DataManager.getInstance().getBus().post(new RemoveUserEvent(user));
                        super.onDismissed(snackbar, event);
                    }
                })
                .show();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        protected AspectRatioImageView userPhoto;
        protected TextView mFullName, mRating, mCodeLine, mProjects, mBio;
        private Button mShowMore;
        protected Drawable mDummy;

        private CustomClickListener mListener;

        public ViewHolder(View itemView, CustomClickListener customClickListener) {
            super(itemView);
            mListener = customClickListener;

            userPhoto = (AspectRatioImageView) itemView.findViewById(R.id.user_photo_img);
            mFullName = (TextView) itemView.findViewById(R.id.user_full_name_txt);
            mRating = (TextView) itemView.findViewById(R.id.rating);
            mCodeLine = (TextView) itemView.findViewById(R.id.code_lines);
            mProjects = (TextView) itemView.findViewById(R.id.projects);
            mBio = (TextView) itemView.findViewById(R.id.bio_txt);
            mShowMore = (Button) itemView.findViewById(R.id.more_info_btn);
            mShowMore.setOnClickListener(this);
            mDummy = userPhoto.getContext().getResources().getDrawable(R.drawable.user_bg);
        }

        @Override
        public void onClick(View view) {
            if (mListener != null) {
                mListener.onUserItemClickListener(getAdapterPosition());
            }
        }

        public interface CustomClickListener {

            void onUserItemClickListener(int position);

        }
    }
}
