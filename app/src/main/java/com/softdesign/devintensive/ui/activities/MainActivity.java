package com.softdesign.devintensive.ui.activities;

import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.softdesign.devintensive.R;
import com.softdesign.devintensive.data.managers.DataManager;
import com.softdesign.devintensive.ui.views.RegExValidateWatcher;
import com.softdesign.devintensive.utils.ConstantManager;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends BaseActivity implements View.OnClickListener{

    private static final String TAG = "MainActivity";

    private DataManager mDataManager;
    private int mCurrentEditMode = 0;

    @BindView(R.id.navigation_view)
    NavigationView mNavigationView;
    @BindView(R.id.coordinator_layout)
    CoordinatorLayout mCoordinatorLayout;
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.navigation_drawer)
    DrawerLayout mNavigationDrawer;
    @BindView(R.id.appbar_layout)
    AppBarLayout mAppBarLayout;
    @BindView(R.id.profile_placeholder)
    RelativeLayout mProfilePlaceholder;
    @BindView(R.id.collapsing_toolbar)
    CollapsingToolbarLayout mCollapsingToolbarLayout;
    @BindView(R.id.user_photo_img)
    ImageView mProfileImage;

    @BindView(R.id.phone_et)
    EditText mUserPhone;
    @BindView(R.id.email_et)
    EditText mUserEmail;
    @BindView(R.id.vk_et)
    EditText mUserVk;
    @BindView(R.id.github_et)
    EditText mUserGithub;
    @BindViews({R.id.phone_et, R.id.email_et, R.id.vk_et, R.id.github_et, R.id.about_et})
    List<EditText> mUserInfoViews;

    @BindViews({ R.id.stats_rating, R.id.stats_lines, R.id.stats_projects })
    List<TextView> mUserValueViews;

    private AppBarLayout.LayoutParams mAppBarParams;
    private File mPhotoFile = null;
    private Uri mSelectedImage;

    //<editor-fold desc="========== Lifecycle methods ==========">
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mDataManager = DataManager.getInstance();
        ButterKnife.bind(this);

        mUserPhone.addTextChangedListener(
                new RegExValidateWatcher(getString(R.string.pattern_phone),
                        getString(R.string.profile_phone_error),
                        (TextInputLayout) mUserPhone.getParent(),
                        (ImageView) findViewById(R.id.phone_action)));
        mUserEmail.addTextChangedListener(
                new RegExValidateWatcher(getString(R.string.pattern_email),
                        getString(R.string.profile_email_error),
                        (TextInputLayout) mUserEmail.getParent(),
                        (ImageView) findViewById(R.id.email_action)));
        mUserVk.addTextChangedListener(
                new RegExValidateWatcher(getString(R.string.pattern_vk),
                        getString(R.string.profile_vk_error),
                        (TextInputLayout) mUserVk.getParent(),
                        (ImageView) findViewById(R.id.vk_action)));
        mUserGithub.addTextChangedListener(
                new RegExValidateWatcher(getString(R.string.pattern_github),
                        getString(R.string.profile_github_error),
                        (TextInputLayout) mUserGithub.getParent(),
                        (ImageView) findViewById(R.id.github_action)));

        setupToolbar();
        setupDrawer();
        loadNavHeaderUserInfo();
        initUserFields();
        initUserValue();
        insertProfileImage(mDataManager.getPreferencesManager().loadUserPhoto());
        insertAvatarImage(mDataManager.getPreferencesManager().loadUserAvatar());
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveUserFields();
        Log.d(TAG, "onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(TAG, "onRestart");
    }
    //</editor-fold>

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            mNavigationDrawer.openDrawer(GravityCompat.START);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (mNavigationView.isShown())
            mNavigationDrawer.closeDrawer(GravityCompat.START);
        else
            super.onBackPressed();
    }

    @Override
    @OnClick({R.id.fab, R.id.profile_placeholder, R.id.phone_action, R.id.email_action, R.id.vk_action, R.id.github_action})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.fab:
                if (mCurrentEditMode == 0)
                    changeEditMode(1);
                else
                    changeEditMode(0);
                break;
            case R.id.profile_placeholder:
                showDialog(ConstantManager.LOAD_PROFILE_PHOTO);
                break;

            case R.id.phone_action:
                Intent makeCallIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("tel:" + mUserPhone.getText()));
                startActivity(makeCallIntent);
                break;
            case R.id.email_action:
                Intent sendMailIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("mailto:" + mUserEmail.getText()));
                startActivity(sendMailIntent);
                break;
            case R.id.vk_action:
                Intent viewVkIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://" + mUserVk.getText()));
                startActivity(viewVkIntent);
                break;
            case R.id.github_action:
                Intent viewRepIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://" + mUserGithub.getText()));
                startActivity(viewRepIntent);
                break;
        }
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case ConstantManager.LOAD_PROFILE_PHOTO:
                String[] selectItems = {getString(R.string.user_profile_dialog_gallery), getString(R.string.user_profile_dialog_camera), getString(R.string.user_profile_dialog_cancel)};

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.user_profile_dialog_title);
                builder.setItems(selectItems, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int choiceItem) {
                        switch (choiceItem) {
                            case 0:
                                loadPhotoFromGallery();
                                break;
                            case 1:
                                loadPhotoFromCamera();
                                break;
                            case 2:
                                dialog.cancel();
                                break;
                        }
                    }
                });
                return builder.create();
        }
        return null;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case ConstantManager.REQUEST_GALLERY_PICTURE:
                if (resultCode == RESULT_OK && data != null) {
                    mSelectedImage = data.getData();
                    insertProfileImage(mSelectedImage);
                    insertAvatarImage(mSelectedImage);
                }
                break;
            case ConstantManager.REQUEST_CAMERA_PICTURE:
                if (resultCode == RESULT_OK && mPhotoFile != null) {
                    mSelectedImage = Uri.fromFile(mPhotoFile);
                    insertProfileImage(mSelectedImage);
                    insertAvatarImage(mSelectedImage);
                }
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == ConstantManager.CAMERA_REQUEST_PERMISSION_CODE && grantResults.length == 2) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                loadPhotoFromCamera();
            }
        }
    }

    private void showMessage(String msg) {
        Snackbar.make(mCoordinatorLayout, msg, Snackbar.LENGTH_SHORT).show();
    }

    private void setupToolbar() {
        setSupportActionBar(mToolbar);

        ActionBar actionBar = getSupportActionBar();

        mAppBarParams = (AppBarLayout.LayoutParams) mCollapsingToolbarLayout.getLayoutParams();
        if (actionBar != null) {
            actionBar.setHomeAsUpIndicator(R.drawable.ic_menu_white);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    private void setupDrawer() {
        mNavigationView.setCheckedItem(R.id.user_profile_menu);
        mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.all_menu:
                        Intent userListIntent = new Intent(MainActivity.this, UserListActivity.class);
                        startActivity(userListIntent);
                        break;
                    case R.id.logout_menu:
                        mDataManager.getPreferencesManager().saveAuthToken("");
                        mDataManager.getPreferencesManager().saveUserId("");
                        Intent authIntent = new Intent(MainActivity.this, AuthActivity.class);
                        authIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                                | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(authIntent);
                        finish();
                        break;
                }
                item.setChecked(true);
                mNavigationDrawer.closeDrawer(GravityCompat.START);
                return false;
            }
        });
    }

    private void changeEditMode(int mode) {
        if (mode == 1) {
            for (EditText userValue: mUserInfoViews) {
                userValue.setFocusable(true);
                userValue.setFocusableInTouchMode(true);
                userValue.setEnabled(true);
            }
            mUserPhone.requestFocus();
            final InputMethodManager inputMethodManager =
                    (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.showSoftInput(mUserPhone, InputMethodManager.SHOW_IMPLICIT);
            showProfilePlaceholder();
            lockToolbar();
            mCollapsingToolbarLayout.setExpandedTitleColor(Color.TRANSPARENT);
            mCurrentEditMode = 1;
        } else {
            for (EditText userValue: mUserInfoViews) {
                userValue.setFocusable(false);
                userValue.setFocusableInTouchMode(false);
                userValue.setEnabled(false);
            }
            final InputMethodManager inputMethodManager =
                    (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                    InputMethodManager.HIDE_NOT_ALWAYS);
            hideProfilePlaceholder();
            unlockToolbar();
            mCollapsingToolbarLayout.setExpandedTitleColor(getResources().getColor(R.color.white));
            mCurrentEditMode = 0;
        }
    }

    private void loadNavHeaderUserInfo() {
        ((TextView) mNavigationView.getHeaderView(0).findViewById(R.id.nav_header_name))
                .setText(mDataManager.getPreferencesManager().loadUserFullName());
        ((TextView) mNavigationView.getHeaderView(0).findViewById(R.id.nav_header_email))
                .setText(mDataManager.getPreferencesManager().loadUserProfileData().get(1));
    }

    private void initUserFields() {
        List<String> userData = mDataManager.getPreferencesManager().loadUserProfileData();
        for (int i = 0; i < userData.size(); i++) {
            mUserInfoViews.get(i).setText(userData.get(i));
        }
    }

    private void saveUserFields() {
        List<String> userData = new ArrayList<>();
        for (EditText userFieldView : mUserInfoViews) {
            userData.add(userFieldView.getText().toString());
        }
        mDataManager.getPreferencesManager().saveUserProfileData(userData);
    }

    private void initUserValue() {
        List<String> userData = mDataManager.getPreferencesManager().loadUserProfileValues();
        for (int i = 0; i < userData.size(); i++) {
            mUserValueViews.get(i).setText(userData.get(i));
        }
    }

    //<editor-fold desc="========== Set user photos ==========">
    private void loadPhotoFromGallery() {
        Intent takeGalleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        takeGalleryIntent.setType("image/*");
        startActivityForResult(Intent.createChooser(takeGalleryIntent, getString(R.string.user_profile_chose_message)), ConstantManager.REQUEST_GALLERY_PICTURE);
    }

    private void loadPhotoFromCamera() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            Intent takeCaptureIntent = new Intent((MediaStore.ACTION_IMAGE_CAPTURE));
            try {
                mPhotoFile = createImageFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (mPhotoFile != null) {
                takeCaptureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(mPhotoFile));
                startActivityForResult(takeCaptureIntent, ConstantManager.REQUEST_CAMERA_PICTURE);
            }
        } else {
            ActivityCompat.requestPermissions(this, new String[]{
                    android.Manifest.permission.CAMERA,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            }, ConstantManager.CAMERA_REQUEST_PERMISSION_CODE);
            Snackbar.make(mCoordinatorLayout, "Для корректной работы приложения неоюходимо дать требуемые разрешения", Snackbar.LENGTH_LONG)
                    .setAction("Разрешить", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            openApplicationSettings();
                        }
                    }).show();
        }
    }

    private void hideProfilePlaceholder() {
        mProfilePlaceholder.setVisibility(View.GONE);
    }

    private void showProfilePlaceholder() {
        mProfilePlaceholder.setVisibility(View.VISIBLE);
    }

    private void lockToolbar() {mAppBarLayout.setExpanded(true, true);
        mAppBarParams.setScrollFlags(0);
        mCollapsingToolbarLayout.setLayoutParams(mAppBarParams);
    }

    private void unlockToolbar() {
        mAppBarParams.setScrollFlags(AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL
                | AppBarLayout.LayoutParams.SCROLL_FLAG_EXIT_UNTIL_COLLAPSED
                | AppBarLayout.LayoutParams.SCROLL_FLAG_SNAP);

        mCollapsingToolbarLayout.setLayoutParams(mAppBarParams);
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);

        File image = File.createTempFile(imageFileName, ".jpg", storageDir);

        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        values.put(MediaStore.MediaColumns.DATA, image.getAbsolutePath());

        this.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        return image;
    }

    private void insertProfileImage(Uri image) {
        Picasso.with(this)
                .load(image)
                .placeholder(R.drawable.user_bg)
                .into(mProfileImage);

        mDataManager.getPreferencesManager().saveUserPhoto(image);
    }

    private void insertAvatarImage (Uri image) {
        ImageView headerPhoto = (ImageView) mNavigationView.getHeaderView(0).findViewById(R.id.nav_header_avatar);
        Picasso.with(this).load(image).into(headerPhoto, new Callback() {
            @Override
            public void onSuccess() {
                ImageView headerPhoto = (ImageView) mNavigationView.getHeaderView(0).findViewById(R.id.nav_header_avatar);
                Bitmap bitmap = ((BitmapDrawable) headerPhoto.getDrawable()).getBitmap();
                RoundedBitmapDrawable roundedImage = RoundedBitmapDrawableFactory.create(getResources(), bitmap);
                roundedImage.setGravity(Gravity.CENTER);
                roundedImage.setCircular(true);
                headerPhoto.setImageDrawable(roundedImage);
            }

            @Override
            public void onError() {
                showMessage("Error loading avatar");
            }
        });
    }
    //</editor-fold>

    public void openApplicationSettings() {
        Intent appSettingsIntent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + getPackageName()));

        startActivityForResult(appSettingsIntent, ConstantManager.PERMISSION_REQUEST_SETTINGS_CODE);
    }
}
