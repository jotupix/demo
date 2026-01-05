package com.jtkj.library.commom.recyclerview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import com.jtkj.library.R;


public class FooterLoadMoreView extends FrameLayout {

    private Status mStatus;

    private View mLoadingView;

    private View mErrorView;

    private View mTheEndView;

    private OnRetryListener mOnRetryListener;

    public FooterLoadMoreView(Context context) {
        this(context, null);
    }

    public FooterLoadMoreView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FooterLoadMoreView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        LayoutInflater.from(context).inflate(R.layout.recycler_foot_load_more_view, this, true);

        mLoadingView = findViewById(R.id.loadingView);
        mErrorView = findViewById(R.id.errorView);
        mTheEndView = findViewById(R.id.theEndView);

        mErrorView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnRetryListener != null) {
                    mOnRetryListener.onRetry(FooterLoadMoreView.this);
                }
            }
        });

        setStatus(Status.GONE);
    }

    public void setOnRetryListener(OnRetryListener listener) {
        this.mOnRetryListener = listener;
    }

    public Status getStatus() {
        return mStatus;
    }

    public void setStatus(Status status) {
        this.mStatus = status;
        change();
    }

    public boolean isLoading() {
        return mStatus == Status.LOADING;
    }

    public boolean canLoadMore() {
        return mStatus == Status.GONE || mStatus == Status.ERROR;
    }

    private void change() {
        switch (mStatus) {
            case GONE:
                mLoadingView.setVisibility(GONE);
                mErrorView.setVisibility(GONE);
                mTheEndView.setVisibility(GONE);
                break;

            case LOADING:
                mLoadingView.setVisibility(VISIBLE);
                mErrorView.setVisibility(GONE);
                mTheEndView.setVisibility(GONE);
                break;

            case ERROR:
                mLoadingView.setVisibility(GONE);
                mErrorView.setVisibility(VISIBLE);
                mTheEndView.setVisibility(GONE);
                break;

            case THE_END:
                mLoadingView.setVisibility(GONE);
                mErrorView.setVisibility(GONE);
                mTheEndView.setVisibility(VISIBLE);
                break;
        }
    }

    public enum Status {
        GONE, LOADING, ERROR, THE_END
    }

    public interface OnRetryListener {
        void onRetry(FooterLoadMoreView view);
    }

}
