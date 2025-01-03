package org.sugar.media.controller.gb;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.convert.Convert;
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
import org.sugar.media.utils.BaseUtil;
import org.sugar.media.utils.BeanConverterUtil;
import org.sugar.media.utils.LeastConnectionUtil;

import java.util.List;
import java.util.Map;
import java.util.Optional;
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

        if (channel.get().getStatus().equals(StatusEnum.offline)) {
            return ResponseEntity.ok(ResponseBean.fail("通道离线"));
        }

        Optional<DeviceModel> device = this.deviceService.getDevice(channel.get().getDeviceId());
        if (device.isEmpty()) return ResponseEntity.ok(ResponseBean.fail());

        if (!this.sipCacheService.isOnline(device.get().getDeviceId())) {
            return ResponseEntity.ok(ResponseBean.fail("设备离线"));
        }


        //
        SsrcInfoBean ssrcByCode = this.ssrcManager.getSsrcByCode(channel.get().getChannelCode());

        if (ObjectUtil.isNotEmpty(ssrcByCode)) {
            Console.log("该设备存在ssrc:{}", ssrcByCode);

            Optional<NodeModel> node = this.nodeService.getNode(ssrcByCode.getNodeId());
            return node.map(nodeModel -> ResponseEntity.ok(ResponseBean.success(this.channelService.genAddr(nodeModel, StrUtil.format("{}_{}", device.get().getDeviceId(), channel.get().getChannelCode()))))).orElseGet(() -> ResponseEntity.ok(ResponseBean.fail("节点不存在")));

        }
        NodeModel node = this.loadBalanceService.executeBalance();


        if (ObjectUtil.isEmpty(node)) return ResponseEntity.ok(ResponseBean.fail("暂无可用播放节点"));

        DeviceBean deviceBean = new DeviceBean();
        BeanUtil.copyProperties(device.get(), deviceBean);


        ChannelBean channelBean = new ChannelBean();

        BeanUtil.copyProperties(channel.get(), channelBean);


        deviceBean.setNodeHost(node.getIp());
        deviceBean.setNodePort(node.getRtpPort());
        deviceBean.setNodeId(node.getId());

        SsrcInfoBean ssrcInfoBean = new SsrcInfoBean();
        ssrcInfoBean.setDeviceCode(device.get().getDeviceId());
        ssrcInfoBean.setChannelCode(channelBean.getChannelCode());
        ssrcInfoBean.setChannelId(channel.get().getId());
        ssrcInfoBean.setName(channel.get().getChannelName());
        ssrcInfoBean.setDeviceHost(device.get().getHost());
        ssrcInfoBean.setDevicePort(device.get().getPort());
        ssrcInfoBean.setNodeId(node.getId());
        ssrcInfoBean.setTenantId(device.get().getTenantId());
        ssrcInfoBean.setTransport(device.get().getTransport());

        String playSsrc = this.ssrcManager.createPlaySsrc(ssrcInfoBean);
        ssrcInfoBean.setSsrc(playSsrc);

        Console.error(ssrcInfoBean.toString());
        this.sipRequestSender.sendInvite(deviceBean, channelBean, playSsrc);


        return ResponseEntity.ok(ResponseBean.success(this.channelService.genAddr(node, this.channelService.genGBStream(device.get().getDeviceId(), channel.get().getChannelCode()))));

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

}
