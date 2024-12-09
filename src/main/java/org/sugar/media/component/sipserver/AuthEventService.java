package org.sugar.media.component.sipserver;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.lang.Console;
import cn.hutool.core.util.ObjectUtil;
import gov.nist.javax.sip.RequestEventExt;
import gov.nist.javax.sip.address.AddressImpl;
import gov.nist.javax.sip.address.SipUri;
import gov.nist.javax.sip.clientauthutils.DigestServerAuthenticationHelper;
import gov.nist.javax.sip.message.SIPRequest;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import javax.sip.header.AuthorizationHeader;
import javax.sip.header.FromHeader;
import javax.sip.message.Request;


/**
 * Date:2024/12/09 11:01:14
 * Author：Tobin
 * Description: 28181  处理认证事件
 */

@Slf4j
@Service
public class AuthEventService {


    @Value("${sip.pwd}")
    private String pwd;


    @Resource
    private SipSenderService sipSenderService;

    @Resource
    private SipUtils sipUtils;

    public void registerMessage(RequestEventExt requestEventExt) {

        log.info("开始处理设备{}:{}注册请求", requestEventExt.getRemoteIpAddress(), requestEventExt.getRemotePort());
        SIPRequest request = (SIPRequest) requestEventExt.getRequest();


        AuthorizationHeader authHead = (AuthorizationHeader) request.getHeader(AuthorizationHeader.NAME);
        if (ObjectUtil.isEmpty(authHead)) {
            Console.log("发送401");
            this.sipSenderService.sendAuthMessage(requestEventExt);
            return;
        }

        try {

            String deviceId = this.sipUtils.getDeviceId(request);
            boolean verify = new helper().doAuthenticatePassword(request, pwd);

            if (!verify) {
                log.warn("{}设备验证失败", deviceId);
                this.sipSenderService.sendAuthErrorMsg(requestEventExt);
                return;
            }

            int expires = request.getExpires().getExpires();
            if (expires == 0) {
                log.warn("{}设备注销", deviceId);
            } else {
                log.info("{}设备上线", deviceId);
            }

            // 发送200消息

            this.sipSenderService.sendOKMessage(requestEventExt);


        } catch (Exception e) {
            e.printStackTrace();
        }

    }


}
