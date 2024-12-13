package org.sugar.media.sipserver.strategy.cmd;

import cn.hutool.core.lang.Console;
import cn.hutool.core.util.ObjectUtil;
import gov.nist.javax.sip.RequestEventExt;
import gov.nist.javax.sip.message.SIPRequest;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.support.TaskUtils;
import org.springframework.stereotype.Component;
import org.sugar.media.beans.gb.DeviceBean;
import org.sugar.media.sipserver.sender.SipRequestSender;
import org.sugar.media.sipserver.sender.SipSenderService;
import org.sugar.media.sipserver.utils.SipCacheService;
import org.sugar.media.sipserver.utils.SipUtils;

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

        this.sipSenderService.sendOKMessage(evtExt);

        // 更新完成后 订阅目录

        this.sipRequestSender.sendCatalogSubscribe(sipDevice);


       // this.sipRequestSender.sendCancelCatalogSubscribe(sipDevice);
    }
}
