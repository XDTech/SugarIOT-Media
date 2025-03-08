package org.sugar.media.service.system;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import jakarta.annotation.Resource;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.sugar.media.model.system.PermissionModel;
import org.sugar.media.model.system.RoleModel;
import org.sugar.media.model.system.RolePermissionModel;
import org.sugar.media.repository.system.PermissionRepo;
import org.sugar.media.repository.system.RolePermissionRepo;
import org.sugar.media.repository.system.RoleRepo;
import org.sugar.media.utils.BaseUtil;
import org.sugar.media.validation.system.RoleVal;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * (Role)服务
 *
 * @author Tobin
 * @since 2025-01-19 11:30:15
 */
@Service
public class RoleService {

    private final String ROLE_PERMISSION_KEY = "role_permission:";


    @Resource
    private RedisTemplate<String, List<String>> redisTemplate;

    @Resource
    private PermissionRepo permissionRepo;

    @Resource
    private RoleRepo roleRepo;

    @Resource
    private RolePermissionRepo rolePermissionRepo;


    public List<RolePermissionModel> getRolePermissions(Long roleId) {
        return this.rolePermissionRepo.findByRoleId(roleId);
    }


    @Transactional
    public RoleModel createRole(RoleModel roleModel) {
        return this.roleRepo.save(roleModel);
    }


    @Transactional
    public void createRole(RoleVal roleVal) {
        RoleModel roleModel = new RoleModel();
        BeanUtil.copyProperties(roleVal, roleModel);
        this.createRole(roleModel);
        // 写入对照表

        List<RolePermissionModel> permissionModels = new ArrayList<>();
        for (Long permissionId : roleVal.getPermissionIds()) {
            RolePermissionModel rolePermissionModel = new RolePermissionModel();
            rolePermissionModel.setRoleId(roleModel.getId());
            rolePermissionModel.setPermissionId(permissionId);
            permissionModels.add(rolePermissionModel);
        }

        this.rolePermissionRepo.saveAll(permissionModels);

        this.genRoleAuth(roleModel);
    }


    @Transactional
    public RoleModel updateRole(RoleModel roleModel, Long[] permissions) {

        // 先把对照表的permission删除
        this.rolePermissionRepo.deleteByRoleId(roleModel.getId());
        this.roleRepo.save(roleModel);


        List<RolePermissionModel> permissionModels = new ArrayList<>();
        for (Long permissionId : permissions) {
            RolePermissionModel rolePermissionModel = new RolePermissionModel();
            rolePermissionModel.setRoleId(roleModel.getId());
            rolePermissionModel.setPermissionId(permissionId);
            permissionModels.add(rolePermissionModel);
        }

        this.rolePermissionRepo.saveAll(permissionModels);

        this.genRoleAuth(roleModel);
        return roleModel;
    }

    public RoleModel getRole(String identity) {
        return this.roleRepo.findByIdentity(identity);
    }

    @Transactional
    public void deleteRole(RoleModel roleModel) {

        roleModel.setIdentity(BaseUtil.genDeleteName(roleModel.getIdentity()));
        roleModel.setDeleted(true);
        this.roleRepo.save(roleModel);

        // 删除对照表
        this.rolePermissionRepo.deleteByRoleId(roleModel.getId());

        // 删除缓存
        this.removeRole(roleModel);
    }

//    @Transactional
//    public void deleteRole(Long id) {
//        this.roleRepo.deleteById(id);
//    }


    public Optional<RoleModel> getRole(Long id) {
        return this.roleRepo.findById(id);
    }

    // 分页查询
    public Page<RoleModel> getRolePageList(Integer pi, Integer ps, String name) {
        // Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        PageRequest pageRequest = PageRequest.of(pi - 1, ps);
        Specification<RoleModel> specification = this.genSpecification(name);
        return this.roleRepo.findAll(specification, pageRequest);

    }

    public List<RoleModel> getRoleList(String name) {

        Specification<RoleModel> specification = this.genSpecification(name);
        return this.roleRepo.findAll(specification);

    }

    public List<RoleModel> getRoleListIn(Long[] roleIds) {

        return this.roleRepo.findByIdIn(roleIds);

    }


    public List<RoleModel> getRoleList() {

        // 查询没有删除的角色
        return this.roleRepo.findByDeleted(false);
    }


    private Specification<RoleModel> genSpecification(String name) {
        return (Root<RoleModel> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {
            /**
             *
             */

            // 用于暂时存放查询条件的集合
            List<Predicate> predicatesList = new ArrayList<>();

            if (!StrUtil.isEmpty(name)) {
                predicatesList.add(cb.like(root.get("name"), "%" + name + "%"));
            }
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
            predicatesList.add(cb.equal(root.get("deleted"), false));// 查询没有删除的
            predicatesList.add(cb.notEqual(root.get("identity"), "root"));
            Predicate[] p = new Predicate[predicatesList.size()];
            query.where(predicatesList.toArray(p));
            query.orderBy(cb.desc(root.get("createdAt")));
            query.orderBy(cb.asc(root.get("sort")));
            return query.getGroupRestriction();

        };
    }


    // 把所有的角色权限放到缓存里
    public void genAuthorities() {


        List<RoleModel> roleList = this.getRoleList();

        for (RoleModel roleModel : roleList) {


            this.genRoleAuth(roleModel);

        }


    }

    // 把角色权限放到缓存里
    public void genRoleAuth(RoleModel roleModel) {
        if (roleModel.getIdentity().equals("root")) return;
        // 查询角色权限
        List<RolePermissionModel> rolePermissions = this.getRolePermissions(roleModel.getId());

        // 放到缓存里

        if (ObjectUtil.isEmpty(rolePermissions)) return;


        // 查询权限

        List<Long> permissionIds = rolePermissions.stream().map(RolePermissionModel::getPermissionId).toList();

        List<PermissionModel> permissionModels = this.permissionRepo.findByIdIn(permissionIds);

        List<String> permission = permissionModels.stream().map(PermissionModel::getIdentity).toList();


        this.redisTemplate.opsForValue().set(this.genKey(roleModel.getId()), permission);
    }


    // 生成root账户的全部权限
    public void gentRootAuthorities() {

        List<PermissionModel> permissionListAll = this.permissionRepo.findAllByDeleted(false);

        List<String> identityList = permissionListAll.stream().map(PermissionModel::getIdentity).toList();


        // 查询root账户
        RoleModel root = this.getRole("root");

        if (ObjectUtil.isNotEmpty(root)) {
            // 生成root账户的权限
            this.redisTemplate.opsForValue().set(this.genKey(root.getId()), identityList);

        }


    }

    public void removeRole(RoleModel root) {
        this.redisTemplate.delete(this.genKey(root.getId()));
    }


    public List<String> getGrantedAuthorities(Long roleId) {


        List<String> authObject = this.redisTemplate.opsForValue().get(this.genKey(roleId));

        if (authObject == null) {
            return new ArrayList<>();
        }

        return authObject;


    }


    public String genKey(Long roleId) {
        return StrUtil.format("{}{}", this.ROLE_PERMISSION_KEY, roleId);
    }
}
