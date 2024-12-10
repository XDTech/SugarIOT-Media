package org.sugar.media.sipserver.strategy.cmd;

import gov.nist.javax.sip.RequestEventExt;

/**
 * Date:2024/12/10 13:44:52
 * Author：Tobin
 * Description: message 回调事件处理
 */
public interface SipCmdHandler {

    void processMessage(RequestEventExt evtExt);
}
