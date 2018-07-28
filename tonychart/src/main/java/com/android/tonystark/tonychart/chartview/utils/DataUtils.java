package com.android.tonystark.tonychart.chartview.utils;

import android.text.TextUtils;
import android.util.Log;


import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.List;

/**
 * 描述：数字格式化工具类
 *
 * @author xuhao
 * @version 1.0
 */
public class DataUtils {

    public static float parseString2Float(String value) {
        if (value == null || TextUtils.isEmpty(value) || "null".equalsIgnoreCase(value)) {
            return 0;
        }
        try {
            return Float.parseFloat(value);
        } catch (NumberFormatException e) {
            Log.e("parseString2Float", "parseString2Float error:" + e.getMessage());
            return 0;
        }
    }

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
        float[] extreme = new float[]{0, 0};
        float max = parseString2Float(data.get(0));
        float min = parseString2Float(data.get(0));

        for (String str : data) {
            if (TextUtils.isEmpty(str) || "null".equalsIgnoreCase(str)) {
                continue;
            }
            float value = parseString2Float(str);
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
