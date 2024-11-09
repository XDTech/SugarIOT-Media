package org.sugar.media.controller;


import cn.dev33.satoken.stp.StpUtil;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class TestController {

    @RequestMapping("/doLogin")
    public String doLogin() {
        // 此处仅作模拟示例，真实项目需要从数据库中查询数据进行比对
        StpUtil.login(10001);
        return "success";
    }

    // 查询登录状态，浏览器访问： http://localhost:8899/test/isLogin
    @RequestMapping("isLogin")
    public String isLogin() {
        return "当前会话是否登录：" + StpUtil.isLogin();
    }
}
