package org.sugar.media.service.node;


import cn.hutool.core.thread.ThreadUtil;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.sugar.media.beans.hooks.zlm.ZlmRemoteConfigBean;
import org.sugar.media.enums.MediaServerEnum;
import org.sugar.media.enums.StatusEnum;
import org.sugar.media.enums.SyncEnum;
import org.sugar.media.model.node.NodeModel;
import org.sugar.media.repository.node.NodeRepo;
import org.sugar.media.service.MediaCacheService;
import org.sugar.media.service.ZlmApiService;

import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * ZLM Node Service
 */
@Service
public class ZlmNodeService {

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
        return this.nodeRepo.findAllByZidAndTypesOrderByIdDesc(zid,MediaServerEnum.zlm);
    }

    @Transactional
    public NodeModel createNode(NodeModel nodeModel) {


        return this.nodeRepo.save(nodeModel);

    }

    @Transactional
    public void deleteNode(Long id) {


        this.nodeRepo.deleteById(id);

    }

    /**
     * 新增节点调用
     *
     * @param nodeModel
     */

    public void createMediaAsync(NodeModel nodeModel) {

        NodeModel node = this.createNode(nodeModel);

        //  将webhook配置到media
        ThreadUtil.execute(() -> {
            boolean written = write2MediaConfig(node, SyncEnum.hook);
            // TODO:新增时同步配置，缓存到数据库
            // 1.读取配置
            this.updateTime(nodeModel, written);
        });

    }


    /**
     * 同步所有配置调用
     *
     * @param nodeModel
     */
    public boolean syncAll(NodeModel nodeModel) {

        boolean written = write2MediaConfig(nodeModel, SyncEnum.all);
        this.updateTime(nodeModel, written);
        return written;
    }

    /**
     * 修改webhook配置时调用
     *
     * @param nodeModel
     */
    @Transactional
    public void updateMediaAsync(NodeModel nodeModel) {

        NodeModel node = this.createNode(nodeModel);

        //  将webhook配置到media
        ThreadUtil.execute(() -> {
            boolean written = write2MediaConfig(node, SyncEnum.hook);
            this.updateTime(nodeModel, written);
        });

    }


    /**
     * 同步配置到流媒体配置文件
     *
     * @param nodeModel
     * @return
     */
    public boolean write2MediaConfig(NodeModel nodeModel, SyncEnum zlmSyncEnum) {

        return this.zlmApiService.syncZlmConfig(nodeModel, zlmSyncEnum);


    }

    public ZlmRemoteConfigBean readMediaConfig(NodeModel nodeModel) {

        return this.zlmApiService.getServerConfig(nodeModel);


    }


    // 根据同步结果，判断是否更新时间
    private void updateTime(NodeModel nodeModel, boolean written) {

        if (written) {
            updateConfigTimeById(nodeModel.getId(), new Date());
            updateHeartbeatTimeById(nodeModel.getId(), new Date());
        } else {
            updateConfigTimeById(nodeModel.getId(), null);
            updateHeartbeatTimeById(nodeModel.getId(), null);
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
            boolean written = write2MediaConfig(nodeModel, SyncEnum.all);

            //

            this.updateTime(nodeModel, written);

            this.mediaCacheService.setMediaStatus(nodeModel.getId(), written ? StatusEnum.online.getStatus() : StatusEnum.offline.getStatus());
        }
    }
}
