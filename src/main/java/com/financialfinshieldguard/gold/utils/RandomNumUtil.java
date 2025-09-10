package com.financialfinshieldguard.gold.utils;

import java.util.Random;

public class RandomNumUtil {

    public static String getRandomNum() {
        Random random = new Random();

        //random.nextInt(900000) 的范围是 左闭右开 的，即 [0, 900000)
        int num = random.nextInt(900000) + 100000;
        //使用 String.format("%06d", num) 确保返回的字符串始终是 6 位，
        // 不足 6 位时前面补零。这是一个很好的设计，确保了返回值的格式一致性。

        //格式化占位符 %06d
        //%06d 是一个格式化占位符，用于指定整数的格式化规则。它由以下几个部分组成：
        //%
        //这是格式化占位符的开始符号，表示后面的内容是一个格式化指令。
        //0
        //这是一个填充字符，表示如果数字的位数不足指定的长度，会用 0 来填充。
        //6
        //这是一个宽度指定符，表示输出的字符串总长度为 6 位。如果数字的位数不足 6 位，会用前面的填充字符（这里是 0）来补足。
        //d
        //这是一个转换符，表示格式化的内容是一个十进制整数。
        return String.format("%06d",num);
    }
}
