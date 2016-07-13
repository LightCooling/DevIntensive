package com.softdesign.devintensive.ui.behaviors;

import android.content.Context;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.util.AttributeSet;
import android.view.View;

import com.softdesign.devintensive.R;
import com.softdesign.devintensive.utils.UIHelper;

@SuppressWarnings("unused")
public class CustomNestedScrollBehavior extends AppBarLayout.ScrollingViewBehavior {

    private Context mContext;

    private final int mMinAppbarHeight;
    private final int mMaxAppbarHeight;
    private final int mMinStatsHeight;
    private final int mMaxStatsHeight;

    public CustomNestedScrollBehavior (Context context, AttributeSet attrs) {
        mContext = context;

        mMinAppbarHeight = UIHelper.getStatusBarHeight() + UIHelper.getActionbarHeight();
        mMaxAppbarHeight = context.getResources().getDimensionPixelSize(R.dimen.profile_image_size);
        mMinStatsHeight = context.getResources().getDimensionPixelSize(R.dimen.stats_min_height);
        mMaxStatsHeight = context.getResources().getDimensionPixelSize(R.dimen.stats_height);
    }

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, View child, View dependency) {
        return dependency instanceof AppBarLayout;
    }

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, View child, View dependency) {
        float currentFactor = UIHelper.getFactor(mMinAppbarHeight, mMaxAppbarHeight, dependency.getBottom());
        int currentHeight = UIHelper.lerp(mMinStatsHeight, mMaxStatsHeight, currentFactor);

        CoordinatorLayout.LayoutParams lp = (CoordinatorLayout.LayoutParams) child.getLayoutParams();
        lp.topMargin = currentHeight;
        child.setLayoutParams(lp);
        return true;
    }
}
