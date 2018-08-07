package com.android.didi.chart;

import android.graphics.DashPathEffect;
import android.graphics.PointF;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.tonystark.tonychart.chartview.adapter.BrokenLineCoordinateAdapter;
import com.android.tonystark.tonychart.chartview.adapter.CandleCoordinateAdapter;
import com.android.tonystark.tonychart.chartview.viewbeans.AbsZoomMoveViewContainer;
import com.android.tonystark.tonychart.chartview.viewbeans.BrokenLine;
import com.android.tonystark.tonychart.chartview.viewbeans.CandleLine;
import com.android.tonystark.tonychart.chartview.viewbeans.CrossLine;
import com.android.tonystark.tonychart.chartview.viewbeans.Histogram;
import com.android.tonystark.tonychart.chartview.viewbeans.IndicatorLine;
import com.android.tonystark.tonychart.chartview.viewbeans.MACDHistogram;
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
import java.util.Random;

import static com.android.tonystark.tonychart.chartview.viewbeans.Histogram.HistogramBean.GREEN;
import static com.android.tonystark.tonychart.chartview.viewbeans.Histogram.HistogramBean.RED;

public class MainActivity extends AppCompatActivity implements CrossLine.OnCrossLineMoveListener {
    public static final int MAX_DATA = 500;
    private JsonArray mKDataJsonArray;
    private JsonArray mMACDDataJsonArray;
    private ChartViewImp mChartViewImp;
    private ChartViewImp mChartSubViewImp;

    private Button mAddKBtn;
    private Button mAddNowBtn;
    private Button mDeleteBtn;
    private Button mChangedBtn;
    private Button mStopChangedBtn;

    private List<CandleLine.CandleLineBean> mKDataList;
    private List<String> mPriceDataList;
    private LoopThread mThread;
    private CandleLine mCandleLine;
    private BrokenLine mPriceLine;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mChartViewImp = findViewById(R.id.chart_view);
        mChartSubViewImp = findViewById(R.id.chart_sub_view);

        mAddKBtn = findViewById(R.id.add_k_btn);
        mAddNowBtn = findViewById(R.id.add_now_btn);
        mDeleteBtn = findViewById(R.id.delete_btn);
        mChangedBtn = findViewById(R.id.start_changed);
        mStopChangedBtn = findViewById(R.id.stop_changed);

