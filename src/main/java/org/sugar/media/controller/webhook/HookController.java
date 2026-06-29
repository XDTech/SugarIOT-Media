package org.sugar.media.controller.webhook;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.lang.Console;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONUtil;
import jakarta.annotation.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.sugar.media.beans.MRecordBean;
import org.sugar.media.beans.ResponseBean;
import org.sugar.media.beans.gb.SsrcInfoBean;
import org.sugar.media.beans.hooks.zlm.BaseBean;
import org.sugar.media.model.TenantModel;
import org.sugar.media.model.gb.DeviceChannelModel;
import org.sugar.media.model.gb.DeviceModel;
import org.sugar.media.model.node.NodeModel;
import org.sugar.media.model.record.RecordModel;
import org.sugar.media.model.stream.StreamPullModel;
import org.sugar.media.model.stream.StreamPushModel;
import org.sugar.media.service.StreamService;
import org.sugar.media.service.gb.ChannelService;
import org.sugar.media.service.gb.DeviceService;
import org.sugar.media.service.media.MediaCacheService;
import org.sugar.media.service.media.ZlmApiService;
import org.sugar.media.service.node.NodeService;
import org.sugar.media.service.record.RecordService;
import org.sugar.media.service.stream.StreamPullService;
import org.sugar.media.service.stream.StreamPushService;
import org.sugar.media.service.tenant.TenantService;
import org.sugar.media.sipserver.manager.SsrcManager;
import org.sugar.media.utils.AesUtil;
import org.sugar.media.utils.BeanConverterUtil;
import org.sugar.media.utils.SecurityUtils;
import oshi.jna.platform.windows.NtDll;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Date:2025/03/30 11:02:20
 * Author：Tobin
 * Description:
 */

@RestController
@RequestMapping("/hook")
public class HookController {


    @Resource
    private StreamService streamService;

    @Resource
    private TenantService tenantService;


    @Resource
    private ChannelService channelService;

    @Resource
    private StreamPushService streamPushService;

    @Resource
    private NodeService nodeService;

    @Resource
    private ZlmApiService zlmApiService;

    @Resource
    private StreamPullService streamPullService;

    @Resource
    private DeviceService deviceService;

    @Resource
    private SsrcManager ssrcManager;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private RecordService recordService;

    @Resource
    private MediaCacheService mediaCacheService;


    // 获取在线的设备
    @GetMapping("/stream/list")
    public ResponseEntity<?> getList(@RequestParam Integer code) {


        TenantModel tenant = this.tenantService.getTenant(code);
        if (ObjectUtil.isEmpty(tenant)) return ResponseEntity.ok(ResponseBean.fail());


        return ResponseEntity.ok(ResponseBean.success(this.streamService.getOnlineStreamList(tenant.getId())));
    }

    // 通过鉴权码播放摄像头，返回视频流地址

    @GetMapping("/stream/addr")
    public ResponseEntity<?> getStreamAddr(@RequestParam String secret) {


        String aesDecrypt = AesUtil.aesDecrypt(secret);

        JSON parse = JSONUtil.parse(aesDecrypt);


        String types = (String) parse.getByPath("types");

        Long id = (Long) parse.getByPath("id");
        Map<String, List<String>> map = new HashMap<>();
        switch (types) {
            case "live" -> map = this.streamService.getPushStreamAddr(id);
            case "proxy" -> map = this.streamService.getPullStreamAddr(id);
            case "rtp" -> {
                Optional<DeviceChannelModel> channel = this.channelService.getChannel(id);
                if (channel.isPresent()) {
                    map = this.channelService.inviteChannel(channel.get());
                }

            }
        }
        return ResponseEntity.ok(ResponseBean.success(map));
    }

