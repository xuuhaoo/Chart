package com.android.tonystark.tonychart.chartview.viewbeans;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.util.Log;
import android.view.MotionEvent;


import java.util.List;

/**
 * 描述：蜡烛线
 *
 * @author xuhao
 * @version 1.0
 */
public class CandleLine extends AbsZoomMoveViewContainer<CandleLine.CandleLineBean> {

    //极值指示线长度
    private static final int EXTREME_INDICATOR_LINE_WIDTH = 30;
    //蜡烛画笔
    private Paint mCandlePaint = null;
    //是否填充
    private boolean isFill = true;
    //涨时颜色
    private int mUpColor = Color.parseColor("#ff322e");
    //跌时颜色
    private int mDownColor = Color.parseColor("#2eff2e");
    //不涨不跌颜色
    private int mEvenColor = Color.parseColor("#656565");
    //柱之间间隙
    private float mSpace = 0f;
    //蜡烛宽度
    private float mCandleWidth = 0;
    //画最大最小值的画笔
    private Paint mTxtPaint;
    //是否显示了最大值
    private boolean isAlreadyShowMax = false;
    //是否显示了最小值
    private boolean isAlreadyShowMin = false;
    //是否显示最大值
    private boolean isNeedShowMaxPrice = false;
    //是否显示最小值
    private boolean isNeedShowMinPrice = false;

    public CandleLine(Context context) {
        super(context);
        init();
    }

    //初始化画笔
    private void init() {
        mCandlePaint = new Paint();
        mCandlePaint.setAntiAlias(true);
        mCandlePaint.setStrokeWidth(1.5f);
        mCandlePaint.setColor(Color.BLACK);
        mCandlePaint.setStyle(Paint.Style.STROKE);

        mTxtPaint = new Paint();
        mTxtPaint.setAntiAlias(true);
        mTxtPaint.setStrokeWidth(1.5f);
        mTxtPaint.setColor(Color.parseColor("#666666"));
        mTxtPaint.setTextSize(getPixelSp(10));
        mTxtPaint.setTextAlign(Paint.Align.LEFT);

        mMinShownPointNums = 1;
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        try {
            if (isShow) {
                isAlreadyShowMax = false;
                isAlreadyShowMin = false;
                checkParamter();
                for (int i = 0; i < mShownPointNums && i < mDataList.size(); i++) {
                    drawCandle(mDataList.get(i + mDrawPointIndex), i, canvas);
                }
                //改变坐标轴显示
                notifyCoordinateChange();
            }
        } catch (Exception ignored) {

        }
    }

