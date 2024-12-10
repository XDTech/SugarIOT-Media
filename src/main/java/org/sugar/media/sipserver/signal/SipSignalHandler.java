package org.sugar.media.sipserver.signal;

import gov.nist.javax.sip.RequestEventExt;

/**
 * Date:2024/12/10 10:40:58
 * Author：Tobin
 * Description: 事件处理接口
 */
public interface SipSignalHandler {


    void processMessage(RequestEventExt evtExt);
}
