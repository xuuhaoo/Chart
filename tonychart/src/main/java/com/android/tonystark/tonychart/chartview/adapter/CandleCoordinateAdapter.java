package com.android.tonystark.tonychart.chartview.adapter;

import com.android.tonystark.tonychart.chartview.utils.DataUtils;
import com.android.tonystark.tonychart.chartview.viewbeans.CandleLine;
import com.android.tonystark.tonychart.chartview.viewbeans.Coordinates;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
 class CandleCoordinateAdapter extends Coordinates.CoordinateScaleAdapter<CandleLine.CandleLineBean> {

    private SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat("MM-dd HH:mm:ss");

    private CandleLine mCandleLine;

    public CandleCoordinateAdapter(CandleLine candleLine) {
        mCandleLine = candleLine;
    }

    @Override
    public String getYLeftScaleString(List<CandleLine.CandleLineBean> dataList, int drawPointIndex, int showPointNums, int scaleIndex, int totalYScaleNum) {
        String scale = "0";

        float[] extreme = mCandleLine.calculateExtremeY();

        float min = extreme[0];
        float max = extreme[1];

        float decrease = (max - min) / (totalYScaleNum - 1);//递减量
        for (int i = 0; i <= scaleIndex; i++) {
            scale = (max - (decrease * i)) + "";
        }

        return DataUtils.format(scale, 3, true);
    }

    @Override
    public String getYRightScaleString(List<CandleLine.CandleLineBean> dataList, int drawPointIndex, int showPointNums, int scaleIndex, int totalYScaleNum) {
        return getYLeftScaleString(dataList, drawPointIndex, showPointNums, scaleIndex, totalYScaleNum);
    }

    @Override
    public String getXBottomScaleString(List<CandleLine.CandleLineBean> dataList, int drawPointIndex, int showPointNums, int scaleIndex, int totalXScaleNum) {
        try {
            for (int i = drawPointIndex, times = 0;
                 i < showPointNums + drawPointIndex && i < dataList.size();
                 i = i + showPointNums / totalXScaleNum - 1, times++) {
                CandleLine.CandleLineBean bean = dataList.get(i);
                if (times == scaleIndex) {
                    return mSimpleDateFormat.format(new Date(bean.getTimeMills()));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "";
    }
}
