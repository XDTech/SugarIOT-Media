package org.sugar.media.controller;


import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.sugar.media.model.UserModel;
import org.sugar.media.security.UserSecurity;

@RestController
@RequestMapping("/login")
@Validated
public class LoginController {

    @Autowired
    private UserSecurity userSecurity;



    @PostMapping("/user")
    public Object userLogin() {
        // 此处仅作模拟示例，真实项目需要从数据库中查询数据进行比对
        UserModel userModel = new UserModel();
        userModel.setId(1L);

        SaTokenInfo login = this.userSecurity.login(userModel);


        return login;
    }


}
