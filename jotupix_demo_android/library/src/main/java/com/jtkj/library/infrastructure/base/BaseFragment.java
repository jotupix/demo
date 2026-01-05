package com.jtkj.library.infrastructure.base;

import android.app.ProgressDialog;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Button;

import com.jtkj.library.R;
import com.jtkj.library.commom.logger.CLog;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by yfxiong on 2017/5/3.
 * Copyright  yfxiong www.jotus-tech.com. All Rights Reserved.
 */
public class BaseFragment extends Fragment {
	private static final String TAG = "BaseFragment";

	private View mProgressContainer;
	private ViewGroup mContentContainer;
	private View mErrNetworkContainer;
	private View mErrContentContainer;
	private View mLoginContainer;

//	private ImageView mLoading;

	private int mState = STATE_DISPLAYING;

	protected static final int STATE_LOADING = 1;
	protected static final int STATE_ERR_NETWORK = 2;
	protected static final int STATE_ERR_CONTENT = 3;
	protected static final int STATE_DISPLAYING = 4;
	protected static final int STATE_LOGIN = 5;
	protected static final int STATE_NONE = 0;

	protected ParallelismMachine mParallelism;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View root = inflater.inflate(R.layout.fragment_base, container, false);
		initializeView(root);

		View contentView = onCreateContentView(inflater, mContentContainer, savedInstanceState);
		if (contentView != null) {
			if (contentView.getParent() != null) {
				ViewGroup parent = (ViewGroup) contentView.getParent();
				parent.removeView(contentView);
			}
			mContentContainer.addView(contentView);
		}
		return root;
	}

	protected View onCreateContentView(LayoutInflater inflater, ViewGroup contentContainer, Bundle savedInstanceState) {
		return null;
	}

	private void initializeView(View root) {
		mProgressContainer = root.findViewById(R.id.progress_container);
		mContentContainer = (ViewGroup) root.findViewById(R.id.content_container);
		mErrNetworkContainer = root.findViewById(R.id.network_err_container);
		mErrContentContainer = root.findViewById(R.id.content_err_container);
		mLoginContainer = root.findViewById(R.id.content_login_container);

//		mLoading = (ImageView) root.findViewById(R.id.mho_loading);

		Button retryNet = (Button) root.findViewById(R.id.btn_retry_network);
		Button retryLoad = (Button) root.findViewById(R.id.btn_retry_content);

		View.OnClickListener onRetry = new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				retryReqData();
			}
		};
		retryNet.setOnClickListener(onRetry);
		retryLoad.setOnClickListener(onRetry);
	}

	/**it will be call by retry button when showing the no network view or error view*/
	protected void retryReqData() {
		if (mState == STATE_LOADING) return;
		moveToState(STATE_LOADING, false);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		CLog.d(TAG, "onCreate: " + this.getClass().getSimpleName());

		mParallelism = new ParallelismMachine();

		if (savedInstanceState != null) {
			final int state = savedInstanceState.getInt("mState");
			moveToState(state, false);
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt("mState", mState);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		setState(mState);
	}

	@Override
	public void onDestroyView() {
		mContentContainer.removeAllViews();
		mProgressContainer = mContentContainer = null;
		mErrContentContainer = mErrNetworkContainer = null;
		mLoginContainer = null;
		super.onDestroyView();
	}

	public void moveToState(int state) {
		moveToState(state, true);
	}

	protected void moveToState(int state, boolean animate) {
		if (mContentContainer == null) {
			mState = state;
			return;
		}

		if (mState == state) {
			return;
		}

		final View from = getContainerForState(mState);
		final View to = getContainerForState(state);

		if (animate) {
			from.startAnimation(AnimationUtils.loadAnimation(getActivity(), android.R.anim.fade_out));
			to.startAnimation(AnimationUtils.loadAnimation(getActivity(), android.R.anim.fade_in));
		} else {
			from.clearAnimation();
			to.clearAnimation();
		}

		setState(state);

//		if (state == STATE_LOADING && mLoading != null) {
//			AnimationDrawable anim = (AnimationDrawable) mLoading.getDrawable();
//			anim.start();
//		}
//		if (state != STATE_LOADING && mLoading != null) {
//			AnimationDrawable anim = (AnimationDrawable) mLoading.getDrawable();
//			if (anim.isRunning())
//				anim.stop();
//		}
	}

	protected void setState(int state) {
		if (mContentContainer != null) {
			mProgressContainer.setVisibility(View.GONE);
			mErrContentContainer.setVisibility(View.GONE);
			mErrNetworkContainer.setVisibility(View.GONE);
			mLoginContainer.setVisibility(View.GONE);
			mContentContainer.setVisibility(View.GONE);

			View v = getContainerForState(state);
			if (v != null)
				v.setVisibility(View.VISIBLE);
		}
		mState = state;
	}

	private View getContainerForState(int state) {
		switch (state) {
			case STATE_LOADING:
				return mProgressContainer;
			case STATE_ERR_NETWORK:
				return mErrNetworkContainer;
			case STATE_ERR_CONTENT:
				return mErrContentContainer;
			case STATE_LOGIN:
				return mLoginContainer;
			case STATE_DISPLAYING:
				return mContentContainer;
			default:
				return null;
		}
	}

	@Override
	public void onResume() {
		super.onResume();

		setState(mState);
	}

	@Override
	public void setUserVisibleHint(boolean isVisibleToUser) {
		super.setUserVisibleHint(isVisibleToUser);
	}

	/**并行request状态处理器*/
	public class ParallelismMachine {
		private int poolIdx;
		private Map<Integer, Boolean> status;
		private boolean bad;

		public ParallelismMachine() {
			init();
		}
		public int add() {
			status.put(poolIdx, false);
			return poolIdx++;
		}
		public void remove(int idx) {
			status.remove(idx);
		}
		public void bad() {
			bad = true;
		}
		public boolean allDone() {
			return status.isEmpty() && !bad;
		}
		public void init() {
			poolIdx = 0;
			status = new HashMap<Integer, Boolean>();
			bad = false;
		}
	}

	private ProgressDialog mProgressDialog;
}
