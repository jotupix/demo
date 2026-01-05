package com.jtkj.demo.device;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jtkj.demo.CoolLED;
import com.jtkj.demo.base.BaseFragment;
import com.jtkj.demo.databinding.TextFragemntBinding;
import com.jtkj.demo.device.utils.FontUtils;
import com.jtkj.jotupix.core.JotuPix;
import com.jtkj.jotupix.program.JColor;
import com.jtkj.jotupix.program.JContentBase;
import com.jtkj.jotupix.program.JProgramContent;
import com.jtkj.jotupix.program.JTextContent;
import com.jtkj.jotupix.program.JTextDiyColorContent;
import com.jtkj.jotupix.program.JTextFont;
import com.jtkj.jotupix.program.JTextFullColorContent;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class TextFragment extends BaseFragment implements JotuPix.JSendProgramCallback {

    TextFragemntBinding mBinding;

    MainActivity mMainActivity;

    List<ChooseDialog.ChooseItem> mModeData;
    ChooseDialog.ChooseItem mModeChooseItem = new ChooseDialog.ChooseItem("Shift left continuously", 2, 1);

    List<ChooseDialog.ChooseItem> mColorData;
    ChooseDialog.ChooseItem mColorChooseItem = new ChooseDialog.ChooseItem("Red", JColor.RED, 0);


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
        mBinding = TextFragemntBinding.inflate(inflater, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mBinding.modeTv.setText(mModeChooseItem.name);
        mModeData = new ArrayList<>();
        mModeData.add(new ChooseDialog.ChooseItem("Static", 1, 0));
        mModeData.add(new ChooseDialog.ChooseItem("Shift left continuously", 2, 1));
        mModeData.add(new ChooseDialog.ChooseItem("Shift right continuously", 3, 2));
        mModeData.add(new ChooseDialog.ChooseItem("Up", 4, 3));
        mModeData.add(new ChooseDialog.ChooseItem("Down", 5, 4));
        mModeData.add(new ChooseDialog.ChooseItem("Accumulation", 6, 5));
        mModeData.add(new ChooseDialog.ChooseItem("Picture scroll", 7, 6));
        mModeData.add(new ChooseDialog.ChooseItem("Flashing", 8, 7));
        mModeData.add(new ChooseDialog.ChooseItem("Pan to the left", 9, 8));
        mModeData.add(new ChooseDialog.ChooseItem("Pan to the right", 10, 9));
        mModeData.add(new ChooseDialog.ChooseItem("Override left", 11, 10));
        mModeData.add(new ChooseDialog.ChooseItem("Override right", 12, 11));
        mModeData.add(new ChooseDialog.ChooseItem("Horizontal interspersed", 13, 12));

        mBinding.modeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChooseDialog.showDialog(mMainActivity, mModeData, mModeChooseItem, new ChooseDialog.ChooseListener() {
                    @Override
                    public void onItemChoose(ChooseDialog.ChooseItem item, int index) {
                        mModeChooseItem = item;
                        mBinding.modeTv.setText(mModeChooseItem.name);
                    }
                });
            }
        });


        mBinding.colorTv.setText(mColorChooseItem.name);
        mColorData = new ArrayList<>();
        mColorData.add(new ChooseDialog.ChooseItem("OneWordOneColor", JColor.RED, -1));
        mColorData.add(new ChooseDialog.ChooseItem("Red", JColor.RED, 0));
        mColorData.add(new ChooseDialog.ChooseItem("Green", JColor.GREEN, 1));
        mColorData.add(new ChooseDialog.ChooseItem("Blue", JColor.BLUE, 2));
        mColorData.add(new ChooseDialog.ChooseItem("Yellow", JColor.YELLOW, 3));
        mColorData.add(new ChooseDialog.ChooseItem("White", JColor.WHITE, 4));
        mColorData.add(new ChooseDialog.ChooseItem("Colorful1", 5, 5));
        mColorData.add(new ChooseDialog.ChooseItem("Colorful2", 6, 6));

        mBinding.colorLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChooseDialog.showDialog(mMainActivity, mColorData, mColorChooseItem, new ChooseDialog.ChooseListener() {
                    @Override
                    public void onItemChoose(ChooseDialog.ChooseItem item, int index) {
                        mColorChooseItem = item;
                        mBinding.colorTv.setText(mColorChooseItem.name);
                    }
                });
            }
        });

        mBinding.sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendText();
            }
        });
    }

    private void sendText() {
        String text = mBinding.inputEt.getText().toString();
        if (!TextUtils.isEmpty(text)) {
            JProgramContent programContents = new JProgramContent();
            int column = DeviceManager.DEVICE_COLUMN;
            int row = DeviceManager.DEVICE_ROW;
            int showMode = mModeChooseItem.value;
            int color = mColorChooseItem.value;
            int showSpeed = mBinding.speedSeekBar.getProgress() + 1;
            int stayTime = 1;
            int moveSpace = column;

            int textSize = row;
            CoolLED.getInstance().getExecutorService().submit(new Runnable() {
                @Override
                public void run() {
                    int textNum = 0;
                    int allTextWidth = 0;
                    boolean isOneWordOneColor = false;
                    if (mColorChooseItem.index == 5 || mColorChooseItem.index == 6) {
                        isOneWordOneColor = false;
                    } else {
                        if (mColorChooseItem.index == -1) {
                            isOneWordOneColor = true;
                        } else {
                            isOneWordOneColor = false;
                        }
                    }
                    List<JTextFont> textData = FontUtils.getInstance(CoolLED.getInstance()).getFontByteDataCoolleduxForEmojinew(text, column, row, textSize, showMode, color, isOneWordOneColor);
                    for (JTextFont item : textData) {
                        allTextWidth += item.textWidth;
                    }
                    textNum = textData.size();
                    JTextFullColorContent textFullColorContent;
                    JTextDiyColorContent textDiyColorContent;
                    if (mColorChooseItem.index == 5 || mColorChooseItem.index == 6) {
                        textFullColorContent = new JTextFullColorContent();
                        textFullColorContent.showX = 0;
                        textFullColorContent.showY = 0;
                        textFullColorContent.showWidth = column;
                        textFullColorContent.showHeight = row;
                        textFullColorContent.textColorType = JTextFullColorContent.TextColorType.HorScroll;
                        textFullColorContent.textColorSpeed = 200;
                        textFullColorContent.textColorDir = JTextFullColorContent.TextColorDir.Right;
                        if (color == 5) {
                            for (int i = 0; i < JTextFullColorContent.TEXT_FULL_COLOR_RAINBOW.length; i++) {
                                textFullColorContent.textFullColor.add(JTextFullColorContent.TEXT_FULL_COLOR_RAINBOW[i]);
                            }
                        } else if (color == 6) {
                            for (int i = 0; i < JTextFullColorContent.TEXT_FULL_COLOR_THREE.length; i++) {
                                textFullColorContent.textFullColor.add(JTextFullColorContent.TEXT_FULL_COLOR_THREE[i]);
                            }
                        }

                        programContents.add(textFullColorContent);
                    } else {
                        textDiyColorContent = new JTextDiyColorContent();
                        textDiyColorContent.moveSpace = moveSpace;
                        textDiyColorContent.showX = 0;
                        textDiyColorContent.showY = 0;
                        textDiyColorContent.showWidth = column;
                        textDiyColorContent.showHeight = row;
                        textDiyColorContent.showMode = showMode;
                        textDiyColorContent.showSpeed = showSpeed;
                        textDiyColorContent.stayTime = stayTime;
                        textDiyColorContent.textNum = textNum;
                        textDiyColorContent.textAllWide = allTextWidth;
                        textDiyColorContent.textData = textData;
                        programContents.add(textDiyColorContent);
                    }

                    JTextContent textContent = new JTextContent();
                    textContent.bgColor = 0;
                    textContent.blendType = JContentBase.BlendType.COVER;
                    textContent.showX = 0;
                    textContent.showY = 0;
                    textContent.showWidth = column;
                    textContent.showHeight = row;
                    textContent.showMode = showMode;
                    textContent.showSpeed = showSpeed;
                    textContent.stayTime = stayTime;
                    textContent.moveSpace = moveSpace;
                    textContent.textNum = textNum;
                    textContent.textAllWide = allTextWidth;
                    textContent.textData = textData;
                    programContents.add(textContent);

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
                    DeviceManager.getInstance().getJotuPix().sendProgram(programInfo, programData, programSize, TextFragment.this);
                }
            });
        } else {
            CoolLED.toastSafe("input is empty!");
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
}
