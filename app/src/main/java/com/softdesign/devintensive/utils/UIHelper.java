package com.softdesign.devintensive.utils;

import android.content.Context;
import android.util.Log;
import android.util.TypedValue;

public class UIHelper {

    private static Context mContext = DevIntensiveApplication.getContext();

    public static int getStatusBarHeight() {
        int result = 0;
        if (mContext == null)
            Log.d("Devint", "context");
        if (mContext.getResources() == null)
            Log.d("Devint", "getResources");
        int resourceId = mContext.getResources().getIdentifier("status_bar_height", "dimen", "android");

        if (resourceId > 0) {
            result = mContext.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    public static int getActionbarHeight() {
        int result = 0;
        TypedValue tv = new TypedValue();
        if (mContext.getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
            result = TypedValue.complexToDimensionPixelSize(tv.data, mContext.getResources().getDisplayMetrics());
        }
        return result;
    }

    public static int lerp(int start, int end, float factor) {
        return (int) (start + (end - start) * factor);
    }

    public static float getFactor(int start, int end, int current) {
        return (float) (current - start) / (end  - start);
    }
}
