package com.kyoko.utils;

import java.text.DecimalFormat;

public class MoneyToChinese {
    // 中文數字
    private static final String[] CHINESE_NUMBER = {"零", "壹", "貳", "叁", "肆", "伍", "陸", "柒", "捌", "玖"};
    // 單位
    private static final String[] UNIT = {"", "拾", "佰", "仟"};
    private static final String[] SECTION_UNIT = {"", "萬", "億", "兆"}; // 每四位一組

    public static String convertMoneyToWord(double amount) {
        if (amount < 0 || amount >= 1_0000_0000_0000.0) {
            throw new IllegalArgumentException("金額超出範圍 (0~9999兆)");
        }

        if (amount == 0) {
            return "零圓正";
        }

        // 取整數部分
        long integerPart = (long) amount;
        // 取小數部分（這裡不處理小數，只保留整數部分）
        // long decimalPart = Math.round((amount - integerPart) * 100); 

        String chineseInteger = convertIntegerPart(integerPart);

        return chineseInteger + "圓正";
    }

    private static String convertIntegerPart(long num) {
        StringBuilder result = new StringBuilder();
        int sectionIndex = 0; // 萬、億、兆的索引
        boolean needZero = false; // 是否需要補零
        
        while (num > 0) {
            int section = (int) (num % 10000); // 每四位處理一次
            if (section > 0) {
                String sectionStr = convertSection(section);
                if (needZero) {
                    result.insert(0, "零");
                }
                result.insert(0, sectionStr + SECTION_UNIT[sectionIndex]);
                needZero = true; // 設置需要補零
            } else {
                needZero = result.length() > 0; // 若當前 section 為零但 result 有內容，則補零
            }
            num /= 10000;
            sectionIndex++;
        }

        return result.toString();
    }

    private static String convertSection(int num) {
        StringBuilder sectionStr = new StringBuilder();
        boolean zeroFlag = false;
        int unitPos = 0;

        while (num > 0) {
            int digit = num % 10;
            if (digit > 0) {
                if (zeroFlag) {
                    sectionStr.insert(0, "零");
                    zeroFlag = false;
                }
                sectionStr.insert(0, CHINESE_NUMBER[digit] + UNIT[unitPos]);
            } else {
                zeroFlag = sectionStr.length() > 0;
            }
            num /= 10;
            unitPos++;
        }

        return sectionStr.toString();
    }

    public static void main(String[] args) {
        System.out.println(convertMoneyToWord(345.0));  // 叁佰肆拾伍圓正
        System.out.println(convertMoneyToWord(1001.0)); // 壹仟零壹圓正
        System.out.println(convertMoneyToWord(10000.0)); // 壹萬圓正
        System.out.println(convertMoneyToWord(1000000.0)); // 壹佰萬圓正
        System.out.println(convertMoneyToWord(999999999999.0)); // 玖仟玖佰玖拾玖億玖仟玖佰玖拾玖萬玖仟玖佰玖拾玖圓正
    }
}
