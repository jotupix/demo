package com.jtkj.library.commom.recycler;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.view.ViewGroup;

import com.jtkj.library.R;
import com.jtkj.library.commom.recyclerview.BaseRecycleAdapter;

import java.util.List;

import static androidx.recyclerview.widget.RecyclerView.Adapter;
import static androidx.recyclerview.widget.RecyclerView.AdapterDataObserver;
import static androidx.recyclerview.widget.RecyclerView.ViewHolder;

/**
 * Created by yfxiong on 2017/3/16.<br>
 * Copyright © yfxiong www.jotus-tech.com. All Rights Reserved.<br><br>
 */
@SuppressWarnings("unchecked")
public class RecyclerViewAdapterWrapper extends Adapter {
	protected final BaseRecycleAdapter wrapped;
	protected Context ctx;

	public RecyclerViewAdapterWrapper(BaseRecycleAdapter wrapped, Context context) {
		super();
		ctx = context;
		this.wrapped = wrapped;
		this.wrapped.registerAdapterDataObserver(new AdapterDataObserver() {
			public void onChanged() {
				notifyDataSetChanged();
			}

			public void onItemRangeChanged(int positionStart, int itemCount) {
				notifyItemRangeChanged(positionStart, itemCount);
			}

			public void onItemRangeInserted(int positionStart, int itemCount) {
				notifyItemRangeInserted(positionStart, itemCount);
			}

			public void onItemRangeRemoved(int positionStart, int itemCount) {
				notifyItemRangeRemoved(positionStart, itemCount);
			}

			public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
				notifyItemMoved(fromPosition, toPosition);
			}
		});
	}

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		return wrapped.onCreateViewHolder(parent, viewType);
	}

	@Override
	public void onBindViewHolder(ViewHolder holder, int position) {
		wrapped.onBindViewHolder(holder, position);
	}

	@Override
	public int getItemCount() {
		return wrapped.getItemCount();
	}

	@Override
	public int getItemViewType(int position) {
		return wrapped.getItemViewType(position);
	}

	@Override
	public void setHasStableIds(boolean hasStableIds) {
		wrapped.setHasStableIds(hasStableIds);
	}

	@Override
	public long getItemId(int position) {
		return wrapped.getItemId(position);
	}

	@Override
	public void onViewRecycled(ViewHolder holder) {
		wrapped.onViewRecycled(holder);
	}

	@Override
	public boolean onFailedToRecycleView(ViewHolder holder) {
		return wrapped.onFailedToRecycleView(holder);
	}

	@Override
	public void onViewAttachedToWindow(ViewHolder holder) {
		wrapped.onViewAttachedToWindow(holder);
	}

	@Override
	public void onViewDetachedFromWindow(ViewHolder holder) {
		wrapped.onViewDetachedFromWindow(holder);
	}

	@Override
	public void registerAdapterDataObserver(AdapterDataObserver observer) {
		wrapped.registerAdapterDataObserver(observer);
	}

	@Override
	public void unregisterAdapterDataObserver(AdapterDataObserver observer) {
		wrapped.unregisterAdapterDataObserver(observer);
	}

	@Override
	public void onAttachedToRecyclerView(RecyclerView recyclerView) {
		wrapped.onAttachedToRecyclerView(recyclerView);
	}

	@Override
	public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
		wrapped.onDetachedFromRecyclerView(recyclerView);
	}

	public Adapter getWrappedAdapter() {
		return wrapped;
	}

	public void refreshData(List list){
		wrapped.removeAll();
		wrapped.addData(list);
		wrapped.notifyDataSetChanged();
		if ((list.size() % 20) > 0  && wrapped.getCount() > 20 ) {//如果不是第一页，并且这一页不满20条
			com.jtkj.library.commom.tools.Toaster.showShortToast(ctx, R.string.has_no_more);
		}
	}

	public void loadData(List list){
		wrapped.addData(list);
		wrapped.notifyDataSetChanged();
	}

	public void dettach(){
		ctx = null;
	}
}