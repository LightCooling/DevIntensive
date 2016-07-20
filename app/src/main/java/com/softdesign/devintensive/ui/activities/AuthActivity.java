package com.softdesign.devintensive.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.softdesign.devintensive.R;
import com.softdesign.devintensive.data.events.LoginEvent;
import com.softdesign.devintensive.data.managers.DataManager;
import com.softdesign.devintensive.data.network.req.UserModelReq;
import com.softdesign.devintensive.data.network.res.UserModelRes;
import com.softdesign.devintensive.data.storage.models.RepositoryDao;
import com.softdesign.devintensive.data.storage.models.UserDao;
import com.softdesign.devintensive.utils.NetworkStatusChecker;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnEditorAction;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AuthActivity extends BaseActivity implements View.OnClickListener, TextView.OnEditorActionListener {
    private static final String TAG = "AuthActivity";

    @BindView(R.id.coordinator_layout)
    CoordinatorLayout mCoordinatorLayout;
    @BindView(R.id.login_et)
    EditText mLogin;
    @BindView(R.id.password_et)
    EditText mPassword;

    private DataManager mDataManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mDataManager = DataManager.getInstance();
        mDataManager.getBus().register(this);

        Log.d("Devin", mDataManager.getPreferencesManager().getAuthToken());
        if (mDataManager.getPreferencesManager().getAuthToken() != null
                && !mDataManager.getPreferencesManager().getAuthToken().isEmpty()) {
            startNextActivity(UserListActivity.class);
            return;
        }
        setContentView(R.layout.activity_auth);
        ButterKnife.bind(this);
        mLogin.setText(mDataManager.getPreferencesManager().getLastEmail());
    }

    @Override
    @OnClick({R.id.signin, R.id.remember})
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
        if (mLogin == null) Log.d(TAG, "mlogin is null");
        if ((mLogin = (EditText) findViewById(R.id.login_et)) == null) Log.d(TAG, "fvbi is null");
        mDataManager.getPreferencesManager().saveLastEmail(mLogin.getText().toString());
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
            showProgress();
            Call<UserModelRes> call = mDataManager.loginUser(new UserModelReq(mLogin.getText().toString(), mPassword.getText().toString()));
            call.enqueue(new Callback<UserModelRes>() {
                @Override
                public void onResponse(Call<UserModelRes> call, Response<UserModelRes> response) {
                    mDataManager.getBus().post(new LoginEvent(true, response.code(), response.body()));
                }

                @Override
                public void onFailure(Call<UserModelRes> call, Throwable t) {
                    mDataManager.getBus().post(new LoginEvent(false, t));
                }
            });
        } else {
            showMessage("No network connection, try again later");
        }
    }

    @Subscribe
    @SuppressWarnings("unused")
    public void loginComplete(LoginEvent event) {
        if (event.isSuccess()) {
            if (event.getResponseCode() == 200) {
                loginSuccess(event.getResponse());
            } else if (event.getResponseCode() == 404) {
                hideProgress();
                showMessage("Неверный логин или пароль");
            } else {
                hideProgress();
                showMessage("Ohh, shit happened!");
            }
        } else {
            Log.e(TAG, event.getError().toString());
            showMessage("Невозможно подключиться к серверу");
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
        for (UserModelRes.Repo r :
                userModel.getData().getUser().getRepositories().getRepo()) {
            repos += r.getGit() + "\n";
        }
        userFields.add(repos.substring(0, repos.length() - 2));
        userFields.add(userModel.getData().getUser().getPublicInfo().getBio());
        mDataManager.getPreferencesManager().saveUserProfileData(userFields);
    }

    private void saveUserPhotos(UserModelRes userModel) {
        mDataManager.getPreferencesManager().saveUserPhoto(Uri.parse(userModel.getData().getUser().getPublicInfo().getPhoto()));
        mDataManager.getPreferencesManager().saveUserAvatar(Uri.parse(userModel.getData().getUser().getPublicInfo().getAvatar()));
    }

    @Override
    @OnEditorAction({R.id.login_et, R.id.password_et})
    public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
        if (actionId == EditorInfo.IME_ACTION_NEXT
                || actionId == EditorInfo.IME_ACTION_GO
                || (keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER
                && keyEvent.getAction() == KeyEvent.ACTION_UP)) {
            switch (textView.getId()) {
                case R.id.login_et:
                    mPassword.requestFocus();
                    return true;
                case R.id.password_et:
                    final InputMethodManager inputMethodManager =
                            (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                            InputMethodManager.HIDE_NOT_ALWAYS);
                    signin();
                    return true;
            }
        }
        return false;
    }
}
