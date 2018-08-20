package com.android.tonystark.tonychart.chartview.viewbeans;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathEffect;
import android.graphics.PointF;
import android.graphics.RectF;
import android.support.annotation.NonNull;

import com.android.tonystark.tonychart.chartview.interfaces.AboveCoordinatesView;
import com.android.tonystark.tonychart.chartview.interfaces.UnabelFocusedsView;
import com.android.tonystark.tonychart.chartview.utils.DataUtils;

import java.util.List;

/**
 * 数据指示器
 */
public class IndicatorLine extends ZoomMoveViewContainer implements UnabelFocusedsView, AboveCoordinatesView {
    //线画笔
    private Paint mLinePaint = null;
    //文字画笔
    private Paint mTextPaint = null;
    //文字背景色画笔
    private Paint mTextBgPaint = null;
    //线颜色
    private int mLineColor = Color.BLACK;
    //保留小数位
    private int mKeepNums = 3;
    //文字颜色
    private int mTextColor = Color.WHITE;
    //文字背景框
    private int mTextBackgroundColor = Color.BLACK;
    //是否显示纬线
    private boolean isShowLatitude = true;
    //是否显示文字区域
    private boolean isShowTextArea = true;
    //是否显示文字背景
    private boolean isShowTextBackground = true;
    //数据指示器数据解析
    private IndicatorLineDataParser mIndicatorLineDataParser;
    //横向的padding
    private float mPaddingHorizontalDP = 2;
    //纵向的padding
    private float mPaddingVerticalDP = 2;
    //圆角半径
    private float mCornerRoundRadius = 4;
    //当前坐标点,仅Y轴即可,X轴数据为0
    private PointF mPointF = new PointF();
    //边距
    private float mSpace;

    public IndicatorLine(Context context) {
        super(context);
        init();
    }

    public IndicatorLine(Context context, IndicatorLineDataParser indicatorLineDataParser) {
        super(context);
        mIndicatorLineDataParser = indicatorLineDataParser;
        init();
    }

    private void init() {
        mLinePaint = new Paint();
        mLinePaint.setColor(mLineColor);
        mLinePaint.setStyle(Paint.Style.STROKE);
        mLinePaint.setAntiAlias(true);
        mLinePaint.setStrokeWidth(getPixelDp(1));

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

        //初始化空隙,该空隙用于文字与边距和纬线之间的距离
        mSpace = getPixelSp(2);
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        try {
            if (isShow) {
                checkParameter();
                float valueWhichNeedIndicated = mIndicatorLineDataParser.indicateData(mDataList,
                        mDrawPointIndex,
                        mShownPointNums,
                        mYMax,
                        mYMin);
                if (valueWhichNeedIndicated > mYMax || valueWhichNeedIndicated < mYMin) {
                    return;
                }
                if (isShowLatitude) {
                    //绘制纬线
                    drawLatitude(canvas, valueWhichNeedIndicated);
                }
                if (isShowTextArea) {
                    //绘制文字相关内容
                    drawTextStuff(canvas, valueWhichNeedIndicated);
                }
            }
        } catch (Exception ignored) {
        }
    }

    /**
     * 绘制纬线
     *
     * @param canvas
     * @param valueWhichNeedIndicated
     */
    private void drawLatitude(Canvas canvas, float valueWhichNeedIndicated) {
        mPointF.y = (1f - (valueWhichNeedIndicated - mYMin) / (mYMax - mYMin)) * mCoordinateHeight;
        Path path = new Path();
        path.moveTo(mLinePaint.getStrokeWidth() + mCoordinateMarginLeft, mPointF.y);
        path.lineTo(mLinePaint.getStrokeWidth() + mCoordinateWidth, mPointF.y);
        canvas.drawPath(path, mLinePaint);
    }

    /**
     * 绘制文字相关的东西
     */
    private void drawTextStuff(Canvas canvas, float valueWhichNeedIndicated) {
        mPointF.y = (1f - (valueWhichNeedIndicated - mYMin) / (mYMax - mYMin)) * mCoordinateHeight;
        mPointF.x = 0;
        String str = DataUtils.format(valueWhichNeedIndicated, mKeepNums, true);
        //文字高度
        Paint.FontMetrics fm = new Paint.FontMetrics();
        mTextPaint.getFontMetrics(fm);
        float textHeight = Math.abs(fm.ascent);
        //文字框子最小高度
        float minHeight = textHeight + getPixelDp(mPaddingVerticalDP) * 2;
        //因为框子是在线中央的
        minHeight -= minHeight / 2f;
        //文字框子的最大高度
        float maxHeight = mCoordinateHeight - minHeight;
        mPointF.y = mPointF.y < minHeight ? minHeight : (mPointF.y > maxHeight ? maxHeight : mPointF.y);
        //计算背景宽高
        RectF roundBg = new RectF();
        roundBg.left = mSpace;
        roundBg.top = mPointF.y;
        roundBg.right = roundBg.left + mTextPaint.measureText(str) + getPixelDp(mPaddingHorizontalDP) * 2;
        roundBg.bottom = roundBg.top + textHeight + getPixelDp(mPaddingVerticalDP) * 2;
        //保证线在这个矩形中间
        float recHeight = roundBg.height();
        roundBg.top -= recHeight / 2;
        roundBg.bottom -= recHeight / 2;

        //绘制背景
        if (isShowTextBackground) {
            canvas.drawRoundRect(roundBg, mCornerRoundRadius, mCornerRoundRadius, mTextBgPaint);
        }
        //绘制文字
        canvas.drawText(str, roundBg.left + getPixelDp(mPaddingHorizontalDP), roundBg.top + getPixelDp(mPaddingVerticalDP) / 2 + textHeight, mTextPaint);
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
        if (this.mIndicatorLineDataParser == null) {
            throw new IllegalArgumentException("indicator can't null,IndicatorLine needs the parser to parse the data to know which data need indicated");
        }
    }

    /**
     * 设置数据分析器
     *
     * @param indicatorLineDataParser 数据指示器
     */
    public void setIndicatorLineDataParser(@NonNull IndicatorLineDataParser indicatorLineDataParser) {
        mIndicatorLineDataParser = indicatorLineDataParser;
    }

    /**
     * 设置保留小数
     */
    public void setKeepNums(int keepNums) {
        mKeepNums = keepNums;
    }

    /**
     * 设置线的样式
     */
    public void setLatitudeLineEffect(PathEffect pathEffect) {
        mLinePaint.setPathEffect(pathEffect);
    }

    public void setLineColor(int lineColor) {
        mLineColor = lineColor;
        mLinePaint.setColor(lineColor);
    }

    public void setTextColor(int textColor) {
        mTextColor = textColor;
        mTextPaint.setColor(textColor);
    }

    public void setTextBackgroundColor(int textBackground) {
        mTextBackgroundColor = textBackground;
        mTextBgPaint.setColor(textBackground);
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

    public void setTextSize(float sp) {
        mTextPaint.setTextSize(getPixelSp(sp));
    }

    public void setLineWidth(float dp) {
        mLinePaint.setStrokeWidth(getPixelDp(dp));
    }

    public interface IndicatorLineDataParser<T extends Object> {
        /**
         * 返回需要标注的值
         *
         * @param dataList       数据集合
         * @param drawPointIndex 左边开始绘制第一条数据的下标
         * @param showPointNums  当前屏幕显示的所有数据个数
         * @param yMax           坐标系最大值
         * @param yMin           坐标系最小值
         * @return
         */
        float indicateData(List<T> dataList, int drawPointIndex, int showPointNums, float yMax, float yMin);
    }


}
