
package com.jtkj.demo.adapter;

import android.content.Context;
import android.view.LayoutInflater;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class BaseAdapter<T> extends android.widget.BaseAdapter {

    protected List<T> dataSet;
    protected Context mContext;
    protected LayoutInflater mInflater;

    public BaseAdapter(Context context) {
        this(context, new ArrayList());
    }

    public BaseAdapter(Context context, List<T> data) {
        this.mContext = context;
        this.dataSet = data;
        mInflater = LayoutInflater.from(mContext);
    }

    public Context getContext() {
        return this.mContext;
    }

    public void addData(T data) {
        if (data != null) {
            this.dataSet.add(data);
        }
    }

    public void addData(Collection<T> data) {
        if (data != null) {
            this.dataSet.addAll(data);
        }
    }

    public void addData(int index, Collection<T> data) {
        if (data != null) {
            this.dataSet.addAll(index, data);
        }
    }

    public void removeData(Collection<T> data) {
        if (data != null) {
            this.dataSet.removeAll(data);
        }
    }

    public void removeAll() {
        if (dataSet != null) {
            this.dataSet.clear();
        }
    }

    public void remove(T data) {
        if (data != null) {
            this.dataSet.remove(data);
        }
    }

    public void remove(int position) {
        this.dataSet.remove(position);
    }

    public List<T> subData(int index, int count) {
        return this.dataSet.subList(index, index + count);
    }

    @Override
    public int getCount() {
        if (this.dataSet == null) {
            return 0;
        } else if (this.dataSet != null && this.dataSet.size() == 0) {
            return 0;
        }
        return this.dataSet.size();
    }

    @Override
    public T getItem(int position) {
        return this.dataSet.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0L;
    }

    public void setItem(int position, T obj) {
        this.dataSet.set(position, obj);
    }

    public void addItem(int position, T obj) {
        this.dataSet.add(position, obj);
    }

    public void addItems(int position, Collection<T> data) {
        this.dataSet.addAll(position, data);
    }

    public List<T> getDataSet() {
        return dataSet;
    }


}

