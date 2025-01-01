package org.sugar.media.sipserver.strategy.signal;

import cn.hutool.core.lang.Console;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import gov.nist.javax.sip.RequestEventExt;
import gov.nist.javax.sip.message.SIPRequest;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.sugar.media.beans.SocketMsgBean;
import org.sugar.media.beans.gb.DeviceBean;
import org.sugar.media.enums.SocketMsgEnum;
import org.sugar.media.enums.StatusEnum;
import org.sugar.media.server.WebSocketServer;
import org.sugar.media.sipserver.sender.SipRequestSender;
import org.sugar.media.sipserver.sender.SipSenderService;
import org.sugar.media.sipserver.manager.SipCacheService;
import org.sugar.media.sipserver.utils.SipConfUtils;
import org.sugar.media.sipserver.utils.SipUtils;
import org.sugar.media.sipserver.utils.helper;

import javax.sip.header.AuthorizationHeader;
import java.util.Date;


/**
 * Date:2024/12/09 11:01:14
 * Author：Tobin
 * Description: 28181  处理注册消息
 */

@Slf4j
@Component
@SipSignal("REGISTER")
public class RegisterEventService implements SipSignalHandler {


    @Resource
    private SipConfUtils sipConfUtils;


    @Resource
    private SipRequestSender sipRequestSender;

    @Resource
    private SipSenderService sipSenderService;

    @Resource
    private SipUtils sipUtils;

    @Resource
    private SipCacheService sipCacheService;
    String authTemplate = "[SIP认证] [{}] {}，设备ID:{}, 主机:{}, 端口:{}";

    public void processMessage(RequestEventExt requestEventExt) {
        try {

            SIPRequest request = (SIPRequest) requestEventExt.getRequest();
            int expires = request.getExpires().getExpires();
            String deviceId = this.sipUtils.getDeviceId(request);
            String tip = "设备注册";
            if (expires == 0) {
                tip = "设备注销";
            }

            //  log.info(authTemplate, tip, "收到设备认证请求", deviceId, request.getViaHost(), request.getViaPort());

            //: 1. 获取协议中的 设备id，在redis中比对是否注册， 没有注册的设备没法接入


            DeviceBean sipDevice = this.sipCacheService.getSipDevice(deviceId);

            if (ObjectUtil.isEmpty(sipDevice)) {
                // 不存在直接发送失败消息
                this.sipSenderService.sendAuthErrorMsg(requestEventExt);
                log.warn(authTemplate, tip, "认证失败:设备不存在", deviceId, request.getViaHost(), request.getViaPort());

                return;
            }


            AuthorizationHeader authHead = (AuthorizationHeader) request.getHeader(AuthorizationHeader.NAME);
            // 为空发送给设备401消息
            if (ObjectUtil.isEmpty(authHead)) {
                log.info(authTemplate, tip, "设备存在:发送401消息", deviceId, request.getViaHost(), request.getViaPort());
                this.sipSenderService.sendAuthMessage(requestEventExt);
                return;
            }


            // sip收到设备的认证回复，在这里鉴权
            // 1. 如果设备设置了密码，优先采用设备的
            String authPwd = sipConfUtils.getPwd();
            if (StrUtil.isNotBlank(sipDevice.getPwd())) {
                log.info(authTemplate, tip, "[采用设备密码鉴权]", deviceId, request.getViaHost(), request.getViaPort());

                authPwd = sipDevice.getPwd();
            } else {
                log.info(authTemplate, tip, "[采用系统密码鉴权]", deviceId, request.getViaHost(), request.getViaPort());
            }


            boolean verify = new helper().doAuthenticatePassword(request, authPwd);

            if (!verify) {
                log.warn("{}设备验证失败", deviceId);
                log.warn(authTemplate, tip, "鉴权失败:密码错误", deviceId, request.getViaHost(), request.getViaPort());
                this.sipSenderService.sendAuthErrorMsg(requestEventExt);
                return;
            }


            // 鉴权成功 进行注册
            // 无论注销还是注册 都要返回200

            this.sipSenderService.sendOKMessage(requestEventExt);


            if (expires == 0) {

                log.info(authTemplate, tip, "设备注销成功", deviceId, request.getViaHost(), request.getViaPort());
                //TODO:把设备下所有的通道踢下线？

            } else {

                log.info(authTemplate, tip, "设备上线成功", deviceId, request.getViaHost(), request.getViaPort());

                boolean online = this.sipCacheService.isOnline(deviceId);

                this.sipCacheService.setDeviceStatus(deviceId, StatusEnum.online.getStatus());
                // :发送ws消息
                if (!online) {
                    ThreadUtil.execute(() -> {
                        WebSocketServer.sendSystemMsg(new SocketMsgBean(SocketMsgEnum.gbOnline, new Date(), sipDevice.getName(),null));
                    });
                }


                DeviceBean deviceBean = new DeviceBean();
                deviceBean.setHost(request.getViaHost());
                deviceBean.setPort(request.getViaPort());
                deviceBean.setTransport(this.sipUtils.getTransportProtocol(request));
                deviceBean.setDeviceId(this.sipUtils.getDeviceId(request));
                Console.log("解析设备的bean{}", deviceBean.toString());
                this.sipRequestSender.sendDeviceInfo(deviceBean);


            }


        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
