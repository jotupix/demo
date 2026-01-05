package com.jtkj.library.commom.tools;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

public class SoftKeyboardUtils {

    /***
     * 打开或者关闭软键盘
     * @param activity
     */
    public static void hideOrOpenInput(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
    }


    /***
     * 使用View 打开软键盘 View 必须获得焦点
     * @param view
     */
    public static void showKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            view.requestFocus();
            imm.showSoftInput(view, 0);
        }
    }

    /***
     * 使用View关闭软键盘 Biew必须获得焦点
     * @param view
     */
    public static void closeKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    /***
     * 使用Activity打开软键盘  Activity之内必须有获得焦点的View
     * @param activity
     */
    public static void showKeyboard(Activity activity) {
        if (activity != null) {
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            if (!imm.isActive()) {
                View view = activity.getCurrentFocus();
                if (null == view) {
                    imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                } else {
                    imm.showSoftInputFromInputMethod(activity.getCurrentFocus().getWindowToken(), 0);
                }
            }
        }
    }

    /**
     * 隐藏软键盘
     *
     * @param activity
     */
    public static void hideKeyboard(Activity activity) {
        if (activity != null) {
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm.isActive()) {
                View v = activity.getCurrentFocus();
                if (v != null) {
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                } else {
                    imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                }
            }
        }
    }

}
