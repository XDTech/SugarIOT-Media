package org.sugar.media.component.sipserver;

import cn.hutool.core.util.ObjectUtil;
import gov.nist.javax.sip.RequestEventExt;
import gov.nist.javax.sip.header.SIPDate;
import gov.nist.javax.sip.header.SIPDateHeader;
import gov.nist.javax.sip.message.SIPRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.sip.ServerTransaction;
import javax.sip.SipFactory;
import javax.sip.SipProvider;
import javax.sip.header.ContactHeader;
import javax.sip.header.HeaderFactory;
import javax.sip.header.WWWAuthenticateHeader;
import javax.sip.message.MessageFactory;
import javax.sip.message.Request;
import javax.sip.message.Response;

/**
 * Date:2024/12/09 11:18:27
 * Author：Tobin
 * Description:
 */


@Slf4j
@Service
public class SipSenderService {


    /**
     * 发送401消息
     *
     * @param requestEventExt
     */
    public void sendAuthMessage(RequestEventExt requestEventExt) {


        try {
            SIPRequest request = (SIPRequest) requestEventExt.getRequest();
            ServerTransaction serverTransaction = this.createServerTransaction(requestEventExt, request);
            MessageFactory message = this.createMessageFactory();

            if (ObjectUtil.isNotNull(serverTransaction) && ObjectUtil.isNotNull(message)) {


                Response response = message.createResponse(Response.UNAUTHORIZED, request);

                WWWAuthenticateHeader authHeader = SipFactory.getInstance().createHeaderFactory().createWWWAuthenticateHeader("Digest");
                authHeader.setParameter("realm", "example.com");
                authHeader.setParameter("nonce", "some_nonce_value");
                response.setHeader(authHeader);
                serverTransaction.sendResponse(response);
            }

        } catch (Exception e) {
            e.printStackTrace();
            log.info("发送认证消息失败");
        }

    }


    public void sendAuthErrorMsg(RequestEventExt requestEventExt) {


        try {
            SIPRequest request = (SIPRequest) requestEventExt.getRequest();
            ServerTransaction serverTransaction = this.createServerTransaction(requestEventExt, request);
            MessageFactory message = this.createMessageFactory();

            if (ObjectUtil.isNotNull(serverTransaction) && ObjectUtil.isNotNull(message)) {

                Response response = message.createResponse(Response.FORBIDDEN, request);

                response.setReasonPhrase("password error");
                serverTransaction.sendResponse(response);
            }

        } catch (Exception e) {
            e.printStackTrace();
            log.info("发送认证消息失败");
        }

    }


    public void sendOKMessage(RequestEventExt requestEventExt) {


        try {
            SIPRequest request = (SIPRequest) requestEventExt.getRequest();
            ServerTransaction serverTransaction = this.createServerTransaction(requestEventExt,request);
            MessageFactory message = this.createMessageFactory();

            if (ObjectUtil.isNotNull(serverTransaction) && ObjectUtil.isNotNull(message)) {

                Response response = message.createResponse(Response.OK, request);
                // 添加日期头
                SIPDateHeader dateHeader = new SIPDateHeader();
                dateHeader.setDate(new SIPDate());
                response.setHeader(dateHeader);
                // 添加Expires
                response.addHeader(request.getExpires());
                // 添加Contact
                response.addHeader(request.getHeader(ContactHeader.NAME));

                serverTransaction.sendResponse(response);
            }

        } catch (Exception e) {
            e.printStackTrace();
            log.info("发送认证消息失败");
        }

    }


    private MessageFactory createMessageFactory() {
        try {
            return SipFactory.getInstance().createMessageFactory();

        } catch (Exception e) {
            e.printStackTrace();
            log.info("创建ServerTransaction失败");
            return null;
        }
    }

    private ServerTransaction createServerTransaction(RequestEventExt requestEventExt, SIPRequest request) {
        try {
            SipProvider source = (SipProvider) requestEventExt.getSource();

            ServerTransaction serverTransaction = requestEventExt.getServerTransaction();

            if (serverTransaction == null) {
                serverTransaction = source.getNewServerTransaction(request);
            }

            return serverTransaction;

        } catch (Exception e) {

            e.printStackTrace();
            log.info("创建ServerTransaction失败");
            return null;

        }
    }


}
