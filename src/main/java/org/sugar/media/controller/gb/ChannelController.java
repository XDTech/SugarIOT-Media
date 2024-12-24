package org.sugar.media.controller.gb;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.lang.Console;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.sugar.media.beans.ResponseBean;
import org.sugar.media.beans.gb.ChannelBean;
import org.sugar.media.beans.gb.DeviceBean;
import org.sugar.media.beans.gb.SsrcInfoBean;
import org.sugar.media.enums.StatusEnum;
import org.sugar.media.model.gb.DeviceChannelModel;
import org.sugar.media.model.gb.DeviceModel;
import org.sugar.media.model.node.NodeModel;
import org.sugar.media.service.LoadBalanceService;
import org.sugar.media.service.gb.ChannelService;
import org.sugar.media.service.gb.DeviceService;
import org.sugar.media.service.node.NodeService;
import org.sugar.media.sipserver.manager.SipCacheService;
import org.sugar.media.sipserver.manager.SsrcManager;
import org.sugar.media.sipserver.sender.SipRequestSender;
import org.sugar.media.sipserver.sender.SipSenderService;
import org.sugar.media.utils.BeanConverterUtil;
import org.sugar.media.utils.LeastConnectionUtil;

import java.util.List;
import java.util.Optional;

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



    @GetMapping("/list/{deviceId}")
    public ResponseEntity<?> getDevice(@PathVariable Long deviceId) {

        List<DeviceChannelModel> deviceChannelList = this.channelService.getDeviceChannelList(deviceId);

        List<ChannelBean> channelBeans = BeanConverterUtil.convertList(deviceChannelList, ChannelBean.class);
        return ResponseEntity.ok(ResponseBean.success(channelBeans));

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

        if (channel.isEmpty()) return ResponseEntity.ok(ResponseBean.fail());

        if(channel.get().getStatus().equals(StatusEnum.offline)){
            return ResponseEntity.ok(ResponseBean.fail("通道离线"));
        }

        Optional<DeviceModel> device = this.deviceService.getDevice(channel.get().getDeviceId());
        if (device.isEmpty()) return ResponseEntity.ok(ResponseBean.fail());

        if(!this.sipCacheService.isOnline(device.get().getDeviceId())){
            return ResponseEntity.ok(ResponseBean.fail("设备离线"));
        }
        // 查找是否已经存在

        //
        SsrcInfoBean ssrcByCode = this.ssrcManager.getSsrcByCode(channel.get().getChannelCode());

        if (ObjectUtil.isNotEmpty(ssrcByCode)) {
            Console.log("该设备存在ssrc:{}",ssrcByCode);
            return ResponseEntity.ok(ResponseBean.success(ssrcByCode.getSsrc()));
        }

        DeviceBean deviceBean = new DeviceBean();
        BeanUtil.copyProperties(device.get(), deviceBean);


        ChannelBean channelBean = new ChannelBean();

        BeanUtil.copyProperties(channel.get(), channelBean);


        NodeModel node = this.loadBalanceService.executeBalance();


        if (ObjectUtil.isEmpty(node)) return ResponseEntity.ok(ResponseBean.fail("暂无可用播放节点"));



        deviceBean.setNodeHost(node.getIp());
        deviceBean.setNodePort(node.getRtpPort());
        deviceBean.setNodeId(node.getId());

        SsrcInfoBean ssrcInfoBean = new SsrcInfoBean();
        ssrcInfoBean.setDeviceCode(device.get().getDeviceId());
        ssrcInfoBean.setChannelCode(channelBean.getChannelCode());
        ssrcInfoBean.setDeviceHost(device.get().getHost());
        ssrcInfoBean.setDevicePort(device.get().getPort());
        ssrcInfoBean.setNodeId(node.getId());
        ssrcInfoBean.setTransport(device.get().getTransport());

        String playSsrc = this.ssrcManager.createPlaySsrc(ssrcInfoBean);
        ssrcInfoBean.setSsrc(playSsrc);
        this.sipRequestSender.sendInvite(deviceBean, channelBean, playSsrc);


        return ResponseEntity.ok(ResponseBean.success(playSsrc));

    }

}
