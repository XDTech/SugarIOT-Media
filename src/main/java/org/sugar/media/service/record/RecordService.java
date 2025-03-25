package org.sugar.media.service.record;

import cn.hutool.core.date.DateUtil;
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
import org.sugar.media.model.record.RecordModel;
import org.sugar.media.model.stream.StreamPushModel;
import org.sugar.media.repository.record.RecordRepo;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Date:2024/12/27 22:41:18
 * Author：Tobin
 * Description:
 */

@Service
public class RecordService {

    @Resource
    private RecordRepo recordRepo;


    public void createRecord(RecordModel recordModel) {

        this.recordRepo.save(recordModel);

    }


    public Page<RecordModel> getRecordPageList(Integer pi, Integer ps, Long startDate, Long endDate, Long tenantId, String app, String stream) {
        // Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        PageRequest pageRequest = PageRequest.of(pi - 1, ps);
        Specification<RecordModel> specification = (Root<RecordModel> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {

            // 用于暂时存放查询条件的集合
            List<Predicate> predicatesList = new ArrayList<>();




            if (startDate != null && startDate != 0) {
                predicatesList.add(cb.between(root.get("startTime"), startDate, endDate));
            }
            if (tenantId != null) {
                predicatesList.add(cb.equal(root.get("tenantId"), tenantId));
            }

            if (StrUtil.isNotEmpty(app)) {
                predicatesList.add(cb.equal(root.get("app"), app));
            }


            if (StrUtil.isNotEmpty(stream)) {
                predicatesList.add(cb.equal(root.get("stream"), stream));
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
        return this.recordRepo.findAll(specification, pageRequest);

    }

}
