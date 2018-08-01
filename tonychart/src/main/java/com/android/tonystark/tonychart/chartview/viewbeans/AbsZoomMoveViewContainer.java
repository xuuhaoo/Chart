package com.android.tonystark.tonychart.chartview.viewbeans;

import android.content.Context;
import android.graphics.PointF;
import android.support.annotation.CallSuper;
import android.view.MotionEvent;

public class AbsZoomMoveViewContainer<T> extends ViewContainer<T> {
    //最小手指间距离
    private static final int MIN_FINGER_DISTANCE = 10;
    //最小移动距离
    private static final int MIN_MOVE_DISTANCE = 5;
    //放大缩小中心蜡烛下标
    private int mZoomCenterIndex = 0;
    //是否正在放大
    private boolean isZooming = false;
    //两指间间隙
    private float mDistance = 0f;
    //滑动的时候摁下的第一个点
    private PointF mMoveDownPointF = new PointF();

    private OnZoomListener mZoomListener;

    private OnMoveListener mMoveListener;

    public AbsZoomMoveViewContainer(Context context) {
        super(context);
    }

    /**
     * 左右移动
     */
    @Override
    public void move(MotionEvent event) {
        if (!isZooming) {//当不缩放的时候
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mMoveDownPointF.x = event.getX();
                    mMoveDownPointF.y = event.getY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    float difX = mMoveDownPointF.x - event.getX();
                    int scale = (int) Math.abs(difX) / 10;
                    scale = scale < 1 ? 1 : scale;
                    if (Math.abs(difX) >= MIN_MOVE_DISTANCE) {
                        move(difX, scale);
                        calculateExtremeYPrivate();
                        notifyMoveListener();
                    }
                    mMoveDownPointF.x = event.getX();
                    mMoveDownPointF.y = event.getY();
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
        mDrawPointIndex = mDrawPointIndex + mShownPointNums >= mDataList.size() ? mDataList.size() - mShownPointNums : mDrawPointIndex;
        mDrawPointIndex = mDrawPointIndex < 0 ? 0 : mDrawPointIndex;
    }

    /**
     * 缩放
     *
     * @param event
     */
    @Override
    public void zoom(MotionEvent event) {
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                isZooming = true;
                mZoomCenterIndex = getZoomCenterPointIndex(event);
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
                            if (zoomOut(scale)) calculateDrawPointIndex(event, scale, -1);//-1代表了缩小
                        } else {
                            //放大
                            if (zoomIn(scale)) calculateDrawPointIndex(event, scale, 1);//1代表了放大
                        }
                        //计算最大最小值
                        calculateExtremeYPrivate();
                        notifyZoomListener();
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

    private void notifyZoomListener() {
        if (mZoomListener != null) {
            try {
                mZoomListener.onZoom(this, mDefaultShowPointNums, mShownPointNums, mMinShownPointNums, mDrawPointIndex, mZoomCenterIndex, mYMax, mYMin);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void notifyMoveListener() {
        if (mMoveListener != null) {
            try {
                mMoveListener.onMove(this, mDrawPointIndex, mShownPointNums, mYMax, mYMin);
            } catch (Exception e) {
                e.printStackTrace();
            }
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
            return true;
        } else {
            //此时显示的点数应该等于最小点数
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
     * 计算坐标极值
     */
    @CallSuper
    protected void calculateExtremeYPrivate() {
        if (isCalculateDataExtremum) {
            float[] value = calculateExtremeY();
            setMinDataValue(value[0]);
            setMaxDataValue(value[1]);
            mYMin = value[0];
            mYMax = value[1];
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

    /**
     * 得到放大缩小的中心点下标
     */
    private int getZoomCenterPointIndex(MotionEvent event) {
        //计算放大中心
        float pointLeft = event.getX(0) < event.getX(1) ? event.getX(0) : event.getX(1);
        float pointRight = event.getX(0) > event.getX(1) ? event.getX(0) : event.getX(1);
        int leftIndex = (int) ((pointLeft * mShownPointNums) / mCoordinateWidth);
        int rightIndex = (int) ((pointRight * mShownPointNums) / mCoordinateWidth);
        //得到两只之间的点相对于总显示根数的根数
        int centerPointNums = (rightIndex - leftIndex) / 2 + leftIndex;
        return mDrawPointIndex + centerPointNums;
    }

    /**
     * 计算绘画点的起始值
     */
    private void calculateDrawPointIndex(MotionEvent event, int scale, int zoomType) {
        //计算左边应消失的根数,从而改变了右边消失的根数,因为总消失根数不变
        int zoomPointIndexTemp = getZoomCenterPointIndex(event);

        if (zoomType == 1) { //放大
            if (zoomPointIndexTemp - mZoomCenterIndex > 0) {
                //目标左移,需要向右纠正,不改变绘图起始坐标,就会让图右移,因为显示条数在变少
            } else if (zoomPointIndexTemp - mZoomCenterIndex < 0) {
                //目标右移,需要向左纠正
                mDrawPointIndex = mDrawPointIndex + scale;
            }
        } else if (zoomType == -1) {//缩小
            if (zoomPointIndexTemp - mZoomCenterIndex > 0) {
                //目标左移,需要向右纠正
                mDrawPointIndex = mDrawPointIndex - scale;
            } else if (zoomPointIndexTemp - mZoomCenterIndex < 0) {
                //目标右移,需要向左纠正,不改变绘图其实坐标,就会让图左移,因为现实条数增多
            }
        }
        //越界判断
        mDrawPointIndex = mDrawPointIndex + mShownPointNums >= mDataList.size() ? mDataList.size() - mShownPointNums : mDrawPointIndex;
        mDrawPointIndex = mDrawPointIndex < 0 ? 0 : mDrawPointIndex;
    }

    public interface OnZoomListener {
        void onZoom(ViewContainer viewContainer, int defaultShownNums, int currentShownNums, int minShownPointNums, int drawPointIndex, int zoomCenterPointIndex, float yMax, float yMin);
    }

    public interface OnMoveListener {
        void onMove(ViewContainer viewContainer, int drawPointIndex, int currentShownNums, float yMax, float yMin);
    }

    public void setOnZoomListener(OnZoomListener zoomListener) {
        mZoomListener = zoomListener;
    }

    public void setOnMoveListener(OnMoveListener moveListener) {
        mMoveListener = moveListener;
    }
}
