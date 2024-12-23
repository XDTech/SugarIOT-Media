package org.sugar.media.sipserver.strategy.signal;

import cn.hutool.core.lang.Console;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import gov.nist.javax.sip.RequestEventExt;
import gov.nist.javax.sip.message.SIPRequest;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.sugar.media.beans.gb.DeviceBean;
import org.sugar.media.beans.gb.SsrcInfoBean;
import org.sugar.media.service.gb.ChannelService;
import org.sugar.media.sipserver.manager.SipCacheService;
import org.sugar.media.sipserver.manager.SsrcManager;
import org.sugar.media.sipserver.sender.SipRequestSender;
import org.sugar.media.sipserver.sender.SipSenderService;
import org.sugar.media.sipserver.strategy.cmd.SipCmdProcessor;
import org.sugar.media.sipserver.utils.SipUtils;


@Component
@SipSignal("BYE")
public class ByeEventService implements SipSignalHandler {


    @Resource
    private SipSenderService sipSenderService;

    @Resource
    private SipUtils sipUtils;

    @Resource
    private SipRequestSender sipRequestSender;

    @Resource
    private SsrcManager ssrcManager;

    @Resource
    private SipCacheService sipCacheService;

    @Resource
    private ChannelService channelService;


    @Override
    public void processMessage(RequestEventExt evtExt) {


        try {

            // 通过call id查找invite session
            SIPRequest sipRequest = (SIPRequest) evtExt.getRequest();

            String deviceId = this.sipUtils.getDeviceId(sipRequest);

            Console.log("=================channel================={}",deviceId);
            SsrcInfoBean invite = this.ssrcManager.getSsrcByCode(deviceId);


            if (ObjectUtil.isEmpty(invite)) {
                Console.log("=================设备为空=================");
                return;
            }
            Console.log("=================收到摄像头bye request=================");


            this.sipSenderService.sendByeOkMsg(evtExt);


            this.ssrcManager.releaseSsrc(invite.getSsrc(), invite.getChannelCode());

        } catch (Exception e) {
            e.printStackTrace();
        }


    }
}
