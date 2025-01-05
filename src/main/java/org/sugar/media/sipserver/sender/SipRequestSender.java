package org.sugar.media.sipserver.sender;

import cn.hutool.core.lang.Console;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.log.StaticLog;
import jakarta.annotation.Resource;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.sugar.media.beans.gb.ChannelBean;
import org.sugar.media.beans.gb.DeviceBean;
import org.sugar.media.beans.gb.SsrcInfoBean;
import org.sugar.media.sipserver.SipServer;
import org.sugar.media.sipserver.manager.SsrcManager;
import org.sugar.media.sipserver.request.SipRequestService;

import javax.sip.*;
import javax.sip.address.URI;
import javax.sip.header.*;
import javax.sip.message.Request;
import javax.sip.message.Response;

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


    @Resource
    private SsrcManager ssrcManager;

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

        Request deviceInfo = this.sipRequestService.createDeviceInfo(deviceBean, newCallId);

        sipProvider.sendRequest(deviceInfo);

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
    public void sendPtzControl(DeviceBean deviceBean,String ptzCmd) {
        SipProvider sipProvider = SipServer.udpSipProvider();
        CallIdHeader newCallId = sipProvider.getNewCallId();

        Request catalog = this.sipRequestService.createPTZ(deviceBean, newCallId,ptzCmd);

        sipProvider.sendRequest(catalog);
        log.info("发送{}，ptz", deviceBean.getDeviceId());


    }


    @SneakyThrows
    public void sendCancelCatalogSubscribe(DeviceBean deviceBean) {
        SipProvider sipProvider = SipServer.udpSipProvider();
        CallIdHeader newCallId = sipProvider.getNewCallId();

        Request catalog = this.sipRequestService.cancelCatalogSubscribe(deviceBean, newCallId);

        sipProvider.sendRequest(catalog);
        log.info("发送取消{}，订阅", deviceBean.getDeviceId());


    }


    @SneakyThrows
    public void sendInvite(DeviceBean deviceBean, ChannelBean channelBean, String ssrc) {

        SipProvider sipProvider = SipServer.udpSipProvider();
        CallIdHeader newCallId = sipProvider.getNewCallId();
        Request invite = this.sipRequestService.createInvite(deviceBean, channelBean, newCallId, ssrc);

        ViaHeader viaHeader = (ViaHeader)invite.getHeader(ViaHeader.NAME);
        Console.error("发送invite方式{}", viaHeader.getTransport());

        sipProvider.sendRequest(invite);

    }


    @SneakyThrows
    public void sendBye(SsrcInfoBean ssrcInfoBean) {
        try {

            Dialog dialog = ssrcInfoBean.getDialog();
            Request byeRequest = dialog.createRequest(Request.BYE);
            Console.log(byeRequest.toString());
            ClientTransaction clientTransaction = SipServer.udpSipProvider().getNewClientTransaction(byeRequest);
            dialog.sendRequest(clientTransaction);


            Console.log("发送bye");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 清除缓存

            this.ssrcManager.releaseSsrc(ssrcInfoBean.getSsrc(), ssrcInfoBean.getChannelCode());

        }


    }

    public void sendBye(String channelCode) {
        // 通过ssrc 查找
        SsrcInfoBean ssrcInfoBean = this.ssrcManager.getSsrcByCode(channelCode);
        if (ObjectUtil.isNotEmpty(ssrcInfoBean)) {

            Console.log(ssrcInfoBean.toString());

            this.sendBye(ssrcInfoBean);


        }
    }



}
