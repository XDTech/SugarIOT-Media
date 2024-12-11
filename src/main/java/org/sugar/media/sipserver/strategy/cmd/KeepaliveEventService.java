package org.sugar.media.sipserver.strategy.cmd;

import cn.hutool.core.lang.Console;
import gov.nist.javax.sip.RequestEventExt;
import gov.nist.javax.sip.message.SIPRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.sugar.media.sipserver.sender.SipSenderService;
import org.sugar.media.sipserver.utils.SipUtils;

/**
 * Date:2024/12/10 13:46:20
 * Author：Tobin
 * Description: 保活事件回调
 */


@Slf4j
@Component
@SipCmdType("Keepalive")
public class KeepaliveEventService implements SipCmdHandler {

    @Autowired
    private SipSenderService sipSenderService;

    @Autowired
    private SipUtils sipUtils;

    @Override
    public void processMessage(RequestEventExt evtExt) {
        log.info("调用保活事件");

        String deviceId = this.sipUtils.getDeviceId((SIPRequest) evtExt.getRequest());
        Console.log("[保活事件] 设备：{}", deviceId);


        this.sipSenderService.sendOKMessage(evtExt);
    }
}
