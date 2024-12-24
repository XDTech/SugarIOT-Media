package org.sugar.media.service.user;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.sugar.media.model.UserModel;
import org.sugar.media.repository.UserRepo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


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



    // 查询账户是否存在


    public UserModel getUser(String username,Long tenantId) {
        return this.userRepo.findAllByUsernameAndTenantId(username,tenantId);
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
}
