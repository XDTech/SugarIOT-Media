package org.sugar.media.hooks;

import cn.hutool.core.convert.Convert;
import cn.hutool.log.StaticLog;
import jakarta.annotation.Resource;
import jakarta.servlet.http.PushBuilder;
import org.springframework.web.bind.annotation.*;
import org.sugar.media.beans.ResponseBean;
import org.sugar.media.enums.StatusEnum;
import org.sugar.media.model.node.NodeModel;
import org.sugar.media.service.MediaCacheService;
import org.sugar.media.service.node.NodeService;

import java.util.Date;
import java.util.Map;
import java.util.Optional;

/**
 * Date:2024/11/13 18:08:41
 * Author：Tobin
 * Description: zlm hook api
 */

@RestController
@RequestMapping("/zlm")
public class ZlmHookController {

    @Resource
    private MediaCacheService mediaCacheService;

    @Resource
    private NodeService nodeService;


    // 服务器定时上报时间，上报间隔可配置，默认10s上报一次
    @PostMapping("/keepalive")
    public ResponseBean keepalive(@RequestBody Map<String, Object> body) {
        StaticLog.info("{}", body);

        Long mediaServerId = Convert.toLong(body.get("mediaServerId"));

        Optional<NodeModel> node = this.nodeService.getNode(mediaServerId);
        if (node.isPresent()) {
            // 如果是false，后续发送上线消息
            boolean online = this.mediaCacheService.isOnline(mediaServerId);
            this.mediaCacheService.setMediaStatus(mediaServerId, StatusEnum.online.getStatus());
            if(!online){
                //TODO:发送上线消息
                StaticLog.info("发送消息");
            }
            this.nodeService.updateHeartbeatTimeById(mediaServerId,new Date());

        }


        return ResponseBean.success();

    }

    // 服务器启动事件，可以用于监听服务器崩溃重启；此事件对回复不敏感。
    @PostMapping("/server/started")
    public ResponseBean started(@RequestBody Map<String, Object> body) {
        StaticLog.info("{}", body);
        Long mediaServerId = Convert.toLong(body.get("mediaServerId"));

        Optional<NodeModel> node = this.nodeService.getNode(mediaServerId);
        if (node.isPresent()) {
            boolean online = this.mediaCacheService.isOnline(mediaServerId);
            this.mediaCacheService.setMediaStatus(mediaServerId, StatusEnum.online.getStatus());
            if(!online){
                //TODO:发送上线消息
                StaticLog.info("发送消息");
            }
            this.nodeService.updateHeartbeatTimeById(mediaServerId,new Date());

        }
        return ResponseBean.success();
    }
}
