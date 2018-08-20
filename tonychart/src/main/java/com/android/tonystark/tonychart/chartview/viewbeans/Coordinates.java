package com.android.tonystark.tonychart.chartview.viewbeans;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathEffect;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;

import com.android.tonystark.tonychart.R;
import com.android.tonystark.tonychart.chartview.interfaces.UnabelFocusedsView;

import java.util.List;

/**
 * 描述：坐标系背景
 *
 * @author xuhao
 * @version 1.0
 */
public class Coordinates extends ViewContainer<Object> implements UnabelFocusedsView {
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
    private float mFixedSpaceWithLeft = 0;
    //固定间隙与纬线之间的
    private float mFixedSpaceWithLatitudeLine = 0;
    //刻度所在的位置相对于纬线
    private TextGravity mLatitudeTextGravity = TextGravity.ABOVE_LINE;

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
        //初始化空隙,该空隙用于文字与左边的距离
        mFixedSpaceWithLeft = getPixelSp(2);
        //初始化间隙,该间隙用于文字与纬线之间的距离
        mFixedSpaceWithLatitudeLine = (int) getPixelDp(2);
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
            if (bottomScale == null) {
                bottomScale = "";
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
                //画经线刻度
                canvas.drawText(bottomScale, mFixedSpaceWithLeft + mCoordinateMarginLeft, mCoordinateHeight + mFixedSpaceWithLatitudeLine + scaleHeight, mBottomTextPaint);
            } else if (i == mLongitudeNums - 1) {//最后一条经线
                //画经线
                path.moveTo(mCoordinateWidth - mLongitudeLinePaint.getStrokeWidth(), 0);
                path.lineTo(mCoordinateWidth - mLongitudeLinePaint.getStrokeWidth(), mCoordinateHeight);
                canvas.drawPath(path, mLongitudeLinePaint);
                path.reset();
                //画经线刻度
                canvas.drawText(bottomScale, mCoordinateWidth - scaleWidth - mFixedSpaceWithLeft, mCoordinateHeight + mFixedSpaceWithLatitudeLine + scaleHeight, mBottomTextPaint);
            } else {//其中所有的经线
                //画经线
                float tempLongitudeSpace = i * longitudeSpace + mCoordinateMarginLeft;//经线间隙,i此时应从1开始,因为第一个if屏蔽了0
                path.moveTo(tempLongitudeSpace - mLongitudeLinePaint.getStrokeWidth(), 0);
                path.lineTo(tempLongitudeSpace - mLongitudeLinePaint.getStrokeWidth(), mCoordinateHeight);
                canvas.drawPath(path, mLongitudeLinePaint);
                path.reset();
                //画经线刻度
                canvas.drawText(bottomScale, tempLongitudeSpace - scaleWidth / 2, mCoordinateHeight + mFixedSpaceWithLatitudeLine + scaleHeight, mBottomTextPaint);
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
            if (leftScale == null) {
                leftScale = "";
            }
            float leftScaleWidth = mLeftTextPaint.measureText(leftScale);//左边文字宽度
            mLeftTextPaint.getFontMetrics(fm);
            float leftScaleHeight = Math.abs(fm.ascent);//左边文字高度

            String rightScale = "";//右边刻度文字
            if (mCoordinateScaleAdapter != null) {
                rightScale = mCoordinateScaleAdapter.getYRightScaleString(mDataList, mDrawPointIndex, mShownPointNums, i, mLatitudeNums);
            }
            if (rightScale == null) {
                rightScale = "";
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
                //画纬线刻度(左)
                TextGravity textGravity = mLatitudeTextGravity;
                if (textGravity == TextGravity.ABOVE_LINE || textGravity == TextGravity.VERTICAL_CENTER_LINE) {//如果第一条纬线的刻度是在纬线之上或者之中,我们就将其设置为线之下模式,因为线之之中上显示不下
                    textGravity = TextGravity.BLEW_LINE;
                }
                if (!TextUtils.isEmpty(leftScale)) {
                    float verticalOffset = getTextVerticalOffsetByGravity(textGravity, leftScaleHeight);
                    canvas.drawText(leftScale, mFixedSpaceWithLeft, verticalOffset, mLeftTextPaint);
                }
                if (!TextUtils.isEmpty(rightScale)) {
                    float verticalOffset = getTextVerticalOffsetByGravity(textGravity, rightScaleHeight);
                    //画纬线刻度(右)
                    canvas.drawText(rightScale, mCoordinateWidth - rightScaleWidth - mFixedSpaceWithLeft, verticalOffset, mRightTextPaint);
                }
            } else if (i == mLatitudeNums - 1) {//最后一条纬线
                path.moveTo(mCoordinateMarginLeft, mCoordinateHeight - 1); //减去1是因为不减就看不到这条线了
                path.lineTo(mCoordinateWidth, mCoordinateHeight - 1); //减去1是因为不减就看不到这条线了
                //画纬线
                canvas.drawPath(path, mLatitudeLinePaint);
                path.reset();

                TextGravity textGravity = mLatitudeTextGravity;
                if (textGravity == TextGravity.BLEW_LINE || textGravity == TextGravity.VERTICAL_CENTER_LINE) {
                    textGravity = TextGravity.ABOVE_LINE;
                }

                if (!TextUtils.isEmpty(leftScale)) {
                    float verticalOffset = getTextVerticalOffsetByGravity(textGravity, leftScaleHeight);
                    //画纬线刻度(左)
                    canvas.drawText(leftScale, mFixedSpaceWithLeft, mCoordinateHeight + verticalOffset, mLeftTextPaint);
                }
                if (!TextUtils.isEmpty(rightScale)) {
                    float verticalOffset = getTextVerticalOffsetByGravity(textGravity, rightScaleHeight);
                    //画纬线刻度(右)
                    canvas.drawText(rightScale, mCoordinateWidth - rightScaleWidth - mFixedSpaceWithLeft, mCoordinateHeight + verticalOffset, mRightTextPaint);
                }
            } else {//中间的纬线
                //画纬线
                float tempLatitudeSpace = i * latitudeSpace;//纬线间隙,i此时应从1开始,因为第一个if屏蔽了0
                path.moveTo(mCoordinateMarginLeft, tempLatitudeSpace);
                path.lineTo(mCoordinateWidth, tempLatitudeSpace);
                canvas.drawPath(path, mLatitudeLinePaint);
                path.reset();

                if (!TextUtils.isEmpty(leftScale)) {
                    float verticalOffset = getTextVerticalOffsetByGravity(mLatitudeTextGravity, leftScaleHeight);
                    //画纬线刻度(左)
                    canvas.drawText(leftScale, mFixedSpaceWithLeft, tempLatitudeSpace + verticalOffset, mLeftTextPaint);
                }
                if (!TextUtils.isEmpty(rightScale)) {
                    float verticalOffset = getTextVerticalOffsetByGravity(mLatitudeTextGravity, rightScaleHeight);
                    //画纬线刻度(右)
                    canvas.drawText(rightScale, mCoordinateWidth - rightScaleWidth - mFixedSpaceWithLeft, tempLatitudeSpace + verticalOffset, mRightTextPaint);
                }
            }
        }
    }

    private float getTextVerticalOffsetByGravity(TextGravity textGravity, float textHeight) {
        switch (textGravity) {
            case ABOVE_LINE: {
                return 0 - mFixedSpaceWithLatitudeLine;
            }
            case VERTICAL_CENTER_LINE: {
                return textHeight / 2;
            }
            case BLEW_LINE: {
                return textHeight + mFixedSpaceWithLatitudeLine;
            }
        }
        return 0;
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
     * 坐标系刻度适配器
     */
    public static abstract class CoordinateScaleAdapter<T> {

        protected ChartView mChartView;

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


        public void setChartView(ChartView chartView) {
            mChartView = chartView;
        }
    }

    /**
     * 刻度位置
     */
    public enum TextGravity {
        /**
         * 在线之上
         */
        ABOVE_LINE,
        /**
         * 在线之中
         */
        VERTICAL_CENTER_LINE,
        /**
         * 在线之下
         */
        BLEW_LINE
    }

    public CoordinateScaleAdapter getCoordinateScaleAdapter() {
        return mCoordinateScaleAdapter;
    }

    public void setBottomTextSize(float sp) {
        mBottomTextPaint.setTextSize(getPixelSp(sp));
    }

    public void setLeftTextSize(float sp) {
        mLeftTextPaint.setTextSize(getPixelSp(sp));
    }

    public void setRightTextSize(float sp) {
        mRightTextPaint.setTextSize(getPixelSp(sp));
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

    public float getFixedSpaceWithBottom() {
        //乘以2因为上下都需要间隙
        return mFixedSpaceWithLatitudeLine * 2 + Math.abs(mBottomTextPaint.getFontMetrics().ascent);
    }

    public void setLatitudeTextGravity(TextGravity latitudeTextGravity) {
        mLatitudeTextGravity = latitudeTextGravity;
    }

    public void setFixedSpaceWithLeft(float dp) {
        mFixedSpaceWithLeft = getPixelDp(dp);
    }

    public void setFixedSpaceWithLatitudeLine(float dp) {
        mFixedSpaceWithLatitudeLine = getPixelDp(dp);
    }

    @Override
    public void setShownPointNums(int shownPointNums) {
        mShownPointNums = shownPointNums;
    }

    public float getLeftTextWidth(int charCount) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < charCount; i++) {
            sb.append("0");
        }
        return mFixedSpaceWithLeft + mLeftTextPaint.measureText(sb.toString());
    }

}
