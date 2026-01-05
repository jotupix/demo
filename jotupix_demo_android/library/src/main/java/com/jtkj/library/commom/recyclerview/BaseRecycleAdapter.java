package com.jtkj.library.commom.recyclerview;

import androidx.recyclerview.widget.RecyclerView;


import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class BaseRecycleAdapter<T> extends RecyclerView.Adapter<IViewHolder> {
    protected List<T> mDataSet;

    @SuppressWarnings({"unchecked", "rawtypes"})
    public BaseRecycleAdapter() {
        this(new ArrayList());
    }

    public BaseRecycleAdapter(List<T> data) {
        mDataSet = data;
    }

    public void addData(T data) {
        if (data != null) {
            mDataSet.add(data);
        }
    }

    public void addData(Collection<T> data) {
        if (data != null) {
            mDataSet.addAll(data);
        }
    }

    public void addData(int index, Collection<T> data) {
        if (data != null) {
            mDataSet.addAll(index, data);
        }
    }

    public void removeData(Collection<T> data) {
        if (data != null) {
            mDataSet.removeAll(data);
        }
    }

    public void removeAll() {
        if (mDataSet != null) {
            mDataSet.clear();
        }
    }

    public void remove(T data) {
        if (data != null) {
            mDataSet.remove(data);
        }
    }

    public boolean isEmpty() {
        return null == mDataSet || mDataSet.size() == 0;
    }

    public void remove(int position) {
        mDataSet.remove(position);
    }

    public List<T> subData(int index, int count) {
        return mDataSet.subList(index, index + count);
    }


    public void setItem(int position, T obj) {
        mDataSet.set(position, obj);
    }

    public void addItem(int position, T obj) {
        mDataSet.add(position, obj);
    }

    public void addItems(int position, Collection<T> data) {
        mDataSet.addAll(position, data);
    }

    public List<T> getDataSet() {
        return mDataSet;
    }

    public int getCount() {
        return null == mDataSet ? 0 : mDataSet.size();
    }

}
