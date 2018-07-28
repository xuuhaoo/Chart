package com.android.tonystark.tonychart.chartview.adapter;

import com.android.tonystark.tonychart.chartview.utils.DataUtils;
import com.android.tonystark.tonychart.chartview.viewbeans.BrokenLine;
import com.android.tonystark.tonychart.chartview.viewbeans.Coordinates;
import com.android.tonystark.tonychart.chartview.viewbeans.MACDHistogram;

import java.util.List;

public class MACDHistogramCoordinateAdapter extends Coordinates.CoordinateScaleAdapter<MACDHistogram.MACDBean> {

    private MACDHistogram mMACDHistogram;

    public MACDHistogramCoordinateAdapter(MACDHistogram macdHistogram) {
        mMACDHistogram = macdHistogram;
    }

    @Override
    public String getYLeftScaleString(List<MACDHistogram.MACDBean> dataList, int drawPointIndex, int showPointNums, int scaleIndex, int totalYScaleNum) {
        String scale = "0";

        float[] extreme = mMACDHistogram.calculateExtremeY();

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
    public String getYRightScaleString(List<MACDHistogram.MACDBean> dataList, int drawPointIndex, int showPointNums, int scaleIndex, int totalYScaleNum) {
        return getYLeftScaleString(dataList, drawPointIndex, showPointNums, scaleIndex, totalYScaleNum);
    }

    @Override
    public String getXBottomScaleString(List<MACDHistogram.MACDBean> dataList, int drawPointIndex, int showPointNums, int scaleIndex, int totalXScaleNum) {
        return "";
    }

}
