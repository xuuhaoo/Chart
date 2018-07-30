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
public class CandleLine extends ViewContainer<CandleLine.CandleLineBean> {
    //最小手指间距离
    private static final int MIN_FINGER_DISTANCE = 10;
    //最小移动距离
    private static final int MIN_MOVE_DISTANCE = 5;
    //极值指示线长度
    private static final int EXTREME_INDICATOR_LINE_WIDTH = 30;
    //蜡烛画笔
    private Paint mCandlePaint = null;
    //是否填充
    private boolean isFill = true;
    //放大缩小中心蜡烛下标
    private int mZoomCandleIndex = 0;
    //涨时颜色
    private int mUpColor = Color.parseColor("#ff322e");
    //跌时颜色
    private int mDownColor = Color.parseColor("#2eff2e");
    //不涨不跌颜色
    private int mEvenColor = Color.parseColor("#656565");
    //柱之间间隙
    private float mSpace = 0f;
    //两指间间隙
    private float mDistance = 0f;
    //是否正在放大
    private boolean isZooming = false;
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

    public CandleLine(Context context, float YMin, float YMax) {
        super(context);
        this.mYMin = YMin;
        this.mYMax = YMax;
        init();
    }

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
    public void zoom(MotionEvent event) {
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                isZooming = true;
                mZoomCandleIndex = getZoomCenterCandleIndex(event);
                mDistance = spacing(event);
                break;
            case MotionEvent.ACTION_MOVE:
                if (event.getPointerCount() >= 2) {
                    float spacing = spacing(event) - mDistance;
                    int scale = (int) Math.abs(spacing) / 4;
                    if (Math.abs(spacing) >= MIN_FINGER_DISTANCE) {
                        mDistance = spacing(event);
                        if (spacing < 0) {
                            //缩小
                            if (zoomOut(scale)) calculateDrawCandleIndex(event, scale, -1);//-1代表了缩小
                        } else {
                            //放大
                            if (zoomIn(scale)) calculateDrawCandleIndex(event, scale, 1);//1代表了放大
                        }
                        //计算最大最小值
                        calculateExtremeYPrivate();
                    }
                }
                break;
            case MotionEvent.ACTION_POINTER_UP:
                break;
            case MotionEvent.ACTION_UP:
                //标志位复位放在这里是因为很用户双手离开屏幕时可能还会移动一下，会先触发ACTION_POINTER_UP,再触发ACTION_UP,
                //这样就会导致复位后调用了move方法，导致视图移动
                isZooming = false;
                break;
        }
    }

    /**
     * 得到放大缩小的中心蜡烛下标
     */
    private int getZoomCenterCandleIndex(MotionEvent event) {
        //计算放大中心
        float pointLeft = event.getX(0) < event.getX(1) ? event.getX(0) : event.getX(1);
        float pointRight = event.getX(0) > event.getX(1) ? event.getX(0) : event.getX(1);
        int leftIndex = (int) ((pointLeft * mShownPointNums) / mCoordinateWidth);
        int rightIndex = (int) ((pointRight * mShownPointNums) / mCoordinateWidth);
        //得到两只之间的蜡烛相对于总显示根数的根数
        int centerCandleNums = (rightIndex - leftIndex) / 2 + leftIndex;
        return mDrawPointIndex + centerCandleNums;
    }

    /**
     * 计算绘画蜡烛的起始值
     */
    private void calculateDrawCandleIndex(MotionEvent event, int scale, int zoomType) {
        //计算左边应消失的根数,从而改变了右边消失的根数,因为总消失根数不变
        int zoomCandleIndexTemp = getZoomCenterCandleIndex(event);

        if (zoomType == 1) { //放大
            if (zoomCandleIndexTemp - mZoomCandleIndex > 0) {
                //目标左移,需要向右纠正,不改变绘图起始坐标,就会让图右移,因为显示条数在变少
            } else if (zoomCandleIndexTemp - mZoomCandleIndex < 0) {
                //目标右移,需要向左纠正
                mDrawPointIndex = mDrawPointIndex + scale;
            }
        } else if (zoomType == -1) {//缩小
            if (zoomCandleIndexTemp - mZoomCandleIndex > 0) {
                //目标左移,需要向右纠正
                mDrawPointIndex = mDrawPointIndex - scale;
            } else if (zoomCandleIndexTemp - mZoomCandleIndex < 0) {
                //目标右移,需要向左纠正,不改变绘图其实坐标,就会让图左移,因为显示条数增多
            }
        }
        //越界判断
        mDrawPointIndex = mDrawPointIndex + mShownPointNums >= mDataList.size() ? mDataList.size() - mShownPointNums : mDrawPointIndex;
        mDrawPointIndex = mDrawPointIndex < 0 ? 0 : mDrawPointIndex;

    }

    /**
     * 计算坐标极值
     */
    private void calculateExtremeYPrivate() {
        if (isCalculateDataExtremum) {
            float[] value = calculateExtremeY();
            setMinDataValue(value[0]);
            setMaxDataValue(value[1]);
            mYMin = value[0];
            mYMax = value[1];
        }
    }

    /**
     * 放大
     *
     * @return 表示是否进行了放大, true代表showPointNums进行了--;
     */
    private boolean zoomIn(int scale) {
        if (mShownPointNums > mMinShownPointNums) {
            //减少点数
            mShownPointNums = mShownPointNums - scale;
            mShownPointNums = mShownPointNums < mMinShownPointNums ? mMinShownPointNums : mShownPointNums;
            Log.i("zoomIn", "mShownPointNums:" + mShownPointNums);
            return true;
        } else {
            //此时显示的点数应该等于最小点数
            mShownPointNums = mMinShownPointNums;
            Log.i("zoomIn", "mShownPointNums:" + mShownPointNums);
            return false;
        }
    }

    /**
     * 缩小
     *
     * @return 标识是否进行了缩小, true代表showPointNums进行了++;
     */
    private boolean zoomOut(int scale) {
        if (mShownPointNums < mDefaultShowPointNums) {
            //增加点根数
            mShownPointNums = mShownPointNums + scale;
            mShownPointNums = mShownPointNums > mDefaultShowPointNums ? mDefaultShowPointNums : mShownPointNums;
            return true;
        } else {
            mShownPointNums = mDefaultShowPointNums;
            return false;
        }
    }

    /**
     * 计算两指距离
     */
    private float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }

    private PointF moveDownPointF = new PointF();


    @Override
    public void move(MotionEvent event) {
        if (!isZooming) {//当不缩放的时候
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    moveDownPointF.x = event.getX();
                    moveDownPointF.y = event.getY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    float difX = moveDownPointF.x - event.getX();
                    int scale = (int) Math.abs(difX) / 10;
                    scale = scale < 1 ? 1 : scale;
                    if (Math.abs(difX) >= MIN_MOVE_DISTANCE) {
                        move(difX, scale);
                        calculateExtremeYPrivate();
                    }
                    moveDownPointF.x = event.getX();
                    moveDownPointF.y = event.getY();

                    break;
                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_UP:
                    break;
            }
        }
    }

    /**
     * 移动
     */
    private void move(float difX, int scale) {
        if (difX > 0) {//手指向左移动
            if ((mDrawPointIndex + mShownPointNums) <= mDataList.size() - 1) {
                mDrawPointIndex = mDrawPointIndex + scale;
            }
        } else if (difX < 0) {//手指向右移动
            if (mDrawPointIndex > 0) {
                mDrawPointIndex = mDrawPointIndex - scale;
            }
        }
        //越界判断
        mDrawPointIndex = mDrawPointIndex >= mDataList.size() - mShownPointNums ? mDataList.size() - mShownPointNums : mDrawPointIndex;
        mDrawPointIndex = mDrawPointIndex < 0 ? 0 : mDrawPointIndex;
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
