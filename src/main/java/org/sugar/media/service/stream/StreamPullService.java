package org.sugar.media.service.stream;

import cn.hutool.core.util.StrUtil;
import jakarta.annotation.Resource;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.sugar.media.model.stream.StreamPullModel;
import org.sugar.media.repository.stream.StreamPullRepo;


import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * (MStreamPull)服务
 *
 * @author Tobin
 * @since 2024-11-27 12:42:56
 */
@Service
public class StreamPullService {

    @Resource
    private StreamPullRepo streamPullRepo;




    public StreamPullModel onlyStream(Long zid,String app,String stream){
      return   this.streamPullRepo.findAllByZidAndAppAndStream(zid,app,stream);
    }

    @Transactional
    public StreamPullModel createMStreamPull(StreamPullModel mStreamPull) {
        return this.streamPullRepo.save(mStreamPull);
    }

    @Transactional
    public StreamPullModel updateMStreamPull(StreamPullModel mStreamPull) {
        return this.streamPullRepo.save(mStreamPull);
    }

    @Transactional
    public void deleteMStreamPull(StreamPullModel mStreamPull) {
        this.streamPullRepo.delete(mStreamPull);
    }

    @Transactional
    public void deleteMStreamPull(Long id) {
        this.streamPullRepo.deleteById(id);
    }


    public Optional<StreamPullModel> getMStreamPull(Long id) {
        return this.streamPullRepo.findById(id);
    }

    // 分页查询
    public Page<StreamPullModel> getMStreamPullPageList(Integer pi, Integer ps,String name) {
        // Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        PageRequest pageRequest = PageRequest.of(pi - 1, ps);
        Specification<StreamPullModel> specification = (Root<StreamPullModel> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {

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
            Predicate[] p = new Predicate[predicatesList.size()];
            query.where(predicatesList.toArray(p));
            query.orderBy(cb.desc(root.get("createdAt")));
            return query.getGroupRestriction();

        };
        return this.streamPullRepo.findAll(specification, pageRequest);

    }
}
