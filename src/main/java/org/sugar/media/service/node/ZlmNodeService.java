package org.sugar.media.service.node;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
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
import org.sugar.media.service.media.MediaCacheService;
import org.sugar.media.service.media.ZlmApiService;

import java.util.Date;
import java.util.List;
import java.util.Map;
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


    public List<NodeModel> getNodeList() {
        return this.nodeRepo.findAllByTypesOrderByIdDesc(MediaServerEnum.zlm);
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


    @Transactional
    public void createMediaAsync(NodeModel nodeModel) {

        NodeModel node = this.createNode(nodeModel);

        //  将webhook配置到media
        ThreadUtil.execute(() -> {
            boolean written = write2MediaConfig(node, SyncEnum.hook);
            // TODO:新增时同步配置，缓存到数据库
            // 1.读取配置

            this.readMediaConfig(node);

            //
            this.createNode(node);

            this.updateTime(node, written);
        });

    }


    /**
     * 同步所有配置调用
     *
     * @param nodeModel
     */
    @Transactional
    public boolean syncAll(NodeModel nodeModel) {

        boolean written = write2MediaConfig(nodeModel, SyncEnum.all);
        this.updateTime(nodeModel, written);
        return written;
    }


    @Transactional
    public void updateMediaAsync(NodeModel nodeModel, SyncEnum syncEnum) {

        NodeModel node = this.createNode(nodeModel);

        //  将webhook配置到media
        ThreadUtil.execute(() -> {
            boolean written = write2MediaConfig(node, syncEnum);
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


    // 读取高级配置，并且赋值到node model

    public NodeModel readMediaConfig(NodeModel nodeModel) {

        ZlmRemoteConfigBean serverConfig = this.zlmApiService.getServerConfig(nodeModel);

        if (serverConfig.getCode().equals(0) && CollUtil.isNotEmpty(serverConfig.getData())) {

            Map<String, String> configMap = serverConfig.getData().get(0);
            nodeModel.setAliveInterval(Convert.toFloat(configMap.get("hook.alive_interval")));
            nodeModel.setTimeoutSec(Convert.toInt(configMap.get("hook.timeoutSec")));
            nodeModel.setRtmpPort(Convert.toInt(configMap.get("rtmp.port")));
            nodeModel.setRtspPort(Convert.toInt(configMap.get("rtsp.port")));

        }
        return nodeModel;
    }


    // 根据同步结果，判断是否更新时间

    @Transactional
    public void updateTime(NodeModel nodeModel, boolean written) {

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

            boolean written = this.writeAllAndUpdateTime(nodeModel);


            this.mediaCacheService.setMediaStatus(nodeModel.getId(), written ? StatusEnum.online.getStatus() : StatusEnum.offline.getStatus(), nodeModel.getAliveInterval());
        }
    }


    @Transactional
    public boolean writeAllAndUpdateTime(NodeModel nodeModel) {
        boolean written = write2MediaConfig(nodeModel, SyncEnum.all);

        this.updateTime(nodeModel, written);

        return written;
    }
}
