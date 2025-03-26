package org.sugar.media.controller.gb;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.TimeInterval;
import cn.hutool.core.lang.Console;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.log.StaticLog;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.sugar.media.beans.ResponseBean;
import org.sugar.media.beans.gb.ChannelBean;
import org.sugar.media.beans.gb.DeviceBean;
import org.sugar.media.beans.gb.SsrcInfoBean;
import org.sugar.media.beans.hooks.zlm.CloseStreamBean;
import org.sugar.media.beans.hooks.zlm.StreamInfoBean;
import org.sugar.media.enums.AutoCloseEnum;
import org.sugar.media.enums.StatusEnum;
import org.sugar.media.model.gb.DeviceChannelModel;
import org.sugar.media.model.gb.DeviceModel;
import org.sugar.media.model.node.NodeModel;
import org.sugar.media.model.stream.StreamPushModel;
import org.sugar.media.security.UserSecurity;
import org.sugar.media.service.LoadBalanceService;
import org.sugar.media.service.gb.ChannelService;
import org.sugar.media.service.gb.DeviceService;
import org.sugar.media.service.media.ZlmApiService;
import org.sugar.media.service.node.NodeService;
import org.sugar.media.service.node.ZlmNodeService;
import org.sugar.media.service.stream.StreamPushService;
import org.sugar.media.sipserver.manager.SipCacheService;
import org.sugar.media.sipserver.manager.SsrcManager;
import org.sugar.media.sipserver.sender.SipRequestSender;
import org.sugar.media.sipserver.sender.SipSenderService;
import org.sugar.media.sipserver.utils.SipUtils;
import org.sugar.media.utils.BaseUtil;
import org.sugar.media.utils.BeanConverterUtil;
import org.sugar.media.utils.LeastConnectionUtil;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Date:2024/12/14 17:24:17
 * Author：Tobin
 * Description: 国标通道
 */

@RestController
@RequestMapping("/gb/channel")
@Validated
public class ChannelController {

    @Resource
    private ChannelService channelService;

    @Resource
    private DeviceService deviceService;

    @Resource
    private SipRequestSender sipRequestSender;

    @Resource
    private NodeService nodeService;

    @Resource
    private SsrcManager ssrcManager;

    @Resource
    private SipCacheService sipCacheService;

    @Resource
    private LoadBalanceService loadBalanceService;


    @Resource
    private UserSecurity userSecurity;

    @Resource
    private ZlmApiService zlmApiService;

    @Resource
    private ZlmNodeService zlmNodeService;

    @Resource
    private StreamPushService streamPushService;

    @Resource
    private SipUtils sipUtils;


    @GetMapping("/list/{deviceId}")
    public ResponseEntity<?> getDevice(@PathVariable Long deviceId) {

        List<DeviceChannelModel> deviceChannelList = this.channelService.getDeviceChannelList(deviceId);

        List<ChannelBean> channelBeans = BeanConverterUtil.convertList(deviceChannelList, ChannelBean.class);
        return ResponseEntity.ok(ResponseBean.success(channelBeans));

    }


    @GetMapping("/page/list")
    public ResponseEntity<?> getChannelPageList(@RequestParam Integer pi, @RequestParam Integer ps, @RequestParam(required = false) String name) {


        Page<DeviceChannelModel> channelPageList = this.channelService.getChannelPageList(pi, ps, name, this.userSecurity.getCurrentTenantId());


        List<DeviceModel> deviceList = this.deviceService.getDeviceList(this.userSecurity.getCurrentTenantId());

        Map<Long, DeviceModel> modelMap = deviceList.stream().collect(Collectors.toMap(DeviceModel::getId, d -> d));


        List<ChannelBean> channelBeans = BeanConverterUtil.convertList(channelPageList.getContent(), ChannelBean.class);
        List<StreamInfoBean> mediaListAll = this.zlmNodeService.getMediaListAll();

        List<StreamPushModel> streamPushList = this.streamPushService.getStreamPushList();

        Map<Long, StreamPushModel> pushModelMap = streamPushList.stream().filter(s -> s.getRelevanceId() != null).collect(Collectors.toMap(StreamPushModel::getRelevanceId, s -> s));
        channelBeans = channelBeans.stream().peek(c -> {
            c.setPlayStatus(StatusEnum.offline);
            DeviceModel deviceModel = modelMap.get(c.getDeviceId());
            if (ObjectUtil.isNotEmpty(deviceModel)) {
                c.setDeviceName(deviceModel.getName());
                c.setDeviceCode(deviceModel.getDeviceId());
            }

            // 查询推流表

            StreamPushModel streamPushModel = pushModelMap.get(c.getId());

            if (ObjectUtil.isNotEmpty(streamPushModel)) {
                Optional<StreamInfoBean> streamInfo = mediaListAll.stream().filter(s -> s.getApp().equals(streamPushModel.getApp())).filter(s -> s.getStream().equals(streamPushModel.getStream())).filter(s -> s.getNodeId().equals(streamPushModel.getNodeId())).findFirst();
                if (streamInfo.isPresent()) {
                    c.setPlayStatus(StatusEnum.online);
                }
            }


        }).toList();

        return ResponseEntity.ok(ResponseBean.success(channelPageList.getTotalElements(), channelBeans));

    }