    //绘制蜡烛一根
    private void drawCandle(CandleLineBean candleLineBean, int i, Canvas canvas) {
        //从收盘价与开盘价之间比较出最大值
        float maxPrice = candleLineBean.closePrice >= candleLineBean.openPrice ? candleLineBean.closePrice : candleLineBean.openPrice;
        //从收盘价与开盘价之间比较出最小值
        float minPrice = candleLineBean.closePrice <= candleLineBean.openPrice ? candleLineBean.closePrice : candleLineBean.openPrice;
        //计算出蜡烛顶端尖尖的Y轴坐标
        float y1 = (1f - (candleLineBean.heightPrice - mYMin) / (mYMax - mYMin)) * mCoordinateHeight;
        //计算出蜡烛顶端横线的Y轴坐标
        float y2 = (1f - (maxPrice - mYMin) / (mYMax - mYMin)) * mCoordinateHeight;
        //计算出蜡烛底端横线的Y轴坐标
        float y3 = (1f - (minPrice - mYMin) / (mYMax - mYMin)) * mCoordinateHeight;
        //计算出蜡烛底端尖尖的Y轴坐标
        float y4 = (1f - (candleLineBean.lowPrice - mYMin) / (mYMax - mYMin)) * mCoordinateHeight;
        //每根蜡烛的宽度
        mCandleWidth = (mCoordinateWidth - mCoordinateMarginLeft) / mShownPointNums;
        //计算间隙
        mSpace = mCandleWidth / 7;

        if (candleLineBean.openPrice < candleLineBean.closePrice) {//红
            mCandlePaint.setColor(mUpColor);
        } else if (candleLineBean.openPrice > candleLineBean.closePrice) {//绿
            mCandlePaint.setColor(mDownColor);
        } else {//黑
            //当收盘价等于开盘价时，就拿收盘价和昨收比较，如果涨就红，如果跌就绿
            if (candleLineBean.closePrice > candleLineBean.yesterdayPrice) {
                mCandlePaint.setColor(mUpColor);
            } else if (candleLineBean.closePrice < candleLineBean.yesterdayPrice) {
                mCandlePaint.setColor(mDownColor);
            } else {
                mCandlePaint.setColor(Color.parseColor("#656565"));
            }
        }
        //进行绘画
        if (y2 != y3 && Math.abs(y2 - y3) > 1) {//非停牌且今开和收盘价差高于1块
            Rect rect = new Rect();
            rect.set((int) (i * mCandleWidth + mSpace + mCoordinateMarginLeft),
                    (int) y2,
                    (int) (i * mCandleWidth + mCandleWidth - mSpace + mCoordinateMarginLeft),
                    (int) y3);
            //画蜡烛的方块主干
            canvas.drawRect(rect, mCandlePaint);
        } else {//停牌,今开等于收盘
            //画蜡烛的方块主干,因为y2和y3相等或者差1,因此我们默认使用y2
            canvas.drawLine(i * mCandleWidth + mSpace + mCoordinateMarginLeft, y2,
                    i * mCandleWidth + mCandleWidth - mSpace + mCoordinateMarginLeft, y2,
                    mCandlePaint);
        }

        float needleX = ((i * mCandleWidth) + mCandleWidth / 2 + mCoordinateMarginLeft) - (mCandlePaint.getStrokeWidth() / 2);

        //画蜡烛的上尖尖
        canvas.drawLine(needleX, y1, needleX, y2, mCandlePaint);
        //画蜡烛的下尖尖
        canvas.drawLine(needleX, y3, needleX, y4, mCandlePaint);

        if (isNeedShowMaxPrice && candleLineBean.heightPrice == getMaxDataValue() && !isAlreadyShowMax) {
            //判断这个数据是在屏幕右边还是左边
            boolean right = i > mShownPointNums / 2;
            //获取要画的蜡烛的中心点
            String txt = "" + candleLineBean.heightPrice;
            float width = mTxtPaint.measureText(txt);
            Paint.FontMetrics fm = new Paint.FontMetrics();
            mTxtPaint.getFontMetrics(fm);
            float height = Math.abs(fm.ascent);//文字高度
            if (right) {
                //如果在屏幕右边就往左边画
                canvas.drawLine(needleX, y1, needleX - EXTREME_INDICATOR_LINE_WIDTH, y1 + height, mTxtPaint);
                canvas.drawText(txt, needleX - EXTREME_INDICATOR_LINE_WIDTH - width, y1 + height * 1.5f, mTxtPaint);
            } else {
                //如果在屏幕左边就往右边画
                canvas.drawLine(needleX, y1, needleX + EXTREME_INDICATOR_LINE_WIDTH, y1 + height, mTxtPaint);
                canvas.drawText(txt, needleX + EXTREME_INDICATOR_LINE_WIDTH, y1 + height * 1.5f, mTxtPaint);
            }
            isAlreadyShowMax = true;
        }
        if (isNeedShowMinPrice && candleLineBean.lowPrice == getMinDataValue() && !isAlreadyShowMin) {
            //判断这个数据是在屏幕右边还是左边
            boolean right = i > mShownPointNums / 2;
            //获取要画的蜡烛的中心点
            String txt = "" + candleLineBean.lowPrice;
            float width = mTxtPaint.measureText(txt);
            Paint.FontMetrics fm = new Paint.FontMetrics();
            mTxtPaint.getFontMetrics(fm);
            float height = Math.abs(fm.ascent);//文字高度
            if (right) {
                //如果在屏幕右边就往左边画
                canvas.drawLine(needleX, y4, needleX - EXTREME_INDICATOR_LINE_WIDTH, y4, mTxtPaint);
                canvas.drawText(txt, needleX - EXTREME_INDICATOR_LINE_WIDTH - width, y4 + height / 2, mTxtPaint);
            } else {
                //如果在屏幕左边就往右边画
                canvas.drawLine(needleX, y4, needleX + EXTREME_INDICATOR_LINE_WIDTH, y4, mTxtPaint);
                canvas.drawText(txt, needleX + EXTREME_INDICATOR_LINE_WIDTH, y4 + height / 2, mTxtPaint);
            }
            isAlreadyShowMin = true;
        }
    }

    private void checkParamter() {
        if (this.mCoordinateHeight <= 0) {
            throw new IllegalArgumentException("mCoordinateHeight can't be zero or smaller than zero");
        }
        if (this.mCoordinateWidth <= 0) {
            throw new IllegalArgumentException("mCoordinateWidth can't be zero or smaller than zero");
        }
    }

