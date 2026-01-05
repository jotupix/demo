package com.jtkj.demo.device;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;
import com.jtkj.demo.CoolLED;
import com.jtkj.demo.R;
import com.jtkj.demo.adapter.BaseRecyclerAdapter;
import com.jtkj.demo.base.BaseFragment;
import com.jtkj.demo.databinding.AnimationFragmentBinding;
import com.jtkj.demo.databinding.CoolleduxMaterialItemBinding;
import com.jtkj.demo.device.utils.GifProcessor;
import com.jtkj.jotupix.core.JotuPix;
import com.jtkj.jotupix.program.JContentBase;
import com.jtkj.jotupix.program.JGifContent;
import com.jtkj.jotupix.program.JProgramContent;
import com.jtkj.library.commom.logger.CLog;
import com.jtkj.library.commom.recyclerview.GridSpaceItemDecoration;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class AnimationFragment extends BaseFragment implements JotuPix.JSendProgramCallback {
    private static final String TAG = AnimationFragment.class.getSimpleName();
    MainActivity mMainActivity;
    AnimationFragmentBinding mBinding;
    MaterialAdapter mAdapter;
    AnimationItem mAnimationItem;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mMainActivity = (MainActivity) getActivity();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mMainActivity = null;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = AnimationFragmentBinding.inflate(inflater, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        GridLayoutManager layoutManagerAnimation = new GridLayoutManager(getActivity(), 1, RecyclerView.VERTICAL, false);
        layoutManagerAnimation.setItemPrefetchEnabled(false);
        mBinding.rv.setLayoutManager(layoutManagerAnimation);
        mBinding.rv.addItemDecoration(new GridSpaceItemDecoration(1, 18, 0));
        mAdapter = new MaterialAdapter();
        mAdapter.setColumnRow(DeviceManager.DEVICE_COLUMN, DeviceManager.DEVICE_ROW);
        mAdapter.setOnItemClickListener(new AnimationFragment.MaterialAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(AnimationItem animationItem, int index) {
                mAdapter.setSelectedIndex(index);
                mAnimationItem = animationItem;
            }
        });

        mBinding.rv.setAdapter(mAdapter);
        mBinding.sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                send(mAnimationItem);
            }
        });
        mAdapter.addData(new AnimationItem(R.drawable.fc_32x96_0_1));
        mAdapter.addData(new AnimationItem(R.drawable.fc_32x96_0_2));
        mAdapter.addData(new AnimationItem(R.drawable.fc_32x96_0_3));
        mAdapter.addData(new AnimationItem(R.drawable.fc_32x96_0_4));
        mAdapter.addData(new AnimationItem(R.drawable.fc_32x96_0_5));
        mAdapter.addData(new AnimationItem(R.drawable.fc_32x96_0_6));
        mAdapter.addData(new AnimationItem(R.drawable.fc_32x96_0_7));
        mAdapter.addData(new AnimationItem(R.drawable.fc_32x96_0_8));
        mAdapter.addData(new AnimationItem(R.drawable.fc_32x96_0_9));
        mAdapter.addData(new AnimationItem(R.drawable.fc_32x96_0_10));
        mAdapter.addData(new AnimationItem(R.drawable.fc_32x96_0_11));
        mAdapter.addData(new AnimationItem(R.drawable.fc_32x96_0_12));
        mAdapter.notifyDataSetChanged();
    }

    private void send(AnimationItem animationItem) {
        if (DeviceManager.getInstance().isDeviceConnectedSuccess()) {
            CoolLED.getInstance().getExecutorService().submit(new Runnable() {
                @Override
                public void run() {
                    /***
                     * Create programContent
                     * set parameter for programContent
                     */
                    JProgramContent programContents = new JProgramContent();
                    JGifContent jGifContent = new JGifContent();
                    jGifContent.blendType = JContentBase.BlendType.COVER;
                    jGifContent.showX = 0;
                    jGifContent.showY = 0;
                    jGifContent.showWidth = DeviceManager.DEVICE_COLUMN;
                    jGifContent.showHeight = DeviceManager.DEVICE_ROW;

                    GifProcessor.processGifFrameByFrame(CoolLED.getInstance(), animationItem.imageId, jGifContent.showWidth, jGifContent.showHeight, new GifProcessor.GifProcessCallback() {
                        @Override
                        public void onSuccess(byte[] gifData) {
                            CLog.i(TAG, "onSuccess>>>gifData.size"+gifData.length);
                            /***
                             * get the gif  image data
                             */
                            jGifContent.setGifData(gifData);
                            programContents.add(jGifContent);
                            byte[] programData = programContents.get();
                            int programSize = programData.length;
                            JotuPix.ProgramInfo programInfo = new JotuPix.ProgramInfo();
                            programInfo.proIndex = 0;
                            programInfo.proAllNum = 1;
                            programInfo.compress = JotuPix.CompressFlag.COMPRESS_FLAG_UNDO;
                            JotuPix.JProgramGroupNor programGroup = new JotuPix.JProgramGroupNor();
                            programGroup.playType = JotuPix.PlayType.PLAY_TYPE_CNT;
                            programGroup.playParam = 0;
                            programInfo.groupParam = programGroup;
                            /***
                             * the AnimationFragment implements JotuPix.JSendProgramCallback interface
                             *  the  function of  "void onEvent(int sendStatus, double percent)" will receive sending data progress
                             */
                            DeviceManager.getInstance().getJotuPix().sendProgram(programInfo, programData, programSize, AnimationFragment.this);
                        }

                        @Override
                        public void onError(Exception e) {
                            CLog.i(TAG, "onError>>>"+e.getMessage());
                        }
                    });
                }
            });
        } else {
            CoolLED.toastSafe(CoolLED.getInstance().string(R.string.device_not_connected));
        }
    }

    @Override
    public void onEvent(int sendStatus, double percent) {
        if (isVisible()) {
            if (sendStatus == SendStatus.FAIL) {
                sendFailed();
                getView().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        dismissProgressDialog();
                    }
                }, 1500);
            } else if (sendStatus == SendStatus.COMPLETED) {
                sendSuccess();
                getView().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        dismissProgressDialog();
                    }
                }, 1500);
            } else if (sendStatus == SendStatus.PROGRESS) {
                sending("", percent + "%");
            }
        }
    }


    public class AnimationItem {
        public int imageId;

        public AnimationItem(int imageId) {
            this.imageId = imageId;
        }
    }

    static class MaterialAdapter extends BaseRecyclerAdapter<AnimationItem> {
        private int selectedIndex = -1;
        private int lastSelectedIndex = -1;

        public interface OnItemClickListener {
            void onItemClick(AnimationItem animationItem, int index);
        }

        private OnItemClickListener mListener;

        public void setOnItemClickListener(OnItemClickListener listener) {
            mListener = listener;
        }

        public void setSelectedIndex(int i) {
            if (selectedIndex != i) {
                lastSelectedIndex = selectedIndex;
                selectedIndex = i;
            }

            if (selectedIndex >= 0) {
                notifyItemChanged(selectedIndex);
            }

            if (lastSelectedIndex >= 0) {
                notifyItemChanged(lastSelectedIndex);
            }
        }

        public int getSelectedIndex() {
            return selectedIndex;
        }

        private int mColumn = 96;
        private int mRow = 32;

        public void setColumnRow(int column, int row) {
            mColumn = column;
            mRow = row;
        }


        public MaterialAdapter() {
        }

        public MaterialAdapter(List<AnimationItem> data) {
            super(data);
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            CoolleduxMaterialItemBinding binding = CoolleduxMaterialItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
            MaterialAdapterViewHolder holder = new MaterialAdapterViewHolder(binding);
            return holder;
        }

        private int getHeightByWidth(int width, int column, int row) {
            float mScreenWidth = width;
            float mRectWidth = (mScreenWidth) / (float) column;
            int newScreenHeight = (int) (mRectWidth * row);
            return newScreenHeight;
        }

        int width;
        int height;

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
            MaterialAdapterViewHolder viewHolder = (MaterialAdapterViewHolder) holder;
            AnimationItem item = getItem(position);
            if (width == 0 || height == 0) {
                viewHolder.content.post(new Runnable() {
                    @Override
                    public void run() {
                        width = viewHolder.content.getWidth();
                        if (mColumn == mRow) {
                            height = width;
                        } else {
                            height = getHeightByWidth(width, mColumn, mRow);
                        }
                        RelativeLayout.LayoutParams paramsIv = (RelativeLayout.LayoutParams) viewHolder.iv.getLayoutParams();
                        paramsIv.width = width;
                        paramsIv.height = height;
                        viewHolder.iv.setLayoutParams(paramsIv);
                        Glide.with(viewHolder.iv.getContext())
                                .asGif()
                                .load(item.imageId)
                                .into(viewHolder.iv);
                    }
                });
            } else {
                RelativeLayout.LayoutParams paramsIv = (RelativeLayout.LayoutParams) viewHolder.iv.getLayoutParams();
                paramsIv.width = width;
                paramsIv.height = height;
                viewHolder.iv.setLayoutParams(paramsIv);
                Glide.with(viewHolder.iv.getContext())
                        .asGif()
                        .load(item.imageId)
                        .into(viewHolder.iv);
            }

            if (position == selectedIndex) {
                viewHolder.content.setBackgroundResource(R.drawable.item_bg1);
                viewHolder.ivSelected.setVisibility(View.VISIBLE);
            } else {
                viewHolder.content.setBackgroundColor(CoolLED.getInstance().color(android.R.color.transparent));
                viewHolder.ivSelected.setVisibility(View.INVISIBLE);
            }

            viewHolder.content.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (null != mListener) {
                        mListener.onItemClick(item, position);
                    }
                }
            });

        }

        @Override
        public int getItemCount() {
            return getCount();
        }

        static class MaterialAdapterViewHolder extends RecyclerView.ViewHolder {
            RelativeLayout content;
            ImageView iv;
            ImageView ivSelected;

            public MaterialAdapterViewHolder(CoolleduxMaterialItemBinding binding) {
                super(binding.getRoot());
                content = binding.content;
                iv = binding.iv;
                ivSelected = binding.ivSelected;
            }
        }
    }

}
