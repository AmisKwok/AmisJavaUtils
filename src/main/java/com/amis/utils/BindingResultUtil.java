package com.amis.utils;

import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;

/**
 * @author : KwokChichung
 * @description : BindingResult 工具类
 * @createDate : 2026/1/3 17:39
 */
public class BindingResultUtil {

    private BindingResultUtil() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * 处理校验失败的BindingResult，返回错误信息
     *
     * @param bindingResult 校验结果
     * @return 错误信息字符串
     */
    public static String handleBindingResultErrors(BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            StringBuilder errorMessage = new StringBuilder("输入参数校验失败: ");

            for (ObjectError error : bindingResult.getAllErrors()) {
                errorMessage.append(error.getDefaultMessage()).append("; ");
            }

            return errorMessage.toString();
        }
        return null;
    }
}
