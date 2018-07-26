package com.android.tonystark.tonychart.chartview.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PathEffect;
import android.graphics.PointF;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;

import com.android.tonystark.tonychart.chartview.viewbeans.ChartView;
import com.android.tonystark.tonychart.chartview.viewbeans.Coordinates;
import com.android.tonystark.tonychart.chartview.viewbeans.CrossLine;
import com.android.tonystark.tonychart.chartview.viewbeans.ViewContainer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 描述：绘图的View
 *
 * @author xuhao
 * @version 1.0
 */
public class ChartViewImp extends View implements ChartView {
    private Context mContext = null;
    //手势跟随View
    private ChartViewImp mFollowView;
    //是否跟随手势了
    private boolean isFollowed;
    //画布容器
    private ViewContainer<Object> mViewContainer = null;
    //坐标系
    private Coordinates mCoordinates = null;
    //十字线
    private CrossLine mCrossLine = null;
    //是否开启快照
    private boolean isSnapshotOpen = false;
    //快照内存空间
    private Bitmap mSnapshotBitmap = null;
    //快照绘图画布
    private Canvas mSnapshotCanvas = null;
    //是否有底部间隙
    private boolean isHasBottomBlack = true;
    //左边间隙
    private int coordinateMarginLeft = 0;
    //手指摁下时的xy点
    private PointF mDownPointF = new PointF();
    //当前聚焦的View,数据都将以这个View为主
    private ViewContainer mFocusedView;
    //聚焦是否变化
    private boolean mFocusHasChanged;
    //强制刷新聚焦变化
    private boolean mForceFlushFocusChanged;
    //长摁回调
    private Handler mLongClickHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg != null) {
                Bundle bundle = msg.getData();
                if (bundle != null && mCrossLine != null && !mCrossLine.isShow()) {
                    MotionEvent event = bundle.getParcelable("down_event");
                    mCrossLine.move(event);
                }
            }
        }
    };

    public ChartViewImp(Context context) {
        super(context);
        this.mContext = context;
        initObject();
    }

    public ChartViewImp(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        initObject();
    }

    public ChartViewImp(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mContext = context;
        initObject();
    }

    /**
     * 初始化必要对象
     */
    private void initObject() {
        this.setBackgroundColor(0xffffffff);
        mViewContainer = new ViewContainer(mContext);//所有控件承载体
        mCoordinates = new Coordinates(mContext);//坐标系
        mCrossLine = new CrossLine(mContext);//十字线
    }

    /**
     * 加入模块组件
     */
    @Override
    final public void addChild(ViewContainer vc) {
        mViewContainer.addChildren(vc);

        vc.setChartView(this);
        vc.setCoordinate(mCoordinates);
        setDrawRect(vc, this.getMeasuredWidth(), this.getMeasuredHeight());
        setMarginLeft(coordinateMarginLeft);
        //传入坐标系最高最低
        vc.setYMax(mViewContainer.getYMax());
        vc.setYMin(mViewContainer.getYMin());
        vc.setMaxDataValue(mViewContainer.getMaxDataValue());
        vc.setMinDataValue(mViewContainer.getMinDataValue());
        //保证聚焦组件不为空
        if (mFocusedView == null) {
            vc.requestFocuse();
        }
        mForceFlushFocusChanged = true;
        invalidate();
    }

    public void followTouch(ChartViewImp view) {
        //让另一个人知道自己
        view.mFollowView = this;
        isFollowed = true;
    }

    @Override
    public void loseFollow(ChartViewImp view) {
        view.mFollowView = null;
        isFollowed = false;
    }

    /**
     * 将模块请求聚焦
     */
    @Override
    public void requestFocusChild(ViewContainer vc) {
        for (ViewContainer temp : getChildren()) {
            if (temp.equals(vc)) {
                if (!temp.equals(mFocusedView)) {
                    mFocusHasChanged = true;
                    mFocusedView = temp;
                    invalidate();
                }
            }
        }
    }

    /**
     * 通知焦点模块数据改变,其他的模块需要刷新数据
     */
    @Override
    public void notifyNeedForceFlushData() {
        mForceFlushFocusChanged = true;
    }

    /**
     * 判断该组件是否是当前聚焦的组件
     *
     * @return true 是聚焦的组件
     */
    @Override
    public boolean isFocused(ViewContainer vc) {
        if (mFocusedView != null) {
            return mFocusedView.equals(vc);
        } else {
            return false;
        }
    }

    /**
     * 是否焦点改变了
     *
     * @return
     */
    public boolean isFocusHasChanged() {
        return mFocusHasChanged;
    }

    /**
     * 获得所有的绘制模块
     *
     * @return
     */
    @Override
    public List<ViewContainer<Object>> getChildren() {
        return mViewContainer.getChildrenList();
    }

    /**
     * 删除组件
     */
    @Override
    final public void removeChild(ViewContainer vc) {
        if (!(vc instanceof Coordinates) && !(vc instanceof CrossLine)) {
            if (vc.equals(mFocusedView)) {
                removeFocused();
            }
            mViewContainer.removeChildren(vc);
            invalidate();
        }
    }

    /**
     * 删除所有组件
     */
    @Override
    final public void removeAllChildren() {
        Iterator<ViewContainer<Object>> it = mViewContainer.getChildrenList().iterator();
        while (it.hasNext()) {
            ViewContainer view = it.next();
            if (!(view instanceof Coordinates) && !(view instanceof CrossLine)) {
                it.remove();
            }
        }
        removeFocused();
        invalidate();
    }

    private void removeFocused() {
        mFocusedView = null;
        setYMax(0);
        setYMin(0);
        mCrossLine.setDataList(new ArrayList<String>());
        mCoordinates.setDataList(new ArrayList<Object>());
        invalidate();
    }

    /**
     * 给组件左间距
     *
     * @param left 左边距
     */
    public void setMarginLeft(int left) {
        this.coordinateMarginLeft = left;
        mViewContainer.setCoordinateMarginLeft(left);
        mCoordinates.setCoordinateMarginLeft(coordinateMarginLeft);
        mCrossLine.setCoordinateMarginLeft(coordinateMarginLeft);
    }


    /**
     * 给组件设置画布的宽高
     */
    private void setDrawRect(int width, int height) {
        //初始化高度
        List<ViewContainer<Object>> list = mViewContainer.getChildrenList();
        for (int i = 0; i < list.size(); i++) {
            ViewContainer<Object> vc = list.get(i);
            setDrawRect(vc, width, height);
        }

        setDrawRect(mCoordinates, width, height);
        setDrawRect(mCrossLine, width, height);
    }

    /**
     * 给组件设置画布的宽高
     */
    private void setDrawRect(ViewContainer vc, int width, int height) {
        if (isHasBottomBlack && mCoordinates != null) {
            height = height - mCoordinates.getMarginBottom();//为底部留出空隙
        }
        vc.setCoordinateHeight(height);
        vc.setCoordinateWidth(width);
    }

    /**
     * 设置数据的最大数值
     *
     * @param dataMax 最大数值
     */
    final public void setDataMax(float dataMax) {
        mViewContainer.setMaxDataValue(dataMax);
    }

    /**
     * 设置数据的最小数值
     *
     * @param dataMin 最小数值
     */
    final public void setDataMin(float dataMin) {
        mViewContainer.setMinDataValue(dataMin);
    }

    /**
     * 设置坐标系最大表示数值
     */
    @Override
    final public void setYMax(float YMax) {
        mViewContainer.setYMax(YMax);
        mCoordinates.setYMax(YMax);
        mCrossLine.setYMax(YMax);
    }

    /**
     * 设置坐标系最小表示数值
     */
    @Override
    final public void setYMin(float YMin) {
        mViewContainer.setYMin(YMin);
        mCoordinates.setYMin(YMin);
        mCrossLine.setYMin(YMin);
    }

    @Override
    final protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setDrawRect(getMeasuredWidth(), getMeasuredHeight());
    }

    @Override
    final protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
    }

    /**
     * 创建快照画布
     */
    private void createBufferPaintCanvas(int width, int height) {
        if (mSnapshotBitmap == null) {
            mSnapshotBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            mSnapshotCanvas = new Canvas(mSnapshotBitmap);
        } else {
            //do nothing;
        }
    }

    /**
     * 销毁快照画布
     */
    private void destroyBufferPaintCanvas() {
        if (mSnapshotBitmap != null) {
            mSnapshotBitmap.recycle();
            mSnapshotBitmap = null;
            mSnapshotCanvas = null;
            System.gc();
        }
    }

    /**
     * <p>开启/关闭快照模式</p>
     * <p>
     * 当绘制内容过多时,且暂时不要求实时绘制,或者屏幕短暂滚动,布局变化等,为了保持流畅度
     * 建议打开快照模式,并且在滚动或者变化结束后,及时关闭该模式,否则导致不能实时绘制
     *
     * @param open true开启,false关闭
     */
    final public void snapshotSwitch(boolean open) {
        if (mSnapshotBitmap != null && open) {
            destroyBufferPaintCanvas();
        }
        if (open) {
            if (this.getMeasuredHeight() == 0 && this.getMeasuredWidth() == 0) {//如果为0时,观察将要绘制时进行快照
                this.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                    @Override
                    public boolean onPreDraw() {
                        getViewTreeObserver().removeOnPreDrawListener(this);
                        createBufferPaintCanvas(getMeasuredWidth(), getMeasuredHeight());
                        clearCanvas(mSnapshotCanvas);
                        //绘画快照
                        mCoordinates.draw(mSnapshotCanvas);
                        mViewContainer.draw(mSnapshotCanvas);
                        isSnapshotOpen = true;
                        invalidate();
                        return true;
                    }
                });
            } else {
                createBufferPaintCanvas(getMeasuredWidth(), getMeasuredHeight());
                clearCanvas(mSnapshotCanvas);
                //绘画快照
                mCoordinates.draw(mSnapshotCanvas);
                mViewContainer.draw(mSnapshotCanvas);
                isSnapshotOpen = true;
                invalidate();
            }

        } else {
            destroyBufferPaintCanvas();
            isSnapshotOpen = false;
        }
    }

    @Override
    final protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (isFocusHasChanged() || mForceFlushFocusChanged) {
            Log.i("OnDraw", "flush the focus data");
            mFocusHasChanged = false;
            changedFocusDataPreDraw();
            //在最后保证changedFocusDataPreDraw函数执行完后还原forceflush状态.
            mForceFlushFocusChanged = false;
        }
        if (isSnapshotOpen && mSnapshotBitmap != null && !isFocusHasChanged()) {   //绘制快照
            canvas.drawBitmap(mSnapshotBitmap, 0, 0, null);
        } else {//实时绘制
            snapshotSwitch(false);
            clearCanvas(canvas);
            mViewContainer.draw(canvas);
            mCoordinates.draw(canvas);
        }
        if (mCrossLine != null) {
            mCrossLine.draw(canvas);
        }
    }

    private void changedFocusDataPreDraw() {
        if (mFocusedView == null) {
            return;
        }
        //设置十字线
        if (mCrossLine != null) {
            //设置十字线数据显示个数,跟随焦点组件
            mCrossLine.setShownPointNums(mFocusedView.getShownPointNums());
            //设置十字线单点偏移量,跟随焦点组件
            mCrossLine.setSinglePointOffset(mFocusedView.getSingleDataWidth() / 2);
            //设置十字线需要展示的数据集,跟随焦点组件
            mCrossLine.setDataList(mFocusedView.getCrossDataList());
        }

        //设置坐标系参数
        if (mCoordinates != null) {
            //设置坐标系数据显示点数,跟随焦点组件
            mCoordinates.setShownPointNums(mFocusedView.getShownPointNums());
            //实时更新坐标系数据
            mCoordinates.setDataList(mFocusedView.getDataList());
        }

        //设置其他的组件默认显示组件点数,跟随焦点组件
        mViewContainer.setDefaultShowPointNums(mFocusedView.getDefaultShowPointNums());
        //设置其他的组件数据开始绘制下标,跟随焦点组件
        mViewContainer.setDrawPointIndex(mFocusedView.getDrawPointIndex());
        //设置最少能显示的数据个数,跟随焦点组件
        mViewContainer.setMinShownPointNums(mFocusedView.getMinShownPointNums());
        //设置其他组件不计算坐标系最大最小值
        mViewContainer.setCalculateDataExtremum(false);
        //一定要在ViewContainer设置之后设置,设置焦点View计算最大最小值
        mFocusedView.setCalculateDataExtremum(true);

        //设置横纵坐坐标极值
        float[] minmax = mFocusedView.calculateExtremeYWhenFocused();
        //设置Y轴最小值
        setYMin(minmax[0]);
        //设置Y轴最大值
        setYMax(minmax[1]);
        //设置数据最大值
        setDataMin(minmax[0]);
        //设置数据最小值
        setDataMax(minmax[1]);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(isFollowed){
            return true;
        }
        onTouchEventPrivate(event);
        return true;
    }

    private void onTouchEventPrivate(MotionEvent event) {
        if (mFocusedView != null) {
            //更新十字线
            if (mCrossLine != null) {
                //设置十字线数据显示个数,跟随焦点组件
                mCrossLine.setShownPointNums(mFocusedView.getShownPointNums());
                //设置十字线单点偏移量,跟随焦点组件
                mCrossLine.setSinglePointOffset(mFocusedView.getSingleDataWidth() / 2);
                //设置十字线组件数据开始绘制下标,跟随焦点组件
                mCrossLine.setDrawPointIndex(mFocusedView.getDrawPointIndex());
                //设置十字线需要展示的数据集,跟随焦点组件
                mCrossLine.setDataList(mFocusedView.getCrossDataList());
            }
            //确定所有组件不计算大小
            mViewContainer.setCalculateDataExtremum(false);
            //一定要在ViewContainer设置之后设置,确保focus是计算大小的
            mFocusedView.setCalculateDataExtremum(true);

            //设置聚焦组件的最大最小
            setYMax(mFocusedView.getYMax());
            setYMin(mFocusedView.getYMin());
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                //当按下时,延迟200毫秒.避免滑动时,会显示十字线
                Message message = new Message();
                Bundle bundle = new Bundle();
                MotionEvent motionEvent = MotionEvent.obtain(event.getDownTime(),
                        event.getEventTime(),
                        MotionEvent.ACTION_DOWN,
                        event.getX(),
                        event.getY(),
                        event.getMetaState());
                bundle.putParcelable("down_event", motionEvent);
                message.setData(bundle);
                mLongClickHandler.sendMessageDelayed(message, 200);
                mDownPointF.x = event.getX();
                mDownPointF.y = event.getY();
                break;
            }
            case MotionEvent.ACTION_POINTER_DOWN: {
                //当2指按下触摸时,模拟up事件发送,让十字线不显示
                if (mCrossLine.isShow() && event.getPointerCount() >= 2) {
                    MotionEvent motionEvent = MotionEvent.obtain(0, 0, MotionEvent.ACTION_UP, 0, 0, 0);
                    mLongClickHandler.removeCallbacksAndMessages(null);
                    mCrossLine.move(motionEvent);

                }
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                //如果长按200毫秒还没达到时,如果滑动就设置isShowCrossLine为false
                if (!mCrossLine.isShow()) {
                    if (spacing(event) > 5) {
                        mLongClickHandler.removeCallbacksAndMessages(null);
                        mCrossLine.setShow(false);
                    }
                } else if (mCrossLine.isShow()) {
                    //让其移动
                    mCrossLine.move(event);
                }
                break;
            }
            case MotionEvent.ACTION_UP: {
                if (mCrossLine.isShow()) {
                    mLongClickHandler.removeCallbacksAndMessages(null);
                    mCrossLine.move(event);
                }
                break;
            }
        }

        if (!getCrossLine().isShow()) {//十字线不显示的时候,处理左右移动
            mViewContainer.zoom(event);
            mViewContainer.move(event);
        }

        invalidate();

        if (mFollowView != null) {
            mFollowView.onTouchEventPrivate(event);
        }
    }

    /**
     * 快照是否打开
     */
    public boolean isSnapshotOpen() {
        return isSnapshotOpen;
    }

    /**
     * 清理画布
     */
    private void clearCanvas(Canvas canvas) {
        canvas.drawColor(Color.WHITE);
    }

    /**
     * 设置坐标系刻度适配器
     */
    public void setCoordinateScaleAdapter(Coordinates.CoordinateScaleAdapter adapter) {
        if (mCoordinates != null) {
            mCoordinates.setCoordinateScaleAdapter(adapter);
        }
    }

    /**
     * 经线
     */
    public void setCoordinateLongitudeNum(int longitude) {
        if (mCoordinates != null) {
            mCoordinates.setLongitudeNums(longitude);
        }
    }

    /**
     * 纬线
     */
    public void setCoordinateLatitudeNum(int latitude) {
        if (mCoordinates != null) {
            mCoordinates.setLatitudeNums(latitude);
        }
    }

    /**
     * 获取纬线个数
     */
    public int getLatitudeNums() {
        if (mCoordinates != null) {
            return mCoordinates.getLatitudeNums();
        }
        return 0;
    }

    /**
     * 获取经线个数
     */
    public int getLongitudeNums() {
        if (mCoordinates != null) {
            return mCoordinates.getLongitudeNums();
        }
        return 0;
    }

    /**
     * 设置坐标系的线的样式
     */
    public void setCoordinateLineEffect(PathEffect pathEffect) {
        if (mCoordinates != null) {
            mCoordinates.setLongitudeLineEffect(pathEffect);
            mCoordinates.setLatitudeLineEffect(pathEffect);
        }
    }

    /**
     * 设置坐标系的线颜色
     */
    public void setCoordinateLineColor(int color) {
        if (mCoordinates != null) {
            mCoordinates.setLatitudeLineColor(color);
            mCoordinates.setLongitudeLineColor(color);
        }
    }

    /**
     * 设置坐标系的文字颜色
     */
    public void setCoordinateTextColor(int color) {
        if (mCoordinates != null) {
            mCoordinates.setLeftTextColor(color);
            mCoordinates.setRightTextColor(color);
            mCoordinates.setBottomTextColor(color);
        }
    }

    /**
     * 设置坐标系数据w3
     */
    public void setCoordinateDataList(List dataList) {
        if (mCoordinates != null) {
            mCoordinates.setDataList(dataList);
        }
    }

    /**
     * 设置底部间隙
     */
    public void setHasBottomScaleBlack(boolean has) {
        this.isHasBottomBlack = has;
    }

    public CrossLine getCrossLine() {
        return mCrossLine;
    }

    private float spacing(MotionEvent event) {
        float x = Math.abs(event.getX() - mDownPointF.x);
        float y = Math.abs(event.getY() - mDownPointF.y);
        return (float) Math.sqrt(x * x + y * y);
    }
}
