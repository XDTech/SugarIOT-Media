package org.sugar.media.sipserver.strategy.cmd;

import cn.hutool.core.lang.Console;
import gov.nist.javax.sip.RequestEventExt;
import org.springframework.stereotype.Component;

/**
 * Date:2024/12/10 13:46:20
 * Author：Tobin
 * Description: 设备信息回调
 */

@Component
@SipCmdType("DeviceInfo")
public class DeviceInfoEventService implements SipCmdHandler {
    @Override
    public void processMessage(RequestEventExt evtExt) {
        Console.log("调用DeviceInfo事件");
    }
}
