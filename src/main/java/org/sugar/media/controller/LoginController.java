package org.sugar.media.controller;


import cn.dev33.satoken.stp.SaTokenInfo;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.sugar.media.beans.ResponseBean;
import org.sugar.media.enums.UserStatusEnum;
import org.sugar.media.model.UserModel;
import org.sugar.media.security.UserSecurity;
import org.sugar.media.service.user.UserService;
import org.sugar.media.utils.SecurityUtils;

@RestController
@RequestMapping("/login")
@Validated
public class LoginController {

    @Autowired
    private UserSecurity userSecurity;

    @Resource
    private UserService userService;

    private final SecurityUtils securityUtils = new SecurityUtils();

    @PostMapping("/user")
    public ResponseBean userLogin(@RequestParam(name = "username") String username, @RequestParam(name = "password") String password) {
        // TODO:查询用户是否存在 ,默认租户
        UserModel user = this.userService.getUser(username, 1l);

        if (user == null) {
            return ResponseBean.fail("用户不存在");
        }
        // 验证密码
        if (!this.securityUtils.shaEncode(password + user.getSalt()).equals(user.getPassword())) {
            return ResponseBean.fail("账号/密码错误");
        }
        // 判断是否锁定

        if (user.getStatus().equals(UserStatusEnum.locked)) {
            return ResponseBean.fail("账号已被锁定");
        }

        SaTokenInfo login = this.userSecurity.login(user);
        return ResponseBean.success(login);
    }


}
