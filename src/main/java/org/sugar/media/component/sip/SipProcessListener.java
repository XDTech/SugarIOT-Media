package org.sugar.media.component.sip;

import gov.nist.javax.sip.RequestEventExt;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.sip.*;

/**
 * Date:2024/12/08 21:48:45
 * Author：Tobin
 * Description:
 */


@Slf4j
@Component
public class SipProcessListener implements SipListener {
    @Override
    public void processRequest(RequestEvent requestEvent) {
        RequestEventExt evtExt = (RequestEventExt) requestEvent;
        String requestAddress = evtExt.getRemoteIpAddress() + ":" + evtExt.getRemotePort();
        log.info("[注册请求] 开始处理: {}", requestAddress);

        log.info("{}",requestEvent.getRequest().getContent());
    }

    @Override
    public void processResponse(ResponseEvent responseEvent) {
        log.info(responseEvent.getResponse().getSIPVersion());
    }

    @Override
    public void processTimeout(TimeoutEvent timeoutEvent) {

    }

    @Override
    public void processIOException(IOExceptionEvent ioExceptionEvent) {

    }

    @Override
    public void processTransactionTerminated(TransactionTerminatedEvent transactionTerminatedEvent) {

    }

    @Override
    public void processDialogTerminated(DialogTerminatedEvent dialogTerminatedEvent) {

    }
}
