package org.sugar.media.sipserver.signal;

import gov.nist.javax.sip.RequestEventExt;
import org.springframework.stereotype.Component;

/**
 * Date:2024/12/10 11:01:35
 * Author：Tobin
 * Description:
 */

@Component
@SipSignal("MESSAGE")
public class MessageEventService implements SipSignalHandler{
    @Override
    public void processMessage(RequestEventExt evtExt) {

    }
}
