package com.android.tonystark.tonychart.chartview.viewbeans;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;

import com.android.tonystark.tonychart.chartview.utils.DataUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 描述：柱状图
 *
 * @author xuhao
 * @version 1.0
 */
public class Histogram extends ZoomMoveViewContainer<Histogram.HistogramBean> {
    //实心画笔
    private Paint mFillPaint = null;
    //是否填充
    private boolean isFill = true;
    //涨时颜色
    private int mUpColor = Color.parseColor("#ff322e");
    //跌时颜色
    private int mDownColor = Color.parseColor("#2eff2e");
    //不涨不跌颜色
    private int mEvenColor = Color.parseColor("#656565");
    //柱之间间隙
    private float mSpace = 0;
    //每个点的宽度
    private float mPointWidth = 0;

    public Histogram(Context context) {
        super(context);
        //初始化线画笔
        init();
    }

    private void init() {
        //初始化线画笔
        this.mFillPaint = new Paint();
        mFillPaint.setAntiAlias(true);
        mFillPaint.setStyle(Paint.Style.FILL);
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        try {
            if (isShow) {
                checkParameter();
                mPointWidth = (mCoordinateWidth - mCoordinateMarginLeft) / mShownPointNums;
                PointF rightBottomPoint;
                PointF leftTopPoint;
                for (int i = 0; i < mShownPointNums && i < mDataList.size(); i++) {
                    HistogramBean bean = mDataList.get(i + mDrawPointIndex);
                    leftTopPoint = getLeftTopPoint(i, bean);
                    rightBottomPoint = getRightBottomPoint(i);
                    if (bean.isUp > 0) {
                        mFillPaint.setColor(mUpColor);
                    } else if (bean.isUp < 0) {
                        mFillPaint.setColor(mDownColor);
                    } else {
                        mFillPaint.setColor(mEvenColor);
                    }
                    //画实心
                    canvas.drawRect(leftTopPoint.x, leftTopPoint.y, rightBottomPoint.x, rightBottomPoint.y, mFillPaint);
                }
                //改变坐标轴显示
                notifyCoordinateChange();
            }
        } catch (Exception ignored) {
        }
    }

    private PointF getLeftTopPoint(int index, HistogramBean bean) {
        PointF pointF = new PointF();
        mSpace = mPointWidth / 7;

        if (mDataList.size() - 1 >= index) {
            float x = index * mPointWidth + mSpace + mCoordinateMarginLeft;
            float y = (1f - (bean.turnover - mYMin) / (mYMax - mYMin)) * mCoordinateHeight;
            pointF.set(x, y);
        } else {
            pointF.set(0, 0);
        }
        return pointF;
    }

    private PointF getRightBottomPoint(int index) {
        PointF pointF = new PointF();
        mPointWidth = (mCoordinateWidth - mCoordinateMarginLeft) / mShownPointNums;
        mSpace = mPointWidth / 7;
        float x = (index + 1) * mPointWidth - mSpace + mCoordinateMarginLeft;
        float y = mCoordinateHeight;
        pointF.set(x, y);
        return pointF;
    }

    private void checkParameter() {
        if (this.mCoordinateHeight <= 0) {
            throw new IllegalArgumentException("mCoordinateHeight can't be zero or smaller than zero");
        }
        if (this.mCoordinateWidth <= 0) {
            throw new IllegalArgumentException("mCoordinateWidth can't be zero or smaller than zero");
        }
    }

    public static class HistogramBean {
        public static final double RED = 1;
        public static final double GREEN = -1;
        public static final double EVEN = 0;
        //成交量颜色变化
        private double isUp = 0;

        private float turnover = 0;

        public HistogramBean() {
        }

        public HistogramBean(double isUp, float turnover) {
            this.isUp = isUp;
            this.turnover = turnover;
        }

        public double getIsUp() {
            return isUp;
        }

        public void setIsUp(double isUp) {
            this.isUp = isUp;
        }

        public float getTurnover() {
            return turnover;
        }

        public void setTurnover(float turnover) {
            this.turnover = turnover;
        }
    }

    public void setColor(int upColor, int evenColor, int downColor) {
        this.mUpColor = upColor;
        this.mDownColor = downColor;
        this.mEvenColor = evenColor;
    }

    public void setUpColor(int upColor) {
        this.mUpColor = upColor;
    }

    public void setEvenColor(int evenColor) {
        this.mEvenColor = evenColor;
    }

    public void setDownColor(int downColor) {
        this.mDownColor = downColor;
    }

    public boolean isFill() {
        return isFill;
    }

    public void setFill(boolean isFill) {
        this.isFill = isFill;
        if (isFill) {
            mFillPaint.setStyle(Paint.Style.FILL);
        } else {
            mFillPaint.setStyle(Paint.Style.STROKE);
        }
    }

    @Override
    public float getSingleDataWidth() {
        return mPointWidth;
    }

    @Override
    public float[] calculateExtremeY() {
        if (mExtremeCalculatorInterface != null) {
            float yMax = mExtremeCalculatorInterface.onCalculateMax(mDrawPointIndex, mShownPointNums);
//            float yMin = mExtremeCalculatorInterface.onCalculateMin(mDrawPointIndex, mShownPointNums);
            return new float[]{0, yMax};
        } else if (mDataList != null && mDataList.size() > mDrawPointIndex) {
            List<String> dataList = new ArrayList<>();
            for (int i = mDrawPointIndex + 1; i < mDrawPointIndex + mShownPointNums && i < mDataList.size(); i++) {
                HistogramBean bean = mDataList.get(i);
                dataList.add(bean.getTurnover() + "");
            }
            float[] result = DataUtils.getExtremeNumber(dataList);
            result[0] = 0;
            return result;
        }
        return new float[]{0, 0};
    }

    @Override
    protected float transDataToCrossDataFromDataList(int crossPointIndexInScreen, int dataInListIndex) {
        if (dataInListIndex >= mDataList.size()) {
            return super.transDataToCrossDataFromDataList(crossPointIndexInScreen, dataInListIndex);
        }

        HistogramBean bean = mDataList.get(dataInListIndex);
        return bean.getTurnover();
    }

}
