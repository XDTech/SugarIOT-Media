package org.sugar.media.service.user;

import cn.hutool.core.util.ObjectUtil;
import jakarta.annotation.Resource;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.sugar.media.model.UserModel;
import org.sugar.media.model.system.RoleModel;
import org.sugar.media.repository.user.UserRepo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.jpa.domain.Specification;
import org.sugar.media.service.system.RoleService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


/**
 * (UserModel)服务
 *
 * @author Tobin
 * @since 2024-11-10 10:05:25
 */
@Service
public class UserService {

    @Autowired
    private UserRepo userRepo;

    @Resource
    private RoleService roleService;



    // 查询账户是否存在


    public UserModel getUser(String username,Long tenantId) {
        return this.userRepo.findAllByUsernameAndTenantId(username,tenantId);
    }

    public UserModel getUser(String username,Integer tenantCode) {
        return this.userRepo.findAllByUsernameAndTenantCode(username,tenantCode);
    }

    @Transactional
    public UserModel createMUser(UserModel mUser) {
        return this.userRepo.save(mUser);
    }

    @Transactional
    public UserModel updateMUser(UserModel mUser) {
        return this.userRepo.save(mUser);
    }

    @Transactional
    public void deleteMUser(UserModel mUser) {
        this.userRepo.delete(mUser);
    }

    @Transactional
    public void deleteMUser(Long id) {
        this.userRepo.deleteById(id);
    }


    public Optional<UserModel> getMUser(Long id) {
        return this.userRepo.findById(id);
    }

    // 分页查询
    public Page<UserModel> getMUserPageList(Integer pi, Integer ps) {
        // Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        PageRequest pageRequest = PageRequest.of(pi - 1, ps);
        Specification<UserModel> specification = new Specification<UserModel>() {
            /**
             *
             */
            private static final long serialVersionUID = -90785455788526421L;

            @Override
            public Predicate toPredicate(Root<UserModel> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                // 用于暂时存放查询条件的集合
                List<Predicate> predicatesList = new ArrayList<>();
                // --------------------------------------------
                // 模糊查询
                /**
                 if (!StrUtil.isEmpty(username)) {
                 predicatesList.add(cb.like(root.get("username"), "%" + username + "%"));
                 }
                 if (!StrUtil.isEmpty(status)) {
                 predicatesList.add(cb.equal(root.get("status"), UserStatusEnum.valueOf(status)));
                 }
                 **/
                Predicate[] p = new Predicate[predicatesList.size()];
                query.where(predicatesList.toArray(p));
                query.orderBy(cb.desc(root.get("createdAt")));
                return query.getGroupRestriction();
            }

        };
        return this.userRepo.findAll(specification, pageRequest);

    }




//    public void createRootAccount() {
//        String accountName = "root";
//
//        RoleModel role = this.roleService.getRole(accountName);
//
//        if (ObjectUtil.isEmpty(role)) {
//            role = new RoleModel();
//
//            role.setName(accountName);
//            role.setIdentity(accountName);
//
//            this.roleService.createRole(role);
//
//        }
//        UserModel root = this.userRepo.findByUsername(accountName);
//
//        if (ObjectUtil.isEmpty(root)) {
//            UserModel userModel = new UserModel();
//
//            userModel.setName(accountName);
//            userModel.setUsername(accountName);
//            // 生成加密盐
//            String salt = this.securityUtils.createSecuritySalt();
//            userModel.setSalt(salt);
//            // 生成密码
//            String pwd = this.securityUtils.shaEncode(accountName + salt);
//
//            userModel.setPassword(pwd);
//            userModel.setRoles(new Long[]{role.getId()});
//
//            this.userRepo.save(userModel);
//
//        }
//
//    }
}
