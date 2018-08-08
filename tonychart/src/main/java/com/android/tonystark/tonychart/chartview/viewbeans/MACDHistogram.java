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
 * 描述：MACD柱状图
 *
 * @author xuhao
 * @version 1.0
 */
public class MACDHistogram extends ZoomMoveViewContainer<MACDHistogram.MACDBean> {
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
    //是否计算极值(有时元素极值并不是元素本身数据的极值,有可能是其他数据极值)
    private boolean isCalculateDataExtraNum = true;
    //每个点的宽度
    private float mPointWidth = 0;

    public MACDHistogram(Context context) {
        super(context);
        //初始化线画笔
        init();
    }

    private void init() {
        //初始化线画笔
        this.mFillPaint = new Paint();
        mFillPaint.setAntiAlias(true);
        mFillPaint.setStyle(Paint.Style.FILL);
        mFillPaint.setStrokeWidth(getPixelDp(1));
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
                    MACDBean bean = mDataList.get(i + mDrawPointIndex);
                    leftTopPoint = getLeftTopPoint(i, bean);
                    rightBottomPoint = getRightBottomPoint(i, bean);
                    if (bean.getMacd() > 0) {
                        mFillPaint.setColor(mUpColor);
                    } else if (bean.getMacd() < 0) {
                        mFillPaint.setColor(mDownColor);
                    } else {
                        mFillPaint.setColor(mEvenColor);
                    }
                    //画实心
                    if (isFill) {
                        canvas.drawRect(leftTopPoint.x, leftTopPoint.y, rightBottomPoint.x, rightBottomPoint.y, mFillPaint);
                    } else {
                        float width = leftTopPoint.x - rightBottomPoint.x;
                        canvas.drawLine(leftTopPoint.x - width / 2f, leftTopPoint.y, rightBottomPoint.x + width / 2f, rightBottomPoint.y, mFillPaint);
                    }
                }
                //改变坐标轴显示
                notifyCoordinateChange();
            }
        } catch (Exception ignored) {
        }
    }

    private PointF getLeftTopPoint(int index, MACDBean bean) {
        PointF pointF = new PointF();
        mSpace = mPointWidth / 7;

        if (mDataList.size() - 1 >= index) {
            float x = index * mPointWidth + mSpace + mCoordinateMarginLeft;
            float y = mCoordinateHeight / 2;
            if (bean.getMacd() > 0) {
                y = (1f - bean.getMacd() / (mYMax - mYMin)) * mCoordinateHeight / 2;
            }
            pointF.set(x, y);
        } else {
            pointF.set(0, 0);
        }
        return pointF;
    }

    private PointF getRightBottomPoint(int index, MACDBean bean) {
        PointF pointF = new PointF();
        mPointWidth = (mCoordinateWidth - mCoordinateMarginLeft) / mShownPointNums;
        mSpace = mPointWidth / 7;
        float x = (index + 1) * mPointWidth - mSpace + mCoordinateMarginLeft;
        float y = mCoordinateHeight / 2;
        if (bean.getMacd() < 0) {
            y = (1f - bean.getMacd() / (mYMax - mYMin)) * mCoordinateHeight / 2;
        }
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

    public static class MACDBean {
        private float macd = 0f;

        public MACDBean(float macd) {
            this.macd = macd;
        }

        public float getMacd() {
            return macd;
        }

        public void setMacd(float macd) {
            this.macd = macd;
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
    }

    public boolean isCalculateDataExtraNum() {
        return isCalculateDataExtraNum;
    }

    public void setCalculateDataExtraNum(boolean isCalculateDataExtremum) {
        this.isCalculateDataExtraNum = isCalculateDataExtremum;
    }

    @Override
    public float getSingleDataWidth() {
        return mPointWidth;
    }

    @Override
    public float[] calculateExtremeY() {
        if (mExtremeCalculatorInterface != null) {
            float yMax = mExtremeCalculatorInterface.onCalculateMax(mDrawPointIndex, mShownPointNums);
            float yMin = mExtremeCalculatorInterface.onCalculateMin(mDrawPointIndex, mShownPointNums);
            return new float[]{yMin, yMax};
        } else if (mDataList != null && mDataList.size() > mDrawPointIndex) {
            List<String> dataList = new ArrayList<>();
            for (int i = mDrawPointIndex + 1; i < mDrawPointIndex + mShownPointNums && i < mDataList.size(); i++) {
                MACDBean bean = mDataList.get(i);
                dataList.add(bean.getMacd() + "");
            }
            float[] result = DataUtils.getExtremeNumber(dataList);
            return result;

        }
        return new float[]{0, 0};
    }


    @Override
    protected float transDataToCrossDataFromDataList(int crossPointIndexInScreen, int dataInListIndex) {
        if (dataInListIndex >= mDataList.size()) {
            return super.transDataToCrossDataFromDataList(crossPointIndexInScreen, dataInListIndex);
        }

        MACDBean bean = mDataList.get(dataInListIndex);
        return bean.getMacd();
    }
}
