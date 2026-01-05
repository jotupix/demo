package com.jtkj.demo.base;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Window;
import android.view.WindowManager;

import com.jtkj.demo.databinding.TipsDialogBinding;

import androidx.annotation.NonNull;

public class TipsDialog extends Dialog {
    TipsDialogBinding mViewBinding;
    int tipsId;

    public static void showDialog(Context context, int id) {
        new TipsDialog(context, id).show();
    }

    public TipsDialog(@NonNull Context context, int id) {
        super(context);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        tipsId = id;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mViewBinding = TipsDialogBinding.inflate(LayoutInflater.from(getContext()));
        setContentView(mViewBinding.getRoot());
        initWindowParams();
        setCancelable(true);
        setCanceledOnTouchOutside(true);
        mViewBinding.tipsTv.setTextById(tipsId);
    }

    private void initWindowParams() {
        Window window = getWindow();
        if (window != null) {
            window.setBackgroundDrawableResource(android.R.color.transparent);
            window.setGravity(Gravity.CENTER);
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        }
    }
}
