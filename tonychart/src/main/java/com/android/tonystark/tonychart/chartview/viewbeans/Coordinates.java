package com.android.tonystark.tonychart.chartview.viewbeans;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathEffect;
import android.support.v4.content.ContextCompat;


import com.android.tonystark.tonychart.R;

import java.util.List;

/**
 * 描述：坐标系背景
 *
 * @author xuhao
 * @version 1.0
 * @date 2015-03-13
 */
public class Coordinates extends ViewContainer<Object> {
    //左边文字画笔
    private Paint mLeftTextPaint = null;
    //右边文字画笔
    private Paint mRightTextPaint = null;
    //底部文字画笔
    private Paint mBottomTextPaint = null;
    //经线画笔
    private Paint mLongitudeLinePaint = null;
    //纬线画笔
    private Paint mLatitudeLinePaint = null;
    //经线
    private int mLongitudeNums = 0;
    //纬线
    private int mLatitudeNums = 0;
    //坐标系刻度适配器
    private CoordinateScaleAdapter mCoordinateScaleAdapter = null;
    //距边距的空隙值
    private float mSpace = 0;
    //坐标系底部空余
    private int mMarginBottom = 0;

    /**
     * 坐标系
     *
     * @param context
     * @param coordinateScaleAdapter
     */
    public Coordinates(Context context, CoordinateScaleAdapter coordinateScaleAdapter) {
        super(context);
        this.mCoordinateScaleAdapter = coordinateScaleAdapter;
        //初始化画笔
        initPaint();
    }

    /**
     * 坐标系
     */
    public Coordinates(Context context) {
        super(context);
        //初始化画笔
        initPaint();
        mMarginBottom = (int) getPixelDp(15);
    }

    /**
     * 初始化画笔
     */
    private void initPaint() {
        //初始化经线画笔
        this.mLongitudeLinePaint = new Paint();
        mLongitudeLinePaint.setStyle(Paint.Style.STROKE);
        mLongitudeLinePaint.setColor(Color.BLACK);
        mLongitudeLinePaint.setStrokeWidth(1f);
        //初始化纬线画笔
        this.mLatitudeLinePaint = new Paint();
        mLatitudeLinePaint.setStyle(Paint.Style.STROKE);
        mLatitudeLinePaint.setColor(Color.BLACK);
        mLatitudeLinePaint.setStrokeWidth(1f);
        //初始化左边文字画笔
        this.mLeftTextPaint = new Paint();
        mLeftTextPaint.setTextSize(getPixelSp(9));
        mLeftTextPaint.setAntiAlias(true);
        mLeftTextPaint.setColor(ContextCompat.getColor(mContext, R.color.tiny_gray));
        //初始化右边文字画笔
        this.mRightTextPaint = new Paint();
        mRightTextPaint.setTextSize(getPixelSp(9));
        mRightTextPaint.setAntiAlias(true);
        mRightTextPaint.setColor(ContextCompat.getColor(mContext, R.color.tiny_gray));
        //初始化底部文字画笔
        this.mBottomTextPaint = new Paint();
        mBottomTextPaint.setTextSize(getPixelSp(9));
        mBottomTextPaint.setAntiAlias(true);
        mBottomTextPaint.setColor(ContextCompat.getColor(mContext, R.color.tiny_gray));

        //初始化空隙,该空隙用于文字与边距和纬线之间的距离
        mSpace = getPixelSp(2);
    }

