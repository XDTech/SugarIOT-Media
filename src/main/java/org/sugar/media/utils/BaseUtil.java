package org.sugar.media.utils;

import cn.hutool.core.util.StrUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * Date:2024/11/27 17:15:51
 * Author：Tobin
 * Description:
 */
public class BaseUtil {

    public static String convertBool(boolean bool) {

        return bool ? "1" : "0";
    }

    public static Map<String, String> paramConvertToMap(String parameterString) {

        Map<String, String> parameterMap = new HashMap<>();

        if (StrUtil.isBlank(parameterString)) return parameterMap;
        String[] keyValuePairs = parameterString.split("&");
        for (String keyValuePair : keyValuePairs) {
            String[] parts = keyValuePair.split("=");
            if (parts.length == 2) {
                parameterMap.put(parts[0], parts[1]);
            }
        }
        return parameterMap;
    }

}

