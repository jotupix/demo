package com.jtkj.demo.base;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.jtkj.demo.CoolLED;
import com.jtkj.demo.databinding.NoticeDialogBinding;

import androidx.annotation.NonNull;

public class NoticeDialog extends Dialog {

    public interface OnCancelListener {
        void onCancelClick(NoticeDialog dialog);
    }

    public interface OnConfirmListener {
        void onConfirmClick(NoticeDialog dialog);
    }

    NoticeDialogBinding mViewBinding;

    String mTitle;
    int mTitleId;
    String mNoticeContent;
    int mNoticeId;
    String mCancelString;
    String mConfirmString;
    boolean isCancelAble;

    OnCancelListener mOnCancelListener;
    OnConfirmListener mOnConfirmListener;
    boolean isCancelShow = true;
    boolean isConfirmShow = true;

    public static NoticeDialog showDialog(Context context, int content, boolean isCancelShowFlag, boolean isConfirmShowFlag) {
        NoticeDialog noticeDialog = new NoticeDialog(context, content, true, isCancelShowFlag, isConfirmShowFlag, null, null, null, null);
        noticeDialog.show();
        return noticeDialog;
    }

    public static NoticeDialog showDialog(Context context, int content, boolean isCancelShowFlag, boolean isConfirmShowFlag,OnConfirmListener onPositiveListener) {
        NoticeDialog noticeDialog = new NoticeDialog(context, content, true, isCancelShowFlag, isConfirmShowFlag, null, null, null, onPositiveListener);
        noticeDialog.show();
        return noticeDialog;
    }

    public static NoticeDialog showDialog(Context context, int titleId, int content, OnConfirmListener onPositiveListener) {
        NoticeDialog noticeDialog = new NoticeDialog(context, titleId, content, true, false, true, null, null, null, onPositiveListener);
        noticeDialog.show();
        return noticeDialog;
    }

    public static NoticeDialog showDialog(Context context, String title, int content, OnConfirmListener onPositiveListener) {
        NoticeDialog noticeDialog = new NoticeDialog(context, title, content, true, false, true, null, null, null, onPositiveListener);
        noticeDialog.show();
        return noticeDialog;
    }

    public static NoticeDialog showDialog(Context context, int content, boolean canCancel) {
        NoticeDialog noticeDialog = new NoticeDialog(context, CoolLED.getInstance().string(content), canCancel, null, null, null, null);
        noticeDialog.show();
        return noticeDialog;
    }

    public static NoticeDialog showDialog(Context context, String content, boolean canCancel) {
        NoticeDialog noticeDialog = new NoticeDialog(context, content, canCancel, null, null, null, null);
        noticeDialog.show();
        return noticeDialog;
    }

    public static NoticeDialog showDialog(Context context, int content) {
        NoticeDialog noticeDialog = new NoticeDialog(context, content, true, null, null, null, null);
        noticeDialog.show();
        return noticeDialog;
    }

    public static NoticeDialog showDialog(Context context, String content) {
        NoticeDialog noticeDialog = new NoticeDialog(context, content, true, null, null, null, null);
        noticeDialog.show();
        return noticeDialog;
    }

    public static NoticeDialog showDialog(Context context, int content, boolean canCancel,
                                          String cancelString, String confirmString, OnCancelListener onNegativeListener, OnConfirmListener onPositiveListener) {
        NoticeDialog noticeDialog = new NoticeDialog(context, CoolLED.getInstance().string(content), canCancel, cancelString, confirmString, onNegativeListener, onPositiveListener);
        noticeDialog.show();
        return noticeDialog;
    }

    public static NoticeDialog showDialog(Context context, int content, boolean canCancel,
                                          OnCancelListener onNegativeListener, OnConfirmListener onPositiveListener) {
        NoticeDialog noticeDialog = new NoticeDialog(context, content, canCancel,
                null, null, onNegativeListener, onPositiveListener);
        noticeDialog.show();
        return noticeDialog;
    }

    public static NoticeDialog showDialog(Context context, String content, boolean canCancel,
                                          String cancelString, String confirmString, OnCancelListener onNegativeListener, OnConfirmListener onPositiveListener) {
        NoticeDialog noticeDialog = new NoticeDialog(context, content, canCancel, cancelString, confirmString, onNegativeListener, onPositiveListener);
        noticeDialog.show();
        return noticeDialog;
    }

    public static NoticeDialog showDialog(Context context, String content, boolean canCancel,
                                          OnCancelListener onNegativeListener, OnConfirmListener onPositiveListener) {
        NoticeDialog noticeDialog = new NoticeDialog(context, content, canCancel,
                null, null, onNegativeListener, onPositiveListener);
        noticeDialog.show();
        return noticeDialog;
    }

