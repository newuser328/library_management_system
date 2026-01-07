package com.example.library_management_system.util;

import net.sourceforge.pinyin4j.PinyinHelper;

public class PinyinUtils {

    private PinyinUtils() {
    }

    /**
     * 获取中文字符串的拼音首字母（大写）。
     * - 中文：取拼音首字母
     * - 英文/数字：取其本身（字母转大写）
     * - 其他字符：忽略
     */
    public static String firstLetters(String text) {
        if (text == null || text.isBlank()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (char c : text.trim().toCharArray()) {
            if (c <= 127) {
                if (Character.isLetterOrDigit(c)) {
                    sb.append(Character.toUpperCase(c));
                }
                continue;
            }
            String[] pinyinArray = PinyinHelper.toHanyuPinyinStringArray(c);
            if (pinyinArray != null && pinyinArray.length > 0 && pinyinArray[0] != null && !pinyinArray[0].isBlank()) {
                sb.append(Character.toUpperCase(pinyinArray[0].charAt(0)));
            }
        }
        return sb.toString();
    }
}

