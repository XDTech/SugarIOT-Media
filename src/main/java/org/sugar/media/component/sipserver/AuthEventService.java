package org.sugar.media.component.sipserver;

import cn.hutool.core.lang.Console;
import cn.hutool.core.util.ObjectUtil;
import gov.nist.javax.sip.RequestEventExt;
import gov.nist.javax.sip.address.AddressImpl;
import gov.nist.javax.sip.address.SipUri;
import gov.nist.javax.sip.clientauthutils.DigestServerAuthenticationHelper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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


    @Resource
    private SipSenderService sipSenderService;

    public void registerMessage(RequestEventExt requestEventExt) {

        log.info("开始处理设备{}:{}注册请求", requestEventExt.getRemoteIpAddress(), requestEventExt.getRemotePort());
        Request request = requestEventExt.getRequest();

        AuthorizationHeader authHead = (AuthorizationHeader) request.getHeader(AuthorizationHeader.NAME);
        if (ObjectUtil.isEmpty(authHead)) {
            Console.log("发送401");
            this.sipSenderService.sendAuthMessage(requestEventExt, request);
            return;
        }

        try {
            FromHeader fromHeader = (FromHeader) request.getHeader(FromHeader.NAME);
            AddressImpl address = (AddressImpl) fromHeader.getAddress();
            SipUri uri = (SipUri) address.getURI();
            //设备ID(保留)
            String deviceId = uri.getUser();
            log.info("{}设备", deviceId);
            boolean verify = new helper().doAuthenticatePassword(request, "smile100");

            if (!verify) {
                log.warn("{}设备验证失败", deviceId);
                return;
            }

            int expires = request.getExpires().getExpires();
            if (expires == 0) {

                log.warn("{}设备注销", deviceId);
            } else {

                log.info("{}设备上线", deviceId);


                this.sipSenderService.sendOKMessage(requestEventExt, request);
            }


        } catch (Exception e) {
            e.printStackTrace();
        }

    }


}