    // 开启录制
    @GetMapping("/stream/start/record")
    public ResponseEntity<?> startRecord(@RequestParam String secret, @RequestParam(required = false) String customPath) {


        try {
            String aesDecrypt = AesUtil.aesDecrypt(secret);

            JSON parse = JSONUtil.parse(aesDecrypt);

            Console.log(customPath);

            String types = (String) parse.getByPath("types");

            Long id = (Long) parse.getByPath("id");
            Map<String, List<String>> map = new HashMap<>();
            String app = "";

            String stream = "";

            NodeModel nodeModel = new NodeModel();
            switch (types) {
                case "live" -> {

                    Optional<StreamPushModel> streamPush = this.streamPushService.getStreamPush(id);
                    if (streamPush.isPresent()) {
                        Optional<NodeModel> node = this.nodeService.getNode(streamPush.get().getNodeId());
                        if (node.isEmpty()) return ResponseEntity.ok(ResponseBean.fail());
                        nodeModel = node.get();
                        app = streamPush.get().getApp();
                        stream = streamPush.get().getStream();
                    }

                    break;
                }
                case "proxy" -> {
                    // 需要先拉流


                    Optional<StreamPullModel> mStreamPull = this.streamPullService.getMStreamPull(id);

                    if (mStreamPull.isEmpty()) return ResponseEntity.ok(ResponseBean.fail());

                    Optional<NodeModel> node = this.nodeService.getNode(mStreamPull.get().getNodeId());
                    if (node.isEmpty()) return ResponseEntity.ok(ResponseBean.fail());

                    nodeModel = node.get();
                    app = mStreamPull.get().getApp();
                    stream = mStreamPull.get().getStream();

                    this.streamPullService.playStreamPull(mStreamPull.get());

                    break;
                }
                case "rtp" -> {
                    Optional<DeviceChannelModel> channel = this.channelService.getChannel(id);
                    if (channel.isPresent()) {
                        Map<String, List<String>> addrMap = this.channelService.inviteChannel(channel.get());


                        if (ObjectUtil.isEmpty(addrMap)) {
                            Console.error("发送rtp失败，暂无返回地址");
                            return ResponseEntity.ok(ResponseBean.fail());
                        }
                        SsrcInfoBean ssrcByCode = this.ssrcManager.getSsrcByCode(channel.get().getChannelCode());
                        Optional<DeviceModel> device = this.deviceService.getDevice(channel.get().getDeviceId());
                        app = "rtp";
                        stream = this.channelService.genGBStream(device.get().getDeviceId(), channel.get().getChannelCode());
                        Optional<NodeModel> node = this.nodeService.getNode(ssrcByCode.getNodeId());
                        nodeModel = node.get();
                    }

                }
            }

            if (app.equals("rtp")) {
                // rtp流需要在流改变事件里调用录像，因为是异步的
                this.stringRedisTemplate.opsForValue().set(StrUtil.format("{}_{}", app, stream), customPath);

                return ResponseEntity.ok(ResponseBean.success());

            }
            BaseBean baseBean = this.zlmApiService.startRecord(app, stream, nodeModel, customPath);

            Console.log("{}========", baseBean.toString());

            if (!baseBean.isResult()) return ResponseEntity.ok(ResponseBean.fail());
            return ResponseEntity.ok(ResponseBean.success());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok(ResponseBean.fail());
        }

    }


    @GetMapping("/stream/stop/record")
    public ResponseEntity<?> stopRecord(@RequestParam String secret) {

        try {
            String aesDecrypt = AesUtil.aesDecrypt(secret);

            JSON parse = JSONUtil.parse(aesDecrypt);


            String types = (String) parse.getByPath("types");

            Long id = (Long) parse.getByPath("id");
            Map<String, List<String>> map = new HashMap<>();
            String app = "";

            String stream = "";

            NodeModel nodeModel = new NodeModel();
            switch (types) {
                case "live" -> {

                    Optional<StreamPushModel> streamPush = this.streamPushService.getStreamPush(id);
                    if (streamPush.isPresent()) {
                        Optional<NodeModel> node = this.nodeService.getNode(streamPush.get().getNodeId());
                        if (node.isEmpty()) return ResponseEntity.ok(ResponseBean.fail());
                        nodeModel = node.get();
                        app = streamPush.get().getApp();
                        stream = streamPush.get().getStream();
                    }

                    break;
                }
                case "proxy" -> {


                    Optional<StreamPullModel> mStreamPull = this.streamPullService.getMStreamPull(id);

                    if (mStreamPull.isEmpty()) return ResponseEntity.ok(ResponseBean.fail());

                    Optional<NodeModel> node = this.nodeService.getNode(mStreamPull.get().getNodeId());
                    if (node.isEmpty()) return ResponseEntity.ok(ResponseBean.fail());

                    nodeModel = node.get();
                    app = mStreamPull.get().getApp();
                    stream = mStreamPull.get().getStream();


                    break;
                }
                case "rtp" -> {
                    Optional<DeviceChannelModel> channel = this.channelService.getChannel(id);
                    if (channel.isPresent()) {
                        Optional<DeviceModel> device = this.deviceService.getDevice(channel.get().getDeviceId());
                        app = "rtp";
                        stream = this.channelService.genGBStream(device.get().getDeviceId(), channel.get().getChannelCode());
                        SsrcInfoBean ssrcByCode = this.ssrcManager.getSsrcByCode(channel.get().getChannelCode());
                        Optional<NodeModel> node = this.nodeService.getNode(ssrcByCode.getNodeId());
                        nodeModel = node.get();

                    }

                }
            }

            BaseBean baseBean = this.zlmApiService.closeRecord(app, stream, nodeModel);

            if (!baseBean.isResult()) return ResponseEntity.ok(ResponseBean.fail());
            return ResponseEntity.ok(ResponseBean.success());
        } catch (Exception e) {
            return ResponseEntity.ok(ResponseBean.fail());
        }

    }


