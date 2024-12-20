package org.sugar.media;

import cn.hutool.core.lang.Console;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.jwt.JWT;
import cn.hutool.jwt.signers.JWTSigner;
import cn.hutool.jwt.signers.JWTSignerUtil;
import cn.hutool.log.StaticLog;
import org.junit.jupiter.api.Test;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URL;
import java.util.Map;

/**
 * Date:2024/11/26 15:49:49
 * Author：Tobin
 * Description:
 */

public class NormalTests {


    @Test
    void normalTest(){

        String format = StrUtil.format("{}0000{}", 1, 1);
        Console.log(format);

//        final JWTSigner signer = JWTSignerUtil.hs256("13".getBytes());
//        JWT jwt = JWT.create().setSigner(signer);


    }
}
