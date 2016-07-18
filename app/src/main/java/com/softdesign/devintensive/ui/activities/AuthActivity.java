package com.softdesign.devintensive.ui.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.softdesign.devintensive.R;
import com.softdesign.devintensive.data.managers.DataManager;
import com.softdesign.devintensive.data.network.req.UserModelReq;
import com.softdesign.devintensive.data.network.res.UserModelRes;
import com.softdesign.devintensive.utils.ConstantManager;
import com.softdesign.devintensive.utils.NetworkStatusChecker;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AuthActivity extends BaseActivity implements View.OnClickListener {

    private CoordinatorLayout mCoordinatorLayout;
    private Button mSignin;
    private TextView mRemember;
    private EditText mLogin, mPassword;

    private DataManager mDataManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mDataManager = DataManager.getInstance();

        Log.d("Devin", mDataManager.getPreferencesManager().getAuthToken());
        if (mDataManager.getPreferencesManager().getAuthToken() != null
                && !mDataManager.getPreferencesManager().getAuthToken().isEmpty()) {
            startNextActivity(UserListActivity.class);
        } else {
            setContentView(R.layout.activity_auth);
            mCoordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinator_layout);
            mSignin = (Button) findViewById(R.id.signin);
            mRemember = (TextView) findViewById(R.id.remember);
            mLogin = (EditText) findViewById(R.id.login_et);
            mPassword = (EditText) findViewById(R.id.password_et);

            mSignin.setOnClickListener(this);
            mRemember.setOnClickListener(this);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.signin:
                signin();
                break;
            case R.id.remember:
                rememberPassword();
                break;
        }
    }

    private void showMessage(String msg) {
        Snackbar.make(mCoordinatorLayout, msg, Snackbar.LENGTH_SHORT).show();
    }

    private void startNextActivity(Class activityClass) {
        Intent authedIntent = new Intent(this, activityClass);
        startActivity(authedIntent);
    }

    private void loginSuccess(UserModelRes userModel) {
        mDataManager.getPreferencesManager().saveAuthToken(userModel.getData().getToken());
        mDataManager.getPreferencesManager().saveUserId(userModel.getData().getUser().getId());
        saveUserFullName(userModel);
        saveUserValues(userModel);
        saveUserFields(userModel);
        saveUserPhotos(userModel);

        startNextActivity(UserListActivity.class);
    }

    private void rememberPassword() {
        Intent rememberIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://devintensive.softdesign-apps.ru/forgotpass"));
        startActivity(rememberIntent);
    }

    private void signin() {
        if (NetworkStatusChecker.isNetworkAvailable(this)) {
            Call<UserModelRes> call = mDataManager.loginUser(new UserModelReq(mLogin.getText().toString(), mPassword.getText().toString()));
            call.enqueue(new Callback<UserModelRes>() {
                @Override
                public void onResponse(Call<UserModelRes> call, Response<UserModelRes> response) {
                    if (response.code() == 200) {
                        loginSuccess(response.body());
                    } else if (response.code() == 403) {
                        showMessage("Неверный логин или пароль");
                    } else {
                        showMessage("Ohh, shit happened!");
                    }
                }

                @Override
                public void onFailure(Call<UserModelRes> call, Throwable t) {

                }
            });
        } else {
            showMessage("No network connection, try again later");
        }
    }

    private void saveUserFullName(UserModelRes userModel) {
        mDataManager.getPreferencesManager().saveUserFullName(userModel.getData().getUser().getFullName());
    }

    private void saveUserValues(UserModelRes userModel) {
        int[] userValues = {
                userModel.getData().getUser().getProfileValues().getRaiting(),
                userModel.getData().getUser().getProfileValues().getLinesCode(),
                userModel.getData().getUser().getProfileValues().getProjects()
        };
        mDataManager.getPreferencesManager().saveUserProfileValues(userValues);
    }

    private void saveUserFields(UserModelRes userModel) {
        List<String> userFields = new ArrayList<>();
        userFields.add(userModel.getData().getUser().getContacts().getPhone());
        userFields.add(userModel.getData().getUser().getContacts().getEmail());
        userFields.add(userModel.getData().getUser().getContacts().getVk());
        String repos = "";
        for (UserModelRes.Repo r:
                userModel.getData().getUser().getRepositories().getRepo()) {
            repos += r.getGit() + "\n";
        }
        userFields.add(repos.substring(0, repos.length()-2));
        userFields.add(userModel.getData().getUser().getPublicInfo().getBio());
        mDataManager.getPreferencesManager().saveUserProfileData(userFields);
    }

    private void saveUserPhotos(UserModelRes userModel) {
        mDataManager.getPreferencesManager().saveUserPhoto(Uri.parse(userModel.getData().getUser().getPublicInfo().getPhoto()));
        mDataManager.getPreferencesManager().saveUserAvatar(Uri.parse(userModel.getData().getUser().getPublicInfo().getAvatar()));
    }
}
