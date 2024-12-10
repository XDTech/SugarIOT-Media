package org.sugar.media.sipserver.signal;

import cn.hutool.core.lang.Console;
import cn.hutool.core.util.ObjectUtil;
import gov.nist.javax.sip.RequestEventExt;
import gov.nist.javax.sip.message.SIPRequest;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.sugar.media.sipserver.sender.SipSenderService;
import org.sugar.media.sipserver.utils.SipUtils;
import org.sugar.media.sipserver.utils.helper;

import javax.sip.ListeningPoint;
import javax.sip.SipProvider;
import javax.sip.header.AuthorizationHeader;


/**
 * Date:2024/12/09 11:01:14
 * Author：Tobin
 * Description: 28181  处理认证事件
 */

@Slf4j
@Component
@SipSignal("REGISTER")
public class RegisterEventService implements SipSignalHandler {


    @Value("${sip.pwd}")
    private String pwd;


    @Resource
    private SipSenderService sipSenderService;

    @Resource
    private SipUtils sipUtils;

    public void processMessage(RequestEventExt requestEventExt) {

        log.info("开始处理设备{}:{}注册请求", requestEventExt.getRemoteIpAddress(), requestEventExt.getRemotePort());
        SIPRequest request = (SIPRequest) requestEventExt.getRequest();

        log.info("ip{}:{}", request.getViaHost(), request.getViaPort());


        AuthorizationHeader authHead = (AuthorizationHeader) request.getHeader(AuthorizationHeader.NAME);
        // 为空发送给设备401消息
        if (ObjectUtil.isEmpty(authHead)) {
            Console.log("发送401");
            this.sipSenderService.sendAuthMessage(requestEventExt);
            return;
        }

        try {

            // sip收到设备的认证回复，在这里鉴权
            String deviceId = this.sipUtils.getDeviceId(request);
            boolean verify = new helper().doAuthenticatePassword(request, pwd);

            if (!verify) {
                log.warn("{}设备验证失败", deviceId);
                this.sipSenderService.sendAuthErrorMsg(requestEventExt);
                return;
            }


            // 鉴权成功 进行注册
            // 无论注销还是注册 都要返回200
            int expires = request.getExpires().getExpires();
            this.sipSenderService.sendOKMessage(requestEventExt);


            if (expires == 0) {
                log.warn("{}设备注销", deviceId);
            } else {
                log.info("{}设备上线", deviceId);
                SipProvider source = (SipProvider) requestEventExt.getSource();
                ListeningPoint[] listeningPoints = source.getListeningPoints();
                log.info("{}设备上线", listeningPoints[0].getIPAddress());

                this.sipSenderService.sendDeviceInfoRequest(source, requestEventExt);
            }


        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    public void messageInfo(RequestEventExt requestEventExt) {
        this.sipSenderService.sendOKMessage(requestEventExt);
    }
}
