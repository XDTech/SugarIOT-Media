package org.sugar.media.service.node;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.log.StaticLog;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.sugar.media.enums.StatusEnum;
import org.sugar.media.model.node.NodeModel;
import org.sugar.media.repository.node.NodeRepo;
import org.sugar.media.service.MediaCacheService;
import org.sugar.media.service.ZlmApiService;

import java.util.List;

@Service
public class NodeService {

    @Resource
    private NodeRepo nodeRepo;


    @Resource
    private ZlmApiService zlmApiService;


    @Resource
    private MediaCacheService mediaCacheService;


    public NodeModel getNode(Long id) {

        return this.nodeRepo.findById(id).get();
    }


    public List<NodeModel> getNodeList(Long zid) {
        return this.nodeRepo.findAllByZid(zid);
    }

    @Transactional
    public NodeModel createNode(NodeModel nodeModel) {


        return this.nodeRepo.save(nodeModel);

    }

    @Transactional
    public boolean createMediaSync(NodeModel nodeModel, boolean sync) {

        NodeModel node = this.createNode(nodeModel);


        // 存入redis
        this.mediaCacheService.setMediaStatus(node.getId(), StatusEnum.offline.getStatus());

        boolean s = true;
        if (sync) {
            switch (node.getTypes()) {
                case zlm -> s = this.zlmApiService.syncZlmConfig(nodeModel);
            }
        }


        return s;
    }


    public void write2Cache() {
        List<NodeModel> modelList = this.nodeRepo.findAll();

        for (NodeModel nodeModel : modelList) {
            List<String> apiList = this.zlmApiService.getApiList(nodeModel);
            this.mediaCacheService.setMediaStatus(nodeModel.getId(), CollUtil.isNotEmpty(apiList) ? StatusEnum.online.getStatus() : StatusEnum.offline.getStatus());
        }
    }
}
