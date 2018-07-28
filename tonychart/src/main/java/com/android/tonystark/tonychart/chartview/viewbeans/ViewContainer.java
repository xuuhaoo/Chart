package com.android.tonystark.tonychart.chartview.viewbeans;

import android.content.Context;
import android.graphics.Canvas;
import android.support.annotation.CallSuper;
import android.util.TypedValue;
import android.view.MotionEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * 描述：视图容器
 *
 * @author xuhao
 * @version 1.0
 */
public class ViewContainer<T extends Object> {
    private ChartView mChartView;
    //是否请求了焦点,因为可能此时还没有被添加,添加的时候再做处理
    private boolean isRequestFocused;
    //上下文
    protected Context mContext;
    //坐标系组件
    protected Coordinates mCoordinates = null;
    //子容器的集合
    private List<ViewContainer<Object>> mChildrenList = null;
    //数据集合
    protected List<T> mDataList = new ArrayList<>();
    //坐标系高度
    protected float mCoordinateHeight = 0;
    //坐标系宽度
    protected float mCoordinateWidth = 0;
    //坐标系的左间距
    protected int mCoordinateMarginLeft = 0;
    //设置坐标系最大值
    protected float mYMax = 0;
    //设置坐标系最小值
    protected float mYMin = 0;
    //是否计算极值(有时元素极值并不是元素本身数据的极值,有可能是其他数据极值)
    protected boolean isCalculateDataExtremum = true;
    //是否显示
    protected boolean isShow = true;
    //数据的最大值，区别于坐标最大值yMax
    private float mMaxDataValue;
    //数据的最小值，区别于坐标最小值yMin
    private float mMinDataValue;
    //放大缩小监听器
    protected ExtremeCalculatorInterface mExtremeCalculatorInterface = null;
    //从第几个点开始画
    protected int mDrawPointIndex = 0;
    //显示的点数
    protected int mShownPointNums = 2;
    //最少显示的点数
    protected int mMinShownPointNums = 2;
    //默认显示点数
    protected int mDefaultShowPointNums = 0;
    //十字光标点的数据集合
    protected List<String> mCrossDataList = new ArrayList<>();

    public ViewContainer(Context context) {
        mContext = context.getApplicationContext();
        mChildrenList = new ArrayList<ViewContainer<Object>>() {
            @Override
            public boolean add(ViewContainer<Object> object) {
                this.remove(object);
                return super.add(object);
            }

            @Override
            public void add(int index, ViewContainer<Object> object) {
                this.remove(object);
                super.add(index, object);
            }
        };
    }

    public void draw(Canvas canvas) {
        for (ViewContainer viewContainer : mChildrenList) {
            viewContainer.draw(canvas);
        }
    }

    public void addChildren(ViewContainer viewContainer) {
        mChildrenList.add(viewContainer);
    }

    public void removeChildren(ViewContainer viewContainer) {
        mChildrenList.remove(viewContainer);
    }

    public List<ViewContainer<Object>> getChildrenList() {
        return mChildrenList;
    }

    public void setCoordinateHeight(float coordinateHeight) {
        this.mCoordinateHeight = coordinateHeight;
        for (ViewContainer<Object> viewContainer : getChildrenList()) {
            viewContainer.setCoordinateHeight(coordinateHeight);
        }
    }

    public void setCoordinateWidth(float coordinateWidth) {
        this.mCoordinateWidth = coordinateWidth;
        for (ViewContainer<Object> viewContainer : getChildrenList()) {
            viewContainer.setCoordinateWidth(coordinateWidth);
        }
    }

    public void setCoordinateMarginLeft(int left) {
        this.mCoordinateMarginLeft = left;
        for (ViewContainer viewContainer : getChildrenList()) {
            viewContainer.setCoordinateMarginLeft(left);
        }
    }

    public void move(MotionEvent event) {
        for (ViewContainer container : getChildrenList()) {
            container.move(event);
        }
    }

    public void zoom(MotionEvent event) {
        for (ViewContainer container : getChildrenList()) {
            container.zoom(event);
        }
    }

    /**
     * 设置坐标系组件
     *
     * @param coordinates
     */
    public void setCoordinate(Coordinates coordinates) {
        this.mCoordinates = coordinates;
    }