    public static NoticeDialog showDialog(Context context, int content,
                                          String cancelString, String confirmString, OnCancelListener onNegativeListener, OnConfirmListener onPositiveListener) {
        NoticeDialog noticeDialog = new NoticeDialog(context, CoolLED.getInstance().string(content), true, cancelString, confirmString, onNegativeListener, onPositiveListener);
        noticeDialog.show();
        return noticeDialog;
    }

    public static NoticeDialog showDialog(Context context, int content,
                                          OnCancelListener onNegativeListener, OnConfirmListener onPositiveListener) {
        NoticeDialog noticeDialog = new NoticeDialog(context, CoolLED.getInstance().string(content),
                true, null, null, onNegativeListener, onPositiveListener);
        noticeDialog.show();
        return noticeDialog;
    }

    public static NoticeDialog showDialog(Context context, int content,
                                          OnCancelListener onNegativeListener, OnConfirmListener onPositiveListener, boolean cancelAble) {
        NoticeDialog noticeDialog = new NoticeDialog(context, CoolLED.getInstance().string(content),
                cancelAble, null, null, onNegativeListener, onPositiveListener);
        noticeDialog.show();
        return noticeDialog;
    }

    public static NoticeDialog showDialog(Context context, String content,
                                          String cancelString, String confirmString, OnCancelListener onNegativeListener, OnConfirmListener onPositiveListener) {
        NoticeDialog noticeDialog = new NoticeDialog(context, content, true, cancelString, confirmString, onNegativeListener, onPositiveListener);
        noticeDialog.show();
        return noticeDialog;
    }

    public static NoticeDialog showDialog(Context context, String content,
                                          OnCancelListener onNegativeListener, OnConfirmListener onPositiveListener) {
        NoticeDialog noticeDialog = new NoticeDialog(context, content, true,
                null, null, onNegativeListener, onPositiveListener);
        noticeDialog.show();
        return noticeDialog;
    }

    public static NoticeDialog showDialog(Context context, int content, boolean canCancel,
                                          String confirmString, OnConfirmListener onPositiveListener) {
        NoticeDialog noticeDialog = new NoticeDialog(context, CoolLED.getInstance().string(content), canCancel, null, confirmString, null, onPositiveListener);
        noticeDialog.show();
        return noticeDialog;
    }

    public static NoticeDialog showDialog(Context context, String content, boolean canCancel,
                                          String confirmString, OnConfirmListener onPositiveListener) {
        NoticeDialog noticeDialog = new NoticeDialog(context, content, canCancel, null, confirmString, null, onPositiveListener);
        noticeDialog.show();
        return noticeDialog;
    }

    public static NoticeDialog showDialog(Context context, String content, boolean canCancel,
                                          OnConfirmListener onPositiveListener) {
        NoticeDialog noticeDialog = new NoticeDialog(context, content, canCancel, null, null, null, onPositiveListener);
        noticeDialog.show();
        return noticeDialog;
    }

    public static NoticeDialog showDialog(Context context, int content,
                                          String confirmString, OnConfirmListener onPositiveListener) {
        NoticeDialog noticeDialog = new NoticeDialog(context, CoolLED.getInstance().string(content), true, null, confirmString, null, onPositiveListener);
        noticeDialog.show();
        return noticeDialog;
    }

    public static NoticeDialog showDialog(Context context, int content,
                                          OnConfirmListener onPositiveListener) {
        NoticeDialog noticeDialog = new NoticeDialog(context, CoolLED.getInstance().string(content), true, null, null, null, onPositiveListener);
        noticeDialog.show();
        return noticeDialog;
    }

    public static NoticeDialog showDialog(Context context, String content,
                                          String confirmString, OnConfirmListener onPositiveListener) {
        NoticeDialog noticeDialog = new NoticeDialog(context, content, true, null, confirmString, null, onPositiveListener);
        noticeDialog.show();
        return noticeDialog;
    }

    public NoticeDialog(@NonNull Context context, String content, boolean canCancel,
                        String cancelString, String confirmString, OnCancelListener onNegativeListener, OnConfirmListener onPositiveListener) {
        super(context);
        mNoticeContent = content;
        isCancelAble = canCancel;
        mCancelString = cancelString;
        mConfirmString = confirmString;
        mOnCancelListener = onNegativeListener;
        mOnConfirmListener = onPositiveListener;
    }

