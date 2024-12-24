package org.sugar.media.security;


import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.SaTokenInfo;
import cn.hutool.core.convert.Convert;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.sugar.media.model.TenantModel;
import org.sugar.media.model.UserModel;
import org.sugar.media.service.tenant.TenantService;

import java.util.Optional;

@Service
public class UserSecurity {


    @Resource
    private TenantService tenantService;


    public SaTokenInfo login(UserModel user) {
        StpKit.USER.login(user.getId());
        // 写入 token session
        SaSession session = StpKit.USER.getTokenSession();
        session.set("user", user);


        if (user.getTenantId() != null) {
            Optional<TenantModel> tenant = this.tenantService.getTenant(user.getTenantId());
            tenant.ifPresent(tenantModel -> session.set("tenant", tenantModel));
        }

        session.set("tenantCode", user);
        session.update();

        return StpKit.USER.getTokenInfo();

    }


    // 获取当前登录用户
    public UserModel getCurrentAdminUser() {
        SaSession tokenSession = StpKit.USER.getTokenSession();
        return Convert.convert(UserModel.class, tokenSession.getDataMap().get("user"));

    }

    public Long getCurrentTenantId() {
        return this.getCurrentAdminUser().getTenantId();
    }

    public TenantModel getCurrentTenant() {
        SaSession tokenSession = StpKit.USER.getTokenSession();
        return Convert.convert(TenantModel.class, tokenSession.getDataMap().get("tenant"));
    }
    public Integer getCurrentTenantCode() {
        SaSession tokenSession = StpKit.USER.getTokenSession();
        return Convert.convert(TenantModel.class, tokenSession.getDataMap().get("tenant")).getCode();
    }

    public UserModel getUser(String token) {

        SaSession tokenSession = StpKit.USER.getTokenSessionByToken(token);
        return Convert.convert(UserModel.class, tokenSession.getDataMap().get("user"));

    }

}
