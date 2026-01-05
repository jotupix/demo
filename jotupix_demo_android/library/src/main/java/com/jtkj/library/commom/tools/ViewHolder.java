package com.jtkj.library.commom.tools;

import android.util.SparseArray;
import android.view.View;

/**
 * Created by agemzhang on 2015/5/6.
 */
public class ViewHolder {
	public static <T extends View> T findViewById(View convertView, int id) {
		SparseArray<View> viewHolder = (SparseArray<View>) convertView.getTag();
		if (viewHolder == null) {
			viewHolder = new SparseArray<View>();
			convertView.setTag(viewHolder);
		}
		View childView = viewHolder.get(id);
		if (childView == null) {
			childView = convertView.findViewById(id);
			viewHolder.put(id, childView);
		}
		return (T) childView;
	}
}
