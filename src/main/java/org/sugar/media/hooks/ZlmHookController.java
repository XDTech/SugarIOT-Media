package org.sugar.media.hooks;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.TimeInterval;
import cn.hutool.core.lang.Console;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTUtil;
import cn.hutool.log.StaticLog;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;
import org.sugar.media.beans.ResponseBean;
import org.sugar.media.beans.SocketMsgBean;
import org.sugar.media.beans.gb.SsrcInfoBean;
import org.sugar.media.beans.hooks.zlm.*;
import org.sugar.media.enums.SocketMsgEnum;
import org.sugar.media.enums.StatusEnum;
import org.sugar.media.model.TenantModel;
import org.sugar.media.model.gb.DeviceModel;
import org.sugar.media.model.node.NodeModel;
import org.sugar.media.model.record.RecordModel;
import org.sugar.media.model.stream.StreamPullModel;
import org.sugar.media.server.WebSocketServer;
import org.sugar.media.service.media.MediaCacheService;
import org.sugar.media.service.media.ZlmApiService;
import org.sugar.media.service.node.ZlmNodeService;
import org.sugar.media.service.record.RecordService;
import org.sugar.media.service.stream.StreamPullService;
import org.sugar.media.service.tenant.TenantService;
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

    @Resource
    private TenantService tenantService;

    @Resource
    private RecordService recordService;


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

        StaticLog.info("{}：{}", "播放鉴权hook", body.toString());
//        if (body.getApp().equals("rtp")) {
//            return ResponseBean.success();
//        }

        if (body.getApp().equals("record")) return ResponseBean.success();
        Map<String, String> authentication = this.authentication(body.getParams());
        if (MapUtil.isEmpty(authentication)) return ResponseBean.fail();

        return ResponseBean.success();
    }


    @PostMapping("/stream/nof/found")
    public ResponseBean streamNotFound(@RequestBody OnPlayBean body) {
        if (body.getApp().equals("record")) return ResponseBean.success();
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
            // TODO:校验stream_id
            String hexString = body.getStream();


            // 通过ssrc 查找

            this.sipRequestSender.sendBye(hexString.split("_")[1]);

            map.put("code", 0);
            map.put("close", true);

            // 给设备发送bye消息
            return map;
        }

        // 重置拉流代理
        /// 查询是哪个node

        ThreadUtil.execute(() -> {
            Optional<NodeModel> node = this.zlmNodeService.getNode(Convert.toLong(body.getMediaServerId()));
            if (node.isPresent()) {

                StreamPullModel streamPullModel = this.streamPullService.onlyStream(body.getApp(), body.getStream());

                if (ObjectUtil.isNotEmpty(streamPullModel)) {
                    this.streamPullService.resetStream(streamPullModel);
                }
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


    @PostMapping("/on_record_mp4")
    public ResponseBean on_record_mp4(@RequestBody HookRecordBean data) {

        StaticLog.info("mp4录制回调{}", data);

        RecordModel recordModel = new RecordModel();

        BeanUtil.copyProperties(data, recordModel);

        // 解析tenant id
        String tenantCode = "0";
        if (data.getApp().equals("rtp")) {
            String deviceId = data.getStream().split("_")[0];

            tenantCode = deviceId.substring(0, 6);


        } else {
            // 通过截取方式获取租户code
            tenantCode = data.getFilePath().substring(data.getUrl().length() - 5).substring(0, 6);

            recordModel.setUrl(tenantCode + "/" + recordModel.getUrl());
        }


        TenantModel tenant = this.tenantService.getTenant(Convert.toInt(tenantCode));

        if (ObjectUtil.isNotEmpty(tenant)) {
            recordModel.setTenantId(tenant.getId());
            this.recordService.createRecord(recordModel);
        }


        return ResponseBean.success();
    }


    @PostMapping("/on_publish")
    public PublishAckBean on_publish(@RequestBody PublishBean data) {

        StaticLog.info("推流鉴权{}", data);

        // 判断rtp
        PublishAckBean publishAckBean = new PublishAckBean();
        publishAckBean.setCode(0);
        publishAckBean.setAddMuteAudio(true);
        publishAckBean.setContinuePushMs(10000);
        publishAckBean.setEnableAudio(true);
        publishAckBean.setEnableFmp4(true);
        publishAckBean.setEnableHls(false);
        publishAckBean.setEnableHlsFmp4(false);
        publishAckBean.setEnableMp4(true);
        publishAckBean.setEnableRtmp(true);
        publishAckBean.setEnableRtsp(true);
        publishAckBean.setEnableTs(true);
        publishAckBean.setModifyStamp(2);
        publishAckBean.setMp4AsPlayer(false);
        publishAckBean.setMp4MaxSecond(3600);
        switch (data.getApp()) {
            case "rtp" -> {
                String ssrc = BaseUtil.hex2ssrc(data.getStream());
                SsrcInfoBean ssrcInfoBean = this.ssrcManager.getSsrc(ssrc);
                if (ObjectUtil.isEmpty(ssrcInfoBean)) {
                    publishAckBean.setCode(-1);
                    publishAckBean.setMsg("auth error");
                    break;
                }


                //    publishAckBean.setAutoClose(false);
                publishAckBean.setStreamReplace(StrUtil.format("{}_{}", ssrcInfoBean.getDeviceCode(), ssrcInfoBean.getChannelCode()));

            }
            case "publish" -> {
                // 其他方式推流，鉴权app 统一为租户code，参数固定位sign=tenantCode


                // 
                Map<String, String> paramMap = BaseUtil.paramConvertToMap(data.getParams());
                if (!paramMap.containsKey("sign")) {
                    publishAckBean.setCode(-1);
                    publishAckBean.setMsg("auth error");

                    break;
                }
                TenantModel tenant = this.tenantService.getTenant(Convert.toInt(paramMap.get("sign")));
                if (ObjectUtil.isEmpty(tenant)) {
                    publishAckBean.setCode(-1);
                    publishAckBean.setMsg("auth error");

                    break;
                }

                publishAckBean.setStreamReplace(StrUtil.format("{}_{}", paramMap.get("sign"), data.getStream()));




            }

            default -> {
                publishAckBean.setCode(-1);
                publishAckBean.setMsg("auth error");
            }
        }
        StaticLog.info(publishAckBean.toString());
        return publishAckBean;
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
