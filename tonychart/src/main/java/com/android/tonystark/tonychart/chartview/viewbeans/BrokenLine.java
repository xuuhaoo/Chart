package com.android.tonystark.tonychart.chartview.viewbeans;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Shader;
import android.text.TextUtils;

import com.android.tonystark.tonychart.chartview.utils.DataUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 描述：折线
 *
 * @author xuhao
 * @version 1.0
 */
public class BrokenLine extends ZoomMoveViewContainer<String> {
    //最小移动距离
    private static final int MIN_MOVE_DISTANCE = 5;
    //线画笔
    private Paint mLinePaint = null;
    //背景色画笔
    private Paint mBackgroundPaint = null;
    // 折线个画圆点的画笔
    private Paint mBrokenPointPaint = null;
    // 折线拐点是否画圆点
    private boolean mShowBrokenPoint = false;
    // 折线拐点圆点半径
    private float mBrokenPointRadiusInPx = 8f;
    //是否填充
    private boolean isFill = true;
    //渐变开始颜色
    private int mStartColor = Color.WHITE;
    //渐变结束颜色
    private int mEndColor = Color.BLACK;
    //背景元素路径
    private Path mBackgroundPath = null;
    //折线元素路径
    private Path mLinePath = null;
    // 折线圆点路径
    private Path mBrokenPointPath = null;
    //每个数据点的宽度
    private float mPointWidth = 0;
    //不需要连续的点的位置
    private List<Integer> mBreakIndex = new ArrayList<>();
    //现在的线默认从每个点宽度的中间开始画，设置为true后第一个点就从零点开始，不会留一点空白
    private boolean mStartZero = false;
    //现在的线默认从每个点宽度的中间开始画，设置为true后最后一个点就画到末尾，不会留一点空白
    private boolean mEndFullView = false;

    public BrokenLine(Context context) {
        super(context);
        //初始化线画笔
        initPaint();
        //初始化路径
        initPath();
    }

    private void initPaint() {
        //初始化线画笔
        this.mLinePaint = new Paint();
        mLinePaint.setAntiAlias(true);
        mLinePaint.setStyle(Paint.Style.STROKE);
        mLinePaint.setStrokeWidth(1.5f);
        mLinePaint.setColor(Color.BLACK);
        //初始化背景画笔
        this.mBackgroundPaint = new Paint();
        mBackgroundPaint.setColor(Color.WHITE);
        mBackgroundPaint.setStyle(Paint.Style.FILL);
        // 圆点画笔
        this.mBrokenPointPaint = new Paint();
        mBrokenPointPaint.setAntiAlias(true);
        mBrokenPointPaint.setStyle(Paint.Style.FILL);
        mBrokenPointPaint.setColor(Color.BLACK);
    }

