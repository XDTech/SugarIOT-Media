package org.sugar.media.sipserver.strategy.cmd;

import cn.hutool.core.lang.Console;
import gov.nist.javax.sip.RequestEventExt;
import org.springframework.stereotype.Component;

/**
 * Date:2024/12/10 13:46:20
 * Author：Tobin
 * Description: 目录事件回调
 */

@Component
@SipCmdType("Catalog")
public class CatalogEventService implements SipCmdHandler {
    @Override
    public void processMessage(RequestEventExt evtExt) {
        Console.log("调用保活事件");
    }
}