    public NoticeDialog(@NonNull Context context, int content, boolean canCancel, boolean isCancelShowFlag, boolean isConfirmShowFlag,
                        String cancelString, String confirmString, OnCancelListener onNegativeListener, OnConfirmListener onPositiveListener) {
        super(context);
        mNoticeId = content;
        isCancelAble = canCancel;
        isCancelShow = isCancelShowFlag;
        isConfirmShow = isConfirmShowFlag;
        mCancelString = cancelString;
        mConfirmString = confirmString;
        mOnCancelListener = onNegativeListener;
        mOnConfirmListener = onPositiveListener;
    }

    public NoticeDialog(@NonNull Context context, int titleId, int content, boolean canCancel, boolean isCancelShowFlag, boolean isConfirmShowFlag,
                        String cancelString, String confirmString, OnCancelListener onNegativeListener, OnConfirmListener onPositiveListener) {
        super(context);
        mTitleId = titleId;
        mNoticeId = content;
        isCancelAble = canCancel;
        isCancelShow = isCancelShowFlag;
        isConfirmShow = isConfirmShowFlag;
        mCancelString = cancelString;
        mConfirmString = confirmString;
        mOnCancelListener = onNegativeListener;
        mOnConfirmListener = onPositiveListener;
    }

    public NoticeDialog(@NonNull Context context, String title, int content, boolean canCancel, boolean isCancelShowFlag, boolean isConfirmShowFlag,
                        String cancelString, String confirmString, OnCancelListener onNegativeListener, OnConfirmListener onPositiveListener) {
        super(context);
        mTitle = title;
        mNoticeId = content;
        isCancelAble = canCancel;
        isCancelShow = isCancelShowFlag;
        isConfirmShow = isConfirmShowFlag;
        mCancelString = cancelString;
        mConfirmString = confirmString;
        mOnCancelListener = onNegativeListener;
        mOnConfirmListener = onPositiveListener;
    }

    public NoticeDialog(@NonNull Context context, int content, boolean canCancel,
                        String cancelString, String confirmString, OnCancelListener onNegativeListener, OnConfirmListener onPositiveListener) {
        super(context);
        mNoticeId = content;
        isCancelAble = canCancel;
        mCancelString = cancelString;
        mConfirmString = confirmString;
        mOnCancelListener = onNegativeListener;
        mOnConfirmListener = onPositiveListener;
    }

    public NoticeDialog(@NonNull Context context, int titleId, int content, boolean canCancel,
                        String cancelString, String confirmString, OnCancelListener onNegativeListener, OnConfirmListener onPositiveListener) {
        super(context);
        mTitleId = titleId;
        mNoticeId = content;
        isCancelAble = canCancel;
        mCancelString = cancelString;
        mConfirmString = confirmString;
        mOnCancelListener = onNegativeListener;
        mOnConfirmListener = onPositiveListener;
    }

    public NoticeDialog(@NonNull Context context, String title, int content, boolean canCancel,
                        String cancelString, String confirmString, OnCancelListener onNegativeListener, OnConfirmListener onPositiveListener) {
        super(context);
        mTitle = title;
        mNoticeId = content;
        isCancelAble = canCancel;
        mCancelString = cancelString;
        mConfirmString = confirmString;
        mOnCancelListener = onNegativeListener;
        mOnConfirmListener = onPositiveListener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        mViewBinding = NoticeDialogBinding.inflate(LayoutInflater.from(getContext()));
        setContentView(mViewBinding.getRoot());
        initView();
        initWindowParams();
    }

    private void initView() {
        setCancelable(isCancelAble);
        setCanceledOnTouchOutside(isCancelAble);
        if (!TextUtils.isEmpty(mTitle)) {
            mViewBinding.titleTv.setText(mNoticeContent);
        }

        if (mTitleId > 0) {
            mViewBinding.titleTv.setTextById(mTitleId);
        }

        if (!TextUtils.isEmpty(mNoticeContent)) {
            mViewBinding.contentTv.setText(mNoticeContent);
        }

        if (mNoticeId > 0) {
            mViewBinding.contentTv.setTextById(mNoticeId);
        }

        if (isCancelShow) {
            mViewBinding.cancelBtn.setVisibility(View.VISIBLE);
        } else {
            mViewBinding.cancelBtn.setVisibility(View.GONE);
        }

        if (isConfirmShow) {
            mViewBinding.confirmBtn.setVisibility(View.VISIBLE);
        } else {
            mViewBinding.confirmBtn.setVisibility(View.GONE);
        }

        mViewBinding.cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mOnCancelListener) {
                    mOnCancelListener.onCancelClick(NoticeDialog.this);
                }
                dismiss();
            }
        });

        mViewBinding.confirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mOnConfirmListener) {
                    mOnConfirmListener.onConfirmClick(NoticeDialog.this);
                }
                dismiss();
            }
        });

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
