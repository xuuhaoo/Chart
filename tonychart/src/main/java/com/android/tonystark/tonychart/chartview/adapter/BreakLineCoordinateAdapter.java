package com.android.tonystark.tonychart.chartview.adapter;

import com.android.tonystark.tonychart.chartview.utils.DataUtils;
import com.android.tonystark.tonychart.chartview.viewbeans.CandleLine;
import com.android.tonystark.tonychart.chartview.viewbeans.Coordinates;

import java.util.List;

public class BreakLineCoordinateAdapter extends Coordinates.CoordinateScaleAdapter<String> {

    public BreakLineCoordinateAdapter() {
    }

    @Override
    public String getYLeftScaleString(List<String> dataList, int drawPointIndex, int showPointNums, int scaleIndex, int totalYScaleNum) {
        String scale = "0.00";

        try {
            if (dataList == null || dataList.isEmpty()) {
                return scale;
            }
            //最大的对象
            float max = Float.parseFloat(dataList.get(drawPointIndex));
            //最小的对象
            float min = Float.parseFloat(dataList.get(drawPointIndex));

            //查找最大最小对象
            for (int i = drawPointIndex + 1; i < drawPointIndex + showPointNums && i < dataList.size(); i++) {
                float value = Float.parseFloat(dataList.get(i));
                max = value > max ? value : max;
                min = value < min ? value : min;
            }

            max = calYMaxWithSpace(max, min, totalYScaleNum);
            min = calYMinWithSpace(max, min, totalYScaleNum);

            float decrease = (max - min) / (totalYScaleNum - 1);//递减量
            for (int i = 0; i <= scaleIndex; i++) {
                scale = (max - (decrease * i)) + "";
            }

            return DataUtils.format(scale, 2, true);
        } catch (Exception e) {
            return scale;
        }
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
