package com.softdesign.devintensive.ui.adapters;

import android.content.Context;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.softdesign.devintensive.R;
import com.softdesign.devintensive.data.network.res.UserListRes;
import com.softdesign.devintensive.ui.views.AspectRatioImageView;
import com.squareup.picasso.Picasso;

import java.util.List;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.ViewHolder> {

    Context mContext;
    List<UserListRes.Datum> mUsers;
    ViewHolder.CustomClickListener mListener;

    public UsersAdapter(List<UserListRes.Datum> users, ViewHolder.CustomClickListener customClickListener) {
        mUsers = users;
        mListener = customClickListener;
    }

    @Override
    public UsersAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        View contentView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user_list, parent, false);
        return new ViewHolder(contentView, mListener);
    }

    @Override
    public void onBindViewHolder(UsersAdapter.ViewHolder holder, int position) {
        UserListRes.Datum user = mUsers.get(position);
        Picasso.with(mContext)
                .load(user.getPublicInfo().getPhoto())
                .placeholder(mContext.getResources().getDrawable(R.drawable.user_bg))
                .error(mContext.getResources().getDrawable(R.drawable.user_bg))
                .into(holder.userPhoto);

        holder.mFullName.setText(user.getFullName());
        holder.mRating.setText(String.valueOf(user.getProfileValues().getRaiting()));
        holder.mCodeLine.setText(String.valueOf(user.getProfileValues().getLinesCode()));
        holder.mProjects.setText(String.valueOf(user.getProfileValues().getProjects()));

        if (user.getPublicInfo().getBio() == null || user.getPublicInfo().getBio().isEmpty()) {
            holder.mBio.setVisibility(View.GONE);
        } else {
            holder.mBio.setVisibility(View.VISIBLE);
            holder.mBio.setText(user.getPublicInfo().getBio());
        }
    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        protected AspectRatioImageView userPhoto;
        protected TextView mFullName, mRating, mCodeLine, mProjects, mBio;
        private Button mShowMore;

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
