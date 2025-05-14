package com.example.dzxh_app.util;

public class RegexUtil {

    /**
     * 正则表达式，只能输入1到10个数字
     */
    private static final String NUM_REGEX ="^\\d{1,10}$";
    /**
     * 正则表达式，只能输入A-F0-9
     */
    private static final String Hex_REGEX  = "^[A-Fa-f0-9]+$";
    /**
     * 判断数字是否符合规范
     * @param value
     * @return
     */
    public static boolean isNum(String value) {
        return value.matches(NUM_REGEX);
    }

    /**
     * 判断十六进制是否符合规范
     * @param value
     * @return
     */
    public static boolean isHex(String value) {
        return value.matches(Hex_REGEX);
    }
}
