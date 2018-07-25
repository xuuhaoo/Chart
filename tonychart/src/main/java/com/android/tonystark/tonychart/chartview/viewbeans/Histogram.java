package com.android.tonystark.tonychart.chartview.viewbeans;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.view.MotionEvent;

import com.android.tonystark.tonychart.chartview.utils.DataUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 描述：柱状图
 *
 * @author xuhao
 * @version 1.0
 * @date 2015-03-16
 */
public class Histogram extends ViewContainer<Histogram.HistogramBean> {
    //最小手指间距离
    private static final int MIN_FINGER_DISTANCE = 10;
    //最小移动距离
    private static final int MIN_MOVE_DISTANCE = 5;
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
    //两指间距离
    private float mDistance = 0f;
    //缩放的中心点下标
    private int mZoomHistogramIndex = 0;
    //是否正在缩放
    private boolean isZooming = false;
    //每个点的宽度
    private float mPointWidth = 0;

    /**
     * 柱状图
     *
     * @param YMax 坐标系中最大值
     * @param YMin 坐标系中最小值
     */
    public Histogram(Context context, float YMax, float YMin) {
        super(context);
        this.mYMax = YMax;
        this.mYMin = YMin;
        //初始化线画笔
        init();
    }

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

        mDrawPointIndex = 0;
        mShownPointNums = 2;
        mMinShownPointNums = 1;
        mDefaultShowPointNums = 2;
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

    private PointF moveDownHistogramF = new PointF();

