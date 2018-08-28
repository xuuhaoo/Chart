package com.android.tonystark.tonychart.chartview.adapter;

import com.android.tonystark.tonychart.chartview.utils.DataUtils;
import com.android.tonystark.tonychart.chartview.viewbeans.Coordinates;
import com.android.tonystark.tonychart.chartview.viewbeans.MACDHistogram;
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
        scale = (max - (decrease * scaleIndex)) + "";
        boolean isOdd = totalYScaleNum % 2 != 0;
        if (viewContainer instanceof MACDHistogram && isOdd) {
            int middleIndex = (totalYScaleNum - 1) / 2;
            if (scaleIndex == middleIndex) {
                scale = 0.00000000 + "";
            }
        }

        return DataUtils.format(scale, mKeepNums, false);
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
