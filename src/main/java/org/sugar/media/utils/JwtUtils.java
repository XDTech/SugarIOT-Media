package org.sugar.media.utils;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTUtil;

import java.util.Date;
import java.util.Map;

/**
 * Date:2024/12/07 21:56:19
 * Author：Tobin
 * Description:
 */
public class JwtUtils {


    final static String key = "dsamdacsicio";

    public static String createToken(Map<String, Object> payload) {

        if (MapUtil.isEmpty(payload)) return "";

        DateTime dateTime = DateUtil.offsetDay(new Date(), 1);
        String token = "";
        JWT jwt = JWT.create().setKey(key.getBytes()).setExpiresAt(dateTime);


        payload.forEach((key, value) -> {
            System.out.println("Key: " + key + ", Value: " + value);

            jwt.setPayload(key, value);
        });


        return token;
    }

    public static String createToken() {

        DateTime dateTime = DateUtil.offsetDay(new Date(), 1);

        return JWT.create().setKey(key.getBytes()).setExpiresAt(dateTime).sign();


    }

}
