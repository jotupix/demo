package com.jtkj.demo.device;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.jtkj.demo.R;
import com.jtkj.demo.databinding.ProgressDialogBinding;

import androidx.annotation.NonNull;

public class ProgressDialog extends Dialog {

    public static final int DEVICE_OVERTIME = 1;
    public static final int DEVICE_NOT_CONNECTED = 2;
    public static final int DEVICE_SEND_SUCCESS = 3;
    public static final int DEVICE_SEND_DATA_ERROR = 4;
    public static final int DEVICE_SEND_SEND_ERROR = 5;
    public static final int DEVICE_SEND_DEVICE_ERROR = 6;
    public static final int DEVICE_SEND_UNKNOWN_ERROR = 7;

    public ProgressDialog(@NonNull Context context) {
        super(context);
    }

    String mMessageOne;
    String mMessageTwo;
    boolean isCancelable;
    boolean isCancelableOutside;
    ProgressDialogBinding mBinding;

    public ProgressDialog(@NonNull Context context, String messageOne, String messageTwo, boolean cancelable, boolean cancelableOutside) {
        super(context);
        mMessageOne = messageOne;
        mMessageTwo = messageTwo;
        isCancelable = cancelable;
        isCancelableOutside = cancelableOutside;
    }

    public ProgressDialog(@NonNull Context context, boolean cancelable, boolean cancelableOutside) {
        super(context);
        isCancelable = cancelable;
        isCancelableOutside = cancelableOutside;
    }

    public static ProgressDialog showLoading(@NonNull Context context, String messageOne, String messageTwo, boolean cancelable, boolean cancelableOutside) {
        ProgressDialog dialog = new ProgressDialog(context, messageOne, messageTwo, cancelable, cancelableOutside);
        dialog.show();
        return dialog;
    }

    public static ProgressDialog showLoading(@NonNull Context context, boolean cancelable, boolean cancelableOutside) {
        ProgressDialog dialog = new ProgressDialog(context, cancelable, cancelableOutside);
        dialog.show();
        return dialog;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        mBinding = ProgressDialogBinding.inflate(LayoutInflater.from(getContext()));
        setContentView(mBinding.getRoot());
        initView();
        initWindowParams();
    }

    private void initView() {
        setCancelable(isCancelable);
        setCanceledOnTouchOutside(isCancelableOutside);
        mBinding.programTv.setText(mMessageOne);
        mBinding.progressTv.setText(mMessageTwo);
        mBinding.cancelSendTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DeviceManager.getInstance().getJotuPix().cancelSendProgram();
                dismiss();
            }
        });
    }

    public void showContent() {
        mBinding.startSendLayout.setVisibility(View.GONE);
        mBinding.sendingLayout.setVisibility(View.VISIBLE);
        showLoading(mBinding.sendingIv);
        mBinding.sendSuccessLayout.setVisibility(View.GONE);
        mBinding.sendFailedLayout.setVisibility(View.GONE);
    }

    public void startSend() {
        mBinding.startSendLayout.setVisibility(View.VISIBLE);
        mBinding.sendSuccessLayout.setVisibility(View.GONE);
        mBinding.sendFailedLayout.setVisibility(View.GONE);
        mBinding.sendingLayout.setVisibility(View.GONE);
    }

    public void sendSuccess() {
        mBinding.startSendLayout.setVisibility(View.GONE);
        mBinding.sendSuccessLayout.setVisibility(View.VISIBLE);
        mBinding.sendFailedLayout.setVisibility(View.GONE);
        mBinding.sendingLayout.setVisibility(View.GONE);
    }

    public void sendFailed() {
        mBinding.startSendLayout.setVisibility(View.GONE);
        mBinding.sendSuccessLayout.setVisibility(View.GONE);
        mBinding.sendFailedLayout.setVisibility(View.VISIBLE);
        mBinding.sendingLayout.setVisibility(View.GONE);
    }

    public void sending(String messageOne, String messageTwo) {
        mBinding.startSendLayout.setVisibility(View.GONE);
        if (mBinding.sendingLayout.getVisibility() != View.VISIBLE) {
            mBinding.sendingLayout.setVisibility(View.VISIBLE);
            showLoading(mBinding.sendingIv);
        }
        mBinding.sendSuccessLayout.setVisibility(View.GONE);
        mBinding.sendFailedLayout.setVisibility(View.GONE);
        mBinding.programTv.setText(messageOne);
        mBinding.progressTv.setText(messageTwo);
    }

    public void showLoading(ImageView view) {
        Animation loadAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.connecting);
        view.startAnimation(loadAnimation);
    }

    private void initWindowParams() {
        Window window = getWindow();
        if (window != null) {
            window.setBackgroundDrawableResource(android.R.color.transparent);
            window.setGravity(Gravity.CENTER);
            window.setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
        }
    }

}
