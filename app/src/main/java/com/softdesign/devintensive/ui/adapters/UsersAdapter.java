package com.softdesign.devintensive.ui.adapters;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.softdesign.devintensive.R;
import com.softdesign.devintensive.data.events.RemoveUserEvent;
import com.softdesign.devintensive.data.events.UpdateUsersEvent;
import com.softdesign.devintensive.data.managers.DataManager;
import com.softdesign.devintensive.data.storage.models.User;
import com.softdesign.devintensive.ui.views.AspectRatioImageView;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.List;
import java.util.Queue;

import butterknife.BindDrawable;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.ViewHolder> {
    private static final String TAG = "UsersAdapter";

    Context mContext;
    List<User> mUsers;
    Queue<User> pendingRemoval;
    Queue<Integer> pendingRemovalPosition;
    CoordinatorLayout mCoordinatorLayout;
    ViewHolder.CustomClickListener mListener;

    public UsersAdapter(Context context, List<User> users, CoordinatorLayout coordinatorLayout,
                        ViewHolder.CustomClickListener customClickListener) {
        mContext = context;
        mUsers = users;
        pendingRemoval = new ArrayDeque<>();
        pendingRemovalPosition = new ArrayDeque<>();
        mCoordinatorLayout = coordinatorLayout;
        mListener = customClickListener;
    }

    @Override
    public UsersAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
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

        if (user.getLikes() == 0) {
            holder.mLikeBtn.setText("");
        } else {
            holder.mLikeBtn.setText(String.valueOf(user.getLikes()));
        }
        Resources r = mContext.getResources();
        Drawable src = r.getDrawable(R.drawable.ic_like);
        Drawable colorized = src.getConstantState().newDrawable();
        holder.mLiked = user.getLikesBy()
                .contains(DataManager.getInstance().getPreferencesManager().getUserId());
        if (holder.mLiked) {
            colorized.mutate().setColorFilter(r.getColor(R.color.like), PorterDuff.Mode.SRC_ATOP);
        } else {
            colorized.mutate().setColorFilter(r.getColor(R.color.grey), PorterDuff.Mode.SRC_ATOP);
        }
        int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, r.getDisplayMetrics());
        colorized.setBounds( 0, 0, px, px);
        holder.mLikeBtn.setCompoundDrawables(colorized, null, null, null);

        if (user.getBio() == null || user.getBio().isEmpty()) {
            holder.mBio.setVisibility(View.GONE);
        } else {
            holder.mBio.setVisibility(View.VISIBLE);
            holder.mBio.setText(user.getBio());
        }
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position, List<Object> payloads) {
        if (!payloads.isEmpty()) {
            List<String> likesBy = (List<String>) payloads.get(0);
            if (likesBy.size() == 0) {
                holder.mLikeBtn.setText("");
            } else {
                holder.mLikeBtn.setText(String.valueOf(likesBy.size()));
            }
            Resources r = mContext.getResources();
            Drawable src = r.getDrawable(R.drawable.ic_like);
            Drawable colorized = src.getConstantState().newDrawable();
            holder.mLiked = likesBy.contains(DataManager.getInstance().getPreferencesManager().getUserId());
            if (holder.mLiked) {
                colorized.mutate().setColorFilter(r.getColor(R.color.like), PorterDuff.Mode.SRC_ATOP);
            } else {
                colorized.mutate().setColorFilter(r.getColor(R.color.grey), PorterDuff.Mode.SRC_ATOP);
            }
            int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, r.getDisplayMetrics());
            colorized.setBounds( 0, 0, px, px);
            holder.mLikeBtn.setCompoundDrawables(colorized, null, null, null);
        } else {
            onBindViewHolder(holder, position);
        }
    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }

    public void moveItem(int fromPosition, int toPosition) {
        Log.d(TAG, "from " + fromPosition + " to " + toPosition);
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
                        if (event != DISMISS_EVENT_ACTION) {
                            pendingRemovalPosition.remove();
                            User user = pendingRemoval.remove();
                            DataManager.getInstance().getBus().post(new RemoveUserEvent(user));
                            super.onDismissed(snackbar, event);
                        }
                    }
                })
                .show();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @BindView(R.id.user_photo_img)
        AspectRatioImageView userPhoto;
        @BindView(R.id.user_full_name_txt)
        TextView mFullName;
        @BindView(R.id.rating)
        TextView mRating;
        @BindView(R.id.code_lines)
        TextView mCodeLine;
        @BindView(R.id.projects)
        TextView mProjects;
        @BindView(R.id.bio_txt)
        TextView mBio;
        @BindView(R.id.like_btn)
        Button mLikeBtn;
        @BindDrawable(R.drawable.user_bg)
        Drawable mDummy;

        boolean mLiked;

        private CustomClickListener mListener;

        public ViewHolder(View itemView, CustomClickListener customClickListener) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            mListener = customClickListener;
        }

        @Override
        @OnClick({R.id.more_info_btn, R.id.like_btn})
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.more_info_btn:
                    if (mListener != null) {
                        mListener.onUserItemClick(getAdapterPosition());
                    }
                    break;
                case R.id.like_btn:
                    if (mListener != null) {
                        mListener.onLike(getAdapterPosition(), !mLiked);
                    }
                    break;
            }
        }

        public interface CustomClickListener {

            void onUserItemClick(int position);

            void onLike(int position, boolean liked);
        }
    }
}
