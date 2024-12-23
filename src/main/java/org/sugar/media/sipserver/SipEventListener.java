package org.sugar.media.sipserver;

import cn.hutool.core.lang.Console;
import gov.nist.javax.sip.RequestEventExt;
import gov.nist.javax.sip.ResponseEventExt;
import gov.nist.javax.sip.message.SIPMessage;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.sugar.media.sipserver.response.ProcessInviteResponse;
import org.sugar.media.sipserver.sender.SipRequestSender;
import org.sugar.media.sipserver.sender.SipSenderService;
import org.sugar.media.sipserver.strategy.signal.SipSignalProcessor;

import javax.sip.*;
import javax.sip.header.CSeqHeader;
import javax.sip.message.Response;

/**
 * Date:2024/12/08 21:48:45
 * Author：Tobin
 * Description:
 */


/**
 * 收到的消息
 */
@Slf4j
@Component
public class SipEventListener implements SipListener {


    /**
     * 策略模式
     */
    @Resource
    private SipSignalProcessor sipSignalProcessor;


    @Resource
    private SipSenderService sipSenderService;

    @Resource
    private SipRequestSender sipRequestSender;


    @Resource
    private ProcessInviteResponse processInviteResponse;


    /**
     * 收到设备的请求
     *
     * @param requestEvent -
     *                     requestEvent fired from the SipProvider to the SipListener
     *                     representing a Request received from the network.
     */
    @Override
    public void processRequest(RequestEvent requestEvent) {
        try {
            log.warn("处理：{}", requestEvent.getRequest().getMethod());
            RequestEventExt evtExt = (RequestEventExt) requestEvent;
//      evtExt.getServerTransaction().;

            SIPMessage sipMessage = (SIPMessage) evtExt.getRequest();

            this.sipSignalProcessor.processRequest(evtExt);
        } catch (Exception e) {
            e.printStackTrace();
        }


    }


    /**
     * 收到设备的响应
     *
     * @param responseEvent -
     *                      the responseEvent fired from the SipProvider to the
     *                      SipListener representing a Response received from the network.
     */
    @Override
    public void processResponse(ResponseEvent responseEvent) {
        try {
            log.info("收到摄像机服务响应");
            Response response = responseEvent.getResponse();
            int status = response.getStatusCode();
            // Success
            if (((status >= Response.OK) && (status < Response.MULTIPLE_CHOICES)) || status == Response.UNAUTHORIZED) {
                CSeqHeader cseqHeader = (CSeqHeader) responseEvent.getResponse().getHeader(CSeqHeader.NAME);
                String method = cseqHeader.getMethod();
                log.info("response method:" + method);
                if (method.equals("INVITE")) {
                    // 回复ack

                    this.processInviteResponse.ProcessEvent(responseEvent);



                }


            } else if ((status >= Response.TRYING) && (status < Response.OK)) {
                // 增加其它无需回复的响应，如101、180等
                log.info("status:" + status);
            } else {
                log.warn("接收到失败的response响应！status：" + status + ",message:" + response.getReasonPhrase());
                if (responseEvent.getDialog() != null) {
                    responseEvent.getDialog().delete();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();

        }

    }

    @Override
    public void processTimeout(TimeoutEvent timeoutEvent) {

        log.warn("sip 超时");

    }

    @Override
    public void processIOException(IOExceptionEvent ioExceptionEvent) {
        log.warn("sip IO异常,{}", ioExceptionEvent.getHost());
    }

    @Override
    public void processTransactionTerminated(TransactionTerminatedEvent event) {
        log.warn("事务终止: {}", event.isServerTransaction() ? "服务器事务" : "客户端事务");
        if (event.getServerTransaction() != null) {
            log.warn("终止事务 ID: {}", event.getServerTransaction().getBranchId());
        }
    }

    @Override
    public void processDialogTerminated(DialogTerminatedEvent dialogTerminatedEvent) {

        log.info("sip:事务中断:{}", dialogTerminatedEvent.getDialog().getDialogId());
    }


}