    public void setFill(boolean isFill) {
        this.isFill = isFill;
        if (this.isFill) {
            mCandlePaint.setStyle(Paint.Style.FILL);
        } else {
            mCandlePaint.setStyle(Paint.Style.STROKE);
        }
    }

    public void setColor(int upColor, int evenColor, int downColor) {
        this.mUpColor = upColor;
        this.mDownColor = downColor;
        this.mEvenColor = evenColor;
    }

    @Override
    public void setCoordinate(Coordinates coordinates) {
        this.mCoordinates = coordinates;
    }

    public Coordinates getCoordinates() {
        return mCoordinates;
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

    @Override
    public float getSingleDataWidth() {
        return mCandleWidth;
    }

    public void setShowMaxPrice(boolean needShowMaxPrice) {
        isNeedShowMaxPrice = needShowMaxPrice;
    }

    public void setShowMinPrice(boolean needShowMinPrice) {
        isNeedShowMinPrice = needShowMinPrice;
    }

    @Override
    public float[] calculateExtremeY() {
        if (mExtremeCalculatorInterface != null) {
            float yMax = mExtremeCalculatorInterface.onCalculateMax(mDrawPointIndex, mShownPointNums);
            float yMin = mExtremeCalculatorInterface.onCalculateMin(mDrawPointIndex, mShownPointNums);
            return new float[]{yMin, yMax};
        } else if (mDataList != null && mDataList.size() > mDrawPointIndex) {
            float min = mDataList.get(mDrawPointIndex).getLowPrice();
            float max = mDataList.get(mDrawPointIndex).getHeightPrice();
            for (int i = mDrawPointIndex + 1; i < mDrawPointIndex + mShownPointNums && i < mDataList.size(); i++) {
                CandleLineBean entity = mDataList.get(i);
                min = entity.getLowPrice() < min && entity.getLowPrice() > 0 ? entity.getLowPrice() : min;
                max = max > entity.getHeightPrice() ? max : entity.getHeightPrice();
            }
            return new float[]{min, max};
        }
        return new float[]{0, 0};
    }

    @Override
    protected List<String> transDataToCrossDataFromDataList(List<CandleLineBean> originDataList) {
        List<String> result = super.transDataToCrossDataFromDataList(originDataList);
        for (CandleLineBean candleLineBean : originDataList) {
            result.add(candleLineBean.getOpenPrice() + "");
        }
        return result;
    }

    /**
     * 蜡烛数据信息
     */
    public static class CandleLineBean {
        public CandleLineBean() {

        }

        public CandleLineBean(int index, float heightPrice, float lowPrice, float openPrice, float closePrice) {
            this.index = index;
            this.heightPrice = heightPrice;
            this.lowPrice = lowPrice;
            this.openPrice = openPrice;
            this.closePrice = closePrice;
        }

        //下标
        private int index = -1;
        //最高价
        private float heightPrice = 0.0f;
        //最低价
        private float lowPrice = 0.0f;
        //开盘价
        private float openPrice = 0.0f;
        //收盘价
        private float closePrice = 0.0f;
        //毫秒数
        private long timeMills = 0;
        //资产类型，用于格式化价格用
        private int type = 0;
        //昨日收盘价
        private float yesterdayPrice = 0.0f;

        public int getIndex() {
            return index;
        }

        public void setIndex(int index) {
            this.index = index;
        }

        public float getHeightPrice() {
            return heightPrice;
        }

        public void setHeightPrice(float heightPrice) {
            this.heightPrice = heightPrice;
        }

        public float getLowPrice() {
            return lowPrice;
        }

        public void setLowPrice(float lowPrice) {
            this.lowPrice = lowPrice;
        }

        public float getOpenPrice() {
            return openPrice;
        }

        public void setOpenPrice(float openPrice) {
            this.openPrice = openPrice;
        }

        public float getClosePrice() {
            return closePrice;
        }

        public void setClosePrice(float closePrice) {
            this.closePrice = closePrice;
        }

        public long getTimeMills() {
            return timeMills;
        }

        public void setTimeMills(long timeMills) {
            this.timeMills = timeMills;
        }

        public float getYesterdayPrice() {
            return yesterdayPrice;
        }

        public void setYesterdayPrice(float yesterdayPrice) {
            this.yesterdayPrice = yesterdayPrice;
        }

        public int getType() {
            return type;
        }

        public void setType(int type) {
            this.type = type;
        }
    }
}
