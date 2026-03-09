package com.amis.utils;

import java.util.Random;

/**
 * @author : KwokChichung
 * @description : 随机码工具类
 * @createDate : 2026/1/5
 */
public class RandomCodeUtil {

    private RandomCodeUtil() {
        throw new IllegalStateException("Utility class");
    }

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int LENGTH = 6;
    private static final Random RANDOM = new Random();

    /**
     * 生成随机的6个字符的字符串
     *
     * @return 随机字符串
     */
    public static String generateRandomCode() {
        StringBuilder stringBuilder = new StringBuilder(LENGTH);
        for (int i = 0; i < LENGTH; i++) {
            int index = RANDOM.nextInt(CHARACTERS.length());
            stringBuilder.append(CHARACTERS.charAt(index));
        }
        return stringBuilder.toString();
    }

}
