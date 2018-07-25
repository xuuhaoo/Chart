package com.android.didi.chart;

import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.android.tonystark.tonychart.chartview.adapter.CandleCoordinateAdapter;
import com.android.tonystark.tonychart.chartview.viewbeans.BrokenLine;
import com.android.tonystark.tonychart.chartview.viewbeans.CandleLine;
import com.android.tonystark.tonychart.chartview.viewbeans.CrossLine;
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

public class MainActivity extends AppCompatActivity {

    private JsonArray mJsonArray;
    private ChartViewImp mChartViewImp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mChartViewImp = findViewById(R.id.chart_view);

        inflateMockData();
        drawBroken();
    }

    private void drawBroken() {

//        BrokenLine brokenLine = getBrokenLine();
//        brokenLine.requestFocuse();
//        mChartViewImp.addChild(brokenLine);



        CandleLine candleLine = new CandleLine(this);
        List<CandleLine.CandleLineBean> list = getCandleLineData();
        candleLine.setDataList(list);
        candleLine.setDefaultShowPointNums(list.size());
        candleLine.setUpColor(0xfff5515f);
        candleLine.setDownColor(0xff00b78f);
        candleLine.setFill(true);
        candleLine.requestFocuse();
        mChartViewImp.addChild(candleLine);

        CrossLine crossLine = mChartViewImp.getCrossLine();
        crossLine.setLineColor(Color.parseColor("#FE7F3F"));
        crossLine.setShowLatitude(true);
        crossLine.setShowPoint(false);

        mChartViewImp.setCoordinateDataList(list);
        mChartViewImp.setCoordinateLineEffect(new DashPathEffect(new float[]{5, 5, 5, 5}, 1));
        mChartViewImp.setCoordinateScaleAdapter(new CandleCoordinateAdapter());
        mChartViewImp.setCoordinateLineColor(Color.parseColor("#989898"));
        mChartViewImp.setCoordinateTextColor(Color.parseColor("#989898"));
        mChartViewImp.setCoordinateLatitudeNum(5);
        mChartViewImp.setCoordinateLongitudeNum(4);
        mChartViewImp.invalidate();
    }

    private BrokenLine getBrokenLine() {
        List<String> brokenLieData = getBrokenLineData();
        BrokenLine brokenLine = new BrokenLine(this);
        brokenLine.setDataList(brokenLieData);
        brokenLine.setDefaultShowPointNums(brokenLieData.size() / 2);
        brokenLine.setFill(false);
        brokenLine.setLineColor(Color.parseColor("#FE7F3F"));
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
}
