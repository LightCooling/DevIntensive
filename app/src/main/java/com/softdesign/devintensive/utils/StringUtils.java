package com.softdesign.devintensive.utils;

import android.support.annotation.NonNull;

import java.util.Arrays;
import java.util.List;

public class StringUtils {
    @NonNull
    public static List<String> strToList(String str) {
        return Arrays.asList(str.split(","));
    }

    public static String listToStr(List<String> list) {
        if (list.size() == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();

        for(String s: list) {
            sb.append(s).append(',');
        }
        sb.deleteCharAt(sb.length()-1);

        return sb.toString();
    }
}
