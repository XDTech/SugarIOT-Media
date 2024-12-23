package org.sugar.media.hooks;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.TimeInterval;
import cn.hutool.core.lang.Console;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTUtil;
import cn.hutool.log.StaticLog;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;
import org.sugar.media.beans.ResponseBean;
import org.sugar.media.beans.SocketMsgBean;
import org.sugar.media.beans.gb.SsrcInfoBean;
import org.sugar.media.beans.hooks.zlm.CommonBean;
import org.sugar.media.beans.hooks.zlm.FlowReportBean;
import org.sugar.media.beans.hooks.zlm.OnPlayBean;
import org.sugar.media.enums.SocketMsgEnum;
import org.sugar.media.enums.StatusEnum;
import org.sugar.media.model.node.NodeModel;
import org.sugar.media.model.stream.StreamPullModel;
import org.sugar.media.server.WebSocketServer;
import org.sugar.media.service.media.MediaCacheService;
import org.sugar.media.service.media.ZlmApiService;
import org.sugar.media.service.node.ZlmNodeService;
import org.sugar.media.service.stream.StreamPullService;
import org.sugar.media.sipserver.manager.SsrcManager;
import org.sugar.media.sipserver.sender.SipRequestSender;
import org.sugar.media.utils.BaseUtil;
import org.sugar.media.utils.JwtUtils;

import java.util.Date;
import java.util.HashMap;
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
    private ZlmNodeService zlmNodeService;

    @Resource
    private ZlmApiService zlmApiService;

    @Resource
    private StreamPullService streamPullService;

    @Resource
    private SsrcManager ssrcManager;

    @Resource
    private SipRequestSender sipRequestSender;


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

        StaticLog.info("{}", "播放鉴权hook");
//        if (body.getApp().equals("rtp")) {
//            return ResponseBean.success();
//        }
        Map<String, String> authentication = this.authentication(body.getParams());
        if (MapUtil.isEmpty(authentication)) return ResponseBean.fail();

        return ResponseBean.success();
    }


    @PostMapping("/stream/nof/found")
    public ResponseBean streamNotFound(@RequestBody OnPlayBean body) {

        Console.log("{}===触发流未找到事件", body);

        // 再次鉴权
        TimeInterval timer = DateUtil.timer();
        Map<String, String> authentication = this.authentication(body.getParams());


        if (MapUtil.isEmpty(authentication)) return ResponseBean.fail();


        JWT parseToken = JWTUtil.parseToken(authentication.get("sign"));
        Object streamId = parseToken.getPayload("streamId");
        if (ObjectUtil.isEmpty(streamId)) return ResponseBean.fail();


        Console.log("{}====鉴权耗时", timer.intervalRestart());
        //判断流媒体是否在线
        boolean online = this.mediaCacheService.isOnline(Convert.toLong(body.getMediaServerId()));

        if (!online) return ResponseBean.fail();


        Optional<NodeModel> node = this.zlmNodeService.getNode(Convert.toLong(body.getMediaServerId()));

        if (node.isEmpty()) return ResponseBean.fail();


        Console.log("{}====节点耗时", timer.intervalRestart());


        Optional<StreamPullModel> streamPullModel = this.streamPullService.getMStreamPull(Convert.toLong(streamId));

        if (streamPullModel.isEmpty()) {
           return ResponseBean.fail();
        }


        Console.log("{}====stream耗时", timer.intervalRestart());
        // 拉流

        CommonBean commonBean = this.zlmApiService.addStreamProxy(streamPullModel.get(), node.get());

        Console.log("{}====拉流耗时", timer.intervalRestart());

        if (commonBean.getCode().equals(0)) {
            // 在此处更新节点
            streamPullModel.get().setStreamKey(Convert.toStr(commonBean.getData().get("key")));
            this.streamPullService.updateMStreamPull(streamPullModel.get());

        }
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


    /**
     * 无人观看事件，关闭流
     *
     * @param body
     * @return
     */
    @PostMapping("/stream/none/reader")
    public Map<String, Object> on_stream_none_reader(@RequestBody OnPlayBean body) {
        Map<String, Object> map = new HashMap<>();
        StaticLog.info("{}。{}", "触发无人观看事件", body);




        if (body.getApp().equals("rtp")) {

            String hexString = body.getStream(); // 16进制字符串
            // 转换为10进制字符串
            String ssrc = BaseUtil.hex2ssrc(hexString);

            // 通过ssrc 查找
            SsrcInfoBean ssrcInfoBean = this.ssrcManager.getSsrc(ssrc);

            if (ObjectUtil.isNotEmpty(ssrcInfoBean)) {

                Console.log(ssrcInfoBean.toString());

                this.sipRequestSender.sendBye(ssrcInfoBean);


            }


            map.put("code", 0);
            map.put("close", true);

            // 给设备发送bye消息
            return map;
        }

        Map<String, String> authentication = this.authentication(body.getParams());
        if (MapUtil.isEmpty(authentication)) {
            map.put("code", 0);
            map.put("close", false);
            return map;
        }
        // 重置拉流代理
        /// 查询是哪个node

        ThreadUtil.execute(() -> {
            Optional<NodeModel> node = this.zlmNodeService.getNode(Convert.toLong(body.getMediaServerId()));
            if (node.isPresent()) {

                JWT parseToken = JWTUtil.parseToken(authentication.get("sign"));
                Object streamId = parseToken.getPayload("streamId");

                Optional<StreamPullModel> streamPullModel = this.streamPullService.getMStreamPull(Convert.toLong(streamId));
                streamPullModel.ifPresent(pullModel -> this.streamPullService.resetStream(pullModel));
            }
        });


        //end=========
        map.put("code", 0);
        map.put("close", true);


        return map;
    }

    /**
     * 流量统计事件
     *
     * @param data
     * @return
     */
    @PostMapping("/on/flow/report")
    public ResponseBean on_flow_report(@RequestBody FlowReportBean data) {


        return ResponseBean.success();
    }

    /**
     * 根据参数鉴权
     *
     * @return
     */
    private Map<String, String> authentication(String param) {
        Map<String, String> stringMap = BaseUtil.paramConvertToMap(param);


        if (MapUtil.isEmpty(stringMap) || !stringMap.containsKey("sign")) {
            return null;
        }

        boolean sign = JwtUtils.verifyToken(stringMap.get("sign"));

        if (!sign) return null;

        return stringMap;
    }

}
