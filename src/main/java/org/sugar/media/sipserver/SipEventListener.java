package org.sugar.media.sipserver;

import gov.nist.javax.sip.RequestEventExt;
import gov.nist.javax.sip.ResponseEventExt;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.sugar.media.sipserver.strategy.signal.SipSignalProcessor;

import javax.sip.*;
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

    /**
     * 收到设备的请求
     *
     * @param requestEvent -
     *                     requestEvent fired from the SipProvider to the SipListener
     *                     representing a Request received from the network.
     */
    @Override
    public void processRequest(RequestEvent requestEvent) {
        RequestEventExt evtExt = (RequestEventExt) requestEvent;
//      evtExt.getServerTransaction().;

        this.sipSignalProcessor.processRequest(evtExt);

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
        ResponseEventExt response1 = (ResponseEventExt) responseEvent.getResponse();

        log.info("收到摄像机服务响应");
        Response response = responseEvent.getResponse();
        int status = response.getStatusCode();
        if ((status >= 200) && (status < 300)) { //Success!
            log.info("sent-----success");
            return;
        }
        log.info("Previous message not sent: " + status);
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
        log.info("sip:事务中断");
    }
}
