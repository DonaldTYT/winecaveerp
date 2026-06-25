package com.kikyosoft.utils;

import org.apache.commons.lang3.StringUtils;

public class NumberUtil {

    public static double max(double a, double b) {
        return Math.max(a, b);
    }

    public static double min(double a, double b) {
        return Math.min(a, b);
    }

    public static int max(int a, int b) {
        return Math.max(a, b);
    }

    public static int min(int a, int b) {
        return Math.min(a, b);
    }

    public static double maxDouble(double a, double b) {
        return Math.max(a, b);
    }

    public static double minDouble(double a, double b) {
        return Math.min(a, b);
    }

    public static int maxInt(int a, int b) {
        return Math.max(a, b);
    }

    public static int minInt(int a, int b) {
        return Math.min(a, b);
    }

    public static double parseDouble(String s) {
        if (StringUtils.isBlank(s) || "null".equalsIgnoreCase(s.trim())) {
            return 0.0;
        }
        try {
            return Double.parseDouble(s.trim());
        } catch (NumberFormatException ex) {
            LogUtil.log(ex);
            return 0.0;
        }
    }

    public static long parseLong(String s) {
        if (StringUtils.isBlank(s) || "null".equalsIgnoreCase(s.trim())) {
            return 0;
        }
        try {
            return Long.parseLong(s.trim());
        } catch (NumberFormatException ex) {
            LogUtil.log("parseLong failed for: [" + s + "]");
            LogUtil.log(ex);
            return 0;
        }
    }

    public static int parseInt(String s) {
        if (StringUtils.isBlank(s) || "null".equalsIgnoreCase(s.trim())) {
            return 0;
        }
        try {
            return Integer.parseInt(s.trim());
        } catch (NumberFormatException ex) {
            LogUtil.log("parseInt failed for: [" + s + "]");
            LogUtil.log(ex);
            return 0;
        }
    }

    public static int atoi(String s) {
        return atoi(s, 0);
    }

    public static int atoi(String s, int defaultValue) {
        try {
            if (s == null) {
                LogUtil.log1("error: input is null");
                return defaultValue;
            }
            return Integer.parseInt(s.replaceAll("[^0-9]", ""));
        } catch (Exception ex) {
            LogUtil.log1("error: " + ex.getMessage());
            return defaultValue;
        }
    }

    public static String removeComma(String s, char commaChar) {
        if (s == null) return "";
        return s.replace(String.valueOf(commaChar), "");
    }

    public static String removeDecimalZero(String value) {
        try {
            String cleaned = removeComma(value, ',');
            double doubleValue = Double.parseDouble(cleaned);
            int intValue = (int) doubleValue;

            if (doubleValue != intValue) {
                return StringUtil.ftostr(doubleValue, "#.#########");
            } else {
                return String.valueOf(intValue);
            }
        } catch (Exception ex) {
            LogUtil.log("removeDecimalZero failed for value: [" + value + "]");
            LogUtil.log(ex);
            return value;
        }
    }

    public static void main(String[] args) {
        LogUtil.log1("atoi(\"123abc888\") = " + atoi("123abc888"));
        LogUtil.log1("atoi(\"abc\") = " + atoi("abc"));
    }
}
