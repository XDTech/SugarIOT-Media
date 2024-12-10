package org.sugar.media.sipserver.strategy.cmd;

import cn.hutool.core.lang.Console;
import gov.nist.javax.sip.RequestEventExt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.sugar.media.sipserver.sender.SipSenderService;

/**
 * Date:2024/12/10 13:46:20
 * Author：Tobin
 * Description: 目录事件回调
 */

@Component
@SipCmdType("Catalog")
public class CatalogEventService implements SipCmdHandler {


    @Autowired
    private SipSenderService sipSenderService;


    @Override
    public void processMessage(RequestEventExt evtExt) {
        Console.log("调用catalog事件============");

        Console.log(evtExt.getRequest().getContent(),"收到的请求");


        this.sipSenderService.sendOKMessage(evtExt);
    }
}
