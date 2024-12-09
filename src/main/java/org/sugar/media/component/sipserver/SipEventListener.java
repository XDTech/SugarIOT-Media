package org.sugar.media.component.sipserver;

import gov.nist.javax.sip.RequestEventExt;
import gov.nist.javax.sip.stack.MessageProcessor;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

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


    @Resource
    private AuthEventService authEventService;

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
        // 获取信令
        String method = evtExt.getRequest().getMethod();

        switch (method) {
            // 注册信令
            case "REGISTER" -> {
                this.authEventService.registerMessage(evtExt);
            }
            // 消息信令
            case "MESSAGE" -> {


            }
            // BYE 信令
            case "BYE" -> {


            }
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
        log.info("收到摄像机服务响应");
        Response response = responseEvent.getResponse();
        int status = response.getStatusCode();
        if( (status >= 200) && (status < 300) ) { //Success!
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
        log.warn("sip IO异常,{}",ioExceptionEvent.getHost());
    }

    @Override
    public void processTransactionTerminated(TransactionTerminatedEvent transactionTerminatedEvent) {
        log.warn("sip IO异常");
    }

    @Override
    public void processDialogTerminated(DialogTerminatedEvent dialogTerminatedEvent) {
        log.info("sip:事务中断");
    }
}
