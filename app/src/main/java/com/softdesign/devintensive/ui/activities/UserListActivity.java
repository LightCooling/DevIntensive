package com.softdesign.devintensive.ui.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Handler;
import android.support.annotation.Nullable;
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
import com.softdesign.devintensive.data.events.UpdateLikeEvent;
import com.softdesign.devintensive.data.events.UpdateUsersEvent;
import com.softdesign.devintensive.data.managers.DataManager;
import com.softdesign.devintensive.data.network.res.UpdateLikeRes;
import com.softdesign.devintensive.data.network.res.UserListRes;
import com.softdesign.devintensive.data.network.res.UserModelRes;
import com.softdesign.devintensive.data.storage.models.Repository;
import com.softdesign.devintensive.data.storage.models.User;
import com.softdesign.devintensive.data.storage.operations.RemoveUserOperation;
import com.softdesign.devintensive.data.storage.operations.SaveUsersOperation;
import com.softdesign.devintensive.data.storage.operations.ShowUsersOperation;
import com.softdesign.devintensive.data.storage.operations.UpdateLikeOperation;
import com.softdesign.devintensive.ui.adapters.UsersAdapter;
import com.softdesign.devintensive.utils.ConstantManager;
import com.softdesign.devintensive.utils.StringUtils;
import com.squareup.picasso.Picasso;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserListActivity extends BaseActivity implements SwipeRefreshLayout.OnRefreshListener {

    static final String TAG = "UserListActivity";
    private static final String SHOW_USER_QUERY = "showUsersByQuery";
    private static final String SEARCH_QUERY = "SEARCH_QUERY";
    private static final String REFRESH_STATUS = "REFRESH_STATUS";

    @BindView(R.id.navigation_view)
    NavigationView mNavigationView;
    @BindView(R.id.coordinator_layout)
    CoordinatorLayout mCoordinatorLayout;
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.navigation_drawer)
    DrawerLayout mNavigationDrawer;
    @BindView(R.id.user_list)
    RecyclerView mRecyclerView;
    @BindView(R.id.swipe_layout)
    SwipeRefreshLayout mSwipeRefreshLayout;

    private DataManager mDataManager;
    private UsersAdapter mUsersAdapter;
    private List<User> mUsers;

    private String mQuery;
    private Handler mHandler;
    private Runnable mSearchUsers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list);
        ButterKnife.bind(this);

        mDataManager = DataManager.getInstance();
        mDataManager.getBus().register(this);
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
            saveUsersListInDb(true);
        } else {
            if (savedInstanceState == null) {
                runOperation(new ShowUsersOperation(null));
                saveUsersListInDb(false);
            } else {
                mQuery = savedInstanceState.getString(SEARCH_QUERY);
                final boolean refreshing = savedInstanceState.getBoolean(REFRESH_STATUS);
                mSwipeRefreshLayout.post(new Runnable() {
                    @Override public void run() {
                        mSwipeRefreshLayout.setRefreshing(refreshing);
                    }
                });
                if (mQuery != null && !mQuery.isEmpty())
                    runOperation(new ShowUsersOperation(mQuery));
                else
                    runOperation(new ShowUsersOperation(null));
            }
        }
    }

    @Override
    protected void onSaveInstanceState(@Nullable Bundle outState) {
        super.onSaveInstanceState(outState);
        if (outState != null) {
            outState.putString(SEARCH_QUERY, mQuery);
            outState.putBoolean(REFRESH_STATUS, mSwipeRefreshLayout.isRefreshing());
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
        MenuItem mSearchItem = menu.findItem(R.id.search_action);
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
                    mQuery = "";
                    showUsers(mDataManager.getUserListFromDb());
                } else {
                    showUsersByQuery(s);
                }
                return false;
            }
        });
        if (mQuery != null && !mQuery.isEmpty()) {
            searchView.setQuery(mQuery, false);
            searchView.setIconified(false);
            searchView.clearFocus();
        }
        return super.onPrepareOptionsMenu(menu);
    }

    private void saveUsersListInDb(final boolean reset) {
        Call<UserListRes> call = mDataManager.getUserListFromNetwork();

        call.enqueue(new Callback<UserListRes>() {
            @Override
            public void onResponse(Call<UserListRes> call, Response<UserListRes> response) {
                if (response.code() == 200) {

                    List<Repository> allRepositories = new ArrayList<>();
                    List<User> allUsers = new ArrayList<>();

                    // create storage objects from network data
                    for (UserListRes.UserData userRes : response.body().getData()) {
                        allRepositories.addAll(getRepoListFromUserRes(userRes));
                        allUsers.add(new User(userRes));
                    }

                    if (reset) {
                        // set list positions in order by rating
                        Collections.sort(allUsers, new Comparator<User>() {
                            @Override
                            public int compare(User user, User t1) {
                                if (user.getRating() > t1.getRating()) {
                                    return -1;
                                } else if (user.getRating() < t1.getRating()) {
                                    return 1;
                                }
                                return 0;
                            }
                        });
                        for (int i = 0; i < allUsers.size(); i++) {
                            allUsers.get(i).setListPosition(i);
                        }
                        runOperation(new SaveUsersOperation(allUsers, allRepositories,
                                SaveUsersOperation.MODE_INSERT_REPLACE));
                    } else {
                        runOperation(new SaveUsersOperation(allUsers, allRepositories,
                                SaveUsersOperation.MODE_UPDATE));
                    }
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

    private List<Repository> getRepoListFromUserRes(UserListRes.UserData userData) {
        final String userId = userData.getId();

        List<Repository> repositories = new ArrayList<>();
        for (UserModelRes.Repo repositoryRes : userData.getRepositories().getRepo()) {
            repositories.add(new Repository(repositoryRes, userId));
        }
        return repositories;
    }

    private void showUsers(List<User> users) {
        mUsers = users;
        mUsersAdapter = new UsersAdapter(this, mUsers, mCoordinatorLayout,
                new UsersAdapter.ViewHolder.CustomClickListener() {
            @Override
            public void onUserItemClick(int position) {
                Intent profileIntent = new Intent(UserListActivity.this, ProfileUserActivity.class);
                profileIntent.putExtra(ConstantManager.REMOTE_ID, mUsers.get(position).getRemoteId());
                startActivity(profileIntent);
            }

            @Override
            public void onLike(int position, boolean liked) {
                doLike(position, liked);
            }
        });
        mRecyclerView.swapAdapter(mUsersAdapter, false);
    }

    private void showUsersByQuery(String query) {
        mQuery = query;

        if (mSearchUsers != null)
            mHandler.removeCallbacks(mSearchUsers);
        cancelOperation(SHOW_USER_QUERY);
        mSearchUsers = new Runnable() {
            @Override
            public void run() {
                runOperation(new ShowUsersOperation(mQuery), SHOW_USER_QUERY);
            }
        };
        mHandler.postDelayed(mSearchUsers, ConstantManager.SEARCH_DELAY);
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
            result.getException().printStackTrace();
        }
    }

    @Subscribe
    @SuppressWarnings("unused")
    public void updateUsers(UpdateUsersEvent event) {
        runOperation(new SaveUsersOperation(event.getUsersToUpdate(), SaveUsersOperation.MODE_UPDATE));
    }

    @Subscribe
    @SuppressWarnings("unused")
    public void removeUser(RemoveUserEvent event) {
        runOperation(new RemoveUserOperation(event.getUserToRemove()));
    }

    @Override
    public void onRefresh() {
        saveUsersListInDb(true);
    }

    private void doLike(final int position, boolean liked) {
        Call<UpdateLikeRes> call;
        if (liked) {
            call = DataManager.getInstance().likeUser(mUsers.get(position).getRemoteId());
        } else {
            call = DataManager.getInstance().unlikeUser(mUsers.get(position).getRemoteId());
        }
        call.enqueue(new Callback<UpdateLikeRes>() {
            @Override
            public void onResponse(Call<UpdateLikeRes> call, Response<UpdateLikeRes> response) {
                if (response.code() == 200) {
                    runOperation(new UpdateLikeOperation(position,
                            mUsers.get(position).getRemoteId(), response.body().getProfileValues()));
                } else {
                    showMessage("Не удалось анлайкнуть (" + response.code() + ")");
                    Log.d(TAG, "onResponse: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<UpdateLikeRes> call, Throwable t) {
                showMessage("Не удалось анлайкнуть");
            }
        });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    @SuppressWarnings("unused")
    public void updateLike(UpdateLikeEvent event) {
        if (event.getPosition() != -1) {
            mUsers.get(event.getPosition()).setLikesBy(StringUtils.listToStr(event.getLikesBy()));
            mUsersAdapter.notifyItemChanged(event.getPosition(), event.getLikesBy());
        } else {
            mUsersAdapter.notifyDataSetChanged();
        }
    }
}
