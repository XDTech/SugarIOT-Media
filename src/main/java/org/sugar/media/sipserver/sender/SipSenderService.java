package org.sugar.media.sipserver.sender;

import cn.hutool.core.lang.Console;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.log.StaticLog;
import gov.nist.javax.sip.RequestEventExt;
import gov.nist.javax.sip.SipProviderImpl;
import gov.nist.javax.sip.header.SIPDate;
import gov.nist.javax.sip.header.SIPDateHeader;
import gov.nist.javax.sip.message.SIPRequest;
import jakarta.annotation.Resource;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.sugar.media.sipserver.SipServer;
import org.sugar.media.sipserver.request.SipRequestService;
import org.sugar.media.sipserver.utils.SipUtils;

import javax.sip.*;
import javax.sip.address.Address;
import javax.sip.address.AddressFactory;
import javax.sip.address.SipURI;
import javax.sip.header.*;
import javax.sip.message.MessageFactory;
import javax.sip.message.Request;
import javax.sip.message.Response;
import java.util.ArrayList;
import java.util.List;

/**
 * Date:2024/12/09 11:18:27
 * Author：Tobin
 * Description:
 */


@Slf4j
@Service
public class SipSenderService {


    @Resource
    private SipUtils sipUtils;

    @Resource
    private SipRequestService sipRequestService;


    /**
     * 发送401消息
     *
     * @param requestEventExt
     */
    public void sendAuthMessage(RequestEventExt requestEventExt) {


        try {
            SIPRequest request = (SIPRequest) requestEventExt.getRequest();
            MessageFactory message = this.createMessageFactory();

            if (ObjectUtil.isNotNull(message)) {


                Response response = message.createResponse(Response.UNAUTHORIZED, request);
                String tag = request.getToHeader().getTag();
                if (tag == null) {
                    request.getToHeader().setTag(this.sipUtils.getNewTag());
                }

                WWWAuthenticateHeader authHeader = SipFactory.getInstance().createHeaderFactory().createWWWAuthenticateHeader("Digest");

                authHeader.setParameter("realm", "sugar.media.com");
                authHeader.setParameter("nonce", "some_nonce_value");
                response.setHeader(authHeader);
                SipServer.udpSipProvider().sendResponse(response);
            }

        } catch (Exception e) {
            e.printStackTrace();
            log.info("发送认证消息失败");
        }

    }


    public void sendAuthErrorMsg(RequestEventExt requestEventExt) {


        try {
            SIPRequest request = (SIPRequest) requestEventExt.getRequest();
            MessageFactory message = this.createMessageFactory();
            String tag = request.getToHeader().getTag();
            if (tag == null) {
                request.getToHeader().setTag(this.sipUtils.getNewTag());
            }
            if (ObjectUtil.isNotNull(message)) {

                Response response = message.createResponse(Response.UNAUTHORIZED, request);

                response.setReasonPhrase("register error");
                SipServer.udpSipProvider().sendResponse(response);

            }

        } catch (Exception e) {
            e.printStackTrace();
            log.info("发送认证消息失败");
        }

    }


    public void sendOKMessage(RequestEventExt requestEventExt) {

        this.sendResponseMessage(requestEventExt, Response.OK);

    }


    public void sendResponseMessage(RequestEventExt requestEventExt, int status) {
        try {
            SIPRequest request = (SIPRequest) requestEventExt.getRequest();


            // ServerTransaction serverTransaction = this.createServerTransaction(requestEventExt, request);
            MessageFactory message = this.createMessageFactory();

            if (ObjectUtil.isNotNull(message)) {

                Response response = message.createResponse(status, request);
                response.setStatusCode(status);


                String tag = request.getToHeader().getTag();
                if (tag == null) {
                    request.getToHeader().setTag(this.sipUtils.getNewTag());
                }

                // 添加日期头
//                SIPDateHeader dateHeader = new SIPDateHeader();
//                dateHeader.setDate(new SIPDate());
//                response.setHeader(dateHeader);
                // 添加Expires
                if (response.getExpires() == null) {
                    response.setExpires(SipFactory.getInstance().createHeaderFactory().createExpiresHeader(3600));
                }


                response.addHeader(response.getExpires());


                // 添加Contact

                if (request.getContactHeader() == null) {
                    response.addHeader(SipFactory.getInstance().createHeaderFactory().createContactHeader());
                }

                SipServer.udpSipProvider().sendResponse(response);
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
            log.info("创建MessageFactory失败");
            return null;
        }
    }


    public void sendAck(ResponseEvent responseEvent) {
        try {
            // 获取响应中的必要头部
            Response response = responseEvent.getResponse();
            CSeqHeader cseqHeader = (CSeqHeader) response.getHeader(CSeqHeader.NAME);
            ToHeader toHeader = (ToHeader) response.getHeader(ToHeader.NAME);
            FromHeader fromHeader = (FromHeader) response.getHeader(FromHeader.NAME);
            CallIdHeader callIdHeader = (CallIdHeader) response.getHeader(CallIdHeader.NAME);

            // 创建ACK请求
            Request ackRequest = responseEvent.getDialog().createAck(cseqHeader.getSeqNumber());

            // 设置ACK请求头部信息
            ackRequest.setHeader(cseqHeader);
            ackRequest.setHeader(toHeader);
            ackRequest.setHeader(fromHeader);
            ackRequest.setHeader(callIdHeader);

            // 发送ACK请求
            responseEvent.getDialog().sendAck(ackRequest);

            log.info("已发送ACK请求");
        } catch (Exception e) {
            e.printStackTrace();
            log.error("发送ACK失败：" + e.getMessage());
        }
    }



    @SneakyThrows
    public void sendByeOkMsg(RequestEventExt requestEventExt) {
        try {
            SIPRequest request = (SIPRequest) requestEventExt.getRequest();
            CSeqHeader cseqHeader = (CSeqHeader) request.getHeader(CSeqHeader.NAME);
            ToHeader toHeader = (ToHeader) request.getHeader(ToHeader.NAME);
            FromHeader fromHeader = (FromHeader) request.getHeader(FromHeader.NAME);
            CallIdHeader callIdHeader = (CallIdHeader) request.getHeader(CallIdHeader.NAME);


            Dialog dialog = requestEventExt.getDialog();
            Request requestDialog = dialog.createRequest(Request.BYE);
            requestDialog.setHeader(cseqHeader);
            requestDialog.setHeader(toHeader);
            requestDialog.setHeader(fromHeader);
            requestDialog.setHeader(callIdHeader);

            Response response = SipFactory.getInstance().createMessageFactory().createResponse(Response.OK, request);

            dialog.sendReliableProvisionalResponse(response);


        } catch (Exception e) {
            e.printStackTrace();
            log.info("发送认证消息失败");
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
