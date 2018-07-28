package com.android.didi.chart;

import com.android.tonystark.tonychart.chartview.utils.DataUtils;
import com.android.tonystark.tonychart.chartview.viewbeans.BrokenLine;
import com.android.tonystark.tonychart.chartview.viewbeans.CandleLine;
import com.android.tonystark.tonychart.chartview.viewbeans.ChartView;
import com.android.tonystark.tonychart.chartview.viewbeans.Histogram;
import com.android.tonystark.tonychart.chartview.viewbeans.MACDHistogram;
import com.android.tonystark.tonychart.chartview.viewbeans.ViewContainer;

import java.util.ArrayList;
import java.util.List;

public class MyExtremeCalculator implements ViewContainer.ExtremeCalculatorInterface {
    private List<String> mDataInRanged = new ArrayList<>();
    private ChartView mChartView;

    public MyExtremeCalculator(ChartView chartView) {
        mChartView = chartView;
    }

    @Override
    public float onCalculateMax(int drawPointIndex, int showPointNums) {
        fetchAllData(drawPointIndex, showPointNums);
        if (!mDataInRanged.isEmpty()) {
            float[] extreme = DataUtils.getExtremeNumber(mDataInRanged);
            return extreme[1];
        }
        return 0;
    }

    @Override
    public float onCalculateMin(int drawPointIndex, int showPointNums) {
        fetchAllData(drawPointIndex, showPointNums);
        if (!mDataInRanged.isEmpty()) {
            float[] extreme = DataUtils.getExtremeNumber(mDataInRanged);
            return extreme[0];
        }

        return 0;
    }

    private void fetchAllData(int drawPointIndex, int showPointNums) {
        List<ViewContainer<Object>> modelList = mChartView.getChildren();
        List<String> dataList = new ArrayList<>();

        for (ViewContainer<?> viewContainer : modelList) {
            if (viewContainer instanceof CandleLine) {
                CandleLine candleLine = (CandleLine) viewContainer;
                List<CandleLine.CandleLineBean> list = candleLine.getDataList();
                for (int i = drawPointIndex; i < drawPointIndex + showPointNums && i < list.size(); i++) {
                    CandleLine.CandleLineBean candleLineBean = list.get(i);
                    dataList.add(candleLineBean.getHeightPrice() + "");
                    dataList.add(candleLineBean.getLowPrice() + "");
                }
            }
            if (viewContainer instanceof BrokenLine) {
                BrokenLine brokenLine = (BrokenLine) viewContainer;
                List<String> list = brokenLine.getDataList();
                for (int i = drawPointIndex; i < drawPointIndex + showPointNums && i < list.size(); i++) {
                    String price = list.get(i);
                    dataList.add(price);
                }
            }
            if (viewContainer instanceof MACDHistogram) {
                MACDHistogram macdHistogram = (MACDHistogram) viewContainer;
                List<MACDHistogram.MACDBean> list = macdHistogram.getDataList();
                for (int i = drawPointIndex; i < drawPointIndex + showPointNums && i < list.size(); i++) {
                    MACDHistogram.MACDBean macdBean = list.get(i);
                    dataList.add(macdBean.getMacd() + "");
                }
            }
            if (viewContainer instanceof Histogram) {
                Histogram histogram = (Histogram) viewContainer;
                List<Histogram.HistogramBean> list = histogram.getDataList();
                for (int i = drawPointIndex; i < drawPointIndex + showPointNums && i < list.size(); i++) {
                    Histogram.HistogramBean macdBean = list.get(i);
                    dataList.add(macdBean.getTurnover() + "");
                }
            }
        }
        mDataInRanged.clear();
        mDataInRanged.addAll(dataList);
    }
}
