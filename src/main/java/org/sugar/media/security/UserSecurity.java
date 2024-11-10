package org.sugar.media.security;


import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpUtil;
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


}
