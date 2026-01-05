package com.jtkj.library.widget.international;

import android.view.View;
import android.view.ViewGroup;


public class ViewUtil {

    public static void updateViewLanguage(View view) {
        if (view instanceof ViewGroup) {
            ViewGroup vg = (ViewGroup) view;
            int count = vg.getChildCount();
            for (int i = 0; i < count; i++) {
                updateViewLanguage(vg.getChildAt(i));
            }
        } else if (view instanceof LanguageView) {
            LanguageView tv = (LanguageView) view;
            tv.reLoadLanguage();
        }
    }
}
