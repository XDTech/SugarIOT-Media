package org.sugar.media.service.stream;

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
import org.sugar.media.model.stream.StreamPullModel;
import org.sugar.media.model.stream.StreamPushModel;
import org.sugar.media.repository.stream.StreamPushRepo;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Date:2025/01/01 22:04:01
 * Author：Tobin
 * Description:
 */

@Service
public class StreamPushService {

    @Resource
    private StreamPushRepo streamPushRepo;


    public StreamPushModel onlyPushStream(String app,String stream,Long tenantId){

       return this.streamPushRepo.findAllByAppAndStreamAndTenantId(app, stream, tenantId);
    }
    public StreamPushModel findAllByRelevanceId(Long relevanceId){

       return this.streamPushRepo.findAllByRelevanceId( relevanceId);
    }

    public StreamPushModel createPushStream(StreamPushModel streamPushModel){

        return this.streamPushRepo.save(streamPushModel);
    }

    public Optional<StreamPushModel> getStreamPush(Long id){

        return this.streamPushRepo.findById(id);
    }
    public List<StreamPushModel> getStreamPushList(){

        return this.streamPushRepo.findAll();
    }



    public Page<StreamPushModel> getMStreamPushPageList(Integer pi, Integer ps, String name, Long tenantId) {
        // Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        PageRequest pageRequest = PageRequest.of(pi - 1, ps);
        Specification<StreamPushModel> specification = (Root<StreamPushModel> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {

            // 用于暂时存放查询条件的集合
            List<Predicate> predicatesList = new ArrayList<>();

            if (!StrUtil.isEmpty(name)) {
                predicatesList.add(cb.like(root.get("name"), "%" + name + "%"));
            }
            if (tenantId != null) {
                predicatesList.add(cb.equal(root.get("tenantId"), tenantId));
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
        return this.streamPushRepo.findAll(specification, pageRequest);

    }

}
