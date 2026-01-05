package com.jtkj.library.commom.adapter;

import android.content.Context;
import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jtkj.library.R;
import com.jtkj.library.commom.tools.Toaster;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;

/**
 * Created by yfxiong on 2017/3/16.<br>
 * Copyright © yfxiong www.jotus-tech.com. All Rights Reserved.<br><br>
 */
public class StatesAdapter<V extends RecyclerView> extends RecyclerViewAdapterWrapper {
	private final View vLoadingView;
	private final View vEmptyView;
	private final View vErrorView;
	private V vRecyclerView;

	public interface RetryClickListener {
		void onRetry();
	}

	private RetryClickListener mListener;

	private View.OnClickListener listener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			mListener.onRetry();
		}
	};

	@IntDef({STATE_NORMAL, STATE_LOADING, STATE_EMPTY, STATE_ERROR})
	@Retention(RetentionPolicy.SOURCE)
	public @interface State {
	}

	public static final int STATE_NORMAL = 0;
	public static final int STATE_LOADING = 1;
	public static final int STATE_EMPTY = 2;
	public static final int STATE_ERROR = 3;

	@State
	private int state = STATE_NORMAL;

	public StatesAdapter(@NonNull BaseRecycleAdapter wrapped, @Nullable View loadingView, @Nullable View emptyView, @Nullable View errorView, V view, Context context) {
		super(wrapped, context);
		this.vLoadingView = loadingView;
		this.vEmptyView = emptyView;
		this.vErrorView = errorView;
		vRecyclerView = view;
	}

	public StatesAdapter(@NonNull BaseRecycleAdapter wrapped, V view, Context context) {
		super(wrapped, context);
		this.vLoadingView = null;//默认没有loading加载图
		this.vEmptyView = LayoutInflater.from(context).inflate(R.layout.state_view_no_data, view, false);
		this.vErrorView = LayoutInflater.from(context).inflate(R.layout.state_view_no_network, view, false);
		vRecyclerView = view;
	}

	@State
	public int getState() {
		return state;
	}

	public void setState(@State int state) {
		this.state = state;
		getWrappedAdapter().notifyDataSetChanged();
		notifyDataSetChanged();
	}

	@Override
	public int getItemCount() {
		switch (state) {
			case STATE_LOADING:
			case STATE_EMPTY:
			case STATE_ERROR:
				return 1;
		}
		return super.getItemCount();
	}

	@Override
	public int getItemViewType(int position) {
		return state;
	}

	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		switch (viewType) {
			case STATE_LOADING:
				return new SimpleViewHolder(vLoadingView);
			case STATE_EMPTY:
				return new SimpleViewHolder(vEmptyView);
			case STATE_ERROR:
				return new SimpleViewHolder(vErrorView);
		}
		return super.onCreateViewHolder(parent, viewType);//如果是默认normal，那么交给wrap adapter去实现具体逻辑
	}

	@Override
	public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
		switch (state) {
			case STATE_LOADING:
			case STATE_EMPTY:
			case STATE_ERROR:
				onBindCustomViewHolder(holder);
				break;
			default:
				super.onBindViewHolder(holder, position);//如果是默认normal，那么交给wrap adapter去实现具体逻辑
				break;
		}
	}

	public void onBindCustomViewHolder(RecyclerView.ViewHolder holder) {
		if(state==STATE_ERROR) {
			SimpleViewHolder simpleViewHolder = (SimpleViewHolder) holder;
			simpleViewHolder.itemView.setOnClickListener(listener);
		}
	}

	public static class SimpleViewHolder extends RecyclerView.ViewHolder {
		public SimpleViewHolder(View itemView) {
			super(itemView);
		}
	}

	public void setRetryClickListener(RetryClickListener listener) {
		mListener = listener;
	}

	@Override
	public void refreshData(List list) {
		if (null != list && list.size() > 0) {
			setState(StatesAdapter.STATE_NORMAL);
			vRecyclerView.scrollToPosition(0);
		} else {
			setState(StatesAdapter.STATE_EMPTY);
		}
		super.refreshData(list);
	}

	@Override
	public void loadData(List list) {
		if (null == list || list.size() == 0) {
			Toaster.showShortToast(ctx, R.string.has_no_more);
		} else {
			setState(StatesAdapter.STATE_NORMAL);
		}
		super.loadData(list);
	}
}
