package org.sugar.media.utils;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.HexUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
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

    // 123 to A1A2
    public static String ssrc2hex(String hexStr) {

        // 将十进制字符串转换为整数
        long decimal = Long.parseLong(hexStr);

        // 将整数转换为十六进制字符串
        String ssrc= Long.toHexString(decimal).toUpperCase();

        if (ssrc.length() != 8) {
            ssrc = "0" + ssrc;
        }
        return ssrc;
    }

    // A1A2 to 123
    public static String hex2ssrc(String hexString) {
        // 转换为10进制字符串
      String ssrc=   Convert.toStr(Long.parseLong(hexString, 16));

        if (ssrc.length() != 10) {
            ssrc = "0" + ssrc;
        }
        return ssrc;

    }
}