    /**
     * 获取绘制坐标系Y轴最大值
     *
     * @return
     */
    public float getYMax() {
        return mYMax;
    }

    /**
     * 设置绘制坐标系的Y轴最大值
     *
     * @param YMax
     */
    @CallSuper
    public void setYMax(float YMax) {
        this.mYMax = YMax;
        for (ViewContainer container : getChildrenList()) {
            container.setYMax(this.mYMax);
        }
    }

    /**
     * 获取绘制坐标系的Y轴最小值
     *
     * @return
     */
    public float getYMin() {
        return mYMin;
    }

    /**
     * 设置绘制坐标系的Y轴最小值
     *
     * @param YMin
     */
    @CallSuper
    public void setYMin(float YMin) {
        this.mYMin = YMin;
        for (ViewContainer container : getChildrenList()) {
            container.setYMin(this.mYMin);
        }
    }

    /**
     * 是否显示该组件
     *
     * @return
     */
    public boolean isShow() {
        return isShow;
    }

    /**
     * 设置是否显示该组件
     *
     * @param isShow
     */
    @CallSuper
    public void setShow(boolean isShow) {
        this.isShow = isShow;
        for (ViewContainer container : getChildrenList()) {
            container.setShow(this.isShow);
        }
    }

    /**
     * 获取单条数据的数据绘制宽度
     *
     * @return
     */
    public float getSingleDataWidth() {
        return 0;
    }

    /**
     * 设置坐标系极值的计算解释器
     *
     * @param zoomAndmoveCalculateInterface
     */
    public final void setExtremeCalculatorInterface(ExtremeCalculatorInterface zoomAndmoveCalculateInterface) {
        mExtremeCalculatorInterface = zoomAndmoveCalculateInterface;
    }

    /**
     * 获取数据中的最小值
     *
     * @return
     */
    public float getMinDataValue() {
        return mMinDataValue;
    }

    /**
     * 设置数据中的最小值
     *
     * @param minDataValue
     */
    @CallSuper
    public void setMinDataValue(float minDataValue) {
        this.mMinDataValue = minDataValue;
        for (ViewContainer container : getChildrenList()) {
            container.setMinDataValue(this.mMinDataValue);
        }
    }

    /**
     * 获取数据中的最大值
     *
     * @return
     */
    public float getMaxDataValue() {
        return mMaxDataValue;
    }

    /**
     * 设置数据中最大值
     *
     * @param maxDataValue
     */
    @CallSuper
    public void setMaxDataValue(float maxDataValue) {
        this.mMaxDataValue = maxDataValue;
        for (ViewContainer container : getChildrenList()) {
            container.setMaxDataValue(this.mMaxDataValue);
        }
    }

    /**
     * 请求变为焦点View,画板内的公共数据将以此组件为主
     */
    @CallSuper
    public void requestFocuse() {
        if (mChartView != null) {
            mChartView.requestFocusChild(this);
        } else {
            isRequestFocused = true;
        }
    }

    /**
     * 是否获得焦点,如果获取了焦点,外层组件会将数据与本组件数据统一
     *
     * @return
     */
    @CallSuper
    public boolean isFocused() {
        if (mChartView != null) {
            return mChartView.isFocused(this);
        } else {
            return isRequestFocused;
        }
    }

    /**
     * 得到当前开始绘制的数据起始下标
     *
     * @return
     */
    public int getDrawPointIndex() {
        return mDrawPointIndex;
    }

    /**
     * 设置开始绘制的数据起始下标
     *
     * @param drawPointIndex
     */
    @CallSuper
    public void setDrawPointIndex(int drawPointIndex) {
        this.mDrawPointIndex = drawPointIndex;
        for (ViewContainer container : getChildrenList()) {
            container.setDrawPointIndex(this.mDrawPointIndex);
        }
        if (mChartView != null) {
            this.mChartView.notifyNeedForceFlushData();
        }
    }

    /**
     * 设置当前显示数据个数,此方法正常情况下是不处理的.因为该值是通过内部计算得出的
     *
     * @param shownPointNums
     */
    void setShownPointNums(int shownPointNums) {
        //do nothing 因为这个正常情况下是内部计算获得的
    }

    /**
     * 获取当前显示的数据个数
     *
     * @return
     */
    public int getShownPointNums() {
        return mShownPointNums;
    }

