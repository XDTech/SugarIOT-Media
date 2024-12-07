package org.sugar.media.hooks;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.TimeInterval;
import cn.hutool.core.lang.Console;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTUtil;
import cn.hutool.jwt.JWTValidator;
import cn.hutool.log.StaticLog;
import jakarta.annotation.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.sugar.media.beans.ResponseBean;
import org.sugar.media.beans.SocketMsgBean;
import org.sugar.media.beans.hooks.zlm.CommonBean;
import org.sugar.media.beans.hooks.zlm.OnPlayBean;
import org.sugar.media.enums.SocketMsgEnum;
import org.sugar.media.enums.StatusEnum;
import org.sugar.media.model.node.NodeModel;
import org.sugar.media.model.stream.StreamPullModel;
import org.sugar.media.server.WebSocketServer;
import org.sugar.media.service.MediaCacheService;
import org.sugar.media.service.ZlmApiService;
import org.sugar.media.service.node.NodeService;
import org.sugar.media.service.node.ZlmNodeService;
import org.sugar.media.service.stream.StreamPullService;
import org.sugar.media.utils.BaseUtil;
import org.sugar.media.utils.JwtUtils;

import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

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
    private ZlmNodeService zlmNodeService;

    @Resource
    private ZlmApiService zlmApiService;

    @Resource
    private StreamPullService streamPullService;


    // 服务器定时上报时间，上报间隔可配置，默认10s上报一次
    @PostMapping("/keepalive")
    public ResponseBean keepalive(@RequestBody Map<String, Object> body) {

        Long mediaServerId = Convert.toLong(body.get("mediaServerId"));

        Optional<NodeModel> node = this.zlmNodeService.getNode(mediaServerId);
        if (node.isPresent()) {
            // 如果是false，后续发送上线消息
            boolean online = this.mediaCacheService.isOnline(mediaServerId);

            this.mediaCacheService.setMediaStatus(mediaServerId, StatusEnum.online.getStatus(), node.get().getAliveInterval());
            if (!online) {
                //TODO:发送上线消息
                StaticLog.info("发送消息");
                WebSocketServer.sendSystemMsg(new SocketMsgBean(SocketMsgEnum.mediaOnline, new Date(), node.get().getName()));
            }
            this.zlmNodeService.updateHeartbeatTimeById(mediaServerId, new Date());


        }


        return ResponseBean.success();

    }

    // 服务器启动事件，可以用于监听服务器崩溃重启；此事件对回复不敏感。
    @PostMapping("/server/started")
    public ResponseBean started(@RequestBody Map<String, Object> body) {
        StaticLog.info("{}", body);
        Long mediaServerId = Convert.toLong(body.get("mediaServerId"));

        Optional<NodeModel> node = this.zlmNodeService.getNode(mediaServerId);
        if (node.isPresent()) {
            boolean online = this.mediaCacheService.isOnline(mediaServerId);
            this.mediaCacheService.setMediaStatus(mediaServerId, StatusEnum.online.getStatus(), node.get().getAliveInterval());
            if (!online) {
                //TODO:发送上线消息
                StaticLog.info("发送消息");
                WebSocketServer.sendSystemMsg(new SocketMsgBean(SocketMsgEnum.mediaOnline, new Date(), node.get().getName()));

            }
            this.zlmNodeService.writeAllAndUpdateTime(node.get());

        }
        return ResponseBean.success();
    }

    /**
     * 播放鉴权hook
     *
     * @param body
     * @return
     */
    @PostMapping("/on_play")
    public ResponseBean onPlay(@RequestBody OnPlayBean body) {

        StaticLog.info("{}", body.toString());

        Map<String, String> stringMap = BaseUtil.paramConvertToMap(body.getParams());


        if (MapUtil.isEmpty(stringMap) || !stringMap.containsKey("sign")) {
            return ResponseBean.fail();
        }

        boolean sign = JwtUtils.verifyToken(stringMap.get("sign"));

        if (!sign) return ResponseBean.fail("鉴权失败");


        return ResponseBean.success();
    }


    @PostMapping("/stream/nof/found")
    public ResponseBean streamNotFound(@RequestBody OnPlayBean body) {

        Console.log("{}===触发流未找到事件", body);

        // 再次鉴权
        TimeInterval timer = DateUtil.timer();
        Map<String, String> stringMap = BaseUtil.paramConvertToMap(body.getParams());
        if (MapUtil.isEmpty(stringMap) || !stringMap.containsKey("sign")) {
            return ResponseBean.fail();
        }

        boolean sign = JwtUtils.verifyToken(stringMap.get("sign"));

        if (!sign) return ResponseBean.fail();
        JWT parseToken = JWTUtil.parseToken(stringMap.get("sign"));
        Object zid = parseToken.getPayload("zid");
        if (ObjectUtil.isEmpty(zid)) return ResponseBean.fail();


        Console.log("{}====鉴权耗时", timer.intervalRestart());
        //判断流媒体是否在线
        boolean online = this.mediaCacheService.isOnline(Convert.toLong(body.getMediaServerId()));

        if (!online) return ResponseBean.fail();


        Optional<NodeModel> node = this.zlmNodeService.getNode(Convert.toLong(body.getMediaServerId()));

        if (node.isEmpty()) return ResponseBean.fail();


        Console.log("{}====节点耗时", timer.intervalRestart());

        StreamPullModel streamPullModel = this.streamPullService.onlyStream(Convert.toLong(zid), body.getApp(), body.getStream());

        if (ObjectUtil.isEmpty(streamPullModel)) return ResponseBean.fail();


        Console.log("{}====stream耗时", timer.intervalRestart());
        // 拉流

        CommonBean commonBean = this.zlmApiService.addStreamProxy(streamPullModel, node.get());

        Console.log("{}====拉流耗时", timer.intervalRestart());
        return ResponseBean.createResponseBean(commonBean.getCode(), commonBean.getMsg());
    }


    // 离线
    @PostMapping("/server/exited")
    public ResponseBean exited(@RequestBody Map<String, Object> body) {
        StaticLog.info("{}", body);
        Long mediaServerId = Convert.toLong(body.get("mediaServerId"));

        Optional<NodeModel> node = this.zlmNodeService.getNode(mediaServerId);
        if (node.isPresent()) {
            this.mediaCacheService.removeMediaStatus(mediaServerId);
        }
        return ResponseBean.success();
    }


}
