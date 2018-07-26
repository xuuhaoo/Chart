package com.android.didi.chart;

import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.PointF;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.android.tonystark.tonychart.chartview.adapter.BreakLineCoordinateAdapter;
import com.android.tonystark.tonychart.chartview.adapter.CandleCoordinateAdapter;
import com.android.tonystark.tonychart.chartview.viewbeans.BrokenLine;
import com.android.tonystark.tonychart.chartview.viewbeans.CandleLine;
import com.android.tonystark.tonychart.chartview.viewbeans.CrossLine;
import com.android.tonystark.tonychart.chartview.viewbeans.Histogram;
import com.android.tonystark.tonychart.chartview.viewbeans.ViewContainer;
import com.android.tonystark.tonychart.chartview.views.ChartViewImp;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.android.tonystark.tonychart.chartview.viewbeans.Histogram.HistogramBean.GREEN;
import static com.android.tonystark.tonychart.chartview.viewbeans.Histogram.HistogramBean.RED;

public class MainActivity extends AppCompatActivity implements CrossLine.OnCrossLineMoveListener {

    private JsonArray mJsonArray;
    private ChartViewImp mChartViewImp;
    private ChartViewImp mChartSubViewImp;

    private Button mAddKBtn;
    private Button mAddNowBtn;
    private Button mDeleteBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mChartViewImp = findViewById(R.id.chart_view);
        mChartSubViewImp = findViewById(R.id.chart_sub_view);

        mAddKBtn = findViewById(R.id.add_k_btn);
        mAddNowBtn = findViewById(R.id.add_now_btn);
        mDeleteBtn = findViewById(R.id.delete_btn);

        inflateMockData();
        init();
    }

    private void init() {
        mAddNowBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BrokenLine brokenLine = getBrokenLine();
                mChartViewImp.addChild(brokenLine);
                mChartViewImp.setCoordinateScaleAdapter(new BreakLineCoordinateAdapter());

            }
        });

        mAddKBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BrokenLine brokenLine = getBrokenLine();
                mChartViewImp.addChild(brokenLine);

                CandleLine candleLine = getCandleLine();
                mChartViewImp.addChild(candleLine);
                candleLine.requestFocuse();

                mChartViewImp.setCoordinateScaleAdapter(new CandleCoordinateAdapter());
                Histogram histogram = getHistogram();
                mChartSubViewImp.addChild(histogram);
            }
        });

        mDeleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mChartViewImp.removeAllChildren();
                mChartSubViewImp.removeAllChildren();
            }
        });

        //主视图的十字线
        CrossLine crossLine = mChartViewImp.getCrossLine();
        crossLine.setLineColor(0xffFE7F3F);
        crossLine.setShowLatitude(true);
        crossLine.setOnCrossLineMoveListener(this);
        crossLine.setShowPoint(false);
        crossLine.setFollowData(false);

        //副视图的十字线
        mChartSubViewImp.getCrossLine().setShowPoint(false);
        mChartSubViewImp.getCrossLine().setShowLatitude(false);
        mChartSubViewImp.getCrossLine().setShowLongitude(true);
        mChartSubViewImp.getCrossLine().setFollowData(false);

        mChartSubViewImp.followTouch(mChartViewImp);
        mChartViewImp.setCoordinateLineEffect(new DashPathEffect(new float[]{5, 5, 5, 5}, 1));
        mChartViewImp.setCoordinateLineColor(0xff989898);
        mChartViewImp.setCoordinateTextColor(0xff989898);
        mChartViewImp.setCoordinateLatitudeNum(5);
        mChartViewImp.setCoordinateLongitudeNum(4);
        mChartViewImp.invalidate();
    }

    private Histogram getHistogram() {
        Histogram histogram = new Histogram(this);
        List<Histogram.HistogramBean> list = getHistogramData();
        histogram.setDataList(list);
        histogram.setDefaultShowPointNums(list.size());
        histogram.setUpColor(0xfff5515f);
        histogram.setDownColor(0xff00b78f);
        histogram.setFill(true);
        return histogram;
    }

    private CandleLine getCandleLine() {
        CandleLine candleLine = new CandleLine(this);
        List<CandleLine.CandleLineBean> list = getCandleLineData();
        candleLine.setDataList(list);
        candleLine.setDefaultShowPointNums(list.size());
        candleLine.setUpColor(0xfff5515f);
        candleLine.setDownColor(0xff00b78f);
        candleLine.setFill(true);
        return candleLine;
    }

    private BrokenLine getBrokenLine() {
        List<String> brokenLieData = getBrokenLineData();
        BrokenLine brokenLine = new BrokenLine(this);
        brokenLine.setDataList(brokenLieData);
        brokenLine.setDefaultShowPointNums(brokenLieData.size());
        brokenLine.setFill(false);
        brokenLine.setLineColor(0xffFE7F3F);
        return brokenLine;
    }

    private void inflateMockData() {
        InputStream is = null;
        BufferedReader reader = null;
        try {
            is = getAssets().open("mock_data.json");
            reader = new BufferedReader(new InputStreamReader(is));
            mJsonArray = new JsonParser().parse(reader).getAsJsonArray();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                reader.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public List<Histogram.HistogramBean> getHistogramData() {
        List<Histogram.HistogramBean> result = new ArrayList<>();
        Iterator<JsonElement> it = mJsonArray.iterator();
        int index = 0;
        while (it.hasNext()) {
            if (index > 100) {
                break;
            }
            JsonElement element = it.next();
            JsonObject jsonObject = element.getAsJsonObject();
            Histogram.HistogramBean bean = new Histogram.HistogramBean();
            bean.setTurnover(jsonObject.get("volume").getAsFloat());
            float open = (jsonObject.get("open").getAsFloat());
            float close = (jsonObject.get("close").getAsFloat());
            bean.setIsUp(open < close ? RED : GREEN);
            result.add(bean);
            index++;
        }
        return result;
    }

    public List<CandleLine.CandleLineBean> getCandleLineData() {
        List<CandleLine.CandleLineBean> result = new ArrayList<>();
        Iterator<JsonElement> it = mJsonArray.iterator();
        int index = 0;
        while (it.hasNext()) {
            if (index > 100) {
                break;
            }
            JsonElement element = it.next();
            JsonObject jsonObject = element.getAsJsonObject();
            CandleLine.CandleLineBean bean = new CandleLine.CandleLineBean(index,
                    jsonObject.get("high").getAsFloat(),
                    jsonObject.get("low").getAsFloat(),
                    jsonObject.get("open").getAsFloat(),
                    jsonObject.get("close").getAsFloat());
            bean.setTimeMills(jsonObject.get("timesign").getAsLong() * 1000);
            result.add(bean);
            index++;
        }
        return result;
    }

    public List<String> getBrokenLineData() {
        List<String> result = new ArrayList<>();
        Iterator<JsonElement> it = mJsonArray.iterator();
        int index = 0;
        while (it.hasNext()) {
            if (index > 100) {
                break;
            }
            JsonElement element = it.next();
            JsonObject jsonObject = element.getAsJsonObject();
            String close = jsonObject.get("close").getAsString();
            result.add(close);
            index++;
        }
        return result;
    }

    @Override
    public void onCrossLineMove(int index, int drawIndex, PointF pointF) {
        mChartSubViewImp.getCrossLine().setPointF(pointF);
    }

    @Override
    public void onCrossLineDismiss() {
    }
}
