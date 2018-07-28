package com.android.tonystark.tonychart.chartview.adapter;

import com.android.tonystark.tonychart.chartview.utils.DataUtils;
import com.android.tonystark.tonychart.chartview.viewbeans.Coordinates;
import com.android.tonystark.tonychart.chartview.viewbeans.Histogram;
import com.android.tonystark.tonychart.chartview.viewbeans.MACDHistogram;

import java.util.List;

public class HistogramCoordinateAdapter extends Coordinates.CoordinateScaleAdapter<Histogram.HistogramBean> {

    private Histogram mHistogram;

    public HistogramCoordinateAdapter(Histogram histogram) {
        mHistogram = histogram;
    }

    @Override
    public String getYLeftScaleString(List<Histogram.HistogramBean> dataList, int drawPointIndex, int showPointNums, int scaleIndex, int totalYScaleNum) {
        String scale = "0";

        float[] extreme = mHistogram.calculateExtremeY();

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
    public String getYRightScaleString(List<Histogram.HistogramBean> dataList, int drawPointIndex, int showPointNums, int scaleIndex, int totalYScaleNum) {
        return getYLeftScaleString(dataList, drawPointIndex, showPointNums, scaleIndex, totalYScaleNum);
    }

    @Override
    public String getXBottomScaleString(List<Histogram.HistogramBean> dataList, int drawPointIndex, int showPointNums, int scaleIndex, int totalXScaleNum) {
        return "";
    }

}
