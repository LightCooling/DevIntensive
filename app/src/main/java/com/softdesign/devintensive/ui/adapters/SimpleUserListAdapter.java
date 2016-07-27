package com.softdesign.devintensive.ui.adapters;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.softdesign.devintensive.R;
import com.softdesign.devintensive.data.storage.models.User;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.List;

public class SimpleUserListAdapter extends BaseAdapter{
    private Context mContext;
    private List<User> mUsers;
    private LayoutInflater mInflater;

    public SimpleUserListAdapter(Context context, List<User> users) {
        mContext = context;
        mUsers = users;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return mUsers.size();
    }

    @Override
    public Object getItem(int i) {
        return mUsers.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if (view == null) {
            view = mInflater.inflate(R.layout.item_simple_user_list, viewGroup, false);
        }

        final ImageView avatar = (ImageView) view.findViewById(R.id.avatar);
        Picasso.with(mContext).load(mUsers.get(i).getAvatar()).fit().into(avatar, new Callback() {
            @Override
            public void onSuccess() {
                Bitmap bitmap = ((BitmapDrawable) avatar.getDrawable()).getBitmap();
                Resources r = mContext.getResources();
                RoundedBitmapDrawable roundedImage = RoundedBitmapDrawableFactory.create(r, bitmap);
                roundedImage.setGravity(Gravity.CENTER);
                roundedImage.setCircular(true);
                avatar.setImageDrawable(roundedImage);
            }

            @Override
            public void onError() {
                Log.d("SimpleUserListAdapter", "Error loading avatar");
            }
        });

        TextView fullName = (TextView) view.findViewById(R.id.full_name);
        fullName.setText(mUsers.get(i).getFullName());
        return view;
    }
}
