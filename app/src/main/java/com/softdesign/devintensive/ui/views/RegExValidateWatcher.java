package com.softdesign.devintensive.ui.views;

import android.support.design.widget.TextInputLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ImageView;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegExValidateWatcher implements TextWatcher {
    private TextInputLayout mLayout;
    private String mPattern;
    private String mError;

    private ImageView mActionButton;

    public RegExValidateWatcher(String pattern, String errorMsg, TextInputLayout parentLayout, ImageView actionButton) {
        mLayout = parentLayout;
        mPattern = pattern;
        mError = errorMsg;
        mActionButton = actionButton;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {}

    @Override
    public void afterTextChanged(Editable s) {
        Pattern pattern = Pattern.compile(mPattern);
        Matcher matcher = pattern.matcher(s.toString());
        if (matcher.matches()) {
            mActionButton.setEnabled(true);
            mLayout.setError("");
            mLayout.setErrorEnabled(false);
        } else {
            mActionButton.setEnabled(false);
            mLayout.setError(mError);
        }
    }
}
