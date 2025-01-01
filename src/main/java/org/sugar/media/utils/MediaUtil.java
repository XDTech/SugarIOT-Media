package org.sugar.media.utils;

import cn.hutool.core.util.StrUtil;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.sugar.media.security.UserSecurity;

/**
 * Date:2025/01/01 09:58:29
 * Author：Tobin
 * Description:
 */
@Component
public class MediaUtil {


    @Resource
    private UserSecurity userSecurity;

    public String genStreamId(String streamId) {
        return StrUtil.format("{}_{}", this.userSecurity.getCurrentTenantCode(), streamId);
    }
}
