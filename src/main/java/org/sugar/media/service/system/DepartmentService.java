package org.sugar.media.service.system;


import cn.hutool.core.util.StrUtil;
import jakarta.annotation.Resource;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.sugar.media.model.system.DepartmentModel;
import org.sugar.media.repository.system.DepartmentRepo;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * (Department)服务
 *
 * @author Tobin
 * @since 2025-01-22 21:42:44
 */
@Service
public class DepartmentService {

    @Resource
    private DepartmentRepo departmentRepo;


    @Transactional
    public DepartmentModel createDepartment(DepartmentModel departmentModel) {
        return this.departmentRepo.save(departmentModel);
    }

    @Transactional
    public DepartmentModel updateDepartment(DepartmentModel departmentModel) {
        return this.departmentRepo.save(departmentModel);
    }

    @Transactional
    public void deleteDepartment(DepartmentModel departmentModel) {
        this.departmentRepo.delete(departmentModel);
    }

    @Transactional
    public void deleteDepartment(Long id) {
        this.departmentRepo.deleteById(id);
    }


    public Optional<DepartmentModel> getDepartment(Long id) {
        return this.departmentRepo.findById(id);
    }

    // 分页查询
    public Page<DepartmentModel> getDepartmentPageList(Integer pi, Integer ps,String name) {
        // Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        PageRequest pageRequest = PageRequest.of(pi - 1, ps);

        return this.departmentRepo.findAll(this.genSpecification(name), pageRequest);

    }

    public List<DepartmentModel> getDepartmentList(String name) {
        // Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");

        return this.departmentRepo.findAll(this.genSpecification(name));

    }

    private Specification<DepartmentModel> genSpecification(String name) {
        return (Root<DepartmentModel> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {
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
            Predicate[] p = new Predicate[predicatesList.size()];
            query.where(predicatesList.toArray(p));
            query.orderBy(cb.desc(root.get("createdAt")));
            query.orderBy(cb.asc(root.get("sort")));
            return query.getGroupRestriction();

        };
    }
}
