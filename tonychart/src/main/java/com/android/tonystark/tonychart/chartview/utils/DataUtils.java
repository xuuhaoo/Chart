package com.android.tonystark.tonychart.chartview.utils;

import android.util.Log;


import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.List;

/**
 * 描述：数字格式化工具类
 *
 * @author xuhao
 * @version 1.0
 * @date 2015-02-03
 */
public class DataUtils {

    /**
     * 求出数据极值
     *
     * @param data
     * @return
     */
    public static float[] getExtremeNumber(List<String> data) {
        if (data == null || data.isEmpty()) {
            return new float[]{0, 0};
        }
        float[] extreme = new float[2];
        float max = Float.parseFloat(data.get(0));
        float min = Float.parseFloat(data.get(0));

        for (String str : data) {
            float value = Float.parseFloat(str);
            if (max < value) {
                max = value;
            }
            if (min > value) {
                min = value;
            }
        }
        extreme[0] = min;
        extreme[1] = max;
        return extreme;
    }


    /**
     * 数字格式化
     *
     * @param in      需要格式化的内容
     * @param keepNum 保留位数,默认保留两位小数
     * @param isRound 是否开启四舍五入
     * @return 字符串格式结果
     */
    public static String format(double in, int keepNum, boolean isRound) {

        String result;

        DecimalFormat format = new DecimalFormat();

        if (keepNum < 0) {
            keepNum = 0;
        }

        format.setMaximumFractionDigits(keepNum);

        format.setMinimumFractionDigits(keepNum);

        format.setGroupingUsed(false);

        if (isRound) {
            format.setRoundingMode(RoundingMode.HALF_UP);
        } else {
            format.setRoundingMode(RoundingMode.FLOOR);
        }

        result = format.format(in);

        return result;
    }

    /**
     * 数字格式化
     *
     * @param in      需要格式化的内容
     * @param keepNum 保留位数,默认保留两位小数
     * @param isRound 是否开启四舍五入
     * @return 返回字符串结果
     */
    public static String format(String in, int keepNum, boolean isRound) {
        String result = "";
        double indouble;
        try {
            indouble = Double.parseDouble(in);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return "";
        }
        return format(indouble, keepNum, isRound);
    }

}