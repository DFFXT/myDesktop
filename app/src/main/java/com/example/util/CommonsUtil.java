package com.example.util;

import android.support.annotation.StringRes;

import org.litepal.LitePalApplication;

public final class CommonsUtil {
    public static String getString(@StringRes int id){
        return LitePalApplication.getContext().getResources().getString(id);
    }
}
