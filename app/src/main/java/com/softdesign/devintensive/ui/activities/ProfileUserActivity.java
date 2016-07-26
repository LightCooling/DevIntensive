package com.softdesign.devintensive.ui.activities;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.softdesign.devintensive.R;
import com.softdesign.devintensive.data.managers.DataManager;
import com.softdesign.devintensive.data.network.res.UpdateLikeRes;
import com.softdesign.devintensive.data.storage.models.Repository;
import com.softdesign.devintensive.data.storage.models.User;
import com.softdesign.devintensive.data.storage.operations.ShowProfileOperation;
import com.softdesign.devintensive.data.storage.operations.UpdateLikeOperation;
import com.softdesign.devintensive.ui.adapters.RepositoriesAdapter;
import com.softdesign.devintensive.utils.ConstantManager;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileUserActivity extends BaseActivity implements View.OnClickListener {

    static final String TAG = "ProfileUserActivity";

    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.user_photo_img)
    ImageView mProfileImage;
    @BindView(R.id.bio_txt)
    TextView mUserBio;
    @BindView(R.id.stats_rating)
    TextView mUserRating;
    @BindView(R.id.stats_lines)
    TextView mUserCodeLines;
    @BindView(R.id.stats_projects)
    TextView mUserProjects;
    @BindView(R.id.likes_txt)
    TextView mUserLikes;
    @BindView(R.id.likes_action)
    ImageView mLikesAction;
    @BindView(R.id.fab)
    FloatingActionButton mFab;
    @BindView(R.id.collapsing_toolbar)
    CollapsingToolbarLayout mCollapsingToolbarLayout;
    @BindView(R.id.coordinator_layout)
    CoordinatorLayout mCoordinatorLayout;

    @BindView(R.id.repositories_list)
    ListView mRepoListView;

    private boolean mLiked;

    private String mRemoteId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_user);
        ButterKnife.bind(this);

        setupToolbar();
        showProgress();
        mRemoteId = getIntent().getStringExtra(ConstantManager.REMOTE_ID);
        runOperation(new ShowProfileOperation(mRemoteId));
    }

    @Override
    @OnClick({R.id.fab, R.id.likes_action})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.fab:
                doLike(!mLiked);
                break;
            case R.id.likes_action:
                Intent likesIntent = new Intent(this, LikesActivity.class);
                likesIntent.putExtra(ConstantManager.REMOTE_ID, mRemoteId);
                startActivity(likesIntent);
                break;
        }
    }

    private void showMessage(String msg) {
        Snackbar.make(mCoordinatorLayout, msg, Snackbar.LENGTH_SHORT).show();
    }

    private void setupToolbar() {
        setSupportActionBar(mToolbar);
        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @SuppressWarnings("unused")
    public void onOperationFinished(ShowProfileOperation.Result result) {
        hideProgress();
        if (result.isSuccessful()) {
            User user = result.getOutput();

            mRemoteId = user.getRemoteId();

            final List<String> repositories = new ArrayList<>();
            for (Repository rep : user.getRepositories()) {
                repositories.add(rep.getRepositoryName());
            }

            RepositoriesAdapter repositoriesAdapter = new RepositoriesAdapter(this, repositories);
            mRepoListView.setAdapter(repositoriesAdapter);
            ViewGroup.LayoutParams params = mRepoListView.getLayoutParams();
            mRepoListView.measure(0, 0);
            params.height = mRepoListView.getMeasuredHeight() * repositoriesAdapter.getCount() +
                    (mRepoListView.getDividerHeight() * (repositoriesAdapter.getCount() - 1));
            mRepoListView.setLayoutParams(params);

            mRepoListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    TextView gitTextView = (TextView) adapterView.findViewById(R.id.github_txt);
                    Intent makeCallIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https:" + gitTextView.getText()));
                    startActivity(makeCallIntent);
                }
            });

            mUserBio.setText(user.getBio());
            mUserRating.setText(String.valueOf(user.getRating()));
            mUserCodeLines.setText(String.valueOf(user.getCodeLines()));
            mUserProjects.setText(String.valueOf(user.getProjects()));
            if (user.getLikes() == 0) {
                mUserLikes.setText(getResources().getString(R.string.profile_like_no_likes));
                mLikesAction.setClickable(false);
            } else {
                String format = getResources().getString(R.string.profile_like_count);
                mUserLikes.setText(String.format(format, user.getLikes()));
                mLikesAction.setClickable(true);
            }
            setLiked(user.getLikesBy().contains(DataManager.getInstance().getPreferencesManager().getUserId()));

            mCollapsingToolbarLayout.setTitle(user.getFullName());

            Picasso.with(this)
                    .load(user.getPhoto())
                    .placeholder(R.drawable.user_bg)
                    .error(R.drawable.user_bg)
                    .into(mProfileImage);
            doLike(mLiked);
        } else {
            showMessage("Невозможно загрузить профиль");
            if (result.getException() != null) {
                result.getException().printStackTrace();
            }
        }
    }

    private void doLike(boolean liked) {
        if (liked) {
            Call<UpdateLikeRes> call = DataManager.getInstance().likeUser(mRemoteId);
            call.enqueue(new Callback<UpdateLikeRes>() {
                @Override
                public void onResponse(Call<UpdateLikeRes> call, Response<UpdateLikeRes> response) {
                    if (response.code() == 200) {
                        runOperation(new UpdateLikeOperation(mRemoteId, response.body().getProfileValues()));
                    } else {
                        showMessage("Не удалось лайкнуть (" + response.code() + ")");
                        Log.d(TAG, "onResponse: " + response.code());
                    }
                }

                @Override
                public void onFailure(Call<UpdateLikeRes> call, Throwable t) {
                    showMessage("Не удалось лайкнуть");
                }
            });
        } else {
            Call<UpdateLikeRes> call = DataManager.getInstance().unlikeUser(mRemoteId);
            call.enqueue(new Callback<UpdateLikeRes>() {
                @Override
                public void onResponse(Call<UpdateLikeRes> call, Response<UpdateLikeRes> response) {
                    if (response.code() == 200) {
                        runOperation(new UpdateLikeOperation(mRemoteId, response.body().getProfileValues()));
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
    }

    private void setLiked(boolean liked) {
        if (liked) {
            Drawable src = getResources().getDrawable(R.drawable.ic_like);
            Drawable colorized = src.getConstantState().newDrawable();
            colorized.mutate().setColorFilter(getResources().getColor(R.color.like), PorterDuff.Mode.SRC_ATOP);
            mFab.setImageDrawable(colorized);
            mLiked = true;
        } else {
            Drawable src = getResources().getDrawable(R.drawable.ic_like);
            Drawable colorized = src.getConstantState().newDrawable();
            colorized.mutate().setColorFilter(getResources().getColor(R.color.grey_light), PorterDuff.Mode.SRC_ATOP);
            mFab.setImageDrawable(colorized);
            mLiked = false;
        }
    }

    @SuppressWarnings("unused")
    public void onOperationFinished(UpdateLikeOperation.Result result) {
        if (result.isSuccessful()) {
            if (result.getOutput().size() == 0) {
                mUserLikes.setText(getResources().getString(R.string.profile_like_no_likes));
                mLikesAction.setClickable(false);
                setLiked(false);
            } else {
                String format = getResources().getString(R.string.profile_like_count);
                mUserLikes.setText(String.format(format, result.getOutput().size()));
                mLikesAction.setClickable(true);
                setLiked(result.getOutput()
                        .contains(DataManager.getInstance().getPreferencesManager().getUserId()));
            }
        } else {
            showMessage("С лайками что-то не так");
            if (result.getException() != null) {
                result.getException().printStackTrace();
            }
        }
    }
}
