package org.sugar.media.sipserver.signal;

import gov.nist.javax.sip.RequestEventExt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.sip.RequestEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Date:2024/12/10 10:46:27
 * Author：Tobin
 * Description:
 */

@Component
public class SipSignalProcessor {

    private final Map<String, SipSignalHandler> handlers = new HashMap<>();

    @Autowired
    public SipSignalProcessor(List<SipSignalHandler> handlerList) {
        for (SipSignalHandler sipSignalHandler : handlerList) {
            String value = sipSignalHandler.getClass().getAnnotation(SipSignal.class).value();
            this.handlers.put(value, sipSignalHandler);
        }
    }

    public void processRequest(RequestEvent requestEvent) {
        RequestEventExt evtExt = (RequestEventExt) requestEvent;
        String method = evtExt.getRequest().getMethod();
        SipSignalHandler handler = handlers.get(method);

        if (handler != null) {
            handler.processMessage(evtExt);
        } else {
            System.out.println("No handler found for method: " + method);
        }
    }
}
