package com.softdesign.devintensive.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.softdesign.devintensive.R;
import com.softdesign.devintensive.data.storage.models.User;
import com.softdesign.devintensive.data.storage.operations.ShowLikesOperation;
import com.softdesign.devintensive.ui.adapters.SimpleUserListAdapter;
import com.softdesign.devintensive.utils.ConstantManager;

import butterknife.BindView;
import butterknife.ButterKnife;

public class LikesActivity extends BaseActivity {

    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_likes);
        ButterKnife.bind(this);
        setupToolbar();
        showProgress();
        runOperation(new ShowLikesOperation(getIntent().getStringExtra(ConstantManager.REMOTE_ID)));
    }

    private void setupToolbar() {
        setSupportActionBar(mToolbar);
        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @SuppressWarnings("unused")
    public void onOperationFinished(ShowLikesOperation.Result result) {
        hideProgress();
        if (result.isSuccessful()) {
            ListView likesList = (ListView) findViewById(R.id.likes_list);
            SimpleUserListAdapter adapter = new SimpleUserListAdapter(this, result.getOutput());
            likesList.setAdapter(adapter);
            likesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    User user = (User) adapterView.getItemAtPosition(i);
                    Intent profileIntent = new Intent(LikesActivity.this, ProfileUserActivity.class);
                    profileIntent.putExtra(ConstantManager.REMOTE_ID, user.getRemoteId());
                    startActivity(profileIntent);
                }
            });
        } else {
            if (result.getException() != null) {
                result.getException().printStackTrace();
            }
        }
    }
}
