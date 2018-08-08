package com.android.tonystark.tonychart.chartview.viewbeans;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.text.TextUtils;
import android.view.MotionEvent;

import com.android.tonystark.tonychart.chartview.interfaces.UnabelFocusedsView;
import com.android.tonystark.tonychart.chartview.utils.DataUtils;

import java.util.List;

/**
 * 描述：十字线
 *
 * @author xuhao
 * @version 1.0
 */
public class CrossLine extends ZoomMoveViewContainer<String> implements UnabelFocusedsView {
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
    //手指焦点
    private PointF mFingerPointF = new PointF();
    //十字焦点
    private PointF mCrossPointF = new PointF();
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
    //是否显示纬线刻度
    private boolean isShowLatitudeScaleText = true;
    //是否显示经线刻度
    private boolean isShowLongitudeScaleText = true;
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
    //边距
    private float mSpace;
    //横向的padding
    private float mPaddingHorizontalDP = 2;
    //纵向的padding
    private float mPaddingVerticalDP = 1;
    //圆角半径
    private float mCornerRoundRadius = 4;
    //是否显示文字背景
    private boolean isShowTextBackground = true;
    //绘制文字的画笔
    private Paint mTextPaint;
    //文字颜色
    private int mTextColor = Color.WHITE;
    //绘制文字的背景色
    private Paint mTextBgPaint;
    //文字背景色
    private int mTextBackgroundColor = Color.BLACK;

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

        mTextPaint = new Paint();
        mTextPaint.setColor(mTextColor);
        mTextPaint.setStyle(Paint.Style.FILL);
        mTextPaint.setAntiAlias(true);
        mTextPaint.setTextSize(getPixelSp(8));
        mTextPaint.setTextAlign(Paint.Align.LEFT);

        mTextBgPaint = new Paint();
        mTextBgPaint.setColor(mTextBackgroundColor);
        mTextBgPaint.setStyle(Paint.Style.FILL);
        mTextBgPaint.setAntiAlias(true);

