package org.sugar.media.sipserver.strategy.cmd;

import cn.hutool.core.util.ObjectUtil;
import gov.nist.javax.sip.RequestEventExt;
import gov.nist.javax.sip.message.SIPRequest;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.sugar.media.beans.gb.DeviceBean;
import org.sugar.media.model.gb.DeviceModel;
import org.sugar.media.service.gb.DeviceService;
import org.sugar.media.sipserver.sender.SipRequestSender;
import org.sugar.media.sipserver.sender.SipSenderService;
import org.sugar.media.sipserver.manager.SipCacheService;
import org.sugar.media.sipserver.utils.SipUtils;

import java.util.Date;

/**
 * Date:2024/12/10 13:46:20
 * Author：Tobin
 * Description: 设备信息回调
 */

@Slf4j
@Component
@SipCmdType("DeviceInfo")
public class DeviceInfoEventService implements SipCmdHandler {

    @Autowired
    private SipSenderService sipSenderService;

    @Resource
    private DeviceService deviceService;

    @Resource
    private SipUtils sipUtils;

    @Resource
    private SipCacheService sipCacheService;

    @Resource
    private SipRequestSender sipRequestSender;

    @Override
    public void processMessage(RequestEventExt evtExt) {

        SIPRequest request = (SIPRequest) evtExt.getRequest();
        String deviceId = this.sipUtils.getDeviceId((request));
        // 判断缓存是否存在

        DeviceBean sipDevice = this.sipCacheService.getSipDevice(deviceId);


        if (ObjectUtil.isEmpty(sipDevice)) {
            log.warn("[SIP服务] {}设备不在缓存中", deviceId);
            return;
        }
        // 获取设备信息后，应该每次都要更新设备信息
        DeviceModel device = this.deviceService.getDevice(deviceId);

        if (ObjectUtil.isNull(device)) {
            device = new DeviceModel();
        }

        String xmlContent =this.sipUtils.getXmlContent(request);

        // 存储设备发过来的信息
        device.setHost(request.getViaHost());
        device.setPort(request.getViaPort());
        device.setTransport(this.sipUtils.getTransportProtocol(request));
        device.setSyncTime(new Date());

        this.sipUtils.getDeviceInfo(xmlContent, device);
        this.deviceService.createDevice(device);
        // 给设备发送200消息
        this.sipSenderService.sendOKMessage(evtExt);

        // 发完之后，要发送catalog消息 获取设备目录
        this.sipRequestSender.sendCatalog(this.sipCacheService.getSipDevice(deviceId));


    }
}
