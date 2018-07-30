package com.android.tonystark.tonychart.chartview.viewbeans;

import android.view.MotionEvent;
import android.view.View;

import com.android.tonystark.tonychart.chartview.views.ChartViewImp;

import java.util.List;

/**
 * 描述：绘图基础组件接口
 *
 * @author xuhao
 * @version 1.0
 */
public interface ChartView {

    void addChild(ViewContainer vc);

    void removeAllChildren();

    void removeChild(ViewContainer vc);

    ViewContainer getFocusedView();

    void requestFocusChild(ViewContainer vc);

    void notifyNeedForceSyncDataWithFocused();

    boolean isFocused(ViewContainer vc);

    List<ViewContainer<Object>> getChildren();

    void setYMax(float YMax);

    void setYMin(float YMin);

    void snapshotSwitch(boolean switcher);

    boolean isSnapshotOpen();

    boolean onTouchEvent(MotionEvent event);

    void followTouch(ChartViewImp view);

    void loseFollow(ChartViewImp view);

}