    @Override
    public void move(MotionEvent event) {
        if (!isZooming) {//当不缩放的时候
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    moveDownHistogramF.x = event.getX();
                    moveDownHistogramF.y = event.getY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    float difX = moveDownHistogramF.x - event.getX();
                    int scale = (int) Math.abs(difX) / 10;
                    scale = scale < 1 ? 1 : scale;
                    if (Math.abs(difX) >= MIN_MOVE_DISTANCE) {
                        move(difX, scale);
                        calculateData();
                    }
                    moveDownHistogramF.x = event.getX();
                    moveDownHistogramF.y = event.getY();
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
                mDrawPointIndex = mDrawPointIndex + 1 * scale;
            }
        } else if (difX < 0) {//手指向右移动
            if (mDrawPointIndex > 0) {
                mDrawPointIndex = mDrawPointIndex - 1 * scale;
            }
        }
        //越界判断
        mDrawPointIndex = mDrawPointIndex >= mDataList.size() - mShownPointNums ? mDataList.size() - mShownPointNums : mDrawPointIndex;
        mDrawPointIndex = mDrawPointIndex < 0 ? 0 : mDrawPointIndex;
    }

    @Override
    public void zoom(MotionEvent event) {
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:

                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                isZooming = true;
                mZoomHistogramIndex = getZoomCenterHistogramIndex(event);
                mDistance = spacing(event);
                break;
            case MotionEvent.ACTION_MOVE:
                if (event.getPointerCount() >= 2) {
                    float spacing = spacing(event) - mDistance;
                    if (Math.abs(spacing) >= MIN_FINGER_DISTANCE) {
                        int scale = (int) Math.abs(spacing) / 10;
                        scale = scale < 1 ? 1 : scale;
                        mDistance = spacing(event);
                        if (spacing < 0) {
                            zoomOut(scale);
                            //缩小
//                            if (zoomOut()) calculateDrawHistogramIndex(event, -1);//-1代表了缩小
                        } else {
                            zoomIn(scale);
                            //放大
//                            if (zoomIn()) calculateDrawHistogramIndex(event, 1);//1代表了放大
                        }
                        //计算最大最小值
                        calculateData();
                    }
                }
                break;
            case MotionEvent.ACTION_POINTER_UP:
//                isZooming = false;
                break;
            case MotionEvent.ACTION_UP:
                //标志位复位放在这里是因为很用户双手离开屏幕时可能还会移动一下，会先触发ACTION_POINTER_UP,再触发ACTION_UP,
                //这样就会导致复位后调用了move方法，导致视图移动
                isZooming = false;
                break;
        }
    }

    /**
     * 计算坐标极值
     */
    public void calculateData() {
        if (isCalculateDataExtremum) {
            if (mZoomAndMoveCalculateInterface != null) {
                mYMax = mZoomAndMoveCalculateInterface.onCalculateMax(mDrawPointIndex, mShownPointNums);
                mYMin = 0;
            } else if (mDataList.size() > mDrawPointIndex) {
                float min = mDataList.get(mDrawPointIndex).turnover;
                float max = mDataList.get(mDrawPointIndex).turnover;
                for (int i = mDrawPointIndex + 1; i < mDrawPointIndex + mShownPointNums && i < mDataList.size(); i++) {
                    float value = mDataList.get(i).turnover;
                    min = value < min && value > 0 ? value : min;
                    max = max > value ? max : value;
                }
                mYMax = max;
                mYMin = 0;
            }
        }
    }

    /**
     * 放大
     *
     * @return 表示是否进行了放大, true代表showPointNums进行了--;
     */
    private boolean zoomIn(int scale) {
//        if (mShownPointNums >= mMinShownPointNums) {
//            //减少主子根数
//            mShownPointNums--;
//            mShownPointNums = mShownPointNums < mMinShownPointNums ? mMinShownPointNums : mShownPointNums;
//            return true;
//        } else {
//            //此时显示的主子数应该等于最小主子数
//            mShownPointNums = mMinShownPointNums;
//            return false;
//        }
        if (mShownPointNums > mMinShownPointNums) {
            //减少蜡烛根数
            mShownPointNums -= (6 * scale);
            mDrawPointIndex += (3 * scale);
            if (mDrawPointIndex + mShownPointNums < mZoomHistogramIndex) {
                mDrawPointIndex += (6 * scale);
            }
            mShownPointNums = mShownPointNums < mMinShownPointNums ? mMinShownPointNums : mShownPointNums;
            //越界判断
            mDrawPointIndex = mDrawPointIndex >= mZoomHistogramIndex ? mZoomHistogramIndex - 3 : mDrawPointIndex;
            mDrawPointIndex = mDrawPointIndex < 0 ? 0 : mDrawPointIndex;
            return true;
        } else {
            //此时显示的蜡烛数应该等于最小蜡烛数
            mShownPointNums = mMinShownPointNums;
            return false;
        }
    }

    /**
     * 缩小
     *
     * @return 标识是否进行了缩小, true代表showPointNums进行了++;
     */
    private boolean zoomOut(int scale) {
//        if (mShownPointNums <= mDefaultShowPointNums) {
//            //增加主子根数
//            mShownPointNums++;
//            mShownPointNums = mShownPointNums > mDefaultShowPointNums ? mDefaultShowPointNums : mShownPointNums;
//            return true;
//        } else {
//            mShownPointNums = mDefaultShowPointNums;
//            return false;
//        }

        if (mShownPointNums < mDefaultShowPointNums) {
            //增加蜡烛根数
            mShownPointNums += (6 * scale);
            mDrawPointIndex -= (3 * scale);

            mShownPointNums = mShownPointNums > mDefaultShowPointNums ? mDefaultShowPointNums : mShownPointNums;
            //越界判断
            mDrawPointIndex = mDrawPointIndex >= mDataList.size() - mDefaultShowPointNums - 1 ? mDataList.size() - mDefaultShowPointNums - 1 : mDrawPointIndex;
            mDrawPointIndex = mDrawPointIndex < 0 ? 0 : mDrawPointIndex;
            return true;
        } else {
            mShownPointNums = mDefaultShowPointNums;
            return false;
        }
    }

    /**
     * 计算两指距离
     *
     * @param event
     * @return
     */
    private float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }

    /**
     * 得到放大缩小的中心主子下标
     *
     * @param event
     * @return
     */
    private int getZoomCenterHistogramIndex(MotionEvent event) {
        //计算放大中心
        float pointLeft = event.getX(0) < event.getX(1) ? event.getX(0) : event.getX(1);
        float pointRight = event.getX(0) > event.getX(1) ? event.getX(0) : event.getX(1);
        int leftIndex = (int) ((pointLeft * mShownPointNums) / mCoordinateWidth);
        int rightIndex = (int) ((pointRight * mShownPointNums) / mCoordinateWidth);
        //得到两只之间的主子相对于总显示根数的根数
        int centerHistogramNums = (rightIndex - leftIndex) / 2 + leftIndex;
        return mDrawPointIndex + centerHistogramNums;
    }

    /**
     * 计算绘画主子的起始值
     */
    private void calculateDrawHistogramIndex(MotionEvent event, int zoomType) {
        //计算左边应消失的根数,从而改变了右边消失的根数,因为总消失根数不变
        int zoomHistogramIndexTemp = getZoomCenterHistogramIndex(event);

        if (zoomType == 1) { //放大
            if (zoomHistogramIndexTemp - mZoomHistogramIndex > 0) {
                //目标左移,需要向右纠正,不改变绘图起始坐标,就会让图右移,因为显示条数在变少
            } else if (zoomHistogramIndexTemp - mZoomHistogramIndex < 0) {
                //目标右移,需要向左纠正
                mDrawPointIndex = mDrawPointIndex + 1;
            }
        } else if (zoomType == -1) {//缩小
            if (zoomHistogramIndexTemp - mZoomHistogramIndex > 0) {
                //目标左移,需要向右纠正
                mDrawPointIndex = mDrawPointIndex - 1;
            } else if (zoomHistogramIndexTemp - mZoomHistogramIndex < 0) {
                //目标右移,需要向左纠正,不改变绘图其实坐标,就会让图左移,因为现实条数增多
            }
        }
        //越界判断
        mDrawPointIndex = mDrawPointIndex >= mDataList.size() ? mDataList.size() - 1 : mDrawPointIndex;
        mDrawPointIndex = mDrawPointIndex < 0 ? 0 : mDrawPointIndex;

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
    public float[] calculateExtremeYWhenFocused() {
        List<String> dataList = new ArrayList<>();
        for (int i = mDrawPointIndex; i < mShownPointNums; i++) {
            HistogramBean bean = mDataList.get(i);
            dataList.add(bean.getTurnover() + "");
        }
        float[] result = DataUtils.getExtremeNumber(dataList);
        return result;
    }
}