        inflateMockData();
        init();
    }

    private void init() {
        mAddNowBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //删除主图所有组件
                mChartViewImp.removeAllChildren();
                //删除副图的所有组件
                mChartSubViewImp.removeAllChildren();

                //得到刚才创建好的组件
                mPriceLine = getBrokenLine();
                //添加组件到主图中,因为当前主图中组件数量为空,所以第一个添加的组件默认为focused(聚焦)组件,不用显示的调用requestFocuse()函数.
                //当然显示的调用也是没有问题的.
                //显示调用如下:brokenLine.requestFocus();
                mChartViewImp.addChild(mPriceLine);
                //设置主图的坐标系刻度适配器(因为当前聚焦的是折线图,所以坐标系需要展示折线的刻度适配器)
                mChartViewImp.setCoordinateScaleAdapter(new BrokenLineCoordinateAdapter(mPriceLine));
                //得到创建好的组件
                MACDHistogram macdHistogram = getMACD();
                //添加组件到副图中
                //因为当前副图中组件数量为空,所以第一个添加的组件默认为focused(聚焦)组件,不用显示的调用requestFocuse()函数.
                //当然显示的调用也是没有问题的.
                //显示调用如下:macdHistogram.requestFocus();
                mChartSubViewImp.addChild(macdHistogram);

            }
        });

        mAddKBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                //删除主图所有组件
                mChartViewImp.removeAllChildren();
                //删除副图的所有组件
                mChartSubViewImp.removeAllChildren();


                //得到刚才创建好的组件
                mPriceLine = getBrokenLine();
                //添加组件到主图中
                mChartViewImp.addChild(mPriceLine);

                //得到创建好的组件
                mCandleLine = getCandleLine();
                //添加组件到主图中
                //因为当前主视图中有折线组建了,
                //但我们希望,主图中坐标系和其他值都以K线为准,所以我们
                //设置K线图为当前聚焦组件,设置聚焦组件后,坐标系的最大最小值都会以当前聚焦的K线组件的最大最小值为准,在此坐标系中的其他组件也会以他为准
                //如果多个组件在一个视图中同时都调用了requestFocuse,将以最后一个调用者为当前focused的组件
                mCandleLine.requestFocused();
                mCandleLine.setExtremeCalculatorInterface(new MyExtremeCalculator(mChartViewImp));
                mChartViewImp.addChild(mCandleLine);
                mCandleLine.setOnMoveListener(new AbsZoomMoveViewContainer.OnMoveListener() {
                    @Override
                    public void onMove(ViewContainer viewContainer, int drawPointIndex, int currentShownNums, float yMax, float yMin) {
                        Log.i("onMove", "drawPointIndex:" + drawPointIndex + " currentShownNums:" + currentShownNums + " yMax:" + yMax + " yMin:" + yMin);

                    }
                });

                //设置主图的坐标系刻度适配器(因为当前聚焦的是K线图,所以坐标系需要展示K线的刻度适配器)
                mChartViewImp.setCoordinateScaleAdapter(new CandleCoordinateAdapter(mCandleLine));


                IndicatorLine indicatorLine = getIndicatorLine();
                mChartViewImp.addChild(indicatorLine);

                //得到创建好的组件
                Histogram histogram = getHistogram();
                //添加组件到副图中
                mChartSubViewImp.addChild(histogram);
            }
        });

        mDeleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //删除主图所有组件
                mChartViewImp.removeAllChildren();
                //删除副图的所有组件
                mChartSubViewImp.removeAllChildren();
            }
        });

        mStopChangedBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mThread != null) {
                    mThread.shutdown();
                }
            }
        });

        mChangedBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mThread != null) {
                    mThread.shutdown();
                } else {
                    mThread = new LoopThread(getApplicationContext()) {
                        private float getRandom(float max, float min) {
                            Random random = new Random();
                            float generatedFloat = min + random.nextFloat() * (max - min);
                            return generatedFloat;
                        }

                        @Override
                        protected void runInLoopThread() throws Exception {
                            if (mKDataList != null && !mKDataList.isEmpty()) {
                                CandleLine.CandleLineBean bean = mKDataList.get(mKDataList.size() - 1);
                                float random = getRandom(1222.001f, 1224.999f);
                                Log.i("random close", "" + random);
                                bean.setHeightPrice(random);
                            }
//                            if (mPriceDataList != null && !mPriceDataList.isEmpty()) {
//                                mPriceDataList.remove(mPriceDataList.size() - 1);
//                                mPriceDataList.add(getRandom(1222.001f, 1224.999f) + "");
//                                mPriceLine.setDataList(mPriceDataList);
//                            }
                            mChartViewImp.notifyNeedForceSyncDataWithFocused();
                            mChartViewImp.postInvalidate();
                            Thread.sleep(1000);
                        }

                        @Override
                        protected void loopFinish(Exception e) {

                        }
                    };
                }
                mThread.start();
            }
        });

        initMainCrossLine();
        initSubCrossLine();

        initMainView();
        initSubView();
    }

    @NonNull
    private IndicatorLine getIndicatorLine() {
        //指示器组件
        IndicatorLine indicatorLine = new IndicatorLine(getApplicationContext(), new IndicatorLine.IndicatorLineDataParser<CandleLine.CandleLineBean>() {
            @Override
            public float indicateData(List<CandleLine.CandleLineBean> dataList, int drawPointIndex, int showPointNums, float yMax, float yMin) {
                CandleLine.CandleLineBean candleLine = dataList.get(drawPointIndex);
                return candleLine.getClosePrice();
            }
        });
        indicatorLine.setLatitudeLineEffect(new DashPathEffect(new float[]{5, 5, 5, 5}, 1));
        indicatorLine.setDataList(mKDataList);
        indicatorLine.setDefaultShowPointNums(50);
        indicatorLine.setDrawPointIndex(mKDataList.size() - indicatorLine.getDefaultShowPointNums());
        return indicatorLine;
    }

    private void initMainCrossLine() {
        //得到一个主图十字线的引用
        CrossLine crossLine = mChartViewImp.getCrossLine();
        //设置主图十字线颜色
        crossLine.setLineColor(0xffFE7F3F);
        //设置主图十字线滑动监听器,this表示当前类实现了该接口
        crossLine.setOnCrossLineMoveListener(this);
        //设置纬线不跟数据
        crossLine.setLatitudeFollowData(false);
    }

    private void initSubCrossLine() {
        //得到一个主图十字线的引用
        CrossLine crossLine = mChartSubViewImp.getCrossLine();
        //设置主图十字线颜色
        crossLine.setLineColor(0xffFE7F3F);
        //设置不显示纬线
        crossLine.setShowLatitude(false);
    }

    private void initMainView() {
        //设置主图的左边距
        mChartViewImp.setMarginLeft((int) mChartViewImp.getCoordinateLeftTextWidth(8));
        //设置坐标系线的样式
        mChartViewImp.setCoordinateLineEffect(new DashPathEffect(new float[]{5, 5, 5, 5}, 1));
        //设置坐标系线的颜色
        mChartViewImp.setCoordinateLineColor(0xff989898);
        //设置坐标系刻度值文字颜色
        mChartViewImp.setCoordinateTextColor(0xff989898);
        //设置坐标系纬线(横着的)个数,包含顶边框和底边框
        mChartViewImp.setCoordinateLatitudeNum(5);
        //设置坐标系经线(竖着的)个数,包含左边框和右边框
        mChartViewImp.setCoordinateLongitudeNum(4);
        //设置坐标系背景
        mChartViewImp.setCoordinateBackground(0xfff2efef);
        //设置点击事件
        mChartViewImp.setOnChartViewClickListener(new ChartViewImp.OnChartViewClickListener() {
            @Override
            public void onClick(View view, ViewContainer focused) {
                Toast.makeText(view.getContext(), "click", Toast.LENGTH_LONG).show();
            }
        });
        //让图表视图更新
        mChartViewImp.invalidate();
    }

    private void initSubView() {
        //副图跟随主图滑动
        mChartSubViewImp.followTouch(mChartViewImp);
        //设置副图的左边间距
        mChartSubViewImp.setMarginLeft((int) mChartSubViewImp.getCoordinateLeftTextWidth(8));
        //设置坐标系线的样式
        mChartSubViewImp.setCoordinateLineEffect(new DashPathEffect(new float[]{5, 5, 5, 5}, 1));
        //设置坐标系线的颜色
        mChartSubViewImp.setCoordinateLineColor(0xff989898);
        //设置坐标系刻度值文字颜色
        mChartSubViewImp.setCoordinateTextColor(0xff989898);
        //设置坐标系纬线(横着的)个数,包含顶边框和底边框
        mChartSubViewImp.setCoordinateLatitudeNum(5);
        //设置坐标系经线(竖着的)个数,包含左边框和右边框
        mChartSubViewImp.setCoordinateLongitudeNum(4);
        //设置点击事件
        mChartSubViewImp.setOnChartViewClickListener(new ChartViewImp.OnChartViewClickListener() {
            @Override
            public void onClick(View view, ViewContainer focused) {
                Toast.makeText(view.getContext(), "click", Toast.LENGTH_LONG).show();
            }
        });
        //让图表视图更新
        mChartSubViewImp.invalidate();
    }

    /**
     * 获取MACD柱状图组件
     *
     * @return
     */
    private MACDHistogram getMACD() {
        //获取假数据(这一步你可以省略)
        MACDHistogram macdHistogram = new MACDHistogram(this);
        //构造一个MACD柱状图组件
        List<MACDHistogram.MACDBean> list = getMACDLineData();
        //设置该组件的数据
        macdHistogram.setDataList(list);
        //设置该组件默认显示的数据量
        macdHistogram.setDefaultShowPointNums(50);
        //设置该组件默认起始绘制的下标数
        macdHistogram.setDrawPointIndex(list.size() - macdHistogram.getDefaultShowPointNums());
        //设置涨的颜色
        macdHistogram.setUpColor(0xfff5515f);
        //设置跌的颜色
        macdHistogram.setDownColor(0xff00b78f);
        //设置MACD柱状图组件中柱子是边框型的还是实心的,也就是说是否填充MACD柱子的颜色
        macdHistogram.setFill(true);
        return macdHistogram;
    }

    /**
     * 获取柱状图组件
     *
     * @return
     */
    private Histogram getHistogram() {
        //获取假数据(这一步你可以省略)
        List<Histogram.HistogramBean> list = getHistogramData();
        //构造一个柱状图组件
        Histogram histogram = new Histogram(this);
        //设置该组件的数据
        histogram.setDataList(list);
        //设置该组件默认显示的数据量
        histogram.setDefaultShowPointNums(50);
        //设置该组件默认起始绘制的下标数
        histogram.setDrawPointIndex(list.size() - histogram.getDefaultShowPointNums());
        //设置涨的颜色
        histogram.setUpColor(0xfff5515f);
        //设置跌的颜色
        histogram.setDownColor(0xff00b78f);
        //设置柱状图组件中柱子是边框型的还是实心的,也就是说是否填充柱子的颜色
        histogram.setFill(true);
        return histogram;
    }

    /**
     * 获取K线组件
     *
     * @return
     */
    private CandleLine getCandleLine() {
        //获取假数据(这一步你可以省略)
        mKDataList = getCandleLineData();
        //构造一个K线组件
        CandleLine candleLine = new CandleLine(this);
        //设置该组件的数据
        candleLine.setDataList(mKDataList);
        //设置该组件默认显示的数据量
        candleLine.setDefaultShowPointNums(50);
        //设置该组件默认起始绘制的下标数
        candleLine.setDrawPointIndex(mKDataList.size() - candleLine.getDefaultShowPointNums());
        //设置涨的颜色
        candleLine.setUpColor(0xfff5515f);
        //设置跌的颜色
        candleLine.setDownColor(0xff00b78f);
        //设置K线组件中蜡烛是边框型蜡烛还是实心的蜡烛,也就是说是否填充蜡烛的颜色
        candleLine.setFill(true);
        //设置K线组件是否显示屏幕中的最高价
        candleLine.setShowMaxPrice(true);
        //设置K线组件是否显示屏幕中的最低价
        candleLine.setShowMinPrice(true);

        return candleLine;
    }

    /**
     * 获取折线组件
     *
     * @return
     */
    private BrokenLine getBrokenLine() {
        //获取假数据(这一步你可以省略)
        mPriceDataList = getBrokenLineData();
        //构造一个折线组件
        BrokenLine brokenLine = new BrokenLine(this);
        //设置该折线组件的数据
        brokenLine.setDataList(mPriceDataList);
        //设置该折线组件默认显示的数据量
        brokenLine.setDefaultShowPointNums(50);
        //设置该折线组件默认起始绘制的下标数
        brokenLine.setDrawPointIndex(mPriceDataList.size() - brokenLine.getDefaultShowPointNums());
        //是否为折线组件填充背景色
        brokenLine.setFill(false);
        //设置折线的线的颜色
        brokenLine.setLineColor(0xffFE7F3F);
        return brokenLine;
    }


    //======================================================以下内容不需要关心,为构造假数据使用======================================================

    private void inflateMockData() {
        InputStream is = null;
        BufferedReader reader = null;

        InputStream is1 = null;
        BufferedReader reader1 = null;
        try {
            is = getAssets().open("mock_data.json");
            reader = new BufferedReader(new InputStreamReader(is));
            mKDataJsonArray = new JsonParser().parse(reader).getAsJsonArray();

            is1 = getAssets().open("macd.json");
            reader1 = new BufferedReader(new InputStreamReader(is1));
            mMACDDataJsonArray = new JsonParser().parse(reader1).getAsJsonArray();
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

            try {
                is1.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                reader1.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public List<Histogram.HistogramBean> getHistogramData() {
        List<Histogram.HistogramBean> result = new ArrayList<>();
        Iterator<JsonElement> it = mKDataJsonArray.iterator();
        int index = 0;
        while (it.hasNext()) {
            if (index >= MAX_DATA) {
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
        Iterator<JsonElement> it = mKDataJsonArray.iterator();
        int index = 0;
        while (it.hasNext()) {
            if (index >= MAX_DATA) {
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

    public List<MACDHistogram.MACDBean> getMACDLineData() {
        List<MACDHistogram.MACDBean> result = new ArrayList<>();
        Iterator<JsonElement> it = mMACDDataJsonArray.iterator();
        int index = 0;
        while (it.hasNext()) {
            if (index >= MAX_DATA) {
                break;
            }
            JsonElement element = it.next();
            MACDHistogram.MACDBean bean = new MACDHistogram.MACDBean(element.getAsFloat());
            result.add(bean);
            index++;
        }
        return result;
    }

    public List<String> getBrokenLineData() {
        List<String> result = new ArrayList<>();
        Iterator<JsonElement> it = mKDataJsonArray.iterator();
        int index = 0;
        while (it.hasNext()) {
            if (index >= MAX_DATA) {
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
    public void onCrossLineMove(int crossIndexInScreen, int drawPointIndex, PointF crossPointF, PointF fingerPointF) {

    }

    @Override
    public void onCrossLineDismiss() {

    }

    @Override
    public String onCrossIndicateYScale(int crossIndexInScreen, int drawPointIndex, int showPointNums, float yMin, float yMax) {
        return 300 + "";
    }

    @Override
    public String onCrossIndicateXScale(int crossIndexInScreen, int drawPointIndex, int showPointNums) {
        return 200 + "";
    }


}
