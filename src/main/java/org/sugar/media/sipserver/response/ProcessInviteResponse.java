package org.sugar.media.sipserver.response;

import cn.hutool.core.lang.Console;
import cn.hutool.core.util.ObjectUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import org.sugar.media.beans.gb.SsrcInfoBean;

import org.sugar.media.sipserver.manager.SsrcManager;
import org.sugar.media.sipserver.sender.SipRequestSender;
import org.sugar.media.sipserver.sender.SipSenderService;
import org.sugar.media.sipserver.utils.SipUtils;

import javax.sip.ResponseEvent;
import javax.sip.header.CallIdHeader;
import javax.sip.header.FromHeader;
import javax.sip.header.ToHeader;
import javax.sip.message.Response;

/**
 * Date:2024/12/23 14:38:00
 * Author：Tobin
 * Description: 处理设备invite 200的响应
 */

@Slf4j
@Service
public class ProcessInviteResponse {


    @Resource
    private SipUtils sipUtils;

    @Resource
    private SsrcManager ssrcManager;



    @Resource
    private SipSenderService sipSenderService;

    @Resource
    private SipRequestSender sipRequestSender;

    public void ProcessEvent(ResponseEvent responseEvent) {

        // 获取ssrc
        Response response = responseEvent.getResponse();


        String sdpContent = this.sipUtils.getXmlContent(response.getRawContent());
        Console.log(sdpContent, "++++++++");
        int index = sdpContent.indexOf("y=");
        if (index != -1) {
            // 如果找到了 y= 字段，获取值
            String ssrc = sdpContent.substring(index + 2,(index + 2)+11).trim();

            SsrcInfoBean ssrcInfoBean = this.ssrcManager.getSsrc(ssrc);
            Console.error(ssrcInfoBean,ssrc);
            if (ObjectUtil.isEmpty(ssrcInfoBean)) {
                log.warn("[ssrc]:invite ssrc暂未生成:{}", ssrc);
                return;
            }



            // 获取from 头
            FromHeader fromHeader = (FromHeader) response.getHeader(FromHeader.NAME);

            ssrcInfoBean.setFromAddr(fromHeader.getAddress().getURI().toString());

            ssrcInfoBean.setFromTag(fromHeader.getTag());
            // 获取 to 头
            ToHeader toHeader = (ToHeader) response.getHeader(ToHeader.NAME);
            ssrcInfoBean.setToAddr(toHeader.getAddress().getURI().toString());
            ssrcInfoBean.setToTag(toHeader.getTag());
            // 获取 call id
            CallIdHeader callIdHeader = (CallIdHeader) response.getHeader(CallIdHeader.NAME);

            ssrcInfoBean.setCallId(callIdHeader.getCallId());

            ssrcInfoBean.setDialog(responseEvent.getDialog());


            this.ssrcManager.updateSsrc(ssrc,ssrcInfoBean);

            // 发送200
            this.sipSenderService.sendAck(responseEvent);




        } else {
            log.warn("[ssrc]:invite ssrc不合法");
        }


    }
}
