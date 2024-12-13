package org.sugar.media.sipserver.strategy.cmd;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.Console;
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
import org.sugar.media.sipserver.utils.SipCacheService;
import org.sugar.media.sipserver.utils.SipUtils;

import javax.sip.message.Request;

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

        if (ObjectUtil.isEmpty(device)) {
            device = new DeviceModel();
        }

        String xmlContent = new String(request.getRawContent());

        // 存储设备发过来的信息
        device.setHost(request.getViaHost());
        device.setPort(request.getViaPort());
        device.setTransport(this.sipUtils.getTransportProtocol(request));


        this.sipUtils.getDeviceInfo(xmlContent, device);

        this.deviceService.createDevice(device);
        this.sipSenderService.sendOKMessage(evtExt);



        this.sipRequestSender.sendCatalog(sipDevice);



    }
}