        mSpace = getPixelSp(2);
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
                mIndex = (int) ((mFingerPointF.x - mCoordinateMarginLeft) / mPointWidth);
                if (mIndex >= mShownPointNums) {
                    mIndex = mShownPointNums - 1;
                }
                //尽在显示区域内绘制十字线
                if (mDrawPointIndex + mIndex < mDrawPointIndex + mShownPointNums) {
                    float currentValue = mFocusedView.transDataToCrossDataFromDataList(mIndex, mIndex + mDrawPointIndex);
                    if (isShowLatitude) {
                        //绘制纬线
                        drawLatitude(canvas, currentValue);
                        if (isShowLatitudeScaleText) {
                            //是否绘制纬线刻度文字
                            drawLatitudeScaleText(canvas, mIndex, currentValue);
                        }
                    }
                    if (isShowLongitude) {
                        //绘制经线
                        drawLongitude(canvas, mIndex);
                        if (isShowLongitudeScaleText) {
                            //是否绘制经线刻度文字
                            drawLongitudeScaleText(canvas, mIndex);
                        }
                    }
                    if (isShowPoint) {
                        //绘制点
                        drawCircle(canvas, mIndex, currentValue);
                    }

                    if (mOnCrossLineMoveListener != null) {
                        mOnCrossLineMoveListener.onCrossLineMove(mIndex, mDrawPointIndex, mCrossPointF, mFingerPointF);
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
        if (mFingerPointF.x < 0f && mFingerPointF.y < 0f) {
            throw new IllegalArgumentException("mFingerPointF.x mFingerPointF.y,must bigger than -1");
        }
    }

    //绘制纬线
    private void drawLatitude(Canvas canvas, float currentValue) {
        float y = mFingerPointF.y;
        if (isLatitudeFollowData) {
            try {
                y = (1f - (currentValue - mYMin) / (mYMax - mYMin)) * mCoordinateHeight;
            } catch (NumberFormatException e) {
                y = mFingerPointF.y;
            }
        }
        mCrossPointF.y = y;
        canvas.drawLine(mCoordinateMarginLeft, y, mCoordinateWidth, y, mLinePaint);
    }

    //绘制经线
    private void drawLongitude(Canvas canvas, int index) {
        float x = mFingerPointF.x;
        if (isLongitudeFollowData) {
            x = index * mPointWidth + mCoordinateMarginLeft + mSinglePointOffset;
        }
        mCrossPointF.x = x;
        canvas.drawLine(x, 0, x, mCoordinateHeight, mLinePaint);
    }

    //绘制圆
    private void drawCircle(Canvas canvas, int index, float currentValue) {
        float x = mFingerPointF.x;
        float y = mFingerPointF.y;
        try {
            x = index * mPointWidth + mCoordinateMarginLeft + mSinglePointOffset;
        } catch (NumberFormatException e) {
            x = mFingerPointF.x;
        }
        try {
            y = (1f - (currentValue - mYMin) / (mYMax - mYMin)) * mCoordinateHeight;
        } catch (NumberFormatException e) {
            y = mFingerPointF.y;
        }
        mCrossPointF.y = y;
        mCrossPointF.x = x;

        canvas.drawCircle(x, y, mRadius, mPointPaint);
    }

    //绘制纬线刻度
    private void drawLatitudeScaleText(Canvas canvas, int index, float currentValue) {
        if (mOnCrossLineMoveListener == null) {
            return;
        }
        float y = mFingerPointF.y;
        if (isLatitudeFollowData) {
            try {
                y = (1f - (currentValue - mYMin) / (mYMax - mYMin)) * mCoordinateHeight;
            } catch (NumberFormatException e) {
                y = mFingerPointF.y;
            }
        }
        mCrossPointF.y = y;
        String indicateValue = mOnCrossLineMoveListener.onCrossIndicateYScale(index, mDrawPointIndex, mShownPointNums, mYMin, mYMax);
        if (TextUtils.isEmpty(indicateValue)) {
            return;
        }
        //文字高度
        float height = getTextHeight();
        //计算背景宽高
        RectF roundBg = new RectF();
        roundBg.left = mSpace;
        roundBg.top = y;
        roundBg.right = roundBg.left + mTextPaint.measureText(indicateValue) + getPixelDp(mPaddingHorizontalDP) * 2;
        roundBg.bottom = roundBg.top + height + getPixelDp(mPaddingVerticalDP) * 2;
        //保证线在这个矩形中间
        float recHeight = roundBg.height();
        roundBg.top -= recHeight / 2;
        roundBg.bottom -= recHeight / 2;
        //绘制背景
        if (isShowTextBackground) {
            canvas.drawRoundRect(roundBg, mCornerRoundRadius, mCornerRoundRadius, mTextBgPaint);
        }
        //绘制文字
        canvas.drawText(indicateValue, roundBg.left + getPixelDp(mPaddingHorizontalDP), roundBg.top + getPixelDp(mPaddingVerticalDP) / 2 + height, mTextPaint);
    }

    /**
     * 绘制经线刻度
     *
     * @param canvas
     * @param index
     */
    private void drawLongitudeScaleText(Canvas canvas, int index) {
        if (mOnCrossLineMoveListener == null) {
            return;
        }
        float x = mFingerPointF.x;
        if (isLongitudeFollowData) {
            x = index * mPointWidth + mCoordinateMarginLeft + mSinglePointOffset;
        }
        mCrossPointF.x = x;
        String indicateValue = mOnCrossLineMoveListener.onCrossIndicateXScale(index, mDrawPointIndex, mShownPointNums);
        if (TextUtils.isEmpty(indicateValue)) {
            return;
        }
        //文字高度
        float height = getTextHeight();
        //计算背景宽高
        RectF roundBg = new RectF();
        roundBg.left = x;
        roundBg.top = mCoordinateHeight;
        roundBg.right = roundBg.left + mTextPaint.measureText(indicateValue) + getPixelDp(mPaddingHorizontalDP) * 2;
        roundBg.bottom = roundBg.top + height + getPixelDp(mPaddingVerticalDP) * 2;
        //保证线在这个矩形中间
        float recWidth = roundBg.width();
        roundBg.left -= recWidth / 2;
        roundBg.right -= recWidth / 2;
        //绘制背景
        if (isShowTextBackground) {
            canvas.drawRoundRect(roundBg, mCornerRoundRadius, mCornerRoundRadius, mTextBgPaint);
        }
        //绘制文字
        canvas.drawText(indicateValue, roundBg.left + getPixelDp(mPaddingHorizontalDP), roundBg.top + getPixelDp(mPaddingVerticalDP) / 2 + height, mTextPaint);
    }

    /**
     * 文字高度
     */
    private float getTextHeight() {
        Paint.FontMetrics fm = new Paint.FontMetrics();
        mTextPaint.getFontMetrics(fm);
        return Math.abs(fm.ascent);
    }

    @Override
    public void move(MotionEvent event) {
        int index;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (initFocusedView()) return;
                mFingerPointF.x = event.getX();
                mFingerPointF.x = mFingerPointF.x < mCoordinateMarginLeft ? mCoordinateMarginLeft : mFingerPointF.x;
                mPointWidth = (mCoordinateWidth - mCoordinateMarginLeft) / mShownPointNums;
                index = (int) ((mFingerPointF.x - mCoordinateMarginLeft) / mPointWidth);
                if (mFocusedView.getDataListSize() > 0 && index > mFocusedView.getDataListSize() - 1) {
                    index = mFocusedView.getDataListSize() - 1;
                    mFingerPointF.x = index * mPointWidth + mCoordinateMarginLeft;
                }

                mFingerPointF.y = event.getY();
                setShow(true);
                break;
            case MotionEvent.ACTION_MOVE:
                mFingerPointF.x = event.getX();
                mFingerPointF.x = mFingerPointF.x < mCoordinateMarginLeft ? mCoordinateMarginLeft : mFingerPointF.x;
                index = (int) ((mFingerPointF.x - mCoordinateMarginLeft) / mPointWidth);
                if (mFocusedView.getDataListSize() > 0 && index > mFocusedView.getDataListSize() - 1) {
                    index = mFocusedView.getDataListSize() - 1;
                    mFingerPointF.x = index * mPointWidth + mCoordinateMarginLeft;
                }
                mFingerPointF.y = event.getY();
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
        void onCrossLineMove(int crossIndexInScreen, int drawPointIndex, PointF crossPointF, PointF fingerPointF);

        void onCrossLineDismiss();

        String onCrossIndicateYScale(int crossIndexInScreen, int drawPointIndex, int showPointNums, float yMin, float yMax);

        String onCrossIndicateXScale(int crossIndexInScreen, int drawPointIndex, int showPointNums);
    }

    public void setOnCrossLineMoveListener(OnCrossLineMoveListener lineMoveListener) {
        this.mOnCrossLineMoveListener = lineMoveListener;
    }

    public void setPaddingHorizontalDP(float paddingHorizontalDP) {
        mPaddingHorizontalDP = paddingHorizontalDP;
    }

    public void setPaddingVerticalDP(float paddingVerticalDP) {
        mPaddingVerticalDP = paddingVerticalDP;
    }

    public void setCornerRoundRadius(float cornerRoundRadius) {
        mCornerRoundRadius = cornerRoundRadius;
    }

    public void setShowTextBackground(boolean showTextBackground) {
        isShowTextBackground = showTextBackground;
    }

    public void setTextColor(int textColor) {
        mTextColor = textColor;
        mTextPaint.setColor(textColor);
    }

    public void setTextBgPaint(Paint textBgPaint) {
        mTextBgPaint = textBgPaint;
    }

    public void setTextBackgroundColor(int textBackgroundColor) {
        mTextBackgroundColor = textBackgroundColor;
        mTextBgPaint.setColor(mTextBackgroundColor);
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

    public PointF getFingerPointF() {
        return mFingerPointF;
    }

    public void setFingerPointF(PointF fingerPointF) {
        this.mFingerPointF = fingerPointF;
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

    public void setTextSize(float sp) {
        mTextPaint.setTextSize(getPixelSp(sp));
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
