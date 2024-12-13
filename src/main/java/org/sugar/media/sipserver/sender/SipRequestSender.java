package org.sugar.media.sipserver.sender;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.sugar.media.beans.gb.DeviceBean;
import org.sugar.media.sipserver.SipServer;
import org.sugar.media.sipserver.request.SipRequestService;

import javax.sip.SipProvider;
import javax.sip.header.CallIdHeader;
import javax.sip.message.Request;

/**
 * Date:2024/12/12 15:12:31
 * Author：Tobin
 * Description: 发送sip请求
 */

@Slf4j
@Service
public class SipRequestSender {


    @Autowired
    private SipRequestService sipRequestService;

    // 发送catalog

    @SneakyThrows
    public void sendCatalog(DeviceBean deviceBean) {
        SipProvider sipProvider = SipServer.udpSipProvider();
        CallIdHeader newCallId = sipProvider.getNewCallId();

        Request catalog = this.sipRequestService.createCatalog(deviceBean, newCallId);

        sipProvider.sendRequest(catalog);

    }


    // 发送device info

    @SneakyThrows
    public void sendDeviceInfo(DeviceBean deviceBean) {
        SipProvider sipProvider = SipServer.udpSipProvider();
        CallIdHeader newCallId = sipProvider.getNewCallId();

        Request catalog = this.sipRequestService.createDeviceInfo(deviceBean, newCallId);

        sipProvider.sendRequest(catalog);

    }


    // 发送 catalog订阅
    @SneakyThrows
    public void sendCatalogSubscribe(DeviceBean deviceBean) {
        SipProvider sipProvider = SipServer.udpSipProvider();
        CallIdHeader newCallId = sipProvider.getNewCallId();

        Request catalog = this.sipRequestService.createCatalogSubscribe(deviceBean, newCallId);

        sipProvider.sendRequest(catalog);
        log.info("发送{}，订阅", deviceBean.getDeviceId());



    }

    @SneakyThrows
    public void sendCancelCatalogSubscribe(DeviceBean deviceBean) {
        SipProvider sipProvider = SipServer.udpSipProvider();
        CallIdHeader newCallId = sipProvider.getNewCallId();

        Request catalog = this.sipRequestService.cancelCatalogSubscribe(deviceBean, newCallId);

        sipProvider.sendRequest(catalog);
        log.info("发送取消{}，订阅", deviceBean.getDeviceId());



    }


    // 发送invite
}
