package org.sugar.media.service.stream;

import cn.hutool.core.lang.Console;
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
import org.sugar.media.enums.AppEnum;
import org.sugar.media.model.node.NodeModel;
import org.sugar.media.model.stream.StreamPullModel;
import org.sugar.media.model.stream.StreamPushModel;
import org.sugar.media.repository.stream.StreamPushRepo;
import org.sugar.media.service.gb.ChannelService;
import org.sugar.media.service.media.MediaCacheService;
import org.sugar.media.service.media.ZlmApiService;
import org.sugar.media.service.node.NodeService;
import org.sugar.media.sipserver.manager.SsrcManager;

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


    @Resource
    private ChannelService channelService;

    @Resource
    private ZlmApiService zlmApiService;

    @Resource
    private MediaCacheService mediaCacheService;

    @Resource
    private NodeService nodeService;


    public StreamPushModel onlyPushStream(String app, String stream, Long tenantId) {

        return this.streamPushRepo.findAllByAppAndStreamAndTenantId(app, stream, tenantId);
    }

    public StreamPushModel findAllByRelevanceId(Long relevanceId) {

        return this.streamPushRepo.findAllByRelevanceId(relevanceId);
    }
    public List<StreamPushModel> findAllByTenantId(Long tenantId) {

        return this.streamPushRepo.findAllByTenantId(tenantId);
    }

    @Transactional
    public StreamPushModel createPushStream(StreamPushModel streamPushModel) {

        return this.streamPushRepo.save(streamPushModel);
    }

    @Transactional
    public StreamPushModel updatePushStream(StreamPushModel streamPushModel) {

        return this.streamPushRepo.save(streamPushModel);
    }


    public Optional<StreamPushModel> getStreamPush(Long id) {

        return this.streamPushRepo.findById(id);
    }

    public List<StreamPushModel> getStreamPushList() {

        return this.streamPushRepo.findAll();
    }


    public void deletePushStream(StreamPushModel streamPushModel) {


        if (streamPushModel.getApp().equals(AppEnum.rtp.toString())) {
            // 发送send by

            this.channelService.sendByCode(streamPushModel.getStream().split("_")[1]);
        }

        // 关闭流

        Optional<NodeModel> node = this.nodeService.getNode(streamPushModel.getNodeId());

        if (node.isPresent() && this.mediaCacheService.isOnline(node.get().getId())) {
            this.zlmApiService.closeSteam(streamPushModel.getApp(), streamPushModel.getStream(), node.get());
        }

        // 删除推流记录
        this.streamPushRepo.delete(streamPushModel);


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
