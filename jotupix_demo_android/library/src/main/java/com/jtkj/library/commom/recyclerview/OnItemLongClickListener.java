package com.jtkj.library.commom.recyclerview;

import android.view.View;

/**
 * yfxiong
 * 时间    2020/10/21 13:51
 * 文件    cooled1248
 * 描述
 */
public interface OnItemLongClickListener<T> {

    void onItemLongClick(int position, T t, View v);

}
