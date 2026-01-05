package com.jtkj.demo.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

import com.jtkj.demo.R;
import com.jtkj.library.commom.logger.CLog;
import com.nineoldandroids.view.ViewHelper;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;


public class DrawView extends View implements OnTouchListener {
    private final String TAG = DrawView.class.getSimpleName();
    public final static String DRAW_VIEW_DATA = "DRAW_VIEW_DATA";
    public final static String INDEX = "DRAW_VIEW_DATA_index";

    public interface ItemClickListener {
        void onItemClick(int index);

        void onItemMove(int index);

        void onItemClear(int index);

        void onScale(float percents);
    }

    public interface DrawViewEditListener {
        void noAreaSelected();

        void noAreaCopied();

        void onAreaCopiedSuccess(CopiedAreaInfo copiedAreaInfo);

        void onPasteSelectedArea();

        void onAreaDeleted();

        void onFillAreaModeOne();

        void onFillAreaModeTwo();

        void onMirrorModeHorizontalSelectedArea();

        void onMirrorModeVerticalSelectedArea();

        void onInputWithSizeAndData();

        void onRotate90SelectedArea();

        void onAreaMoving();

        void onRedo();

        void onUndo();

        void onOneTimeDrawFinish();

    }

    private ItemClickListener mListener;

    private DrawViewEditListener mDrawViewEditListener;

    public void setOnItemClickListener(ItemClickListener listener) {
        mListener = listener;
    }

    public void setCopyPasteChooseAreaListener(DrawViewEditListener listener) {
        mDrawViewEditListener = listener;
    }

    private float mScreenWidth;
    private float mScreenHeight;
    private float mRectWidth;
    private float mRecHeight;
    private final float mStartX = 0;
    private final float mStartY = 0;
    private RectF[][] mRectFs = null;
    private RectF[][] mRectFsFull = null;
    private boolean[][] mRectFlags = null;
    private int mColumn;
    private int mRow;
    private int mSelectedColor;
    private int mDisSelectedColor;

    /**
     * 初始化状态常量
     */
    private static final int STATUS_INIT = 1;

    /**
     * 放大状态常量
     */
    private static final int STATUS_ZOOM_OUT = 2;

    /**
     * 缩小状态常量
     */
    private static final int STATUS_ZOOM_IN = 3;

    /**
     * 拖动状态常量
     */
    private static final int STATUS_MOVE = 4;

    /**
     * 记录当前操作的状态，可选值为STATUS_INIT、STATUS_ZOOM_OUT、STATUS_ZOOM_IN和STATUS_MOVE
     */
    private int mCurrentStatus;

    /**
     * 记录两指同时放在屏幕上时，中心点的横坐标值
     */
    private float mCenterPointX;

    /**
     * 记录两指同时放在屏幕上时，中心点的纵坐标值
     */
    private float mCenterPointY;

    /**
     * 记录上次手指移动时的横坐标
     */
    private float mLastXMove = -1;

    /**
     * 记录上次手指移动时的纵坐标
     */
    private float mLastYMove = -1;

    /**
     * 记录手指在横坐标方向上的移动距离
     */
    private float mMovedDistanceX;

    /**
     * 记录手指在纵坐标方向上的移动距离
     */
    private float mMovedDistanceY;

    /**
     * 记录横向偏移值
     */
    private float mTotalTranslateX;

    /**
     * 记录纵向偏移值
     */
    private float mTotalTranslateY;

    /**
     * 记录总缩放比例
     */
    private float mTotalRatio = 1f;

    /**
     * 记录手指移动的距离所造成的缩放比例
     */
    private float mScaledRatio;

    /**
     * 记录初始化时的缩放比例
     */
    private float mInitRatio = 1f;

    /**
     * 记录上次两指之间的距离
     */
    private double mLastFingerDis;

    private boolean isForListItemShow;

    private boolean isHeightFirst;

    private boolean isEditAble;

    private List<DrawItem> mData = new ArrayList<>();

    private List<List<DrawItem>> mDatas;

    private long mSpeed;

    private int mCurrentIndex = -1;

    private RectF mSelectionRect;
    private boolean mSelecting = false;
    private boolean mSelected = false;
    private boolean mIsMoving = false;  // 判断是否正在移动
    private float mStartSelectX, mStartSelectY;
    private float mLastTouchX, mLastTouchY; // 上次触摸的位置
    private List<RectF> mRectangles = new ArrayList<>();  // 存储所有矩形
    private List<DrawItem> mCopiedAllDrawItems = new ArrayList<>();  // 选

    private static final int MESSAGE_UPDATE = 100;
    private ViewHandler mHandler;

    private static class ViewHandler extends Handler {
        WeakReference<DrawView> mView;

        public ViewHandler(DrawView view) {
            super(Looper.myLooper());
            mView = new WeakReference<>(view);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            DrawView mDrawView = mView.get();
            if (null != mDrawView) {
                mDrawView.processMessage(msg);
            }
        }
    }

    private void processMessage(Message msg) {
        try {
            if (msg.what == MESSAGE_UPDATE) {
                mHandler.removeMessages(MESSAGE_UPDATE);
                if (mCurrentIndex == (mDatas.size() - 1)) {
                    mCurrentIndex = 0;
                } else {
                    mCurrentIndex++;
                }
                mData = mDatas.get(mCurrentIndex);
                initData();
                postInvalidate();
                if (mCurrentIndex >= 0 && null != mDatas && mDatas.size() > 1) {
                    mHandler.sendEmptyMessageDelayed(MESSAGE_UPDATE, mSpeed);
                }
            }
        } catch (Exception exception) {
            exception.printStackTrace();
            CLog.i(TAG, "processMessage>>>exception>>>" + exception.getMessage());
        }

    }

    private boolean isEdit = false;

    private boolean isDraw = true;

    private boolean mMoveFlag = false;

    private boolean mClearFlag = false;

    public void setEdit(boolean flag) {
        isEdit = flag;
    }

    public void setDraw(boolean flag) {
        isDraw = flag;
    }

    public void setMoveFlag(boolean flag) {
        mMoveFlag = flag;
    }

    public void setClearFlag(boolean flag) {
        mClearFlag = flag;
    }

    public DrawView(Context context) {
        this(context, null);
    }

