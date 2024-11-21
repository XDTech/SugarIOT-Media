package org.sugar.media.security;


import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.convert.Convert;
import org.springframework.stereotype.Service;
import org.sugar.media.model.UserModel;

@Service
public class UserSecurity {


    public SaTokenInfo login(UserModel user) {
        StpKit.USER.login(user.getId());
        // 写入 token session
        SaSession session = StpKit.USER.getTokenSession();
        session.set("user", user);
        session.update();

        return StpKit.USER.getTokenInfo();

    }


    // 获取当前登录用户
    public UserModel getCurrentAdminUser() {
        SaSession tokenSession = StpKit.USER.getTokenSession();
        return Convert.convert(UserModel.class, tokenSession.getDataMap().get("user"));

    }

    public UserModel getUser(String token) {

        SaSession tokenSession = StpKit.USER.getTokenSessionByToken(token);
        return Convert.convert(UserModel.class, tokenSession.getDataMap().get("user"));

    }

}
