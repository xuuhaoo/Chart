package com.android.tonystark.tonychart.chartview.adapter;

import com.android.tonystark.tonychart.chartview.utils.DataUtils;
import com.android.tonystark.tonychart.chartview.viewbeans.BrokenLine;
import com.android.tonystark.tonychart.chartview.viewbeans.CandleLine;
import com.android.tonystark.tonychart.chartview.viewbeans.Coordinates;

import java.util.List;
 class BrokenLineCoordinateAdapter extends Coordinates.CoordinateScaleAdapter<String> {

    private BrokenLine mBrokenLine;

    public BrokenLineCoordinateAdapter(BrokenLine brokenLine) {
        mBrokenLine = brokenLine;
    }

    @Override
    public String getYLeftScaleString(List<String> dataList, int drawPointIndex, int showPointNums, int scaleIndex, int totalYScaleNum) {
        String scale = "0";

        float[] extreme = mBrokenLine.calculateExtremeY();

        float min = extreme[0];
        float max = extreme[1];

        float decrease = (max - min) / (totalYScaleNum - 1);//递减量
        for (int i = 0; i <= scaleIndex; i++) {
            scale = (max - (decrease * i)) + "";
        }

        return DataUtils.format(scale, 3, true);
    }

    @Override
    public String getYRightScaleString(List<String> dataList, int drawPointIndex, int showPointNums, int scaleIndex, int totalYScaleNum) {
        return getYLeftScaleString(dataList, drawPointIndex, showPointNums, scaleIndex, totalYScaleNum);
    }

    @Override
    public String getXBottomScaleString(List<String> dataList, int drawPointIndex, int showPointNums, int scaleIndex, int totalXScaleNum) {
        return "";
    }

}
