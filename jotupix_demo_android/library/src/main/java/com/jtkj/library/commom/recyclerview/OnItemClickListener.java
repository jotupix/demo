package com.jtkj.library.commom.recyclerview;

import android.view.View;

public interface OnItemClickListener<T> {
	void onItemClick(int position, T t, View v);
}
