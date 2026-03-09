package com.amis.utils;

/**
 * @author : KwokChichung
 * @description : ThreadLocal工具类
 * @createDate : 2026/1/6
 */
@SuppressWarnings("all")
public class ThreadLocalUtil {

    private ThreadLocalUtil() {
        throw new IllegalStateException("Utility class");
    }

    private static final ThreadLocal THREAD_LOCAL = new ThreadLocal();

    public static <T> T get() {
        return (T) THREAD_LOCAL.get();
    }

    public static void set(Object value) {
        THREAD_LOCAL.set(value);
    }

    public static void remove() {
        THREAD_LOCAL.remove();
    }
}