    public DrawView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DrawView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.DrawView);
        mColumn = a.getInt(R.styleable.DrawView_column, 0);
        mRow = a.getInt(R.styleable.DrawView_row, 0);
        isForListItemShow = a.getBoolean(R.styleable.DrawView_isForListItemShow, false);
        isHeightFirst = a.getBoolean(R.styleable.DrawView_isHeightFirst, false);
        isEditAble = a.getBoolean(R.styleable.DrawView_isEditAble, true);
        mSelectedColor = a.getColor(R.styleable.DrawView_selectedColor, 0);
        mDisSelectedColor = a.getColor(R.styleable.DrawView_disSelectedColor, 0);
        a.recycle();
        setEditAble(isEditAble);
        mHandler = new ViewHandler(this);
    }

    public void setEditAble(boolean isForEdit) {
        if (isForEdit) {
            setOnTouchListener(this);
        } else {
            setOnTouchListener(null);
        }
        setFocusable(isForEdit);
        setFocusableInTouchMode(isForEdit);
    }

    public void disAbleTouch() {
        setOnTouchListener(null);
    }

    public boolean isEmpty() {
        if (null != mData && mData.size() > 0) {
            return false;
        }

        if (null != mDatas && mDatas.size() > 0) {
            return false;
        }

        return true;
    }

    public void setData(List<DrawItem> data) {
        if (null != data && data.size() > 0) {
            mData = data;
            initData();
            postInvalidate();
        }
    }

    public List<DrawItem> getData() {
        return mData;
    }

    public List<List<DrawItem>> getListData() {
        return mDatas;
    }

    public long getSpeed() {
        return mSpeed;
    }

    public List<DrawItem> getDataByColumn() {
        List<DrawItem> result = new ArrayList<>();

        if (null != mData && mData.size() > 0) {
            for (int i = 0; i < mColumn; i++) {
                for (int j = 0; j < mRow; j++) {
                    int index = j * mColumn + i;
                    DrawItem item = mData.get(index);
                    result.add(item);
                }
            }
        }
        CLog.i(TAG, "getDataByColumn  size>>>" + result.size());
        return result;
    }

    public List<DrawItem> getCopiedData() {
        List<DrawItem> result = new ArrayList<>();
        for (DrawItem item : mData) {
            result.add(item.copy());
        }

        CLog.i(TAG, "getCopiedData  size>>>" + result.size());
        return result;
    }

    public List<DrawItem> getCopiedDataByColumn() {
        List<DrawItem> result = new ArrayList<>();

        for (int i = 0; i < mColumn; i++) {
            for (int j = 0; j < mRow; j++) {
                int index = j * mColumn + i;
                DrawItem item = mData.get(index);
                result.add(item.copy());
            }
        }

        CLog.i(TAG, "getCopiedDataByColumn  size>>>" + result.size());
        return result;
    }

    public void setListAnimationData(List<AnimationData> animationDatas, long speed) {
        if (null != animationDatas && animationDatas.size() > 0) {
            mDatas = new ArrayList<>();
            for (AnimationData animationData : animationDatas) {
                mDatas.add(animationData.drawItems);
            }
            mCurrentIndex = 0;
            mData = mDatas.get(mCurrentIndex);
            mSpeed = speed;
            initData();
            postInvalidate();
            if (mCurrentIndex >= 0 && null != mDatas && mDatas.size() > 1) {
                mHandler.sendEmptyMessageDelayed(MESSAGE_UPDATE, mSpeed);
            }
            CLog.i(TAG, "mSpeed>>>" + mSpeed);
        }
    }

    public void setSpeed(int speed) {
        mSpeed = speed;
    }


    public void setListData(List<List<DrawItem>> data, long speed) {
        if (null != data && data.size() > 0) {
            mDatas = data;
            mCurrentIndex = 0;
            mData = mDatas.get(mCurrentIndex);
            mSpeed = speed;
            initData();
            postInvalidate();
            if (mCurrentIndex >= 0 && null != mDatas && mDatas.size() > 1) {
                mHandler.sendEmptyMessageDelayed(MESSAGE_UPDATE, mSpeed);
            }
        }
    }

    private float cellSpace = 0.5f;

    public void clear() {
        try {
            mData = null;
            mDatas = null;
            mHandler.removeMessages(MESSAGE_UPDATE);
            mRectFs = new RectF[mColumn][mRow];
            mRectFsFull = new RectF[mColumn][mRow];
            mRectFlags = new boolean[mColumn][mRow];
            float left;
            float top;
            float right;
            float bottom;

            for (int i = 0; i < mColumn; i++) {
                for (int j = 0; j < mRow; j++) {
                    left = mStartX + i * mRectWidth;
                    right = left + mRectWidth - (cellSpace);
                    top = mStartY + j * mRectWidth;
                    bottom = top + mRectWidth - (cellSpace);
                    if (isEdit) {
                        mRectFs[i][j] = new RectF(left + 1, top + 1, right - 1, bottom - 1);
                    } else {
                        mRectFs[i][j] = new RectF(left, top, right, bottom);
                    }
                    int index = j * mColumn + i;
                    mRectFlags[i][j] = false;
                }
            }

            if (isEdit) {
                for (int i = 0; i < mColumn; i++) {
                    for (int j = 0; j < mRow; j++) {
                        left = mStartX + i * mRectWidth;
                        right = left + mRectWidth;
                        top = mStartY + j * mRectWidth;
                        bottom = top + mRectWidth;
                        mRectFsFull[i][j] = new RectF(left, top, right, bottom);
                        mRectangles.add(mRectFsFull[i][j]);
                    }
                }
            }

            postInvalidate();
        } catch (Exception e) {
            e.printStackTrace();
            CLog.i(TAG, "clear>>>>exception>>>" + e.getMessage());
        }
    }

    private void initData() {
        try {
            mRectFs = new RectF[mColumn][mRow];
            mRectFlags = new boolean[mColumn][mRow];
            mRectFsFull = new RectF[mColumn][mRow];
            float left;
            float top;
            float right;
            float bottom;

            for (int i = 0; i < mColumn; i++) {
                for (int j = 0; j < mRow; j++) {
                    left = mStartX + i * mRectWidth;
                    right = left + mRectWidth - (cellSpace);
                    top = mStartY + j * mRectWidth;
                    bottom = top + mRectWidth - (cellSpace);
                    if (isEdit) {
                        mRectFs[i][j] = new RectF(left + 1, top + 1, right - 1, bottom - 1);
                    } else {
                        mRectFs[i][j] = new RectF(left, top, right, bottom);
                    }
                    int index = j * mColumn + i;
                    mRectFlags[i][j] = getValue(index);
                }
            }

            if (isEdit) {
                for (int i = 0; i < mColumn; i++) {
                    for (int j = 0; j < mRow; j++) {
                        left = mStartX + i * mRectWidth;
                        right = left + mRectWidth;
                        top = mStartY + j * mRectWidth;
                        bottom = top + mRectWidth;
                        mRectFsFull[i][j] = new RectF(left, top, right, bottom);
                        mRectangles.add(mRectFsFull[i][j]);
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            CLog.i(TAG, "initData>>>>exception>>>" + e.getMessage());
        }
    }

    public void setColumnRow(int column, int row) {
        mColumn = column;
        mRow = row;
        postInvalidate();
    }

    private boolean getValue(int index) {
        if (null != mData && mData.size() > 0) {
            DrawItem item = mData.get(index);
            if (item.color == Color.BLACK) {
                item.data = String.valueOf(Boolean.FALSE);
            }

            return Boolean.parseBoolean(item.data);

        }
        return false;
    }

    private int getMySize(int defaultSize, int measureSpec) {
        int mySize = defaultSize;

        int mode = MeasureSpec.getMode(measureSpec);
        int size = MeasureSpec.getSize(measureSpec);

        switch (mode) {
            case MeasureSpec.UNSPECIFIED: {//如果没有指定大小，就设置为默认大小
                mySize = defaultSize;
                break;
            }
            case MeasureSpec.AT_MOST: {//如果测量模式是最大取值为size
                //我们将大小取最大值,你也可以取其他值
                mySize = size;
                break;
            }
            case MeasureSpec.EXACTLY: {//如果是固定的大小，那就不要去改变它
                mySize = size;
                break;
            }
        }
        return mySize;
    }

    public void updateSize(int width, int height, int column, int row) {
        mRectWidth = (width) / (float) column;
        setMeasuredDimension(width, height);
        setColumnRow(column, row);
    }


    public List<DrawItem> getCopiedSelectedDrawItemsInChoosedArea() {
        List<DrawItem> selectedDrawItems = new ArrayList<>();

        if (null != mSelectionRect) {
            for (int i = 0; i < mColumn; i++) {
                for (int j = 0; j < mRow; j++) {
                    RectF rect = mRectFs[i][j];
                    if (RectF.intersects(mSelectionRect, rect)) {
                        mRectFlags[i][j] = true;
                        int index = j * mColumn + i;
                        DrawItem item = mData.get(index);
                        selectedDrawItems.add(item.copy());
                    }
                }
            }
        }

        CLog.i(TAG, "getCopiedSelectedDrawItemsInChoosedArea  size>>>" + selectedDrawItems.size());
        return selectedDrawItems;
    }

    public Map<Integer, DrawItem> getCopiedSelectedDrawItemsMapInChoosedArea() {
        Map<Integer, DrawItem> selectedDrawItems = new HashMap<>();
        // 计算选区的宽度和高度
        int selectedColumnCount = (int) ((mSelectionRect.width() + (cellSpace)) / mRectWidth);
        int selectedRowCount = (int) ((mSelectionRect.height() + (cellSpace)) / mRectWidth);

        int startColumn = 0;
        int startRow = 0;
        boolean isFirst = false;
        if (null != mSelectionRect) {
            for (int i = 0; i < mColumn; i++) {
                for (int j = 0; j < mRow; j++) {
                    RectF rect = mRectFs[i][j];
                    if (RectF.intersects(mSelectionRect, rect)) {
                        if (!isFirst) {
                            isFirst = true;
                            startColumn = i;
                            startRow = j;
                        }
                        mRectFlags[i][j] = true;
                        int index = j * mColumn + i;
                        DrawItem item = mData.get(index);
                        int newColumn = (i - startColumn);
                        int newCow = (j - startRow);
                        int newIndex = newCow * selectedColumnCount + newColumn;
                        selectedDrawItems.put(newIndex, item.copy());
                    }
                }
            }
        }

        CLog.i(TAG, "getCopiedSelectedDrawItemsMapInChoosedArea  size>>>" + selectedDrawItems.size());
        return selectedDrawItems;
    }

    public List<DrawItem> getOriginalSelectedDrawItemsInChoosedArea() {
        List<DrawItem> selectedDrawItems = new ArrayList<>();

        if (null != mSelectionRect) {
            for (int i = 0; i < mColumn; i++) {
                for (int j = 0; j < mRow; j++) {
                    RectF rect = mRectFs[i][j];
                    if (RectF.intersects(mSelectionRect, rect)) {
                        mRectFlags[i][j] = true;
                        int index = j * mColumn + i;
                        DrawItem item = mData.get(index);
                        item.index = index;
                        selectedDrawItems.add(item);
                    }
                }
            }
        }

        CLog.i(TAG, "getOriginalSelectedDrawItemsInChoosedArea  size>>>" + selectedDrawItems.size());

        return selectedDrawItems;
    }


    private int isAreaChoosed(RectF inputRect) {
        int result = -1;
        for (int i = 0; i < mColumn; i++) {
            for (int j = 0; j < mRow; j++) {
                RectF rect = mRectFs[i][j];
                if (RectF.intersects(inputRect, rect)) {
                    mRectFlags[i][j] = true;
                    result = j * mColumn + i;
                    return result;
                }
            }
        }
        return result;
    }


    public Map<Integer, DrawItem> getOriginalSelectedDrawItemsMapInChoosedArea() {
        Map<Integer, DrawItem> selectedDrawItems = new HashMap<>();
        // 计算选区的宽度和高度
        int selectedColumnCount = (int) ((mSelectionRect.width() + (cellSpace)) / mRectWidth);
        int selectedRowCount = (int) ((mSelectionRect.height() + (cellSpace)) / mRectWidth);

        float startX = mSelectionRect.left;
        float startY = mSelectionRect.top;
        for (int i = 0; i < selectedColumnCount; i++) {
            for (int j = 0; j < selectedRowCount; j++) {
                float left = startX + (i) * mRectWidth;
                float right = left + mRectWidth;
                float top = startY + (j) * mRectWidth;
                float bottom = top + mRectWidth;
                RectF rect = new RectF(left, top, right, bottom);
                int result = isAreaChoosed(rect);
                if (result >= 0) {
                    DrawItem item = mData.get(result);
                    int index = j * selectedColumnCount + i;
                    selectedDrawItems.put(index, item);
                }
            }
        }

        CLog.i(TAG, "getOriginalSelectedDrawItemsInChoosedArea  size>>>" + selectedDrawItems.size());

        return selectedDrawItems;
    }

    public void deleteAllData() {
        saveCurrentState();
        for (DrawItem item : mData) {
            item.color = Color.BLACK;
            item.data = "true";
        }
        postInvalidate();
    }

    public void setChooseArea(boolean choose) {
        if (choose) {
            mSelecting = true;
        } else {
            mSelectionRect = null;
            mSelecting = false;
            mSelected = false;
            mIsMoving = false;

            mLastTouchX = 0;
            mLastTouchY = 0;
            postInvalidate();
        }
    }

    public void deleteSelectedArea() {
        CLog.i(TAG, "deleteSelectedArea");
        if (null != mSelectionRect && mSelecting && mSelected) {
            saveCurrentState();

            mSelectionRect = null;
            mSelecting = true;
            mSelected = false;
            mIsMoving = false;
            mStartSelectX = 0;
            mStartSelectY = 0;
            mLastTouchX = 0;
            mLastTouchY = 0;

            backToStateBeforeMoving();

            postInvalidate();

            if (null != mDrawViewEditListener) {
                mDrawViewEditListener.onAreaDeleted();
            }
        } else {
            if (null != mDrawViewEditListener) {
                mDrawViewEditListener.noAreaSelected();
            }
        }
    }

    public void clearSelectionRect() {
        CLog.i(TAG, "clearSelectedArea");
        if (null != mSelectionRect && mSelecting) {
            mSelectionRect = null;
            mSelecting = true;
            mSelected = false;
            mIsMoving = false;
            mStartSelectX = 0;
            mStartSelectY = 0;
            mLastTouchX = 0;
            mLastTouchY = 0;
            postInvalidate();
        }
    }

    public void copySelectedArea() {
        CLog.i(TAG, "copySelectedArea");
        CLog.i(TAG, "copySelectedArea  mSelectionRect>>>" + mSelectionRect);
        CLog.i(TAG, "copySelectedArea  mSelecting>>>" + mSelecting);
        CLog.i(TAG, "copySelectedArea  mSelected>>>" + mSelected);
        if (null == mSelectionRect || !mSelecting || !mSelected) {
            if (null != mDrawViewEditListener) {
                mDrawViewEditListener.noAreaSelected();
            }
            return;
        }
        float left = mSelectionRect.left;
        float top = mSelectionRect.top;
        float right = mSelectionRect.right;
        float bottom = mSelectionRect.bottom;


        RectF copiedSelectionRect = new RectF();
        copiedSelectionRect.set(0, 0, right - left, bottom - top);
        CopiedAreaInfo copiedAreaInfo = new CopiedAreaInfo();
        copiedAreaInfo.copiedSelectionRect = copiedSelectionRect;
        copiedAreaInfo.mapDrawItems = mTempMapDrawItems;
        copiedAreaInfo.tempDrawLists = mTempData;
        if (null != mDrawViewEditListener) {
            mDrawViewEditListener.onAreaCopiedSuccess(copiedAreaInfo);
        }
    }

    public void pasteSelectedArea(CopiedAreaInfo copiedAreaInfo) {
        CLog.i(TAG, "pasteSelectedArea");
        if (null == copiedAreaInfo || null == copiedAreaInfo.copiedSelectionRect) {
            if (null != mDrawViewEditListener) {
                mDrawViewEditListener.noAreaCopied();
            }
            return;
        }

        saveCurrentState();
        isActionDownOutSideArea = false;
        isActionDownOutSideAreaMoving = false;
        mSelectionRect = null;
        mSelecting = true;
        mSelected = false;
        mIsMoving = false;

        RectF copiedSelectionRect = copiedAreaInfo.copiedSelectionRect;
        mTempMapDrawItems = copiedAreaInfo.mapDrawItems;
        mTempData = copiedAreaInfo.tempDrawLists;
        float left = copiedSelectionRect.left;
        float top = copiedSelectionRect.top;
        float right = copiedSelectionRect.right;
        float bottom = copiedSelectionRect.bottom;
        mSelectionRect = new RectF();
        mSelectionRect.set(0, 0, right - left, bottom - top);
//        mSelectionRect.set(0 + 1, 0 + 1, right - left - 1, bottom - top - 1);
        mSelecting = true;
        mSelected = true;
        mIsMoving = true;
        mOriginalSelectionRect = null;
        mCopiedAllDrawItems = getCopiedDataByColumn();
        updateWhenMoving();
        postInvalidate();

        if (null != mDrawViewEditListener) {
            mDrawViewEditListener.onPasteSelectedArea();
        }
    }

    public void inputWithSizeAndData(int textSize, List<DrawItem> textDrawItems) {
        CLog.i(TAG, "inputWithSizeAndData");
        saveCurrentState();
        isActionDownOutSideArea = false;
        isActionDownOutSideAreaMoving = false;
        mSelectionRect = null;
        mSelecting = true;
        mSelected = false;
        mIsMoving = false;
        mSelectionRect = new RectF();
        float left = 0;
        float top = 0;
        float right = mRectWidth * textSize;
        float bottom = mRectWidth * textSize;
        mSelectionRect.set(left, top, right, bottom);
//        mSelectionRect.set(left + 1, top + 1, right - 1, bottom - 1);
        CLog.i(TAG, "inputWithSizeAndData>>>textDrawItems  size>>>" + textDrawItems.size());
        mSelecting = true;
        mSelected = true;
        mIsMoving = true;
        mOriginalSelectionRect = null;
        initTempRectFWithData(textDrawItems);
        mCopiedAllDrawItems = getCopiedDataByColumn();

        updateWhenMoving();

        postInvalidate();
        if (null != mDrawViewEditListener) {
            mDrawViewEditListener.onInputWithSizeAndData();
        }
    }

    public void fillAreaModeOne(int color) {
        CLog.i(TAG, "fillAreaModeOne");
        if (null != mSelectionRect && mSelecting && mSelected) {
            saveCurrentState();
            for (DrawItem item : mTempData) {
                item.color = color;
                item.data = "true";
            }

            updateWhenMoving();

            postInvalidate();
            mSelecting = true;
            mSelected = true;
            mIsMoving = true;
            if (null != mDrawViewEditListener) {
                mDrawViewEditListener.onFillAreaModeOne();
            }
        } else {
            if (null != mDrawViewEditListener) {
                mDrawViewEditListener.noAreaSelected();
            }
        }
    }

    public void fillAreaModeTwo(int color) {
        CLog.i(TAG, "fillAreaModeTwo");
        if (null != mSelectionRect && mSelecting && mSelected) {
            saveCurrentState();
            for (DrawItem item : mTempData) {
                if (item.color == Color.BLACK || item.color == 0) {

                } else {
                    item.color = color;
                    item.data = "true";
                }
            }

            updateWhenMoving();

            postInvalidate();
            mSelecting = true;
            mSelected = true;
            mIsMoving = true;
            if (null != mDrawViewEditListener) {
                mDrawViewEditListener.onFillAreaModeTwo();
            }
        } else {
            if (null != mDrawViewEditListener) {
                mDrawViewEditListener.noAreaSelected();
            }
        }
    }

    public void mirrorModeHorizontalSelectedArea() {
        CLog.i(TAG, "mirrorModeHorizontalSelectedArea>>>");
        if (null != mSelectionRect && mSelecting && mSelected) {
            saveCurrentState();
            List<DrawItem> selectedDrawItems = getTempDrawItemsInChoosedArea();
            List<DrawItem> newSelectedDrawItems = new ArrayList<>();
            for (int i = (mTempColumn - 1); i >= 0; i--) {
                for (int j = 0; j < mTempRow; j++) {
                    int index = j * mTempColumn + i;
                    DrawItem item = mTempData.get(index);
                    newSelectedDrawItems.add(item.copy());
                }
            }

            CLog.i(TAG, "mirrorModeHorizontalSelectedArea>>>selectedDrawItems size>>>" + mTempData.size());
            CLog.i(TAG, "mirrorModeHorizontalSelectedArea>>>newSelectedDrawItems size>>>" + newSelectedDrawItems.size());

            for (int i = 0; i < selectedDrawItems.size(); i++) {
                selectedDrawItems.get(i).color = newSelectedDrawItems.get(i).color;
                selectedDrawItems.get(i).data = newSelectedDrawItems.get(i).data;
            }

            updateWhenMoving();

            postInvalidate();
            mSelecting = true;
            mSelected = true;
            mIsMoving = true;
            if (null != mDrawViewEditListener) {
                mDrawViewEditListener.onMirrorModeHorizontalSelectedArea();
            }
        } else {
            if (null != mDrawViewEditListener) {
                mDrawViewEditListener.noAreaSelected();
            }
        }
    }

    public void mirrorModeVerticalSelectedArea() {
        if (null != mSelectionRect && mSelecting && mSelected) {
            saveCurrentState();
            List<DrawItem> selectedDrawItems = getTempDrawItemsInChoosedArea();
            List<DrawItem> newSelectedDrawItems = new ArrayList<>();
            for (int i = 0; i < mTempColumn; i++) {
                for (int j = (mTempRow - 1); j >= 0; j--) {
                    int index = j * mTempColumn + i;
                    DrawItem item = mTempData.get(index);
                    newSelectedDrawItems.add(item.copy());
                }
            }

            CLog.i(TAG, "mirrorModeHorizontalSelectedArea>>>selectedDrawItems size>>>" + selectedDrawItems.size());
            CLog.i(TAG, "mirrorModeHorizontalSelectedArea>>>newSelectedDrawItems size>>>" + newSelectedDrawItems.size());

            for (int i = 0; i < selectedDrawItems.size(); i++) {
                selectedDrawItems.get(i).color = newSelectedDrawItems.get(i).color;
                selectedDrawItems.get(i).data = newSelectedDrawItems.get(i).data;
            }

            updateWhenMoving();

            postInvalidate();
            mSelecting = true;
            mSelected = true;
            mIsMoving = true;
            if (null != mDrawViewEditListener) {
                mDrawViewEditListener.onMirrorModeVerticalSelectedArea();
            }
        } else {
            if (null != mDrawViewEditListener) {
                mDrawViewEditListener.noAreaSelected();
            }
        }
    }

    List<DrawItem> mTempData = new ArrayList<>();
    RectF[][] mTempRectFs = null;
    RectF[][] mTempRectFsFull = null;
    int mTempColumn;
    int mTempRow;
    Map<Integer, DrawItem> mTempMapDrawItems;

    public void rotate90SelectedArea() {
        CLog.i(TAG, "rotate90SelectedArea  mSelectionRect>>>" + mSelectionRect);
        CLog.i(TAG, "rotate90SelectedArea  mSelecting>>>" + mSelecting);
        CLog.i(TAG, "rotate90SelectedArea  mSelected>>>" + mSelected);

        if (null != mSelectionRect && mSelecting && mSelected) {
            saveCurrentState();
            // 获取选区的左上角顶点
            float startX = mSelectionRect.left;
            float startY = mSelectionRect.top;
            float width = (mSelectionRect.right - mSelectionRect.left);
            float height = (mSelectionRect.bottom - mSelectionRect.top);

            // 计算选区的宽度和高度
            int selectedColumnCount = (int) ((mSelectionRect.width() + (cellSpace)) / mRectWidth);
            int selectedRowCount = (int) ((mSelectionRect.height() + (cellSpace)) / mRectWidth);

            CLog.i(TAG, "rotate90SelectedArea>>>selectedColumnCount>>>" + selectedColumnCount);
            CLog.i(TAG, "rotate90SelectedArea>>>selectedRowCount>>>" + selectedRowCount);

            List<DrawItem> selectedDrawItems = getCopiedTempDrawItemsInChoosedArea();
            CLog.i(TAG, "rotate90SelectedArea>>>selectedDrawItems size>>>" + selectedDrawItems.size());

            List<DrawItem> originalSelectedDrawItems = new ArrayList<>();

            for (int i = 0; i < mColumn; i++) {
                for (int j = 0; j < mRow; j++) {
                    RectF rect = mRectFsFull[i][j];
                    if (RectF.intersects(mSelectionRect, rect)) {
                        int index = j * mColumn + i;
                        DrawItem item = mData.get(index);
                        originalSelectedDrawItems.add(item);
                    }
                }
            }
            CLog.i(TAG, "rotate90SelectedArea>>>originalSelectedDrawItems size>>>" + originalSelectedDrawItems.size());

            for (DrawItem item : originalSelectedDrawItems) {
                item.color = 0;
                item.data = "true";
            }


            RectF newSelectionRect = new RectF();
            newSelectionRect.set(0, 0, height, width);

            mTempColumn = selectedRowCount;
            mTempRow = selectedColumnCount;
            int count = mTempColumn * mTempRow;
            CLog.i(TAG, "rotate90SelectedArea>>>mTempColumn>>>" + mTempColumn);
            CLog.i(TAG, "rotate90SelectedArea>>>mTempRow>>>" + mTempRow);
            CLog.i(TAG, "rotate90SelectedArea>>>count size>>>" + count);
            mTempData = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                mTempData.add(new DrawItem());
            }

            List<DrawItem> newSelectedDrawItems = new ArrayList<>();

            for (int j = 0; j < mTempRow; j++) {
                for (int i = (mTempColumn - 1); i >= 0; i--) {
                    int index = j * mTempColumn + i;
                    DrawItem item = mTempData.get(index);
                    newSelectedDrawItems.add(item);
                }
            }

            CLog.i(TAG, "rotate90SelectedArea>>>newSelectedDrawItems size>>>" + newSelectedDrawItems.size());

            for (int i = 0; i < newSelectedDrawItems.size(); i++) {
                newSelectedDrawItems.get(i).color = selectedDrawItems.get(i).color;
                newSelectedDrawItems.get(i).data = selectedDrawItems.get(i).data;
            }

            mTempMapDrawItems = new HashMap<>();
            for (int i = 0; i < mTempColumn; i++) {
                for (int j = 0; j < mTempRow; j++) {
                    int index = j * mTempColumn + i;
                    DrawItem item = mTempData.get(index);
                    mTempMapDrawItems.put(index, item);
                }
            }


            //重新计算选区大小，宽度和高度互换
            float newRight = (startX + (height));
            float newBottom = (startY + (width));
            mSelectionRect = new RectF();
            mSelectionRect.set(startX, startY, newRight, newBottom);

            updateWhenMoving();

            postInvalidate();  // 重绘
            mSelecting = true;
            mSelected = true;
            mIsMoving = true;
            if (null != mDrawViewEditListener) {
                mDrawViewEditListener.onRotate90SelectedArea();
            }
        } else {
            if (null != mDrawViewEditListener) {
                mDrawViewEditListener.noAreaSelected();
            }
        }
    }

    public void initUndoRedoStack(int size) {
        undoStackList = new ArrayList<>();
        redoStackList = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            undoStackList.add(new Stack<>());
            redoStackList.add(new Stack<>());
        }
    }


    public void removeUndoRedoStackIndex(int index) {
        undoStackList.remove(index);
        redoStackList.remove(index);
    }

    public void removeRedoUndoStackAll() {
        undoStackList.clear();
        redoStackList.clear();
        undoStack.clear();
        redoStack.clear();
    }

    public void swapRedoUndoStack(int fromPosition, int toPosition) {
        Collections.swap(undoStackList, fromPosition, toPosition);
        Collections.swap(redoStackList, fromPosition, toPosition);
    }

    public void insertAddRedoUndoStackAtIndex(int index) {
        undoStackList.add(index, new Stack<>());
        redoStackList.add(index, new Stack<>());
    }

    public void addAddRedoUndoStack() {
        undoStackList.add(new Stack<>());
        redoStackList.add(new Stack<>());
    }

    public void setRedoUndoStackIndex(int index) {
        CLog.i(TAG, "setRedoUndoStackIndex>>>" + index);
        undoStack = undoStackList.get(index);
        redoStack = redoStackList.get(index);
    }

    List<Stack<EditAction>> undoStackList = new ArrayList<>();
    List<Stack<EditAction>> redoStackList = new ArrayList<>();
    // 保存历史状态用于撤销
    Stack<EditAction> undoStack = new Stack<>();
    // 保存撤销状态用于重做
    Stack<EditAction> redoStack = new Stack<>();

    private static final int MAX_REDO_UNDO_COUNT = 20;

    private RectF copy(RectF input) {
        RectF result = new RectF();
        result.left = input.left;
        result.right = input.right;
        result.bottom = input.bottom;
        result.top = input.top;
        return result;
    }

    // 保存当前绘制状态到撤销栈
    private void saveCurrentState() {
        EditAction currentState = new EditAction();
        List<DrawItem> currentStateDrawItems = new ArrayList<>();
        for (DrawItem item : mData) {
            // 深拷贝绘制数据
            currentStateDrawItems.add(item.copy());
        }
        currentState.listDrawItem = currentStateDrawItems;
//        if (null != mSelectionRect) {
//            currentState.selectionRect = copy(mSelectionRect);
//        }
//        if (mSelecting) {
//            currentState.selecting = true;
//        } else {
//            currentState.selecting = false;
//        }

        // 检查撤销栈是否超过限制，限制在X个操作
        if (undoStack.size() >= MAX_REDO_UNDO_COUNT) {
            undoStack.remove(0); // 移除最早的状态
        }

        undoStack.push(currentState);

        // 如果有新操作，清空重做栈
        redoStack.clear();
    }


    public void undo() {
        if (!undoStack.isEmpty()) {
            // 将当前状态推入重做栈
            EditAction currentState = new EditAction();
            List<DrawItem> currentStateDrawItems = new ArrayList<>();
            for (DrawItem item : mData) {
                currentStateDrawItems.add(item.copy());
            }
            currentState.listDrawItem = currentStateDrawItems;
//            if (null != mSelectionRect) {
//                currentState.selectionRect = copy(mSelectionRect);
//            }
//            if (mSelecting) {
//                currentState.selecting = true;
//            } else {
//                currentState.selecting = false;
//            }

            if (redoStack.size() >= MAX_REDO_UNDO_COUNT) {
                redoStack.remove(0); // 移除最早的状态
            }

            // 将当前状态保存到重做栈
            redoStack.push(currentState);

            // 从撤销栈中取出上一个状态
            EditAction previousState = undoStack.pop();
//            mSelectionRect = previousState.selectionRect;
//            mSelecting = previousState.selecting;
            mData.clear();
            mData.addAll(previousState.listDrawItem); // 恢复到上一个状态
            setData(mData);
            clearSelectionRect();
            // 更新视图
            postInvalidate();
            if (null != mDrawViewEditListener) {
                mDrawViewEditListener.onUndo();
            }
        }
    }

    public void redo() {
        if (!redoStack.isEmpty()) {
            // 将当前状态推入撤销栈
            EditAction currentState = new EditAction();
            List<DrawItem> currentStateDrawItems = new ArrayList<>();
            for (DrawItem item : mData) {
                currentStateDrawItems.add(item.copy());
            }
            currentState.listDrawItem = currentStateDrawItems;
//            if (null != mSelectionRect) {
//                currentState.selectionRect = copy(mSelectionRect);
//            }
//            if (mSelecting) {
//                currentState.selecting = true;
//            } else {
//                currentState.selecting = false;
//            }

            if (undoStack.size() >= MAX_REDO_UNDO_COUNT) {
                undoStack.remove(0); // 移除最早的状态
            }

            undoStack.push(currentState);

            // 从重做栈中取出上一个状态
            EditAction redoState = redoStack.pop();
//            mSelectionRect = redoState.selectionRect;
//            mSelecting = redoState.selecting;
            mData.clear();
            mData.addAll(redoState.listDrawItem); // 恢复重做状态
            setData(mData);
            clearSelectionRect();
            // 更新视图
            postInvalidate();
            if (null != mDrawViewEditListener) {
                mDrawViewEditListener.onRedo();
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = getMySize(0, widthMeasureSpec);
        int height = getMySize(0, heightMeasureSpec);
        mScreenWidth = width;
        mScreenHeight = height;
        if (isForListItemShow) {
            if (isHeightFirst) {
                mRectWidth = (mScreenHeight) / (float) mRow;
                setMeasuredDimension((int) mScreenWidth, (int) mScreenHeight);
            } else {
                mRectWidth = (mScreenWidth) / (float) mColumn;
                int newScreenHeight = (int) (mRectWidth * mRow);
                setMeasuredDimension((int) mScreenWidth, newScreenHeight);
            }
        } else {
            if (isHeightFirst) {
                mRectWidth = (mScreenHeight) / (float) mRow;
                int newScreenWidth = (int) (mRectWidth * mColumn);
                if (newScreenWidth < mScreenWidth) {
                    setMeasuredDimension(newScreenWidth, height);
                } else {
                    setMeasuredDimension(width, height);
                }
            } else {
                mRectWidth = (mScreenWidth) / (float) mColumn;
                int newScreenHeight = (int) (mRectWidth * mRow);
                if (newScreenHeight <= mScreenHeight) {
                    setMeasuredDimension((int) mScreenWidth, newScreenHeight);
                } else {
                    mRectWidth = mScreenHeight / (float) mRow;
                    int newScreenWidth = (int) (mRectWidth * mColumn);
                    setMeasuredDimension(newScreenWidth, (int) mScreenHeight);
                }
            }
        }
        initData();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        try {
            canvas.drawColor(Color.TRANSPARENT);
            Paint paintUnSelected = new Paint();
            paintUnSelected.setColor(mDisSelectedColor);
            paintUnSelected.setStyle(Style.FILL);
            Paint paintSelected = new Paint();
            paintSelected.setColor(mSelectedColor);
            paintSelected.setStyle(Style.FILL);

            for (int i = 0; i < mColumn; i++) {
                for (int j = 0; j < mRow; j++) {
                    int index = j * mColumn + i;
                    DrawItem item = mData.get(index);
                    if (item.color == Color.BLACK || item.color == 0) {
                        paintSelected.setColor(mDisSelectedColor);
                    } else {
                        paintSelected.setColor(item.color);
                    }
                    canvas.drawRect(mRectFs[i][j], paintSelected);
                }
            }

            if (mSelecting && null != mSelectionRect) {
                RectF rect = new RectF();
                float left = mSelectionRect.left;
                float top = mSelectionRect.top;
                float right = mSelectionRect.right;
                float bottom = mSelectionRect.bottom;
                rect.set(left + 1, top + 1, right - 1, bottom - 1);
                Paint paintSelection = new Paint();
                paintSelection.setColor(Color.RED);
                paintSelection.setStyle(Style.STROKE);
                paintSelection.setStrokeWidth(1);
//                canvas.drawRect(mSelectionRect, paintSelection);
                canvas.drawRect(rect, paintSelection);
            }
        } catch (Exception e) {
            e.printStackTrace();
            CLog.i(TAG, "onDraw>>>exception>>>" + e.getMessage());
        }
    }

    private RectF mOriginalSelectionRect;

    /***
     * 获取一个临时选区  所有操作针对临时选区 操作然后同步到真实选区
     */
    private void initTempRectF() {
        try {
            float startX = mSelectionRect.left;
            float startY = mSelectionRect.top;
            float width = (mSelectionRect.right - mSelectionRect.left);
            float height = (mSelectionRect.bottom - mSelectionRect.top);
            // 计算选区的宽度和高度
            int selectedColumnCount = (int) ((mSelectionRect.width() + (cellSpace)) / mRectWidth);
            int selectedRowCount = (int) ((mSelectionRect.height() + (cellSpace)) / mRectWidth);


            CLog.i(TAG, "initTempRectF>>>selectedColumnCount>>>" + selectedColumnCount);
            CLog.i(TAG, "initTempRectF>>>selectedRowCount>>>" + selectedRowCount);

            mTempColumn = selectedColumnCount;
            mTempRow = selectedRowCount;
            int count = mTempColumn * mTempRow;
            mTempData = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                mTempData.add(new DrawItem());
            }

            /***
             * 获取第一次开始旋转之前的原始数据
             */

            List<DrawItem> selectedDrawItems = new ArrayList<>();

            for (int i = 0; i < mColumn; i++) {
                for (int j = 0; j < mRow; j++) {
                    RectF rect = mRectFs[i][j];
                    if (RectF.intersects(mSelectionRect, rect)) {
                        int index = j * mColumn + i;
                        DrawItem item = mData.get(index);
                        selectedDrawItems.add(item.copy());
                    }
                }
            }
            CLog.i(TAG, "initTempRectF>>>selectedDrawItems size>>>" + selectedDrawItems.size());


            RectF newSelectionRect = new RectF();
            newSelectionRect.set(0, 0, width, height);
            List<DrawItem> newSelectedDrawItems = new ArrayList<>();
            //将原始数据复制到左上角同样大小的区域内
            for (int i = 0; i < mTempColumn; i++) {
                for (int j = 0; j < mTempRow; j++) {
                    int index = j * mTempColumn + i;
                    DrawItem item = mTempData.get(index);
                    newSelectedDrawItems.add(item);
                }
            }

            CLog.i(TAG, "initTempRectF>>>newSelectedDrawItems>>>size>>>" + newSelectedDrawItems.size());

            for (int i = 0; i < newSelectedDrawItems.size(); i++) {
                newSelectedDrawItems.get(i).color = selectedDrawItems.get(i).color;
                newSelectedDrawItems.get(i).data = selectedDrawItems.get(i).data;
            }

            mTempMapDrawItems = new HashMap<>();
            for (int i = 0; i < mTempColumn; i++) {
                for (int j = 0; j < mTempRow; j++) {
                    int index = j * mTempColumn + i;
                    DrawItem item = mTempData.get(index);
                    mTempMapDrawItems.put(index, item);
                }
            }
        } catch (Exception e) {
            CLog.i(TAG, "initTempRectF>>>e>>>" + e.getMessage());
        }

    }

    private void initTempRectFWithData(List<DrawItem> textDrawItems) {
        try {
            float startX = mSelectionRect.left;
            float startY = mSelectionRect.top;
            float width = (mSelectionRect.right - mSelectionRect.left);
            float height = (mSelectionRect.bottom - mSelectionRect.top);
            // 计算选区的宽度和高度
            int selectedColumnCount = (int) ((mSelectionRect.width() + (cellSpace)) / mRectWidth);
            int selectedRowCount = (int) ((mSelectionRect.height() + (cellSpace)) / mRectWidth);


            CLog.i(TAG, "initTempRectF>>>selectedColumnCount>>>" + selectedColumnCount);
            CLog.i(TAG, "initTempRectF>>>selectedRowCount>>>" + selectedRowCount);

            mTempColumn = selectedColumnCount;
            mTempRow = selectedRowCount;
            int count = mTempColumn * mTempRow;
            mTempData = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                mTempData.add(new DrawItem());
            }


            List<DrawItem> tempDataList = new ArrayList<>();
            for (int i = 0; i < mTempColumn; i++) {
                for (int j = 0; j < mTempRow; j++) {
                    int index = j * mTempColumn + i;
                    DrawItem item = mTempData.get(index);
                    tempDataList.add(item);
                }
            }

            CLog.i(TAG, "initTempRectF>>>tempDataList>>>size>>>" + tempDataList.size());
            CLog.i(TAG, "initTempRectF>>>textDrawItems>>>size>>>" + textDrawItems.size());

            for (int i = 0; i < tempDataList.size(); i++) {
                tempDataList.get(i).color = textDrawItems.get(i).color;
                tempDataList.get(i).data = textDrawItems.get(i).data;
            }

            mTempMapDrawItems = new HashMap<>();
            for (int i = 0; i < mTempColumn; i++) {
                for (int j = 0; j < mTempRow; j++) {
                    int index = j * mTempColumn + i;
                    DrawItem item = mTempData.get(index);
                    mTempMapDrawItems.put(index, item);
                }
            }
        } catch (Exception e) {
            CLog.i(TAG, "initTempRectF>>>e>>>" + e.getMessage());
        }

    }

    private void updateChooseArea() {
        Map<Integer, DrawItem> movingSelectedDrawItemsMap = getOriginalSelectedDrawItemsMapInChoosedArea();

        for (Map.Entry<Integer, DrawItem> entry : mTempMapDrawItems.entrySet()) {
            int indexKey = entry.getKey();
            DrawItem drawItemValue = entry.getValue();
            int color = drawItemValue.color;
            if (movingSelectedDrawItemsMap.containsKey(indexKey)) {
                DrawItem drawItem = movingSelectedDrawItemsMap.get(indexKey);
                drawItem.color = drawItemValue.color;
                drawItem.data = drawItemValue.data;
            }
        }
    }

    public List<DrawItem> getTempDrawItemsInChoosedArea() {
        List<DrawItem> selectedDrawItems = new ArrayList<>();

        for (int i = 0; i < mTempColumn; i++) {
            for (int j = 0; j < mTempRow; j++) {
                int index = j * mTempColumn + i;
                DrawItem item = mTempData.get(index);
                selectedDrawItems.add(item);
            }
        }

        CLog.i(TAG, "getTempDrawItemsInChoosedArea  size>>>" + selectedDrawItems.size());

        return selectedDrawItems;
    }

    public List<DrawItem> getCopiedTempDrawItemsInChoosedArea() {
        List<DrawItem> selectedDrawItems = new ArrayList<>();

        for (int i = 0; i < mTempColumn; i++) {
            for (int j = 0; j < mTempRow; j++) {
                int index = j * mTempColumn + i;
                DrawItem item = mTempData.get(index);
                selectedDrawItems.add(item.copy());
            }
        }

        CLog.i(TAG, "getTempDrawItemsInChoosedArea  size>>>" + selectedDrawItems.size());

        return selectedDrawItems;
    }

    private void backToStateBeforeMoving() {
        List<DrawItem> allOriginalDrawItems = getDataByColumn();
        for (int i = 0; i < allOriginalDrawItems.size(); i++) {
            allOriginalDrawItems.get(i).color = mCopiedAllDrawItems.get(i).color;
            allOriginalDrawItems.get(i).data = mCopiedAllDrawItems.get(i).data;
        }

        if (null != mOriginalSelectionRect) {
            for (int i = 0; i < mColumn; i++) {
                for (int j = 0; j < mRow; j++) {
                    RectF rect = mRectFs[i][j];
                    if (RectF.intersects(mOriginalSelectionRect, rect)) {
                        mRectFlags[i][j] = true;
                        int index = j * mColumn + i;
                        DrawItem item = mData.get(index);
                        item.color = 0;
                        item.data = "true";
                    }
                }
            }
        }

    }

    private void updateWhenMoving() {
        List<DrawItem> allOriginalDrawItems = getDataByColumn();
        for (int i = 0; i < allOriginalDrawItems.size(); i++) {
            allOriginalDrawItems.get(i).color = mCopiedAllDrawItems.get(i).color;
            allOriginalDrawItems.get(i).data = mCopiedAllDrawItems.get(i).data;
        }


        if (null != mOriginalSelectionRect) {
            for (int i = 0; i < mColumn; i++) {
                for (int j = 0; j < mRow; j++) {
                    RectF rect = mRectFs[i][j];
                    if (RectF.intersects(mOriginalSelectionRect, rect)) {
                        mRectFlags[i][j] = true;
                        int index = j * mColumn + i;
                        DrawItem item = mData.get(index);
                        item.color = 0;
                        item.data = "true";
                    }
                }
            }
        }


        Map<Integer, DrawItem> movingSelectedDrawItemsMap = getOriginalSelectedDrawItemsMapInChoosedArea();

        for (Map.Entry<Integer, DrawItem> entry : mTempMapDrawItems.entrySet()) {
            int indexKey = entry.getKey();
            DrawItem drawItemValue = entry.getValue();
            int color = drawItemValue.color;
            if (color != 0 && color != Color.BLACK) {
                if (movingSelectedDrawItemsMap.containsKey(indexKey)) {
                    DrawItem drawItem = movingSelectedDrawItemsMap.get(indexKey);
                    drawItem.color = drawItemValue.color;
                    drawItem.data = drawItemValue.data;
                }
            }
        }

    }

    boolean isActionDownOutSideArea = false;
    boolean isActionDownOutSideAreaMoving = false;
    float lastLeft;
    float lastTop;
    float lastRight;
    float lastBottom;

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                if (!mMoveFlag) {
                    if (mClearFlag) {
                        CLog.i(TAG, "mClearFlag>>>");
                        saveCurrentState();
                    } else if (isDraw) {
                        CLog.i(TAG, "isDraw>>>");
                        saveCurrentState();
                    } else if (mSelected) {
                        /***
                         * 选取完成 点中选区之内代表要进行移动选区操作
                         */
                        saveCurrentState();
                        CLog.i(TAG, "location>>>1>>>");
                        if (!mIsMoving) {
                            CLog.i(TAG, "location>>>2>>>");

                            if (null != mSelectionRect &&
                                    !mSelectionRect.contains(event.getX(), event.getY())) {
                                CLog.i(TAG, "location>>>actionDown>>>1>>>");
                                isActionDownOutSideArea = true;
                                isActionDownOutSideAreaMoving = false;
                            }

                            mLastTouchX = event.getX();
                            mLastTouchY = event.getY();
                            mIsMoving = true;

                            for (int i = 0; i < mColumn; i++) {
                                for (int j = 0; j < mRow; j++) {
                                    RectF rect = mRectFsFull[i][j];
                                    if (rect.contains(mLastTouchX, mLastTouchY)) {
                                        mLastTouchX = rect.left;
                                        mLastTouchY = rect.top;
                                        break;
                                    }
                                }
                            }
//                            mOriginalSelectionRect = new RectF();
//                            mOriginalSelectionRect.left = mSelectionRect.left;
//                            mOriginalSelectionRect.right = mSelectionRect.right;
//                            mOriginalSelectionRect.bottom = mSelectionRect.bottom;
//                            mOriginalSelectionRect.top = mSelectionRect.top;
//                            // 获取选区内的所有内容
//                            mCopiedAllDrawItems = getCopiedDataByColumn();
                        } else {
                            if (null != mSelectionRect &&
                                    !mSelectionRect.contains(event.getX(), event.getY())) {
                                CLog.i(TAG, "location>>>3>>>");
                                isActionDownOutSideArea = true;
                                isActionDownOutSideAreaMoving = false;

                                mLastTouchX = event.getX();
                                mLastTouchY = event.getY();

                                for (int i = 0; i < mColumn; i++) {
                                    for (int j = 0; j < mRow; j++) {
                                        RectF rect = mRectFsFull[i][j];
                                        if (rect.contains(mLastTouchX, mLastTouchY)) {
                                            mLastTouchX = rect.left;
                                            mLastTouchY = rect.top;
                                            break;
                                        }
                                    }
                                }
                            } else {
                                CLog.i(TAG, "location>>>4>>>");
                                mLastTouchX = event.getX();
                                mLastTouchY = event.getY();

                                for (int i = 0; i < mColumn; i++) {
                                    for (int j = 0; j < mRow; j++) {
                                        RectF rect = mRectFsFull[i][j];
                                        if (rect.contains(mLastTouchX, mLastTouchY)) {
                                            mLastTouchX = rect.left;
                                            mLastTouchY = rect.top;
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    } else if (mIsMoving) {
                        CLog.i(TAG, "location>>>5>>>");
                        mLastTouchX = event.getX();
                        mLastTouchY = event.getY();
                        for (int i = 0; i < mColumn; i++) {
                            for (int j = 0; j < mRow; j++) {
                                RectF rect = mRectFsFull[i][j];
                                if (rect.contains(mLastTouchX, mLastTouchY)) {
                                    mLastTouchX = rect.left;
                                    mLastTouchY = rect.top;
                                    break;
                                }
                            }
                        }
                    } else if (mSelecting) {
                        CLog.i(TAG, "location>>>6>>>");
                        //先设置选区的左上角
                        mStartSelectX = event.getX();
                        mStartSelectY = event.getY();

                        for (int i = 0; i < mColumn; i++) {
                            for (int j = 0; j < mRow; j++) {
                                RectF rect = mRectFsFull[i][j];
                                if (rect.contains(mStartSelectX, mStartSelectY)) {
                                    lastLeft = rect.left;
                                    lastTop = rect.top;
                                    lastRight = rect.right;
                                    lastBottom = rect.bottom;
                                    mStartSelectX = rect.left;
                                    mStartSelectY = rect.top;
                                    break;
                                }
                            }
                        }

                        if (null == mSelectionRect) {
                            mSelectionRect = new RectF();
                        }
                        mSelectionRect.set(mStartSelectX, mStartSelectY, mStartSelectX, mStartSelectY);
                        postInvalidate();
                    }
                }
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                if (event.getPointerCount() == 2 && !mMoveFlag) {
                    // 当有两个手指按在屏幕上时，计算两指之间的距离
                    mLastFingerDis = distanceBetweenFingers(event);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (event.getPointerCount() == 1) {
                    if (mMoveFlag) {
                        // 只有单指按在屏幕上移动时，为拖动状态
                        float xMove = event.getX();
                        float yMove = event.getY();
                        if (mLastXMove == -1 && mLastYMove == -1) {
                            mLastXMove = xMove;
                            mLastYMove = yMove;
                        }
                        mCurrentStatus = STATUS_MOVE;
                        mMovedDistanceX = xMove - mLastXMove;
                        mMovedDistanceY = yMove - mLastYMove;

                        moveOrZoom();
                        mLastXMove = xMove;
                        mLastYMove = yMove;
                    } else if (isActionDownOutSideArea) {
                        CLog.i(TAG, "ACTION_UP>>>moving");
                        float tempX = mLastTouchX;
                        float tempY = mLastTouchY;
                        for (int i = 0; i < mColumn; i++) {
                            for (int j = 0; j < mRow; j++) {
                                RectF rect = mRectFsFull[i][j];
                                if (rect.contains(event.getX(), event.getY())) {
                                    tempX = rect.left;
                                    tempY = rect.top;
                                    break;
                                }
                            }
                        }
                        CLog.i(TAG, "movingSelection>>>tempX>>>" + tempX);
                        CLog.i(TAG, "movingSelection>>>tempY>>>" + tempY);
                        CLog.i(TAG, "movingSelection>>>mLastTouchX>>>" + mLastTouchX);
                        CLog.i(TAG, "movingSelection>>>mLastTouchY>>>" + mLastTouchY);
                        // 移动选区并检测边界
                        float dx = tempX - mLastTouchX;
                        float dy = tempY - mLastTouchY;
                        CLog.i(TAG, "movingSelection>>>dx>>>" + dx);
                        CLog.i(TAG, "movingSelection>>>dy>>>" + dy);
                        if (!isActionDownOutSideAreaMoving) {
                            if (dx == 0 && dy == 0) {
                                isActionDownOutSideAreaMoving = false;
                            } else {
                                isActionDownOutSideAreaMoving = true;
                            }
                        }


                        List<DrawItem> allOriginalDrawItems = getDataByColumn();
                        for (int i = 0; i < allOriginalDrawItems.size(); i++) {
                            allOriginalDrawItems.get(i).color = mCopiedAllDrawItems.get(i).color;
                            allOriginalDrawItems.get(i).data = mCopiedAllDrawItems.get(i).data;
                        }


                        if (null != mOriginalSelectionRect) {
                            for (int i = 0; i < mColumn; i++) {
                                for (int j = 0; j < mRow; j++) {
                                    RectF rect = mRectFs[i][j];
                                    if (RectF.intersects(mOriginalSelectionRect, rect)) {
                                        mRectFlags[i][j] = true;
                                        int index = j * mColumn + i;
                                        DrawItem item = mData.get(index);
                                        item.color = 0;
                                        item.data = "true";
                                    }
                                }
                            }
                        }


                        // 更新选区位置
                        mSelectionRect.offset(dx, dy);
                        CLog.i(TAG, "movingSelection>>>" + mSelectionRect);
                        CLog.i(TAG, "AcionDownOutSideAreaWhenMoving>>>");
                        Map<Integer, DrawItem> movingSelectedDrawItemsMap = getOriginalSelectedDrawItemsMapInChoosedArea();

                        for (Map.Entry<Integer, DrawItem> entry : mTempMapDrawItems.entrySet()) {
                            int indexKey = entry.getKey();
                            DrawItem drawItemValue = entry.getValue();
                            int color = drawItemValue.color;
                            if (color != 0 && color != Color.BLACK) {
                                if (movingSelectedDrawItemsMap.containsKey(indexKey)) {
                                    DrawItem drawItem = movingSelectedDrawItemsMap.get(indexKey);
                                    drawItem.color = drawItemValue.color;
                                    drawItem.data = drawItemValue.data;
                                }
                            }
                        }


                        mLastTouchX = tempX;
                        mLastTouchY = tempY;
                        postInvalidate();
                        if (null != mDrawViewEditListener) {
                            mDrawViewEditListener.onAreaMoving();
                        }
                    } else if (mIsMoving) {
                        float tempX = mLastTouchX;
                        float tempY = mLastTouchY;
                        for (int i = 0; i < mColumn; i++) {
                            for (int j = 0; j < mRow; j++) {
                                RectF rect = mRectFsFull[i][j];
                                if (rect.contains(event.getX(), event.getY())) {
                                    tempX = rect.left;
                                    tempY = rect.top;
                                    break;
                                }
                            }
                        }


                        CLog.i(TAG, "movingSelection>>>tempX>>>" + tempX);
                        CLog.i(TAG, "movingSelection>>>tempY>>>" + tempY);
                        CLog.i(TAG, "movingSelection>>>mLastTouchX>>>" + mLastTouchX);
                        CLog.i(TAG, "movingSelection>>>mLastTouchY>>>" + mLastTouchY);
                        // 移动选区并检测边界
                        float dx = tempX - mLastTouchX;
                        float dy = tempY - mLastTouchY;
                        CLog.i(TAG, "movingSelection>>>dx>>>" + dx);
                        CLog.i(TAG, "movingSelection>>>dy>>>" + dy);

                        List<DrawItem> allOriginalDrawItems = getDataByColumn();
                        for (int i = 0; i < allOriginalDrawItems.size(); i++) {
                            allOriginalDrawItems.get(i).color = mCopiedAllDrawItems.get(i).color;
                            allOriginalDrawItems.get(i).data = mCopiedAllDrawItems.get(i).data;
                        }


                        if (null != mOriginalSelectionRect) {
                            for (int i = 0; i < mColumn; i++) {
                                for (int j = 0; j < mRow; j++) {
                                    RectF rect = mRectFs[i][j];
                                    if (RectF.intersects(mOriginalSelectionRect, rect)) {
                                        mRectFlags[i][j] = true;
                                        int index = j * mColumn + i;
                                        DrawItem item = mData.get(index);
                                        item.color = 0;
                                        item.data = "true";
                                    }
                                }
                            }
                        }

                        // 更新选区位置
                        mSelectionRect.offset(dx, dy);
                        CLog.i(TAG, "movingSelection>>>" + mSelectionRect);
                        CLog.i(TAG, "movingSelection>>>" + mSelectionRect);
                        CLog.i(TAG, "moving>>>");
                        Map<Integer, DrawItem> movingSelectedDrawItemsMap = getOriginalSelectedDrawItemsMapInChoosedArea();

                        for (Map.Entry<Integer, DrawItem> entry : mTempMapDrawItems.entrySet()) {
                            int indexKey = entry.getKey();
                            DrawItem drawItemValue = entry.getValue();
                            int color = drawItemValue.color;
                            if (color != 0 && color != Color.BLACK) {
                                if (movingSelectedDrawItemsMap.containsKey(indexKey)) {
                                    DrawItem drawItem = movingSelectedDrawItemsMap.get(indexKey);
                                    drawItem.color = drawItemValue.color;
                                    drawItem.data = drawItemValue.data;
                                }
                            }
                        }


                        mLastTouchX = tempX;
                        mLastTouchY = tempY;
                        postInvalidate();
                        if (null != mDrawViewEditListener) {
                            mDrawViewEditListener.onAreaMoving();
                        }
                    } else if (mSelecting) {
                        //设置选择区域的右下角
                        for (int i = 0; i < mColumn; i++) {
                            for (int j = 0; j < mRow; j++) {
                                RectF rect = mRectFsFull[i][j];
                                float tempX = event.getX();
                                float tempY = event.getY();
                                if (rect.contains(tempX, tempY)) {
                                    if (tempX < mStartSelectX && tempY < mStartSelectY) {
                                        mSelectionRect.left = rect.left;
                                        mSelectionRect.top = rect.top;
                                        mSelectionRect.right = lastRight;
                                        mSelectionRect.bottom = lastBottom;
                                    } else if (tempX > mStartSelectX && tempY > mStartSelectY) {
                                        mSelectionRect.left = lastLeft;
                                        mSelectionRect.top = lastTop;
                                        mSelectionRect.right = rect.right;
                                        mSelectionRect.bottom = rect.bottom;
                                    } else if (tempX > mStartSelectX && tempY < mStartSelectY) {
                                        mSelectionRect.left = lastLeft;
                                        mSelectionRect.top = rect.top;
                                        mSelectionRect.right = rect.right;
                                        mSelectionRect.bottom = lastBottom;
                                    } else if (tempX < mStartSelectX && tempY > mStartSelectY) {
                                        mSelectionRect.left = rect.left;
                                        mSelectionRect.top = lastTop;
                                        mSelectionRect.right = lastRight;
                                        mSelectionRect.bottom = rect.bottom;
                                    }
                                    break;
                                }
                            }
                        }
                        postInvalidate();
                    } else {
                        if (mClearFlag) {
                            clearWithMove(event);
                        } else if (isDraw) {
                            drawWithMove(event);
                        }
                    }
                } else if (event.getPointerCount() == 2 && !mMoveFlag) {
                    // 有两个手指按在屏幕上移动时，为缩放状态
                    centerPointBetweenFingers(event);
                    double fingerDis = distanceBetweenFingers(event);
                    if (fingerDis > mLastFingerDis) {
                        mCurrentStatus = STATUS_ZOOM_OUT;
                    } else {
                        mCurrentStatus = STATUS_ZOOM_IN;
                    }
                    // 进行缩放倍数检查，最大只允许将图片放大4倍，最小可以缩小到初始化比例
                    if ((mCurrentStatus == STATUS_ZOOM_OUT && mTotalRatio < 4 * mInitRatio) || (mCurrentStatus == STATUS_ZOOM_IN)) {
                        mScaledRatio = (float) (fingerDis / mLastFingerDis);
                        mTotalRatio = mTotalRatio * mScaledRatio;
                        if (mTotalRatio > 4 * mInitRatio) {
                            mTotalRatio = 4 * mInitRatio;
                        }
                        moveOrZoom();
                        mLastFingerDis = fingerDis;
                    }
                }
                break;
            case MotionEvent.ACTION_POINTER_UP:
                if (event.getPointerCount() == 2 && !mMoveFlag) {
                    // 手指离开屏幕时将临时值还原
                    mLastXMove = -1;
                    mLastYMove = -1;
                }

                break;
            case MotionEvent.ACTION_UP:
                // 手指离开屏幕时将临时值还原
                mLastXMove = -1;
                mLastYMove = -1;

                if (mSelecting && !mSelected) {
                    CLog.i(TAG, "ACTION_UP>>>1");
                    //选择完成代表选区动作完成
                    mSelected = true;
                    mOriginalSelectionRect = new RectF();
                    mOriginalSelectionRect.left = mSelectionRect.left;
                    mOriginalSelectionRect.right = mSelectionRect.right;
                    mOriginalSelectionRect.bottom = mSelectionRect.bottom;
                    mOriginalSelectionRect.top = mSelectionRect.top;
                    // 获取选区内的所有内容
                    mCopiedAllDrawItems = getCopiedDataByColumn();
                    initTempRectF();
                    postInvalidate();
                } else if (isActionDownOutSideArea) {
                    CLog.i(TAG, "ACTION_UP>>>2");
                    CLog.i(TAG, "ACTION_UP>>>isActionDownOutSideAreaMoving>>>" + isActionDownOutSideAreaMoving);
                    if (!isActionDownOutSideAreaMoving) {
                        CLog.i(TAG, "ACTION_UP>>>3");
                        isActionDownOutSideArea = false;
                        isActionDownOutSideAreaMoving = false;
                        mSelectionRect = null;
                        mSelecting = true;
                        mSelected = false;
                        mIsMoving = false;
                        postInvalidate();
                    } else {
                        isActionDownOutSideArea = false;
                        isActionDownOutSideAreaMoving = false;
                    }
                } else if (mIsMoving) {
                    CLog.i(TAG, "ACTION_UP>>>4");
                    postInvalidate();
                } else {
                    if (mClearFlag) {
                        if (null != mDrawViewEditListener) {
                            mDrawViewEditListener.onOneTimeDrawFinish();
                        }
                    } else if (isDraw) {
                        if (null != mDrawViewEditListener) {
                            mDrawViewEditListener.onOneTimeDrawFinish();
                        }
                    }
                }
                break;
            default:
                break;
        }
        return true;
    }

    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        if (visibility == VISIBLE) {
            // 窗口变为可见
            CLog.i(TAG, "onWindowVisibilityChanged>>>Window is visible");
            if (mCurrentIndex >= 0 && null != mDatas && mDatas.size() > 1) {
                CLog.i(TAG, "onWindowVisibilityChanged>>>Window is visible>>>udpateView");
                mHandler.sendEmptyMessageDelayed(MESSAGE_UPDATE, mSpeed);
            }
        } else {
            // 窗口变为不可见
            CLog.i(TAG, "onWindowVisibilityChanged>>>Window is not visible");
            if (mCurrentIndex >= 0 && null != mDatas && mDatas.size() > 1) {
                CLog.i(TAG, "onWindowVisibilityChanged>>>Window is visible>>>not udpateView");
                mHandler.removeMessages(MESSAGE_UPDATE);
            }
        }
    }

    protected void moveOrZoom() {
        switch (mCurrentStatus) {
            case STATUS_ZOOM_OUT:
            case STATUS_ZOOM_IN:
                zoom();
                break;
            case STATUS_MOVE:
                move();
                break;
            case STATUS_INIT:
            default:
                break;
        }
    }

    public void setScale(float delta) {
        if (mMoveFlag) {
            return;
        }

        if (delta < 0) {
            if (mTotalRatio <= 0.5) {
                return;
            }
        }

        if (delta > 0) {
            mCurrentStatus = STATUS_ZOOM_OUT;
        } else {
            mCurrentStatus = STATUS_ZOOM_IN;
        }
        // 进行缩放倍数检查，最大只允许将图片放大4倍，最小可以缩小到初始化比例
        if ((mCurrentStatus == STATUS_ZOOM_OUT && mTotalRatio < 4 * mInitRatio) || (mCurrentStatus == STATUS_ZOOM_IN)) {
            mTotalRatio = mTotalRatio + delta;
            if (mTotalRatio > 4 * mInitRatio) {
                mTotalRatio = 4 * mInitRatio;
            }
            moveOrZoom();
        }

    }

    private void zoom() {
        ViewHelper.setScaleX(this, mTotalRatio);
        ViewHelper.setScaleY(this, mTotalRatio);
        if (null != mListener) {
            mListener.onScale(Math.abs(mTotalRatio));
        }
    }

    private void move() {
        // 根据手指移动的距离计算出总偏移值
        float translateX = mTotalTranslateX + mMovedDistanceX;
        float translateY = mTotalTranslateY + mMovedDistanceY;
        ViewHelper.setX(this, getX() + translateX);
        ViewHelper.setY(this, getY() + translateY);
        mTotalTranslateX = translateX;
        mTotalTranslateY = translateY;
    }


    private void drawWithDown(MotionEvent event) {
        if (event.getX() > mStartX && event.getX() < mStartX + mColumn * mRectWidth && event.getY() > mStartY && event.getY() < mStartY + mRow * mRectWidth) {
            rectOperationDown(event.getX(), event.getY());
            invalidate();
        }
    }

    private void drawWithMove(MotionEvent event) {
        if (event.getX() > mStartX && event.getX() < mStartX + mColumn * mRectWidth && event.getY() > mStartY && event.getY() < mStartY + mRow * mRectWidth) {
            rectOperationMove(event.getX(), event.getY());
            invalidate();
        }
    }

    private void clearWithMove(MotionEvent event) {
        if (event.getX() > mStartX && event.getX() < mStartX + mColumn * mRectWidth && event.getY() > mStartY && event.getY() < mStartY + mRow * mRectWidth) {
            rectOperationClear(event.getX(), event.getY());
            invalidate();
        }
    }

    private void rectOperationClear(float inputX, float inputY) {
        int numX = (int) ((int) (inputX - mStartX) / mRectWidth);
        int numY = (int) ((int) (inputY - mStartY) / mRectWidth);
        mRectFlags[numX][numY] = false;
        if (null != mListener) {
            mListener.onItemClear(numY * mColumn + numX);
        }
        CLog.i(TAG, "rectOperationClear>>>" + (numY * mColumn + numX));
        CLog.i(TAG, "rectOperationClear>>>" + "numX>>>" + numX + "numY>>>" + numY);
    }

    private void rectOperationMove(float inputX, float inputY) {
        int numX = (int) ((int) (inputX - mStartX) / mRectWidth);
        int numY = (int) ((int) (inputY - mStartY) / mRectWidth);
        mRectFlags[numX][numY] = true;
        if (null != mListener) {
            mListener.onItemMove(numY * mColumn + numX);
        }
        CLog.i(TAG, "rectOperationMove>>>" + (numY * mColumn + numX));
        CLog.i(TAG, "rectOperationMove>>>" + "numX>>>" + numX + "numY>>>" + numY);
    }

    private void rectOperationDown(float inputX, float inputY) {
        int numX = (int) ((int) (inputX - mStartX) / mRectWidth);
        int numY = (int) ((int) (inputY - mStartY) / mRectWidth);
        mRectFlags[numX][numY] = !mRectFlags[numX][numY];
        if (null != mListener) {
            mListener.onItemClick(numY * mColumn + numX);
        }
        CLog.i(TAG, "rectOperationDown>>>" + (numY * mColumn + numX));
        CLog.i(TAG, "rectOperationDown>>>" + "numX>>>" + numX + "numY>>>" + numY);
    }

    /**
     * 计算两个手指之间的距离。
     *
     * @param event
     * @return 两个手指之间的距离
     */
    private double distanceBetweenFingers(MotionEvent event) {
        float disX = Math.abs(event.getX(0) - event.getX(1));
        float disY = Math.abs(event.getY(0) - event.getY(1));
        return Math.sqrt(disX * disX + disY * disY);
    }

    /**
     * 计算两个手指之间中心点的坐标。
     *
     * @param event
     */
    private void centerPointBetweenFingers(MotionEvent event) {
        float xPoint0 = event.getX(0);
        float yPoint0 = event.getY(0);
        float xPoint1 = event.getX(1);
        float yPoint1 = event.getY(1);
        mCenterPointX = (xPoint0 + xPoint1) / 2;
        mCenterPointY = (yPoint0 + yPoint1) / 2;
    }

    public static class CopiedAreaInfo {
        public Map<Integer, DrawItem> mapDrawItems = new HashMap<>();
        public List<DrawItem> tempDrawLists = new ArrayList<>();
        public RectF copiedSelectionRect = new RectF();
    }

    public static class ListAnimationDatas implements Serializable, Parcelable {
        private static final long serialVersionUID = -1180276265504479558L;
        public List<AnimationDatas> animationDatas;

        public ListAnimationDatas(List<AnimationDatas> animationDatas) {
            this.animationDatas = animationDatas;
        }

        public ListAnimationDatas() {
        }

        protected ListAnimationDatas(Parcel in) {
            animationDatas = in.createTypedArrayList(AnimationDatas.CREATOR);
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeTypedList(animationDatas);
        }

        @Override
        public int describeContents() {
            return 0;
        }

        public static final Creator<ListAnimationDatas> CREATOR = new Creator<ListAnimationDatas>() {
            @Override
            public ListAnimationDatas createFromParcel(Parcel in) {
                return new ListAnimationDatas(in);
            }

            @Override
            public ListAnimationDatas[] newArray(int size) {
                return new ListAnimationDatas[size];
            }
        };

        public List<AnimationDatas> getAnimationDatas() {
            return animationDatas;
        }

        public void setAnimationDatas(List<AnimationDatas> animationDatas) {
            this.animationDatas = animationDatas;
        }

        @Override
        public String toString() {
            return "ListAnimationDatas{" + "animationDatas=" + animationDatas + '}';
        }
    }

    public static class AnimationDatas implements Serializable, Parcelable {
        private static final long serialVersionUID = 6940560134677241494L;
        public List<AnimationData> showData;
        public int animationNumber;
        public long speed;

        public AnimationDatas(List<AnimationData> showData, int animationNumber, long speed) {
            this.showData = showData;
            this.animationNumber = animationNumber;
            this.speed = speed;
        }

        public AnimationDatas() {
        }

        protected AnimationDatas(Parcel in) {
            showData = in.createTypedArrayList(AnimationData.CREATOR);
            animationNumber = in.readInt();
            speed = in.readLong();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeTypedList(showData);
            dest.writeInt(animationNumber);
            dest.writeLong(speed);
        }

        @Override
        public int describeContents() {
            return 0;
        }

        public static final Creator<AnimationDatas> CREATOR = new Creator<AnimationDatas>() {
            @Override
            public AnimationDatas createFromParcel(Parcel in) {
                return new AnimationDatas(in);
            }

            @Override
            public AnimationDatas[] newArray(int size) {
                return new AnimationDatas[size];
            }
        };

        @Override
        public String toString() {
            return "AnimationDatas{" + "showData=" + showData + ", animationNumber=" + animationNumber + ", speed=" + speed + '}';
        }

        public AnimationDatas copy() {
            AnimationDatas outer = null;
            try { // 将该对象序列化成流,因为写在流里的是对象的一个拷贝，而原对象仍然存在于JVM里面。所以利用这个特性可以实现对象的深拷贝
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(baos);
                oos.writeObject(this);// 将流序列化成对象
                ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
                ObjectInputStream ois = new ObjectInputStream(bais);
                outer = (AnimationDatas) ois.readObject();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            return outer;
        }
    }

    public static class ListAnimationData implements Serializable {
        private static final long serialVersionUID = 673137767979815771L;
        public List<AnimationData> animationDatas;

        public ListAnimationData(List<AnimationData> animationDatas) {
            this.animationDatas = animationDatas;
        }

        public ListAnimationData() {
        }

        public List<AnimationData> getAnimationDatas() {
            return animationDatas;
        }

        public void setAnimationDatas(List<AnimationData> animationDatas) {
            this.animationDatas = animationDatas;
        }

        public ListAnimationData copy() {
            ListAnimationData outer = null;
            try { // 将该对象序列化成流,因为写在流里的是对象的一个拷贝，而原对象仍然存在于JVM里面。所以利用这个特性可以实现对象的深拷贝
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(baos);
                oos.writeObject(this);// 将流序列化成对象
                ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
                ObjectInputStream ois = new ObjectInputStream(bais);
                outer = (ListAnimationData) ois.readObject();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            return outer;
        }

        @Override
        public String toString() {
            return "ListAnimationData{" + "animationDatas=" + animationDatas + '}';
        }
    }

    public static class AnimationData implements Serializable, Parcelable {
        private static final long serialVersionUID = -4122926598315173069L;
        public List<DrawItem> drawItems;

        public AnimationData(List<DrawItem> drawItems) {
            this.drawItems = drawItems;
        }

        public AnimationData() {
        }

        public List<DrawItem> getDrawItems() {
            return drawItems;
        }

        public void setDrawItems(List<DrawItem> drawItems) {
            this.drawItems = drawItems;
        }

        protected AnimationData(Parcel in) {
            drawItems = in.createTypedArrayList(DrawItem.CREATOR);
        }

        public static final Creator<AnimationData> CREATOR = new Creator<AnimationData>() {
            @Override
            public AnimationData createFromParcel(Parcel in) {
                return new AnimationData(in);
            }

            @Override
            public AnimationData[] newArray(int size) {
                return new AnimationData[size];
            }
        };

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeTypedList(drawItems);
        }

        public AnimationData copy() {
            AnimationData outer = null;
            try { // 将该对象序列化成流,因为写在流里的是对象的一个拷贝，而原对象仍然存在于JVM里面。所以利用这个特性可以实现对象的深拷贝
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(baos);
                oos.writeObject(this);// 将流序列化成对象
                ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
                ObjectInputStream ois = new ObjectInputStream(bais);
                outer = (AnimationData) ois.readObject();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            return outer;
        }

        @Override
        public String toString() {
            return "AnimationData{" + "drawItems=" + drawItems + '}';
        }
    }


    public static class Draw32Item implements Serializable {
        public int color;
    }


    public static class DrawItem implements Serializable, Parcelable {
        private static final long serialVersionUID = 7174582783687958632L;
        public String data;
        public List<String> colors;
        public int color;
        public int index;

        public DrawItem() {
        }

        public DrawItem(String data) {
            this.data = data;
        }

        public DrawItem(String data, List<String> colors) {
            this.data = data;
            this.colors = colors;
        }

        public DrawItem(String data, List<String> colors, int color) {
            this.data = data;
            this.colors = colors;
            this.color = color;
        }

        protected DrawItem(Parcel in) {
            data = in.readString();
            colors = in.createStringArrayList();
            color = in.readInt();
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(data);
            dest.writeStringList(colors);
            dest.writeInt(color);
        }

        public static final Creator<DrawItem> CREATOR = new Creator<DrawItem>() {
            @Override
            public DrawItem createFromParcel(Parcel in) {
                return new DrawItem(in);
            }

            @Override
            public DrawItem[] newArray(int size) {
                return new DrawItem[size];
            }
        };

        @Override
        public String toString() {
            return "DrawItem{" + "data='" + data + '\'' + ", colors=" + colors + ", color=" + color + '}';
        }

        public DrawItem copy() {
            DrawItem outer = null;
            try { // 将该对象序列化成流,因为写在流里的是对象的一个拷贝，而原对象仍然存在于JVM里面。所以利用这个特性可以实现对象的深拷贝
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(baos);
                oos.writeObject(this);// 将流序列化成对象
                ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
                ObjectInputStream ois = new ObjectInputStream(bais);
                outer = (DrawItem) ois.readObject();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            return outer;
        }
    }

    public static class DrawListData implements Serializable {
        private static final long serialVersionUID = 1065129318655329262L;
        public List<DrawItem> listData;
        public List<List<DrawItem>> listDatas;
        public long speed;
        public boolean isChinese;

        public DrawListData(List<DrawItem> listData, List<List<DrawItem>> listDatas, long speed) {
            this.listData = listData;
            this.listDatas = listDatas;
            this.speed = speed;
        }

        public DrawListData(List<DrawItem> listData, List<List<DrawItem>> listDatas) {
            this.listData = listData;
            this.listDatas = listDatas;
        }

        public DrawListData(List<DrawItem> listData, List<List<DrawItem>> listDatas, long speed, boolean isChinese) {
            this.listData = listData;
            this.listDatas = listDatas;
            this.speed = speed;
            this.isChinese = isChinese;
        }

        public DrawListData(List<DrawItem> listData) {
            this.listData = listData;
        }

        public DrawListData() {
        }

        public DrawListData copy() {
            DrawListData outer = null;
            try { // 将该对象序列化成流,因为写在流里的是对象的一个拷贝，而原对象仍然存在于JVM里面。所以利用这个特性可以实现对象的深拷贝
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(baos);
                oos.writeObject(this);// 将流序列化成对象
                ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
                ObjectInputStream ois = new ObjectInputStream(bais);
                outer = (DrawListData) ois.readObject();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            return outer;
        }

        @Override
        public String toString() {
            return "DrawListData{" + "listData=" + listData + ", listDatas=" + listDatas + ", showCount=" + speed + ", isChinese=" + isChinese + '}';
        }
    }


    public static class DrawListDatas implements Serializable {
        private static final long serialVersionUID = 5260325503632649839L;
        public List<DrawListData> data = new ArrayList<>();

        public DrawListDatas(List<DrawListData> data) {
            this.data = data;
        }

        public DrawListDatas() {
        }

        @Override
        public String toString() {
            return "DrawListDatas{" + "data=" + data + '}';
        }
    }

    public static class EditAction {
        public List<DrawItem> listDrawItem;
        public RectF selectionRect;
        public boolean selecting;
    }
}