    private void initPath() {
        mBackgroundPath = new Path();
        mLinePath = new Path();
        mBrokenPointPath = new Path();
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        try {
            if (isShow) {
                checkParameter();
                mPointWidth = (mCoordinateWidth - mCoordinateMarginLeft) / mShownPointNums;
                mBackgroundPath.reset();
                mLinePath.reset();
                mBrokenPointPath.reset();
                boolean isFirstDraw = true;
                PointF normalPoint = null;//普通坐标点,有可能会在放大缩小左右移动时出现脏数据0,0
                boolean isLastPoint = false; //是否是屏幕能容纳的最后一个点
                for (int i = 0; i < mShownPointNums && i < mDataList.size(); i++) {
                    if (TextUtils.isEmpty(mDataList.get(i + mDrawPointIndex)) ||
                            "null".equalsIgnoreCase(mDataList.get(i + mDrawPointIndex)) ||
                            mDataList.get(i + mDrawPointIndex) == null) {
                        continue;
                    }
                    normalPoint = getCoordinatePoint(i + mDrawPointIndex, i);
                    if (normalPoint.x == 0 && normalPoint.y == 0) {
                        continue;
                    }
                    if (isFirstDraw) {
                        float startX = mStartZero ? normalPoint.x - mPointWidth / 2 : normalPoint.x;
                        mLinePath.moveTo(startX, normalPoint.y);
                        if (isFill) {
                            mBackgroundPath.moveTo(startX, mCoordinateHeight);
                            mBackgroundPath.lineTo(startX, normalPoint.y);
                        }
                        if (mShowBrokenPoint) {
                            mBrokenPointPath.moveTo(startX, normalPoint.y);
                            mBrokenPointPath.addCircle(startX, normalPoint.y, mBrokenPointRadiusInPx, Path.Direction.CW);
                        }
                        isFirstDraw = false;
                    }

                    if (i == mShownPointNums - 1) {
                        isLastPoint = true;
                        float endX = mEndFullView ? normalPoint.x + mPointWidth / 2 : normalPoint.x;
                        mLinePath.lineTo(endX, normalPoint.y);

                        if (mShowBrokenPoint) {
                            mBrokenPointPath.addCircle(endX, normalPoint.y, mBrokenPointRadiusInPx, Path.Direction.CW);
                        }
                    } else {
                        if (mBreakIndex != null && mBreakIndex.contains(i + mDrawPointIndex)) {
                            mLinePath.moveTo(normalPoint.x, normalPoint.y);
                        } else {
                            mLinePath.lineTo(normalPoint.x, normalPoint.y);
                        }
                        if (mShowBrokenPoint) {
                            mBrokenPointPath.addCircle(normalPoint.x, normalPoint.y, mBrokenPointRadiusInPx, Path.Direction.CW);
                        }
                        if (isFill) {
                            mBackgroundPath.lineTo(normalPoint.x, normalPoint.y);
                        }
                    }
                }

                if (isFill && normalPoint != null) {
                    float endX = mEndFullView && isLastPoint ? normalPoint.x + mPointWidth / 2 : normalPoint.x;
                    mBackgroundPath.lineTo(endX, normalPoint.y);
                    mBackgroundPath.lineTo(endX, mCoordinateHeight);
                    mBackgroundPath.close();
                    canvas.drawPath(mBackgroundPath, mBackgroundPaint);
                }
                //画线
                //mLinePaint.setPathEffect(new CornerPathEffect(5)); //增加了圆角效果后线就会变短～ -_-！！
                canvas.drawPath(mLinePath, mLinePaint);
                // 画咪
                if (mShowBrokenPoint) {
                    canvas.drawPath(mBrokenPointPath, mBrokenPointPaint);
                }
                //改变坐标轴显示
                notifyCoordinateChange();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 得到坐标系中点
     *
     * @param dataIndex 数据下标
     * @param i         屏幕上的点的个数下标
     */
    private PointF getCoordinatePoint(int dataIndex, int i) {
        PointF pointF = new PointF();
        if (mDataList.size() - 1 >= dataIndex) {
            String str = mDataList.get(dataIndex);
            float x = i * mPointWidth + mPointWidth / 2 + mCoordinateMarginLeft;
            float y = (1f - (DataUtils.parseString2Float(str) - mYMin) / (mYMax - mYMin)) * mCoordinateHeight;
            pointF.set(x, y);
        }
        return pointF;
    }

    public void setCoordinateHeight(float coordinateHeight) {
        super.setCoordinateHeight(coordinateHeight);
        //获得高度后,设置渐变颜色
        LinearGradient lg = new LinearGradient(0, 0, 0, coordinateHeight, mStartColor, mEndColor, Shader.TileMode.MIRROR);
        mBackgroundPaint.setShader(lg);
    }

    public List<Integer> getBreakIndex() {
        return mBreakIndex;
    }

    public void setStartZero(boolean startZero) {
        this.mStartZero = startZero;
    }

    public void setEndFullView(boolean endFullView) {
        this.mEndFullView = endFullView;
    }

    public void setBreakIndex(List<Integer> breakIndex) {
        this.mBreakIndex = breakIndex;
    }

    /**
     * 设置线的颜色,要求在调用draw之前调用
     *
     * @param color 颜色色值
     */
    public void setLineColor(int color) {
        mLinePaint.setColor(color);
    }

    /**
     * 设置填充后的颜色及透明度
     *
     * @param startColor 开始颜色
     * @param endColor   结束颜色
     * @param alpha      透明度0...255
     */
    public void setLineFillColor(int startColor, int endColor, int alpha) {
        isFill = true;
        this.mStartColor = startColor;
        this.mEndColor = endColor;
        mBackgroundPaint.setAlpha(alpha);
    }

    /**
     * 设置拐点圆点颜色
     *
     * @param color
     */
    public void setBrokenPointColor(int color) {
        mBrokenPointPaint.setColor(color);
    }


    public void setBrokenPointRadiusInPx(float sizeInPx) {
        mBrokenPointRadiusInPx = sizeInPx;
    }

    /**
     * 设置填充后的颜色及透明度
     *
     * @param fillColor 填充颜色颜色
     * @param alpha     透明度0...255
     */
    public void setLineFillColor(int fillColor, int alpha) {
        setLineFillColor(fillColor, fillColor, alpha);
    }

    private void checkParameter() {
        if (this.mCoordinateHeight <= 0) {
            throw new IllegalArgumentException("mCoordinateHeight can't be zero or smaller than zero");
        }
        if (this.mCoordinateWidth <= 0) {
            throw new IllegalArgumentException("mCoordinateWidth can't be zero or smaller than zero");
        }
    }

    public void setFill(boolean isFill) {
        this.isFill = isFill;
    }

    public boolean isFill() {
        return isFill;
    }

    public void setShowBrokenPoint(boolean showBrokenPoint) {
        this.mShowBrokenPoint = showBrokenPoint;
    }

    public boolean isShowBrokenPoint() {
        return mShowBrokenPoint;
    }

    @Override
    public float getSingleDataWidth() {
        return mPointWidth;
    }

    @Override
    public float[] calculateExtremeY() {
        if (mExtremeCalculatorInterface != null) {
            return mExtremeCalculatorInterface.onCalculateExtreme(mDrawPointIndex, mShownPointNums);
        } else if (mDataList != null && mDataList.size() > mDrawPointIndex) {
            List<String> dataList = new ArrayList<>();
            for (int i = mDrawPointIndex; i < mDrawPointIndex + mShownPointNums && i < mDataList.size(); i++) {
                if (TextUtils.isEmpty(mDataList.get(i)) || "null".equalsIgnoreCase(mDataList.get(i))) {
                    continue;
                }
                dataList.add(mDataList.get(i));
            }
            return DataUtils.getExtremeNumber(dataList);
        }

        return new float[]{0, 0};
    }

    @Override
    protected float transDataToCrossDataFromDataList(int crossPointIndexInScreen, int dataInListIndex) {
        if (dataInListIndex >= mDataList.size()) {
            return super.transDataToCrossDataFromDataList(crossPointIndexInScreen, dataInListIndex);
        }

        try {
            return Float.parseFloat(mDataList.get(dataInListIndex));
        } catch (NumberFormatException e) {
            return super.transDataToCrossDataFromDataList(crossPointIndexInScreen, dataInListIndex);
        }
    }
}
