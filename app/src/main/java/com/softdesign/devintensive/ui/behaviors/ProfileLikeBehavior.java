package com.softdesign.devintensive.ui.behaviors;

import android.content.Context;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import com.softdesign.devintensive.R;
import com.softdesign.devintensive.utils.UIHelper;

@SuppressWarnings("unused")
public class ProfileLikeBehavior extends AppBarLayout.ScrollingViewBehavior {

    private Context mContext;

    private final int mMinAppbarHeight;
    private final int mMaxAppbarHeight;
    private final int mFabSize;
    private final int mBaseMargin;

    public ProfileLikeBehavior (Context context, AttributeSet attrs) {
        mContext = context;

        mMinAppbarHeight = UIHelper.getStatusBarHeight() + UIHelper.getActionbarHeight();
        mMaxAppbarHeight = context.getResources().getDimensionPixelSize(R.dimen.profile_image_size);
        mFabSize = context.getResources().getDimensionPixelSize(R.dimen.fab);
        mBaseMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16,
                context.getResources().getDisplayMetrics());
    }

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, View child, View dependency) {
        return dependency instanceof AppBarLayout;
    }

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, View child, View dependency) {
        float currentFactor = UIHelper.getFactor(mMinAppbarHeight, mMaxAppbarHeight, dependency.getBottom());
        if (currentFactor <= 0.8 && child.getVisibility() == View.VISIBLE) {
            ((FloatingActionButton) child).hide();
            return true;
        } else if (currentFactor > 0.8) {
            if (child.getVisibility() == View.GONE) {
                ((FloatingActionButton) child).show();
                return true;
            } else if (child.getVisibility() == View.VISIBLE){
                float fixedFactor = 1 - (1 - currentFactor) * 2;
                int currentHeight = UIHelper.lerp(0, mFabSize, fixedFactor);
                int currentMargin = UIHelper.lerp(0, mFabSize/2, 1 - fixedFactor) + mBaseMargin;

                CoordinatorLayout.LayoutParams lp = (CoordinatorLayout.LayoutParams) child.getLayoutParams();
                lp.height = currentHeight;
                lp.rightMargin = currentMargin;
                child.setLayoutParams(lp);
                return true;
            }
        }
        return false;
    }
}
