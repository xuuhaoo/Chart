package com.android.tonystark.tonychart.chartview.viewbeans;

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

    void requestFocusChild(ViewContainer vc);

    boolean isFocused(ViewContainer vc);

    List<ViewContainer<Object>> getChildren();

    void setYMax(float YMax);

    void setYMin(float YMin);

    void snapshotSwitch(boolean switcher);

    boolean isSnapshotOpen();


}