    /**
     * 获取屏幕最少显示数据个数
     *
     * @return
     */
    public int getMinShownPointNums() {
        return mMinShownPointNums;
    }

    /**
     * 设置最少显示的数据个数
     *
     * @param minShownPointNums
     */
    @CallSuper
    public void setMinShownPointNums(int minShownPointNums) {
        this.mMinShownPointNums = minShownPointNums;
        for (ViewContainer container : getChildrenList()) {
            container.setMinShownPointNums(this.mMinShownPointNums);
        }
        if (mChartView != null) {
            this.mChartView.notifyNeedForceFlushData();
        }
    }

    /**
     * 获取默认显示的数据个数
     *
     * @return
     */
    public int getDefaultShowPointNums() {
        return mDefaultShowPointNums;
    }

    /**
     * 设置默认显示的数据个数
     *
     * @param defaultShowPointNums
     */
    @CallSuper
    public void setDefaultShowPointNums(int defaultShowPointNums) {
        this.mDefaultShowPointNums = defaultShowPointNums;
        this.mShownPointNums = this.mDefaultShowPointNums;
        for (ViewContainer container : getChildrenList()) {
            container.setDefaultShowPointNums(defaultShowPointNums);
        }
        if (mChartView != null) {
            this.mChartView.notifyNeedForceFlushData();
        }
    }

    /**
     * 设置真正承载绘制的View
     *
     * @param chartView
     */
    @CallSuper
    public void setChartView(ChartView chartView) {
        mChartView = chartView;
        if (isRequestFocused) {
            isRequestFocused = false;
            requestFocuse();
        }
        for (ViewContainer container : getChildrenList()) {
            container.setChartView(mChartView);
        }
    }

    /**
     * 获取SP的像素值
     *
     * @param sp
     * @return
     */
    protected float getPixelSp(float sp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, mContext.getResources().getDisplayMetrics());
    }

    /**
     * 获取DP的像素值
     *
     * @param dp
     * @return
     */
    protected float getPixelDp(float dp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, mContext.getResources().getDisplayMetrics());
    }

    /**
     * 通知坐标系起始点改变,显示个数改变
     * 改变大小时drawPointIndex会改变
     */
    protected void notifyCoordinateChange() {
        if (mCoordinates != null) {
            mCoordinates.setDrawPointIndex(mDrawPointIndex);
            mCoordinates.setShownPointNums(mShownPointNums);
        }
    }

    /**
     * 计算Y的最大最小值,因为当聚焦后
     * 整个坐标系的最大最小值将按照此模块的数据为准
     */
    public float[] calculateExtremeY() {
        float[] result = new float[]{0, 0};
        return result;
    }

    /**
     * 获取基本数据集合
     *
     * @return
     */
    public List<T> getDataList() {
        return mDataList;
    }

    /**
     * 设置基本数据集合
     *
     * @param dataList
     */
    @CallSuper
    public void setDataList(List<T> dataList) {
        mDataList = dataList;
        mCrossDataList = transDataToCrossDataFromDataList(mDataList);
        if (mChartView != null) {
            this.mChartView.notifyNeedForceFlushData();
        }
    }

    /**
     * 格式化数据为十字线所用的数据
     *
     * @param originDataList 原始数据
     * @return 十字线能用的String类型的浮点数据
     */
    protected List<String> transDataToCrossDataFromDataList(List<T> originDataList) {
        return new ArrayList<>();
    }

    /**
     * 获取十字线基本数据
     *
     * @return
     */
    public List<String> getCrossDataList() {
        return mCrossDataList;
    }

    /**
     * 该模块是否计算极值
     *
     * @return
     */
    public boolean isCalculateDataExtremum() {
        return isCalculateDataExtremum;
    }

    /**
     * 设置是否计算该模块的极值
     *
     * @param isCalculateDataExtremum true计算极值,false不计算
     */
    @CallSuper
    public void setCalculateDataExtremum(boolean isCalculateDataExtremum) {
        this.isCalculateDataExtremum = isCalculateDataExtremum;
        for (ViewContainer container : getChildrenList()) {
            container.setCalculateDataExtremum(isCalculateDataExtremum);
        }
    }

    public interface ExtremeCalculatorInterface {
        float onCalculateMax(int drawPointIndex, int showPointNums);

        float onCalculateMin(int drawPointIndex, int showPointNums);
    }

}
