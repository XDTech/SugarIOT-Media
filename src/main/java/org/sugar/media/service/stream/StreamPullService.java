package org.sugar.media.service.stream;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.lang.Console;
import cn.hutool.core.util.ObjectUtil;
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
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.sugar.media.beans.ResponseBean;
import org.sugar.media.beans.hooks.zlm.CommonBean;
import org.sugar.media.enums.PlayerTypeEnum;
import org.sugar.media.model.node.NodeModel;
import org.sugar.media.model.stream.StreamPullModel;
import org.sugar.media.repository.stream.StreamPullRepo;
import org.sugar.media.service.MediaCacheService;
import org.sugar.media.service.ZlmApiService;
import org.sugar.media.service.node.NodeService;
import org.sugar.media.utils.LeastConnectionUtil;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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

    @Resource
    private ZlmApiService zlmApiService;

    @Resource
    private NodeService nodeService;

    @Resource
    private MediaCacheService mediaCacheService;


    public StreamPullModel onlyStream(Long zid, String app, String stream) {
        return this.streamPullRepo.findAllByZidAndAppAndStream(zid, app, stream);
    }

    @Transactional
    public StreamPullModel createMStreamPull(StreamPullModel mStreamPull) {
        return this.streamPullRepo.save(mStreamPull);
    }


    // 创建并且立即拉流
    @Transactional
    public CommonBean autoPullStream(StreamPullModel mStreamPull) {

        CommonBean commonBean = new CommonBean();
        if (!mStreamPull.isEnablePull()) {
            this.createMStreamPull(mStreamPull);
            commonBean.setCode(0);
            return commonBean;
        }

        commonBean = this.playStreamPull(mStreamPull);
        if (commonBean.getCode().equals(0)) {
            // 把key存下来
            String key = Convert.toStr(commonBean.getData().get("key"));
            mStreamPull.setStreamKey(key);
            mStreamPull.setNodeId(commonBean.getNodeId());

        } else {
            // 失败直接返回
            return commonBean;
        }

        this.streamPullRepo.save(mStreamPull);
        commonBean.setCode(0);
        return commonBean;
    }


    // 执行负载均衡，返回合适的node
    public NodeModel executeBalance() {

        String serverId = LeastConnectionUtil.leastConnections();

        if (StrUtil.isBlank(serverId)) return null;

        //
        Long mediaId = Convert.toLong(serverId);
        Optional<NodeModel> node = this.nodeService.getNode(mediaId);

        if (node.isEmpty()) return null;

        boolean online = this.mediaCacheService.isOnline(mediaId);
        if (!online) return null;

        Console.log("执行负载均衡策略{}", node.get().toString());

        return node.get();


    }

    // 播放拉流代理
    public CommonBean playStreamPull(StreamPullModel streamPullModel) {
        CommonBean commonBean = new CommonBean();
        NodeModel nodeModel = new NodeModel();
        if (streamPullModel.getPlayerType().equals(PlayerTypeEnum.manual)) {
            Optional<NodeModel> node = this.nodeService.getNode(streamPullModel.getNodeId());
            if (node.isPresent()) nodeModel = node.get();

        } else {
            // 根据负载均衡策略选择合适的node

            NodeModel balanceModel = this.executeBalance();

            if (ObjectUtil.isNotEmpty(balanceModel)) {
                nodeModel = balanceModel;
            }
        }

        if (!this.mediaCacheService.isOnline(nodeModel.getId())) {
            commonBean.setCode(-4);
            commonBean.setMsg("节点已经离线");
        }

        commonBean = this.zlmApiService.addStreamProxy(streamPullModel, nodeModel);
        commonBean.setNodeId(nodeModel.getId());

        if (!commonBean.getCode().equals(0)) {

            if (commonBean.getMsg().equals("DESCRIBE:401 Unauthorized")) {

                commonBean.setMsg("拉流地址认证失败，请检查用户名密码准确性");
            }

            if (commonBean.getMsg().equals("play rtsp timeout")) {

                commonBean.setMsg("播放超时，请检查拉流地址准确性");
            }
        }


        return commonBean;

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
    public Page<StreamPullModel> getMStreamPullPageList(Integer pi, Integer ps, String name, Long zid) {
        // Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        PageRequest pageRequest = PageRequest.of(pi - 1, ps);
        Specification<StreamPullModel> specification = (Root<StreamPullModel> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {

            // 用于暂时存放查询条件的集合
            List<Predicate> predicatesList = new ArrayList<>();

            if (!StrUtil.isEmpty(name)) {
                predicatesList.add(cb.like(root.get("name"), "%" + name + "%"));
            }
            if (zid != null) {
                predicatesList.add(cb.equal(root.get("zid"), zid));
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
