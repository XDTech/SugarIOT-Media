package org.sugar.media.utils;

import cn.hutool.core.bean.BeanUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Date:2024/11/18 10:19:20
 * Author：Tobin
 * Description:
 */
public class BeanConverterUtil {


    /**
     * 将源对象的属性复制到目标对象，并忽略指定的字段
     *
     * @param source 源对象
     * @param target 目标对象
     * @param ignoreProperties 要忽略的字段
     */
    private static void copyProperties(Object source, Object target, String... ignoreProperties) {
        BeanUtil.copyProperties(source, target, ignoreProperties);
    }

    /**
     * 将源列表转换为目标类型列表，并忽略指定字段
     *
     * @param sourceList 源列表
     * @param targetClass 目标类型的 Class
     * @param ignoreProperties 要忽略的字段
     * @param <S> 源类型
     * @param <T> 目标类型
     * @return 目标类型的列表
     */
    public static <S, T> List<T> convertList(List<S> sourceList, Class<T> targetClass, String... ignoreProperties) {
        List<T> targetList = new ArrayList<>();
        if (sourceList != null && !sourceList.isEmpty()) {
            for (S source : sourceList) {
                try {
                    T target = targetClass.getDeclaredConstructor().newInstance();
                    copyProperties(source, target, ignoreProperties);
                    targetList.add(target);
                } catch (Exception e) {
                    throw new RuntimeException("Error converting bean", e);
                }
            }
        }
        return targetList;
    }
}
