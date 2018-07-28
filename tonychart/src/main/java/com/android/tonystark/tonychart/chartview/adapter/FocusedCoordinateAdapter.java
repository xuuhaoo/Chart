package com.android.tonystark.tonychart.chartview.adapter;

import com.android.tonystark.tonychart.chartview.utils.DataUtils;
import com.android.tonystark.tonychart.chartview.viewbeans.ChartView;
import com.android.tonystark.tonychart.chartview.viewbeans.Coordinates;
import com.android.tonystark.tonychart.chartview.viewbeans.ViewContainer;

import java.util.List;

public class FocusedCoordinateAdapter extends Coordinates.CoordinateScaleAdapter {

    private ChartView mChartView;

    public FocusedCoordinateAdapter(ChartView chartView) {
        mChartView = chartView;
    }

    @Override
    public String getYLeftScaleString(List dataList, int drawPointIndex, int showPointNums, int scaleIndex, int totalYScaleNum) {
        String scale = "0";

        ViewContainer viewContainer = mChartView.getFocusedView();

        float[] extreme = viewContainer.calculateExtremeY();

        float min = extreme[0];
        float max = extreme[1];

        max = calYMaxWithSpace(max, min, totalYScaleNum);
        min = calYMinWithSpace(max, min, totalYScaleNum);

        float decrease = (max - min) / (totalYScaleNum - 1);//递减量
        for (int i = 0; i <= scaleIndex; i++) {
            scale = (max - (decrease * i)) + "";
        }

        return DataUtils.format(scale, 3, true);
    }

    @Override
    public String getYRightScaleString(List dataList, int drawPointIndex, int showPointNums, int scaleIndex, int totalYScaleNum) {
        return getYLeftScaleString(dataList, drawPointIndex, showPointNums, scaleIndex, totalYScaleNum);
    }

    @Override
    public String getXBottomScaleString(List dataList, int drawPointIndex, int showPointNums, int scaleIndex, int totalXScaleNum) {
        return "";
    }
}