    @Override
    public void draw(Canvas canvas) {
        //绘制子view
        super.draw(canvas);
        try {
            if (isShow) {
                checkParamter();
                //画经线,及其刻度
                drawLongitude(canvas);
                //画纬线,及其刻度
                drawLatitude(canvas);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //画经线
    private void drawLongitude(Canvas canvas) {
        //经线间的宽度
        float longitudeSpace = (mCoordinateWidth - mCoordinateMarginLeft) / (mLongitudeNums - 1);
        Paint.FontMetrics fm = new Paint.FontMetrics();
        Path path = new Path();
        for (int i = 0; i < mLongitudeNums; i++) {
            String bottomScale = "";//左边刻度文字
            if (mCoordinateScaleAdapter != null) {
                bottomScale = mCoordinateScaleAdapter.getXBottomScaleString(mDataList, mDrawPointIndex, mShownPointNums, i, mLongitudeNums);
            }
            float scaleWidth = mBottomTextPaint.measureText(bottomScale);//文字宽度
            mBottomTextPaint.getFontMetrics(fm);
            float scaleHeight = Math.abs(fm.ascent);//文字高度
            if (i == 0) {//第一条经线
                //画经线
                path.moveTo(mLongitudeLinePaint.getStrokeWidth() + mCoordinateMarginLeft, 0);
                path.lineTo(mLongitudeLinePaint.getStrokeWidth() + mCoordinateMarginLeft, mCoordinateHeight);
                canvas.drawPath(path, mLongitudeLinePaint);
                path.reset();
//                canvas.drawLine(mLongitudeLinePaint.getStrokeWidth(), 0, mLongitudeLinePaint.getStrokeWidth(), mCoordinateHeight, mLongitudeLinePaint);
                //画经线刻度
                canvas.drawText(bottomScale, mSpace + mCoordinateMarginLeft, mCoordinateHeight + mMarginBottom / 2 + scaleHeight / 2, mBottomTextPaint);
            } else if (i == mLongitudeNums - 1) {//最后一条经线
                //画经线
                path.moveTo(mCoordinateWidth - mLongitudeLinePaint.getStrokeWidth(), 0);
                path.lineTo(mCoordinateWidth - mLongitudeLinePaint.getStrokeWidth(), mCoordinateHeight);
                canvas.drawPath(path, mLongitudeLinePaint);
                path.reset();
//                canvas.drawLine(mCoordinateWidth - mLongitudeLinePaint.getStrokeWidth(), 0, mCoordinateWidth - mLongitudeLinePaint.getStrokeWidth(), mCoordinateHeight, mLongitudeLinePaint);
                //画经线刻度
                canvas.drawText(bottomScale, mCoordinateWidth - scaleWidth - mSpace, mCoordinateHeight + mMarginBottom / 2 + scaleHeight / 2, mBottomTextPaint);
            } else {//其中所有的经线
                //画经线
                float tempLongitudeSpace = i * longitudeSpace + mCoordinateMarginLeft;//经线间隙,i此时应从1开始,因为第一个if屏蔽了0
                path.moveTo(tempLongitudeSpace - mLongitudeLinePaint.getStrokeWidth(), 0);
                path.lineTo(tempLongitudeSpace - mLongitudeLinePaint.getStrokeWidth(), mCoordinateHeight);
                canvas.drawPath(path, mLongitudeLinePaint);
                path.reset();
//                canvas.drawLine(tempLongitudeSpace - mLongitudeLinePaint.getStrokeWidth(), 0, tempLongitudeSpace - mLongitudeLinePaint.getStrokeWidth(), mCoordinateHeight, mLongitudeLinePaint);
                //画经线刻度
                canvas.drawText(bottomScale, tempLongitudeSpace - scaleWidth / 2, mCoordinateHeight + mMarginBottom / 2 + scaleHeight / 2, mBottomTextPaint);
            }
        }
    }

    //画纬线
    private void drawLatitude(Canvas canvas) {
        //纬线间宽度
        float latitudeSpace = mCoordinateHeight / (mLatitudeNums - 1);
        Paint.FontMetrics fm = new Paint.FontMetrics();
        Path path = new Path();
        for (int i = 0; i < mLatitudeNums; i++) {
            String leftScale = "";//左边刻度文字
            if (mCoordinateScaleAdapter != null) {
                leftScale = mCoordinateScaleAdapter.getYLeftScaleString(mDataList, mDrawPointIndex, mShownPointNums, i, mLatitudeNums);
            }
            float leftScaleWidth = mLeftTextPaint.measureText(leftScale);//左边文字宽度
            mLeftTextPaint.getFontMetrics(fm);
            float leftScaleHeight = Math.abs(fm.ascent);//左边文字高度

            String rightScale = "";//右边刻度文字
            if (mCoordinateScaleAdapter != null) {
                rightScale = mCoordinateScaleAdapter.getYRightScaleString(mDataList, mDrawPointIndex, mShownPointNums, i, mLatitudeNums);
            }
            float rightScaleWidth = mRightTextPaint.measureText(rightScale);//右边文字宽度
            mRightTextPaint.getFontMetrics(fm);
            float rightScaleHeight = Math.abs(fm.ascent);//右边文字高度
            if (i == 0) {//第一条纬线
                path.moveTo(mCoordinateMarginLeft, 0);
                path.lineTo(mCoordinateWidth, 0);
                //画纬线
                canvas.drawPath(path, mLatitudeLinePaint);
                path.reset();
//                canvas.drawLine(0, 0, mCoordinateWidth, 0, mLatitudeLinePaint);
                //画纬线刻度(左)
                canvas.drawText(leftScale, mSpace, leftScaleHeight + mSpace, mLeftTextPaint);
                //画纬线刻度(右)
                canvas.drawText(rightScale, mCoordinateWidth - rightScaleWidth - mSpace, leftScaleHeight + mSpace, mRightTextPaint);
            } else if (i == mLatitudeNums - 1) {//最后一条纬线
                path.moveTo(mCoordinateMarginLeft, mCoordinateHeight - 1); //减去1是因为不减就看不到这条线了
                path.lineTo(mCoordinateWidth, mCoordinateHeight - 1); //减去1是因为不减就看不到这条线了
                //画纬线
                canvas.drawPath(path, mLatitudeLinePaint);
                path.reset();
//                canvas.drawLine(0, mCoordinateHeight, mCoordinateWidth, mCoordinateHeight, mLatitudeLinePaint);
                //画纬线刻度(左)
                canvas.drawText(leftScale, mSpace, mCoordinateHeight - leftScaleHeight + mSpace, mLeftTextPaint);
                //画纬线刻度(右)
                canvas.drawText(rightScale, mCoordinateWidth - rightScaleWidth - mSpace, mCoordinateHeight - rightScaleHeight + mSpace, mRightTextPaint);
            } else {//中间的纬线
                //画纬线
                float tempLatitudeSpace = i * latitudeSpace;//纬线间隙,i此时应从1开始,因为第一个if屏蔽了0
                path.moveTo(mCoordinateMarginLeft, tempLatitudeSpace);
                path.lineTo(mCoordinateWidth, tempLatitudeSpace);
                canvas.drawPath(path, mLatitudeLinePaint);
                path.reset();
//                canvas.drawLine(0, tempLatitudeSpace, mCoordinateWidth, tempLatitudeSpace, mLatitudeLinePaint);
                //画纬线刻度(左)
                canvas.drawText(leftScale, mSpace, tempLatitudeSpace - leftScaleHeight + mSpace, mLeftTextPaint);
                //画纬线刻度(右)
                canvas.drawText(rightScale, mCoordinateWidth - rightScaleWidth - mSpace, tempLatitudeSpace - rightScaleHeight + mSpace, mRightTextPaint);
            }
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

    /**
     * 设置底部文字大小
     *
     * @param size
     */
    public void setBottomTextSize(float size) {
        mBottomTextPaint.setTextSize(size);
    }

    /**
     * 坐标系刻度适配器
     */
    public static abstract class CoordinateScaleAdapter<T> {
        /**
         * 为了留一点空间，重新计算最小值
         *
         * @param max   最大值
         * @param min   最小值
         * @param yNums 纵轴显示的数量（纬线数量）
         * @return
         */
        protected float calYMaxWithSpace(float max, float min, int yNums) {
            return max + Math.abs(max - min) / (yNums - 1) / 5;
        }

        /**
         * 为了留一点空间，重新计算最小值
         *
         * @param max   最大值
         * @param min   最小值
         * @param yNums 纵轴显示的数量（纬线数量）
         * @return
         */
        protected float calYMinWithSpace(float max, float min, int yNums) {
            return min + Math.abs(max - min) / (yNums - 1) / 5;
        }

        /**
         * 得到Y轴左边的刻度
         *
         * @param scaleIndex     刻度下标(第几个刻度)
         * @param totalYScaleNum 总下标个数
         */
        public abstract String getYLeftScaleString(List<T> dataList, int drawPointIndex, int showPointNums, int scaleIndex, int totalYScaleNum);

        /**
         * 得到Y轴右边的刻度
         */
        public abstract String getYRightScaleString(List<T> dataList, int drawPointIndex, int showPointNums, int scaleIndex, int totalYScaleNum);

        /**
         * 得到X轴底部的刻度
         */
        public abstract String getXBottomScaleString(List<T> dataList, int drawPointIndex, int showPointNums, int scaleIndex, int totalXScaleNum);
    }

    public CoordinateScaleAdapter getCoordinateScaleAdapter() {
        return mCoordinateScaleAdapter;
    }

    public void setCoordinateScaleAdapter(CoordinateScaleAdapter coordinateScaleAdapter) {
        this.mCoordinateScaleAdapter = coordinateScaleAdapter;
    }

    public int getLatitudeNums() {
        return mLatitudeNums;
    }

    public void setLatitudeNums(int latitudeNums) {
        this.mLatitudeNums = latitudeNums;
    }

    public int getLongitudeNums() {
        return mLongitudeNums;
    }

    public void setLongitudeNums(int longitudeNums) {
        this.mLongitudeNums = longitudeNums;
    }

    public void setLongitudeLineEffect(PathEffect pathEffect) {
        mLongitudeLinePaint.setPathEffect(pathEffect);
    }

    public void setLatitudeLineEffect(PathEffect pathEffect) {
        mLatitudeLinePaint.setPathEffect(pathEffect);
    }

    public void setLeftTextColor(int color) {
        mLeftTextPaint.setColor(color);
    }

    public void setRightTextColor(int color) {
        mRightTextPaint.setColor(color);
    }

    public void setBottomTextColor(int color) {
        mBottomTextPaint.setColor(color);
    }

    public void setLongitudeLineColor(int color) {
        mLongitudeLinePaint.setColor(color);
    }

    public void setLatitudeLineColor(int color) {
        mLatitudeLinePaint.setColor(color);
    }

    public int getMarginBottom() {
        return mMarginBottom;
    }

    @Override
    public void setShownPointNums(int shownPointNums) {
        mShownPointNums = shownPointNums;
    }
}
