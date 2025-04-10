package org.sugar.media.controller;

import cn.hutool.log.StaticLog;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.sugar.media.enums.ResponseEnum;
import org.sugar.media.enums.RoleEnum;
import org.sugar.media.enums.UserStatusEnum;
import org.sugar.media.model.TenantModel;
import org.sugar.media.model.UserModel;
import org.sugar.media.security.UserSecurity;
import org.sugar.media.service.tenant.TenantService;
import org.sugar.media.service.user.UserService;
import org.sugar.media.beans.UserBean;
import org.sugar.media.beans.ResponseBean;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import cn.hutool.core.bean.BeanUtil;
import org.sugar.media.utils.SecurityUtils;
import org.sugar.media.validation.UserVal;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * (UserModel)表控制层
 *
 * @author Tobin
 * @since 2024-11-10 10:05:25
 */
@RestController
@RequestMapping("/user")
@Validated  //单参数校验时我们需要，在方法的类上加上@Validated注解，否则校验不生效。
public class UserController {
    /**
     * 服务对象
     */
    @Autowired
    private UserService mUserService;

    @Resource
    private UserSecurity userSecurity;

    @Resource
    private TenantService tenantService;


    private final SecurityUtils securityUtils = new SecurityUtils();

    /**
     * 分页查询
     *
     * @return 查询结果
     */
    @GetMapping("/page/list")
    public ResponseEntity<?> getMUserPageList(@RequestParam Integer pi, @RequestParam Integer ps) {


        Page<UserModel> mUserList = this.mUserService.getMUserPageList(pi, ps);

        return ResponseEntity.ok(ResponseBean.createResponseBean(ResponseEnum.Success.getCode(), mUserList.getTotalElements(), mUserList.getContent(), ResponseEnum.Success.getMsg()));


    }

    /**
     * 通过主键查询单条数据
     *
     * @param id 主键
     * @return 单条数据
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getMUser(@PathVariable("id") Long id) {

        Optional<UserModel> mUser = this.mUserService.getMUser(id);
        if (mUser.isEmpty()) {
            return ResponseEntity.ok(ResponseBean.fail());
        }
        return ResponseEntity.ok(ResponseBean.createResponseBean(ResponseEnum.Success.getCode(), mUser.get(), ResponseEnum.Success.getMsg()));

    }

    /**
     * 新增数据
     *
     * @param userBean 实体
     * @return 新增结果
     */
    @PostMapping
    public ResponseEntity<?> createMUser(@RequestBody @Validated(UserVal.Create.class) UserVal userBean) {

        StaticLog.info("{}", userBean.toString());

        Long tenantId = this.userSecurity.getCurrentAdminUser().getTenantId();
        UserModel user = this.mUserService.getUser(userBean.getUsername(), tenantId);
        if (user != null) {
            return ResponseEntity.ok(ResponseBean.fail("用户已存在"));
        }
        UserModel newUser = new UserModel();

        newUser.setTenantId(tenantId);
        newUser.setTenantCode(this.userSecurity.getCurrentAdminUser().getTenantCode());


        BeanUtil.copyProperties(userBean, newUser);


        // 生成加密盐
        String salt = this.securityUtils.createSecuritySalt();

        newUser.setSalt(salt);
        // 生成密码

        String pwd = this.securityUtils.shaEncode(newUser.getPassword() + salt);

        newUser.setPassword(pwd);
        newUser.setRole(RoleEnum.tenant_user);
        UserModel userPojo = this.mUserService.createMUser(newUser);
        // 序列化返回
        UserBean newAminUserBean = new UserBean();
        BeanUtil.copyProperties(userPojo, newAminUserBean);

        return ResponseEntity.ok(ResponseBean.success(newAminUserBean));
    }

    /**
     * 编辑数据
     *
     * @param userBean 实体
     * @return 编辑结果
     */
    @PutMapping
    public ResponseEntity<?> updateMUser(@RequestBody @Validated(UserVal.Update.class) UserVal userBean) {

        Optional<UserModel> mUserOptional = this.mUserService.getMUser(userBean.getId());
        if (mUserOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("数据不存在");
        }

        UserModel mUser = mUserOptional.get();

        mUser.setPhone(userBean.getPhone());
        mUser.setName(userBean.getName());
        mUser.setEmail(userBean.getEmail());
        mUser.setPostName(userBean.getPostName());
        mUser.setAvatar(userBean.getAvatar());
        mUser.setStatus(UserStatusEnum.valueOf(userBean.getStatus()));



        return ResponseEntity.ok(ResponseBean.success(this.mUserService.updateMUser(mUser)));
    }

    /**
     * 删除数据
     *
     * @param id 主键
     * @return 删除是否成功
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteMUserById(@PathVariable("id") Long id) {

        Optional<UserModel> mUser = this.mUserService.getMUser(id);
        if (mUser.isEmpty()) {
            return ResponseEntity.ok(ResponseBean.fail());
        }

        this.mUserService.deleteMUser(id);
        return ResponseEntity.ok(ResponseBean.success());
    }

    /**
     * 获取当前登录用户的用户信息
     *
     * @return
     */
    @GetMapping("/info")
    public ResponseEntity<?> getUserInfo() {

        UserModel currentAdminUser = this.userSecurity.getCurrentAdminUser();


        UserBean userBean = new UserBean();

        BeanUtil.copyProperties(currentAdminUser, userBean);


        Optional<TenantModel> tenant = this.tenantService.getTenant(userBean.getTenantId());

        if (tenant.isEmpty()) return ResponseEntity.ok(ResponseBean.fail());

        userBean.setTenantCode(tenant.get().getCode());

        return ResponseEntity.ok(ResponseBean.success(userBean));
    }

    @GetMapping("/codes")
    public ResponseEntity<?> getUserCodes() {


        UserModel currentAdminUser = this.userSecurity.getCurrentAdminUser();

        // 用户角色码
        List<String> userRoles = new ArrayList<>();

        userRoles.add(currentAdminUser.getRole().toString());


        return ResponseEntity.ok(ResponseBean.success(userRoles));


    }

}

