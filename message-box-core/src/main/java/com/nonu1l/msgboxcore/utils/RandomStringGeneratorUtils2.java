package com.nonu1l.msgboxcore.utils;

import java.util.concurrent.ThreadLocalRandom;

/**
 * 随机字符串生成工具类
 */
public class RandomStringGeneratorUtils2 {

    // 定义可用字符集：数字 +小写字母
    private static final String CHARACTERS = "abcdefghijklmnopqrstuvwxyz0123456789";

    /**
     * 生成一个指定长度的随机字符串。
     *
     * @param length 字符串长度
     * @return 随机字符串
     */
    public static String generateRandomString(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int index = ThreadLocalRandom.current().nextInt(CHARACTERS.length());
            sb.append(CHARACTERS.charAt(index));
        }
        return sb.toString();
    }
}
