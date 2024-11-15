package org.sugar.media.utils;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.crypto.SecureUtil;

public class SecurityUtils {

    // sha1加密
    public String shaEncode(String inStr) {

        return SecureUtil.sha1(inStr);

    }

    public String createSecuritySalt() {

        return RandomUtil.randomString(8);
    }

}