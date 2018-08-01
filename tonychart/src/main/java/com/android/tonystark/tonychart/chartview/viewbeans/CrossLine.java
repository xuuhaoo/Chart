package com.android.tonystark.tonychart.chartview.viewbeans;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.view.MotionEvent;

import com.android.tonystark.tonychart.chartview.interfaces.UnabelFocusedsView;

import java.util.List;

/**
 * 描述：十字线
 *
 * @author xuhao
 * @version 1.0
 */
public class CrossLine extends ViewContainer<String> implements UnabelFocusedsView {
    //线画笔
    private Paint mLinePaint = null;
    //点画笔
    private Paint mPointPaint = null;
    //当前下标
    private int mIndex = 0;
    //线颜色
    private int mLineColor = Color.BLACK;
    //点颜色
    private int mPointColor = Color.BLACK;
    //焦点
    private PointF mPointF = new PointF();
    //每个点的偏移量,保证十字在每个点的中间
    private float mSinglePointOffset = 0f;
    //点的半径
    private int mRadius = 10;
    //是否显示点
    private boolean isShowPoint = false;
    //是否显示纬线
    private boolean isShowLatitude = true;
    //是否显示经线
    private boolean isShowLongitude = true;
    //是否十字线纬线跟随数据
    private boolean isLatitudeFollowData = true;
    //是否十字线经线跟随数据
    private boolean isLongitudeFollowData = true;
    //每个点的宽度
    private float mPointWidth = 0;
    //十字拖动监听器
    private OnCrossLineMoveListener mOnCrossLineMoveListener = null;
    //当前聚焦的View
    private ViewContainer mFocusedView;

    public CrossLine(Context context) {
        super(context);
        this.isShow = false;
        initPaint();
    }

    //初始化画笔
    private void initPaint() {
        mLinePaint = new Paint();
        mLinePaint.setAntiAlias(true);
        mLinePaint.setStyle(Paint.Style.STROKE);
        mLinePaint.setStrokeWidth(2);
        mLinePaint.setColor(mLineColor);

        mPointPaint = new Paint();
        mPointPaint.setAntiAlias(true);
        mPointPaint.setStyle(Paint.Style.FILL);
        mPointPaint.setColor(mPointColor);
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        try {
            if (isShow) {
                checkParameter();
                if (initFocusedView()) return;
                //计算点的宽度
                mPointWidth = (mCoordinateWidth - mCoordinateMarginLeft) / mShownPointNums;
                //计算触摸的
                mIndex = (int) ((mPointF.x - mCoordinateMarginLeft) / mPointWidth);
                if (mIndex >= mShownPointNums) {
                    mIndex = mShownPointNums - 1;
                }
                //尽在显示区域内绘制十字线
                if (mDrawPointIndex + mIndex < mDrawPointIndex + mShownPointNums) {
                    float currentValue = mFocusedView.transDataToCrossDataFromDataList(mIndex, mIndex + mDrawPointIndex);
                    if (isShowLatitude) {
                        //绘制纬线
                        drawLatitude(canvas, mIndex, currentValue);
                    }
                    if (isShowLongitude) {
                        //绘制经线
                        drawLongitude(canvas, mIndex);
                    }
                    if (isShowPoint) {
                        //绘制点
                        drawCircle(canvas, mIndex, currentValue);
                    }
                }
            }
        } catch (Exception ignored) {
        }
    }

    private boolean initFocusedView() {
        ChartView chartView = getChartView();
        if (chartView == null) {
            return true;
        }

        mFocusedView = chartView.getFocusedView();
        if (mFocusedView == null) {
            return true;
        }
        return false;
    }

    private void checkParameter() {
        if (this.mShownPointNums < 0) {
            throw new IllegalArgumentException("maxPointNum must be larger than 0");
        }
        if (this.mCoordinateHeight <= 0) {
            throw new IllegalArgumentException("mCoordinateHeight can't be zero or smaller than zero");
        }
        if (this.mCoordinateWidth <= 0) {
            throw new IllegalArgumentException("mCoordinateWidth can't be zero or smaller than zero");
        }
        if (mPointF.x < 0f && mPointF.y < 0f) {
            throw new IllegalArgumentException("mPointF.x mPointF.y,must bigger than -1");
        }
    }

    //绘制纬线
    private void drawLatitude(Canvas canvas, int index, float currentValue) {
        float y = mPointF.y;
        if (isLatitudeFollowData) {
            try {
                y = (1f - (currentValue - mYMin) / (mYMax - mYMin)) * mCoordinateHeight;
            } catch (NumberFormatException e) {
                y = mPointF.y;
            }
        }
        canvas.drawLine(mCoordinateMarginLeft, y, mCoordinateWidth, y, mLinePaint);
    }

    //绘制经线
    private void drawLongitude(Canvas canvas, int index) {
        float x = mPointF.x;
        if (isLongitudeFollowData) {
            x = index * mPointWidth + mCoordinateMarginLeft + mSinglePointOffset;
        }
        canvas.drawLine(x, 0, x, mCoordinateHeight, mLinePaint);
    }

