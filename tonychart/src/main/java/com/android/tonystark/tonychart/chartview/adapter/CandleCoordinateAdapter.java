package com.android.tonystark.tonychart.chartview.adapter;

import com.android.tonystark.tonychart.chartview.utils.DataUtils;
import com.android.tonystark.tonychart.chartview.viewbeans.CandleLine;
import com.android.tonystark.tonychart.chartview.viewbeans.Coordinates;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CandleCoordinateAdapter extends Coordinates.CoordinateScaleAdapter<CandleLine.CandleLineBean> {

    private SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat("MM-dd HH:mm:ss");

    @Override
    public String getYLeftScaleString(List<CandleLine.CandleLineBean> dataList, int drawPointIndex, int showPointNums, int scaleIndex, int totalYScaleNum) {
        String scale = "0.00";

        if (dataList == null || dataList.isEmpty()) {
            return scale;
        }
        //最大的对象
        CandleLine.CandleLineBean maxBean = dataList.get(drawPointIndex);
        //最小的对象
        CandleLine.CandleLineBean minBean = dataList.get(drawPointIndex);

        //查找最大最小对象
        for (int i = drawPointIndex + 1; i < drawPointIndex + showPointNums && i < dataList.size(); i++) {
            CandleLine.CandleLineBean bean = dataList.get(i);
            maxBean = bean.getHeightPrice() > maxBean.getHeightPrice() ? bean : maxBean;
            minBean = bean.getLowPrice() < minBean.getLowPrice() && bean.getLowPrice() > 0 ? bean : minBean;
        }

        //最大值
        float max = maxBean.getHeightPrice();
        //最小值
        float min = minBean.getLowPrice();

        max = calYMaxWithSpace(max, min, totalYScaleNum);
        min = calYMinWithSpace(max, min, totalYScaleNum);

        float decrease = (max - min) / (totalYScaleNum - 1);//递减量
        for (int i = 0; i <= scaleIndex; i++) {
            scale = (max - (decrease * i)) + "";
        }

        return DataUtils.format(scale, 2, true);
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
