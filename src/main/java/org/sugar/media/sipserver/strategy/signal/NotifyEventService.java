package org.sugar.media.sipserver.strategy.signal;

import cn.hutool.core.lang.Console;
import cn.hutool.core.util.ObjectUtil;
import gov.nist.javax.sip.RequestEventExt;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.sugar.media.sipserver.sender.SipSenderService;
import org.sugar.media.sipserver.strategy.cmd.SipCmdProcessor;
import org.sugar.media.sipserver.utils.SipUtils;

/**
 * Date:2024/12/10 11:01:35
 * Author：Tobin
 * Description: message 消息处理类
 */

@Component
@SipSignal("NOTIFY")
public class NotifyEventService implements SipSignalHandler {

    @Autowired
    private SipUtils sipUtils;

    @Resource
    private SipCmdProcessor sipCmdProcessor;

    @Resource
    private SipSenderService sipSenderService;

    @Override
    public void processMessage(RequestEventExt evtExt) {


        try {
            String xmlContent = this.sipUtils.getXmlContent(evtExt.getRequest().getRawContent());

            Console.log(xmlContent);
            if (ObjectUtil.isEmpty(xmlContent)) return;

            String cmdType = this.sipUtils.getCmdType(xmlContent);



            this.sipSenderService.sendByeOkMsg(evtExt);

        } catch (Exception e) {
            e.printStackTrace();
        }


    }
}