    private void drawCircle(Canvas canvas, int index, float currentValue) {
        float x = mPointF.x;
        float y = mPointF.y;
        try {
            x = index * mPointWidth + mCoordinateMarginLeft + mSinglePointOffset;
        } catch (NumberFormatException e) {
            x = mPointF.x;
        }
        try {
            y = (1f - (currentValue - mYMin) / (mYMax - mYMin)) * mCoordinateHeight;
        } catch (NumberFormatException e) {
            y = mPointF.y;
        }
        canvas.drawCircle(x, y, mRadius, mPointPaint);
    }

    @Override
    public void move(MotionEvent event) {
        int index;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (initFocusedView()) return;
                mPointF.x = event.getX();
                mPointF.x = mPointF.x < mCoordinateMarginLeft ? mCoordinateMarginLeft : mPointF.x;
                mPointWidth = (mCoordinateWidth - mCoordinateMarginLeft) / mShownPointNums;
                index = (int) ((mPointF.x - mCoordinateMarginLeft) / mPointWidth);
                if (mFocusedView.getDataListSize() > 0 && index > mFocusedView.getDataListSize() - 1) {
                    index = mFocusedView.getDataListSize() - 1;
                    mPointF.x = index * mPointWidth + mCoordinateMarginLeft;
                }
                if (index >= mShownPointNums) {
                    index = mShownPointNums - 1;
                }
                mPointF.y = event.getY();
                setShow(true);
                if (mOnCrossLineMoveListener != null) {
                    mOnCrossLineMoveListener.onCrossLineMove(index, mDrawPointIndex, mPointF);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                mPointF.x = event.getX();
                mPointF.x = mPointF.x < mCoordinateMarginLeft ? mCoordinateMarginLeft : mPointF.x;
                index = (int) ((mPointF.x - mCoordinateMarginLeft) / mPointWidth);
                if (mFocusedView.getDataListSize() > 0 && index > mFocusedView.getDataListSize() - 1) {
                    index = mFocusedView.getDataListSize() - 1;
                    mPointF.x = index * mPointWidth + mCoordinateMarginLeft;
                }
                if (index >= mShownPointNums) {
                    index = mShownPointNums - 1;
                }
                mPointF.y = event.getY();
                if (mOnCrossLineMoveListener != null) {
                    mOnCrossLineMoveListener.onCrossLineMove(index, mDrawPointIndex, mPointF);
                }
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                setShow(false);
                if (mOnCrossLineMoveListener != null) {
                    mOnCrossLineMoveListener.onCrossLineDismiss();
                }
                break;
        }
    }

    public interface OnCrossLineMoveListener {
        void onCrossLineMove(int index, int drawIndex, PointF pointF);

        void onCrossLineDismiss();
    }

    public void setOnCrossLineMoveListener(OnCrossLineMoveListener lineMoveListener) {
        this.mOnCrossLineMoveListener = lineMoveListener;
    }

    @Override
    public void setDataList(List<String> dataList) {
        //该组件不需要数据
    }

    public int getRadius() {
        return mRadius;
    }

    public void setRadius(int radius) {
        this.mRadius = radius;
    }

    public PointF getPointF() {
        return mPointF;
    }

    public void setPointF(PointF pointF) {
        this.mPointF = pointF;
    }

    public int getLineColor() {
        return mLineColor;
    }

    public void setLineColor(int lineColor) {
        this.mLineColor = lineColor;
        mLinePaint.setColor(lineColor);
    }

    public int getPointColor() {
        return mPointColor;
    }

    public void setPointColor(int pointColor) {
        this.mPointColor = pointColor;
        mPointPaint.setColor(this.mPointColor);
    }

    public boolean isShowPoint() {
        return isShowPoint;
    }

    public void setShowPoint(boolean isShowPoint) {
        this.isShowPoint = isShowPoint;
    }

    public boolean isShowLatitude() {
        return isShowLatitude;
    }

    public void setShowLatitude(boolean isShowLatitude) {
        this.isShowLatitude = isShowLatitude;
    }

    public boolean isShowLongitude() {
        return isShowLongitude;
    }

    public void setShowLongitude(boolean isShowLongitude) {
        this.isShowLongitude = isShowLongitude;
    }

    @Override
    public void setShownPointNums(int shownPointNums) {
        mShownPointNums = shownPointNums;
    }

    @Override
    public float getSingleDataWidth() {
        return mSinglePointOffset;
    }

    public void setSinglePointOffset(float singlePointOffset) {
        this.mSinglePointOffset = singlePointOffset;
    }

    public void setLatitudeFollowData(boolean latitudeFollowData) {
        isLatitudeFollowData = latitudeFollowData;
    }

    public void setLongitudeFollowData(boolean longitudeFollowData) {
        isLongitudeFollowData = longitudeFollowData;
    }
}