    //

    @GetMapping("/mp4/record/list")
    public ResponseEntity<?> getMRecordPageList(@RequestParam String secret, @RequestParam String path,

                                                @RequestParam(required = false) Long startDate, @RequestParam(required = false) Long endDate

    ) {


        String aesDecrypt = AesUtil.aesDecrypt(secret);

        JSON parse = JSONUtil.parse(aesDecrypt);


        String types = (String) parse.getByPath("types");

        Long tenantId = null;
        String app = null;
        String stream = null;


        Long id = (Long) parse.getByPath("id");
        Map<String, List<String>> map = new HashMap<>();
        switch (types) {
            case "live" -> {


                Optional<StreamPushModel> streamPush = this.streamPushService.getStreamPush(id);

                if (streamPush.isPresent()) {
                    tenantId = streamPush.get().getTenantId();
                    app = streamPush.get().getApp();
                    stream = streamPush.get().getStream();
                }


            }
            case "proxy" -> {

                Optional<StreamPullModel> mStreamPull = this.streamPullService.getMStreamPull(id);
                if (mStreamPull.isPresent()) {
                    tenantId = mStreamPull.get().getTenantId();
                    app = mStreamPull.get().getApp();
                    stream = mStreamPull.get().getStream();
                }

            }
            case "rtp" -> {
                Optional<DeviceChannelModel> channel = this.channelService.getChannel(id);
                if (channel.isPresent()) {
                    tenantId = channel.get().getTenantId();
                    app = "rtp";
                    Optional<DeviceModel> device = this.deviceService.getDevice(channel.get().getDeviceId());
                    if (device.isEmpty()) return null;
                    stream = StrUtil.format("{}_{}", device.get().getDeviceId(), channel.get().getChannelCode());
                }

            }
        }

        List<RecordModel> mRecordList = this.recordService.getRecordList(startDate, endDate, tenantId, app, stream);


        List<MRecordBean> mRecordBeans = BeanConverterUtil.convertList(mRecordList, MRecordBean.class);

        List<NodeModel> nodeAll = this.nodeService.getNodeAll();

        Map<Long, NodeModel> nodeModelMap = nodeAll.stream().collect(Collectors.toMap(NodeModel::getId, s -> s));

        mRecordBeans = mRecordBeans.stream().peek(s -> {
            NodeModel nodeModel = nodeModelMap.get(Convert.toLong(s.getMediaServerId()));
            // 筛选路径

            if (ObjectUtil.isNotEmpty(nodeAll) && this.mediaCacheService.isOnline(nodeModel.getId())) {
                s.setPlayUrl(StrUtil.format("http://{}:{}{}", nodeModel.getRemoteIp(), nodeModel.getHttpPort(), s.getFilePath().substring(ZlmApiService.savePathPrefix.length())));
            }

        }).filter(s -> s.getFolder().contains(path)).collect(Collectors.toList());


        return ResponseEntity.ok(ResponseBean.success(mRecordBeans));


    }
}
