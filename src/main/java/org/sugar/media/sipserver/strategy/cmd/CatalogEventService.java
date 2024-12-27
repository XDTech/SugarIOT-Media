package org.sugar.media.sipserver.strategy.cmd;

import cn.hutool.core.lang.Console;
import cn.hutool.core.util.ObjectUtil;
import gov.nist.javax.sip.RequestEventExt;
import gov.nist.javax.sip.message.SIPRequest;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.sugar.media.beans.gb.DeviceBean;
import org.sugar.media.model.gb.DeviceChannelModel;
import org.sugar.media.model.gb.DeviceModel;
import org.sugar.media.service.gb.ChannelService;
import org.sugar.media.service.gb.DeviceService;
import org.sugar.media.sipserver.sender.SipRequestSender;
import org.sugar.media.sipserver.sender.SipSenderService;
import org.sugar.media.sipserver.manager.SipCacheService;
import org.sugar.media.sipserver.utils.SipUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Date:2024/12/10 13:46:20
 * Author：Tobin
 * Description: 目录事件回调
 */

@Slf4j
@Component
@SipCmdType("Catalog")
public class CatalogEventService implements SipCmdHandler {


    @Autowired
    private SipSenderService sipSenderService;

    @Autowired
    private SipRequestSender sipRequestSender;

    @Resource
    private SipUtils sipUtils;

    @Resource
    private SipCacheService sipCacheService;

    @Resource
    private DeviceService deviceService;

    @Resource
    private ChannelService channelService;


    @Override
    public void processMessage(RequestEventExt evtExt) {
        Console.log("调用catalog事件============");


        SIPRequest request = (SIPRequest) evtExt.getRequest();
        String deviceId = this.sipUtils.getDeviceId((request));
        // 判断缓存是否存在

        DeviceBean sipDevice = this.sipCacheService.getSipDevice(deviceId);

        if (ObjectUtil.isEmpty(sipDevice)) {
            log.warn("[SIP服务] {}设备不在缓存中", deviceId);
            this.sipSenderService.sendAuthErrorMsg(evtExt);
            return;
        }

        // 更新catalog目录

        // 1. 查询设备
        DeviceModel device = this.deviceService.getDevice(deviceId);
        if (ObjectUtil.isEmpty(device)) {
            this.sipSenderService.sendAuthErrorMsg(evtExt);
            return;
        }

        // 查询设备通道
        List<DeviceChannelModel> deviceChannelList = this.channelService.getDeviceChannelList(device.getId());

        // :然后把解析出来的数据都存到数据库中
        String xmlContent = this.sipUtils.getXmlContent(request);
        String tenantCode = device.getDeviceId().substring(0, 6);
        List<DeviceChannelModel> channelModels = this.sipUtils.parseCatalog(xmlContent, device.getId(), device.getTenantId(), tenantCode, deviceChannelList);


        // 通过 code 字段去重，保留每个 code 的第一条记录
        Map<String, DeviceChannelModel> uniqueByCode = channelModels.stream().collect(Collectors.toMap(DeviceChannelModel::getChannelCode,  // key 是 code 字段
                channelModel -> channelModel,             // value 是 DeviceChannelModel 实例
                (existing, replacement) -> existing // 如果有重复的 code，保留第一个出现的条目
        ));

        channelModels = new ArrayList<>(uniqueByCode.values());

        this.channelService.createChannel(channelModels);

        this.sipSenderService.sendOKMessage(evtExt);

        // 更新完成后 订阅目录

        this.sipRequestSender.sendCatalogSubscribe(sipDevice);

    }
}
