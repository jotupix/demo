package com.jtkj.demo.base;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import com.jtkj.demo.R;
import com.jtkj.library.widget.international.AppTextView;

public class NoticeToast  {

    public static void showCustomToast(Context context, String message) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View layout = inflater.inflate(R.layout.notice_toast_layout, null);

        AppTextView text = layout.findViewById(R.id.toast_text);
        text.setText(message);

        Toast toast = new Toast(context);
        toast.setDuration(Toast.LENGTH_SHORT); // 或 Toast.LENGTH_LONG
        toast.setView(layout);
        toast.setGravity(Gravity.CENTER, 0, 0); // 居中显示，可调节偏移
        toast.show();
    }

    public static void showCustomToast(Context context, int message) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View layout = inflater.inflate(R.layout.notice_toast_layout, null);

        AppTextView text = layout.findViewById(R.id.toast_text);
        text.setTextById(message);

        Toast toast = new Toast(context);
        toast.setDuration(Toast.LENGTH_SHORT); // 或 Toast.LENGTH_LONG
        toast.setView(layout);
        toast.setGravity(Gravity.CENTER, 0, 0); // 居中显示，可调节偏移
        toast.show();
    }

}
