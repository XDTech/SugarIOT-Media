package org.sugar.media.service.node;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.log.StaticLog;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.sugar.media.enums.MediaServerEnum;
import org.sugar.media.enums.StatusEnum;
import org.sugar.media.model.node.NodeModel;
import org.sugar.media.repository.node.NodeRepo;
import org.sugar.media.service.MediaCacheService;
import org.sugar.media.service.ZlmApiService;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class NodeService {

    @Resource
    private NodeRepo nodeRepo;


    @Resource
    private ZlmApiService zlmApiService;


    @Resource
    private MediaCacheService mediaCacheService;


    @Transactional
    public void updateConfigTimeById(Long id, Date syncConfigTime) {
        this.nodeRepo.updateConfigTimeById(id, syncConfigTime);
    }

    @Transactional
    public void updateHeartbeatTimeById(Long id, Date syncHeartbeatTime) {
        this.nodeRepo.updateHeartbeatTimeById(id, syncHeartbeatTime);
    }

    public Optional<NodeModel> getNode(Long id) {

        return this.nodeRepo.findById(id);
    }


    public List<NodeModel> getNodeList(Long zid) {
        return this.nodeRepo.findAllByZidOrderByIdDesc(zid);
    }

    @Transactional
    public NodeModel createNode(NodeModel nodeModel) {


        return this.nodeRepo.save(nodeModel);

    }

    @Transactional
    public void deleteNode(Long id) {


        this.nodeRepo.deleteById(id);

    }


    @Transactional
    public boolean createMediaSync(NodeModel nodeModel, boolean sync) {

        NodeModel node = this.createNode(nodeModel);


        boolean s = true;
        if (sync) {
            s = write2Config(node);
        }


        return s;
    }

    public void createMediaAsync(NodeModel nodeModel) {

        NodeModel node = this.createNode(nodeModel);

        //  将webhook配置到media
        ThreadUtil.execute(() -> {
            write2Config(node);
            // TODO:同步配置，缓存到数据库
        });

    }


    /**
     * 同步到流媒体配置文件
     *
     * @param nodeModel
     * @return
     */
    public boolean write2Config(NodeModel nodeModel) {
        switch (nodeModel.getTypes()) {
            case zlm -> {
                return this.zlmApiService.syncZlmConfig(nodeModel);
            }
            default -> {
                return false;
            }
        }

    }

    public boolean readConfig(NodeModel nodeModel) {
        switch (nodeModel.getTypes()) {
            case zlm -> {
                return this.zlmApiService.syncZlmConfig(nodeModel);
            }
            default -> {
                return false;
            }
        }

    }

    /**
     * 每次重启服务，都同步一下config 并且写入redis缓存
     */

    @Transactional
    public void write2Cache() {
        List<NodeModel> modelList = this.nodeRepo.findAll();


        for (NodeModel nodeModel : modelList) {

            // 首先同步一下配置
            boolean written = write2Config(nodeModel);

            //
            if (written) {
                updateConfigTimeById(nodeModel.getId(), new Date());
                updateHeartbeatTimeById(nodeModel.getId(), new Date());
            } else {
                updateConfigTimeById(nodeModel.getId(), null);
                updateHeartbeatTimeById(nodeModel.getId(), null);
            }

            this.mediaCacheService.setMediaStatus(nodeModel.getId(), written ? StatusEnum.online.getStatus() : StatusEnum.offline.getStatus());
        }
    }
}
