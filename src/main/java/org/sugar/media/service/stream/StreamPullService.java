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
import org.sugar.media.beans.hooks.zlm.StreamProxyInfoBean;
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


    public StreamPullModel onlyStream(Long tenantId, String app, String stream) {
        return this.streamPullRepo.findAllByTenantIdAndAppAndStream(tenantId, app, stream);
    }

    @Transactional
    public StreamPullModel createMStreamPull(StreamPullModel mStreamPull) {
        return this.streamPullRepo.save(mStreamPull);
    }


    /**
     * 创建并且立即拉流，此处运行负载均衡策略
     * (新建拉流代理时调用)
     *
     * @param mStreamPull
     * @return
     */
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

        } else {
            // 失败直接返回
            return commonBean;
        }

        this.streamPullRepo.save(mStreamPull);
        commonBean.setCode(0);
        return commonBean;
    }


    // 执行负载均衡，返回合适的node
    private NodeModel executeBalance() {

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


    /**
     * 获取播放节点，包含运行负载均衡策略
     * 播放时调用，自动保存要播放的节点id
     *
     * @param mStreamPull
     * @return
     */

    @Transactional
    public Optional<NodeModel> getPlayerNode(StreamPullModel mStreamPull) {
        Optional<NodeModel> node = Optional.empty();
        // 判断节点是否离线
        if (mStreamPull.getPlayerType().equals(PlayerTypeEnum.manual)) {
            node = this.nodeService.getNode(mStreamPull.getNodeId());
        } else {
            // :1.如果是负载均衡，判断node id是否存在 不存在 直接生成一个

            if (mStreamPull.getNodeId() == null) {

                NodeModel nodeModel = this.executeBalance();
                // 把node id存下来
                if (ObjectUtil.isNotNull(nodeModel)) {

                    mStreamPull.setNodeId(nodeModel.getId());
                    this.createMStreamPull(mStreamPull);
                }
                node = Optional.ofNullable(nodeModel);


            } else {
                //  2.如果存在 判断是否在线，如果不在线，生成一个
                boolean online = this.mediaCacheService.isOnline(mStreamPull.getNodeId());

                if (!online) {
                    NodeModel nodeModel = this.executeBalance();

                    // 把node id存下来
                    if (ObjectUtil.isNotNull(nodeModel)) {
                        mStreamPull.setNodeId(nodeModel.getId());
                        this.createMStreamPull(mStreamPull);
                    }

                    node = Optional.ofNullable(nodeModel);

                } else {
                    // 在线
                    node = this.nodeService.getNode(mStreamPull.getNodeId());
                }

            }
        }

        // 如果不在线，返回空
        if (node.isPresent() && !this.mediaCacheService.isOnline(node.get().getId())) {
            node = Optional.empty();
        }

        return node;
    }


    /**
     * 调用zlm api 添加拉流代理，此处自动调用 负载均衡策略
     *
     * @param streamPullModel
     * @return
     */
    private CommonBean playStreamPull(StreamPullModel streamPullModel) {
        CommonBean commonBean = new CommonBean();

        Optional<NodeModel> playerNode = this.getPlayerNode(streamPullModel);

        if (playerNode.isEmpty()) {

            commonBean.setCode(-4);
            commonBean.setMsg("暂无可以用播放节点");
            return commonBean;
        }

        NodeModel nodeModel = playerNode.get();
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
    public void resetStream(StreamPullModel mStreamPull) {
        mStreamPull.setStreamKey(null);
        //如果是负载均衡，把node节点存为空
        if (mStreamPull.getPlayerType().equals(PlayerTypeEnum.balance)) {
            mStreamPull.setNodeId(null);
        }
        this.updateMStreamPull(mStreamPull);
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
    public Page<StreamPullModel> getMStreamPullPageList(Integer pi, Integer ps, String name, Long tenantId) {
        // Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        PageRequest pageRequest = PageRequest.of(pi - 1, ps);
        Specification<StreamPullModel> specification = (Root<StreamPullModel> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {

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
        return this.streamPullRepo.findAll(specification, pageRequest);

    }


    /**
     * 手动添加拉流代理
     *
     * @return
     */

    @Transactional
    public CommonBean manualPullStream(StreamPullModel streamPullModel) {
        CommonBean commonBean = new CommonBean();
        // 1.获取播放节点
        Optional<NodeModel> node = this.getPlayerNode(streamPullModel);

        if (node.isEmpty()) {
            commonBean.setCode(-1);
            commonBean.setMsg("暂无可用播放节点");
            return commonBean;
        }

        // 2. 如果key存在 查询拉流代理是否正在拉流

        if (StrUtil.isNotBlank(streamPullModel.getStreamKey())) {
            StreamProxyInfoBean streamProxyInfo = this.zlmApiService.getStreamProxyInfo(streamPullModel.getStreamKey(), node.get());
            if (streamProxyInfo.getCode() == 0 && streamProxyInfo.getData() != null && streamProxyInfo.getData().getStatus() == 0) {
                // 拉流代理已经存在，此处返回播放成功
                commonBean.setCode(0);
                return commonBean;
            }
        }


        // 3. 添加拉流代理
        commonBean = this.zlmApiService.addStreamProxy(streamPullModel, node.get());
        if (commonBean.getCode().equals(0)) {
            // 在此处更新节点
            streamPullModel.setStreamKey(Convert.toStr(commonBean.getData().get("key")));
            this.updateMStreamPull(streamPullModel);

        }

        return commonBean;

    }
}
