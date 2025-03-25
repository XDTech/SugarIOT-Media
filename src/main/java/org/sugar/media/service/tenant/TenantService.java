package org.sugar.media.service.tenant;

import cn.hutool.core.bean.BeanUtil;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.sugar.media.beans.ResponseBean;
import org.sugar.media.beans.UserBean;
import org.sugar.media.enums.RoleEnum;
import org.sugar.media.enums.UserStatusEnum;
import org.sugar.media.model.TenantModel;
import org.sugar.media.model.UserModel;
import org.sugar.media.repository.TenantRepo;
import org.sugar.media.service.user.UserService;
import org.sugar.media.utils.SecurityUtils;

import java.util.Optional;

/**
 * Date:2024/12/18 18:15:15
 * Author：Tobin
 * Description:
 */

@Service
public class TenantService {

    @Autowired
    private TenantRepo tenantRepo;

    @Resource
    private UserService userService;
    private final SecurityUtils securityUtils = new SecurityUtils();

    public Optional<TenantModel> getTenant(Long id) {
        return this.tenantRepo.findById(id);
    }


    public TenantModel getTenant(Integer tenantCode) {
        return this.tenantRepo.findByCode(tenantCode);
    }

    public void createRoot() {
        TenantModel tenant = this.getTenant(100000);
        if (tenant == null) {
            TenantModel tenantModel = new TenantModel();
            tenantModel.setCode(100000);

            tenant = this.tenantRepo.save(tenantModel);

        }


        UserModel user = this.userService.getUser("root", tenant.getId());
        if (user != null) {
            return;
        }
        UserModel newUser = new UserModel();

        newUser.setTenantId(tenant.getId());
        newUser.setTenantCode(tenant.getCode());
        newUser.setEmail("944192161@qq.com");
        newUser.setPassword("smile100");
        newUser.setStatus(UserStatusEnum.normal);
        newUser.setName("root");
        newUser.setUsername("root");
        newUser.setPhone("15653646089");
        // 生成加密盐
        String salt = this.securityUtils.createSecuritySalt();

        newUser.setSalt(salt);
        // 生成密码

        String pwd = this.securityUtils.shaEncode(newUser.getPassword() + salt);

        newUser.setPassword(pwd);
        newUser.setRole(RoleEnum.platform_admin);
        this.userService.createMUser(newUser);

    }

}
