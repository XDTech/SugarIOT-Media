package org.sugar.media.utils;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTUtil;
import cn.hutool.jwt.JWTValidator;
import jakarta.servlet.http.PushBuilder;

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

        token = jwt.sign();

        return token;
    }

    public static String createToken() {

        DateTime dateTime = DateUtil.offsetDay(new Date(), 1);

        return JWT.create().setKey(key.getBytes()).setExpiresAt(dateTime).sign();


    }

    public static boolean verifyToken(String token) {

        try {
            // 首先验证签名

            boolean verify = JWTUtil.verify(token, key.getBytes());


            if (!verify) return false;
            // 验证是否过期
            JWTValidator.of(token).validateDate(new Date());
            return true;

        } catch (Exception e) {
            e.printStackTrace();

            return false;
        }

    }

}
