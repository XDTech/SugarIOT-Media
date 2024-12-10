package org.sugar.media.sipserver.strategy.cmd;

import cn.hutool.core.lang.Console;
import gov.nist.javax.sip.RequestEventExt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.sugar.media.sipserver.sender.SipSenderService;
import org.sugar.media.sipserver.strategy.signal.SipSignal;
import org.sugar.media.sipserver.strategy.signal.SipSignalHandler;

import javax.sip.RequestEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Date:2024/12/10 13:47:17
 * Author：Tobin
 * Description:
 */

@Component
public class SipCmdProcessor {

    @Autowired
    private SipSenderService sipSenderService;

    private final Map<String, SipCmdHandler> handlers = new HashMap<>();

    @Autowired
    public SipCmdProcessor(List<SipCmdHandler> handlerList) {
        for (SipCmdHandler sipCmdHandler : handlerList) {
            String value = sipCmdHandler.getClass().getAnnotation(SipCmdType.class).value();
            this.handlers.put(value, sipCmdHandler);
        }
    }

    public void processCmdType(String cmdType, RequestEventExt evtExt) {

        SipCmdHandler handler = handlers.get(cmdType);

        if (handler != null) {
            handler.processMessage(evtExt);
        } else {
            System.out.println("No CMD found for method: " + cmdType);
            // 任何情况都回复200
            this.sipSenderService.sendOKMessage(evtExt);
        }
    }
}
