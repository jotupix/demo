package com.jtkj.demo.device;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;

import com.jtkj.demo.CoolLED;
import com.jtkj.demo.R;
import com.jtkj.demo.adapter.BaseAdapter;
import com.jtkj.demo.databinding.ChooseDialogBinding;
import com.jtkj.demo.databinding.ChooseItemBinding;

import java.util.List;

import androidx.annotation.NonNull;

public class ChooseDialog extends Dialog {

    public interface ChooseListener {
        void onItemChoose(ChooseItem item, int index);
    }

    List<ChooseItem> mData;
    ChooseItem mItem;
    Activity mActivity;
    ChooseListener mListener;
    ChooseAdapter mAdapter;
    ChooseDialogBinding mBinding;

    public static ChooseDialog showDialog(@NonNull Activity activity, List<ChooseItem> datas, ChooseItem chooseItem, ChooseListener listener) {
        ChooseDialog dialog = new ChooseDialog(activity, datas, chooseItem, listener);
        dialog.show();
        return dialog;
    }

    public ChooseDialog(@NonNull Activity activity, List<ChooseItem> datas, ChooseItem chooseItem, ChooseListener listener) {
        super(activity);
        mData = datas;
        mItem = chooseItem;
        mActivity = activity;
        mListener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        mBinding = ChooseDialogBinding.inflate(LayoutInflater.from(getContext()));
        setContentView(mBinding.getRoot());
        initView();
        initWindowParams();
    }

    private void initView() {
        mBinding.cancelIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        mAdapter = new ChooseAdapter(mActivity);
        mBinding.lv.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();
        mBinding.lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mAdapter.setSelectedIndex(position);
                mItem = mAdapter.getItem(position);
            }
        });

        mAdapter.setSelectedIndex(mItem.index);
        mAdapter.addData(mData);
        mAdapter.notifyDataSetChanged();

        mBinding.confirmIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    mListener.onItemChoose(mItem, mItem.index);
                }
                dismiss();
            }
        });
    }

    private void initWindowParams() {
        setCancelable(false);
        setCanceledOnTouchOutside(false);
        Window window = getWindow();
        if (window != null) {
            window.setBackgroundDrawableResource(android.R.color.transparent);
            window.setGravity(Gravity.BOTTOM);
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        }
    }

    public static class ChooseAdapter extends BaseAdapter<ChooseItem> {

        public ChooseAdapter(Context context) {
            super(context);
        }

        public ChooseAdapter(Context context, List<ChooseItem> data) {
            super(context, data);
        }

        public ChooseAdapter(Activity activity) {
            super(activity);
            mActivity = activity;
        }

        Activity mActivity;
        int selectedIndex = -1;

        public void setSelectedIndex(int i) {
            selectedIndex = i;
            notifyDataSetChanged();
        }

        public int getSelectedIndex() {
            return selectedIndex;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ChooseItemBinding binding;
            if (convertView == null) {
                binding = ChooseItemBinding.inflate(mActivity.getLayoutInflater(), null, false);
                convertView = binding.getRoot();
                convertView.setTag(binding);
            } else {
                binding = (ChooseItemBinding) convertView.getTag();
            }
            ChooseItem item = getItem(position);

            binding.tv.setText(item.name);
            if (item.index == selectedIndex) {
                binding.tv.setTextColor(CoolLED.getInstance().color(R.color.color_7a6aee));
            } else {
                binding.tv.setTextColor(CoolLED.getInstance().color(R.color.color_ffffff));
            }
            return convertView;
        }
    }

    public static class ChooseItem {
        public String name;
        public int value;
        public int index;

        public ChooseItem(String name, int value, int index) {
            this.name = name;
            this.value = value;
            this.index = index;
        }

    }

}