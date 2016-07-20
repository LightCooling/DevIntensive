package com.softdesign.devintensive.ui.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Handler;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;

import com.softdesign.devintensive.R;
import com.softdesign.devintensive.data.events.RemoveUserEvent;
import com.softdesign.devintensive.data.events.UpdateUsersEvent;
import com.softdesign.devintensive.data.managers.DataManager;
import com.softdesign.devintensive.data.network.res.UserListRes;
import com.softdesign.devintensive.data.storage.models.User;
import com.softdesign.devintensive.data.storage.models.UserDTO;
import com.softdesign.devintensive.data.storage.operations.RemoveUserOperation;
import com.softdesign.devintensive.data.storage.operations.SaveUsersOperation;
import com.softdesign.devintensive.data.storage.operations.ShowUsersOperation;
import com.softdesign.devintensive.data.storage.operations.UpdateUsersOperation;
import com.softdesign.devintensive.ui.adapters.UsersAdapter;
import com.softdesign.devintensive.utils.ConstantManager;
import com.squareup.otto.Subscribe;
import com.squareup.picasso.Picasso;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserListActivity extends BaseActivity implements SwipeRefreshLayout.OnRefreshListener {

    private static final String SHOW_USER_QUERY = "showUsersByQuery";

    private NavigationView mNavigationView;
    private CoordinatorLayout mCoordinatorLayout;
    private Toolbar mToolbar;
    private DrawerLayout mNavigationDrawer;
    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    private DataManager mDataManager;
    private UsersAdapter mUsersAdapter;
    private List<User> mUsers;

    private MenuItem mSearchItem;
    private String mQuery;
    private Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list);

        mDataManager = DataManager.getInstance();
        mDataManager.getBus().register(this);
        mNavigationView = (NavigationView) findViewById(R.id.navigation_view);
        mCoordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinator_layout);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mNavigationDrawer = (DrawerLayout) findViewById(R.id.navigation_drawer);
        mRecyclerView = (RecyclerView) findViewById(R.id.user_list);
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_layout);
        mSwipeRefreshLayout.setOnRefreshListener(this);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(linearLayoutManager);

        mHandler = new Handler();

        setupToolbar();
        setupDrawer();
        setupItemTouchHelper();
        loadNavHeaderUserInfo();
        insertAvatarImage(mDataManager.getPreferencesManager().loadUserAvatar());
        showProgress();
        if (mDataManager.getDaoSession().getUserDao().count() == 0) {
            saveUsersListInDb();
        } else {
            runOperation(new ShowUsersOperation(null));
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            mNavigationDrawer.openDrawer(GravityCompat.START);
        }
        return super.onOptionsItemSelected(item);
    }

    private void showMessage(String msg) {
        Snackbar.make(mCoordinatorLayout, msg, Snackbar.LENGTH_SHORT).show();
    }

    private void setupToolbar() {
        setSupportActionBar(mToolbar);
        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setHomeAsUpIndicator(R.drawable.ic_menu_white);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    private void setupDrawer() {
        mNavigationView.setCheckedItem(R.id.all_menu);
        mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.user_profile_menu:
                        Intent userListIntent = new Intent(UserListActivity.this, MainActivity.class);
                        startActivity(userListIntent);
                        break;
                    case R.id.logout_menu:
                        mDataManager.getPreferencesManager().saveAuthToken("");
                        mDataManager.getPreferencesManager().saveUserId("");
                        mDataManager.getDaoSession().clear();
                        Intent authIntent = new Intent(UserListActivity.this, AuthActivity.class);
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

    private void setupItemTouchHelper() {
        ItemTouchHelper.SimpleCallback simpleItemTouchCallback =
                new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN,
                        ItemTouchHelper.LEFT) {

                    @Override
                    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                        UsersAdapter adapter = (UsersAdapter) mRecyclerView.getAdapter();
                        adapter.moveItem(viewHolder.getAdapterPosition(), target.getAdapterPosition());
                        return true;
                    }

                    @Override
                    public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                        int swipedPosition = viewHolder.getAdapterPosition();
                        UsersAdapter adapter = (UsersAdapter) mRecyclerView.getAdapter();
                        adapter.pendingRemoval(swipedPosition);
                    }
                };
        ItemTouchHelper mItemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        mItemTouchHelper.attachToRecyclerView(mRecyclerView);
    }

    private void loadNavHeaderUserInfo() {
        ((TextView) mNavigationView.getHeaderView(0).findViewById(R.id.nav_header_name))
                .setText(mDataManager.getPreferencesManager().loadUserFullName());
        ((TextView) mNavigationView.getHeaderView(0).findViewById(R.id.nav_header_email))
                .setText(mDataManager.getPreferencesManager().loadUserProfileData().get(1));
    }

    private void insertAvatarImage(Uri image) {
        ImageView headerPhoto = (ImageView) mNavigationView.getHeaderView(0).findViewById(R.id.nav_header_avatar);
        Picasso.with(this).load(image).into(headerPhoto, new com.squareup.picasso.Callback() {
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

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search_menu, menu);
        mSearchItem = menu.findItem(R.id.search_action);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(mSearchItem);
        searchView.setQueryHint("Введите имя пользователя");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                if (s.isEmpty()) {
                    showUsers(mDataManager.getUserListFromDb());
                } else {
                    showUsersByQuery(s);
                }
                return false;
            }
        });
        return super.onPrepareOptionsMenu(menu);
    }

    private void saveUsersListInDb() {
        Call<UserListRes> call = mDataManager.getUserListFromNetwork();

        call.enqueue(new Callback<UserListRes>() {
            @Override
            public void onResponse(Call<UserListRes> call, Response<UserListRes> response) {
                if (response.code() == 200) {
                    runOperation(new SaveUsersOperation(response.body().getData()));
                } else {
                    hideProgress();
                    Log.e(TAG, "onResponse: " + response.code());
                    showMessage("Ошибка при получении списка пользователей");
                }
            }

            @Override
            public void onFailure(Call<UserListRes> call, Throwable t) {
                hideProgress();
                Log.e(TAG, t.toString());
                t.printStackTrace();
                showMessage("Невозможно подключиться к серверу");
            }
        });
    }

    private void showUsers(List<User> users) {
        mUsers = users;
        mUsersAdapter = new UsersAdapter(mUsers, mCoordinatorLayout, new UsersAdapter.ViewHolder.CustomClickListener() {
            @Override
            public void onUserItemClickListener(int position) {
                UserDTO userDTO = new UserDTO(mUsers.get(position));

                Intent profileIntent = new Intent(UserListActivity.this, ProfileUserActivity.class);
                profileIntent.putExtra(ConstantManager.PARCELABLE_KEY, userDTO);
                startActivity(profileIntent);
            }
        });
        mRecyclerView.swapAdapter(mUsersAdapter, false);
    }

    private void showUsersByQuery(String query) {
        mQuery = query;

        Runnable searchUsers = new Runnable() {
            @Override
            public void run() {
                showProgress();
                runOperation(new ShowUsersOperation(mQuery), SHOW_USER_QUERY);
            }
        };
        mHandler.removeCallbacks(searchUsers);
        cancelOperation(SHOW_USER_QUERY);
        mHandler.postDelayed(searchUsers, ConstantManager.SEARCH_DELAY);
    }

    @SuppressWarnings("unused")
    public void onOperationFinished(ShowUsersOperation.Result result) {
        hideProgress();
        mSwipeRefreshLayout.setRefreshing(false);
        if (result.isSuccessful()) {
            showUsers(result.getOutput());
        } else {
            showMessage("Невозможно загрузить список пользователей из базы данных");
            Log.e("UserListActivity", result.getErrorMessage());
        }
    }

    @SuppressWarnings("unused")
    public void onOperationFinished(SaveUsersOperation.Result result) {
        hideProgress();
        if (result.isSuccessful()) {
            runOperation(new ShowUsersOperation(null));
        } else {
            showMessage("Невозможно сохранить список пользователей в базу данных");
            Log.e(TAG, result.getErrorMessage());
        }
    }

    @Subscribe
    @SuppressWarnings("unused")
    public void updateUsers(UpdateUsersEvent event) {
        runOperation(new UpdateUsersOperation(event.getUsersToUpdate()));
    }

    @Subscribe
    @SuppressWarnings("unused")
    public void removeUser(RemoveUserEvent event) {
        runOperation(new RemoveUserOperation(event.getUserToRemove()));
    }

    @Override
    public void onRefresh() {
        saveUsersListInDb();
    }
}
