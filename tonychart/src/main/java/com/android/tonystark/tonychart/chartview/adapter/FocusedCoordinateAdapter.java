package com.android.tonystark.tonychart.chartview.adapter;

import com.android.tonystark.tonychart.chartview.utils.DataUtils;
import com.android.tonystark.tonychart.chartview.viewbeans.Coordinates;
import com.android.tonystark.tonychart.chartview.viewbeans.ViewContainer;

import java.util.List;

public class FocusedCoordinateAdapter extends Coordinates.CoordinateScaleAdapter {


    private int mKeepNums = 3;

    @Override
    public String getYLeftScaleString(List dataList, int drawPointIndex, int showPointNums, int scaleIndex, int totalYScaleNum) {
        String scale = "0";
        if (mChartView == null) {
            return scale;
        }

        ViewContainer viewContainer = mChartView.getFocusedView();

        float[] extreme = viewContainer.calculateExtremeY();

        float min = extreme[0];
        float max = extreme[1];

        float decrease = (max - min) / (totalYScaleNum - 1);//递减量
        for (int i = 0; i <= scaleIndex; i++) {
            scale = (max - (decrease * i)) + "";
        }

        return DataUtils.format(scale, mKeepNums, true);
    }

    @Override
    public String getYRightScaleString(List dataList, int drawPointIndex, int showPointNums, int scaleIndex, int totalYScaleNum) {
        return getYLeftScaleString(dataList, drawPointIndex, showPointNums, scaleIndex, totalYScaleNum);
    }

    @Override
    public String getXBottomScaleString(List dataList, int drawPointIndex, int showPointNums, int scaleIndex, int totalXScaleNum) {
        return "";
    }

    public void setKeepNums(int keepNums) {
        mKeepNums = keepNums;
    }

}