    /**
     * 返回播放地址
     *
     * @param channelId
     * @return
     */
    @GetMapping("/invite/{channelId}")
    public ResponseEntity<?> sendInvite(@PathVariable Long channelId) {

        Optional<DeviceChannelModel> channel = this.channelService.getChannel(channelId);

        if (channel.isEmpty()) return ResponseEntity.ok(ResponseBean.fail("通道不存在"));


        Map<String, List<String>> stringListMap = this.channelService.inviteChannel(channel.get());

        if (ObjectUtil.isEmpty(stringListMap)) return ResponseEntity.ok(ResponseBean.fail("设备离线，播放失败"));


        return ResponseEntity.ok(ResponseBean.success(stringListMap));

    }


    @PostMapping("/send/bye/{channelId}")
    public ResponseEntity<?> sendBy(@PathVariable Long channelId) {
        Optional<DeviceChannelModel> channel = this.channelService.getChannel(channelId);
        if (channel.isEmpty()) return ResponseEntity.ok(ResponseBean.fail());
        SsrcInfoBean ssrcInfoBean = this.ssrcManager.getSsrcByCode(channel.get().getChannelCode());

        if (ObjectUtil.isEmpty(ssrcInfoBean)) return ResponseEntity.ok(ResponseBean.fail("该流暂未播放"));

        // send bye
        this.sipRequestSender.sendBye(ssrcInfoBean);

        // close stream
        if (ssrcInfoBean.getNodeId() != null) {
            Optional<NodeModel> node = this.nodeService.getNode(ssrcInfoBean.getNodeId());
            if (node.isPresent()) {
                String gbStream = this.channelService.genGBStream(ssrcInfoBean.getDeviceCode(), ssrcInfoBean.getChannelCode());
                CloseStreamBean rtp = this.zlmApiService.closeSteam("rtp", gbStream, node.get());

                StaticLog.info(rtp.toString());
            }
        }


        return ResponseEntity.ok(ResponseBean.success());
    }


    @DeleteMapping("/{channelId}")
    public ResponseEntity<?> deleteChannel(@PathVariable Long channelId) {

        Optional<DeviceChannelModel> channel = this.channelService.getChannel(channelId);

        if (channel.isEmpty()) {
            return ResponseEntity.ok(ResponseBean.fail());
        }


        this.channelService.deleteChannel(channel.get());


        return ResponseEntity.ok(ResponseBean.success());


    }

    @GetMapping("/{channelId}")
    public ResponseEntity<?> getChannel(@PathVariable Long channelId) {

        Optional<DeviceChannelModel> channel = this.channelService.getChannel(channelId);

        return channel.<ResponseEntity<?>>map(channelModel -> ResponseEntity.ok(ResponseBean.success(channelModel))).orElseGet(() -> ResponseEntity.ok(ResponseBean.fail()));


    }


    @PutMapping
    public ResponseEntity<?> updateChannel(@RequestParam Long id, @RequestParam String autoClose, @RequestParam Boolean enablePull, @RequestParam Boolean enableMp4) {

        Optional<DeviceChannelModel> channel = this.channelService.getChannel(id);

        if (channel.isEmpty()) return ResponseEntity.ok(ResponseBean.fail());


        channel.get().setEnableMp4(enableMp4);
        channel.get().setEnablePull(enablePull);
        channel.get().setAutoClose(AutoCloseEnum.valueOf(autoClose));


        this.channelService.updateChannel(channel.get());

        return ResponseEntity.ok(ResponseBean.success());
    }

    @GetMapping("/ptz/{deviceCode}/{channelCode}/{speed}")
    public ResponseEntity<?> ptzChannel(@PathVariable String deviceCode, @RequestParam List<String> directions, @PathVariable String channelCode, @PathVariable Integer speed) {

        TimeInterval timer = DateUtil.timer();


        DeviceBean deviceBean = this.sipCacheService.getSipDevice(deviceCode);
        if (ObjectUtil.isEmpty(deviceBean)) {
            return ResponseEntity.ok(ResponseBean.fail());
        }


        deviceBean.setDeviceId(channelCode);
        deviceBean.setHost(deviceBean.getHost());
        deviceBean.setPort(deviceBean.getPort());
        deviceBean.setTransport(deviceBean.getTransport());


        Console.log("{}====查询耗时", timer.intervalRestart());
        byte[] bytes = this.sipUtils.genPtzCommand(directions, speed);

        Console.log("{}====生成耗时", timer.intervalRestart());

        this.sipRequestSender.sendPtzControl(deviceBean, this.sipUtils.byteArrayToHexString(bytes));

        Console.log("{}====发送耗时", timer.intervalRestart());

        return ResponseEntity.ok(ResponseBean.success());


    }


}
