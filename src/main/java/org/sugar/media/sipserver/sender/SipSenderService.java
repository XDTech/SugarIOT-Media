package org.sugar.media.sipserver.sender;

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
import org.sugar.media.sipserver.utils.SipUtils;

import javax.sip.ServerTransaction;
import javax.sip.SipFactory;
import javax.sip.SipProvider;
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
                authHeader.setParameter("realm", "example.com");
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

                Response response = message.createResponse(Response.FORBIDDEN, request);

                response.setReasonPhrase("register error");
                SipServer.udpSipProvider().sendResponse(response);

            }

        } catch (Exception e) {
            e.printStackTrace();
            log.info("发送认证消息失败");
        }

    }


    public void sendDeviceInfoRequest() {
        try {

            MessageFactory message = this.createMessageFactory();

            if (ObjectUtil.isNotNull(message)) {

                // 构建 SIP 请求行
                AddressFactory addressFactory = SipFactory.getInstance().createAddressFactory();
                MessageFactory messageFactory = SipFactory.getInstance().createMessageFactory();
                HeaderFactory headerFactory = SipFactory.getInstance().createHeaderFactory();
                ContentTypeHeader contentTypeHeader = headerFactory.createContentTypeHeader("Application", "MANSCDP+xml");

                SipURI requestURI = SipFactory.getInstance().createAddressFactory().createSipURI("34020000001110000004", "192.168.31.28");

                EventHeader catalog = headerFactory.createEventHeader("Catalog");

                List<String> objects = new ArrayList<>();

                objects.add("sugar");

                UserAgentHeader userAgentHeader = headerFactory.createUserAgentHeader(objects);

                // to
                SipURI toSipURI = SipFactory.getInstance().createAddressFactory().createSipURI("34020000001110000004", "192.168.31.28");
                Address toAddress = SipFactory.getInstance().createAddressFactory().createAddress(toSipURI);
                ToHeader toHeader = SipFactory.getInstance().createHeaderFactory().createToHeader(toAddress, null);


                // via
                ArrayList<ViaHeader> viaHeaders = new ArrayList<ViaHeader>();
                ViaHeader viaHeader = SipFactory.getInstance().createHeaderFactory().createViaHeader("192.168.31.65", 5060, "UDP", "2");
                viaHeader.setRPort();  // 使用 RPort 标志，告知对方设备回送到消息的实际来源地址
                viaHeaders.add(viaHeader);
                // from
                SipURI fromSipURI = SipFactory.getInstance().createAddressFactory().createSipURI("34020000002000000001", "192.168.31.65");
                Address fromAddress = SipFactory.getInstance().createAddressFactory().createAddress(fromSipURI);
                FromHeader fromHeader = SipFactory.getInstance().createHeaderFactory().createFromHeader(fromAddress, "3");


                String xmlContent = """
                        <?xml version="1.0"?>
                        <Query>
                            <CmdType>Catalog</CmdType>
                            <SN>1</SN>
                            <DeviceID>34020000001110000004</DeviceID>
                        </Query>
                        """;
                // 构建 MESSAGE 请求
                // Forwards
                MaxForwardsHeader maxForwards = SipFactory.getInstance().createHeaderFactory().createMaxForwardsHeader(70);
                // ceq
                CSeqHeader cSeqHeader = SipFactory.getInstance().createHeaderFactory().createCSeqHeader(1L, Request.MESSAGE);


                Request request = SipFactory.getInstance().createMessageFactory().createRequest(requestURI, Request.MESSAGE, SipServer.udpSipProvider().getNewCallId(), cSeqHeader, fromHeader, toHeader, viaHeaders, maxForwards);


                request.addHeader(catalog);
                request.addHeader(userAgentHeader);
                request.setContent(xmlContent, contentTypeHeader);
                SipServer.udpSipProvider().sendRequest(request);

            }

        } catch (Exception e) {
            e.printStackTrace();
            log.info("发送消息失败");
        }

    }

    public void sendOKMessage(RequestEventExt requestEventExt) {

        this.sendMessage(requestEventExt, Response.OK);

    }


    public void sendMessage(RequestEventExt requestEventExt, int status) {
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


}
