package org.sugar.media.sipserver.request;

import cn.hutool.core.util.StrUtil;
import gov.nist.javax.sip.message.SIPRequest;
import jakarta.annotation.Resource;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.sugar.media.beans.gb.ChannelBean;
import org.sugar.media.beans.gb.DeviceBean;
import org.sugar.media.beans.gb.SsrcInfoBean;
import org.sugar.media.sipserver.manager.SipCacheService;
import org.sugar.media.sipserver.manager.SsrcManager;
import org.sugar.media.sipserver.utils.SipConfUtils;
import org.sugar.media.sipserver.utils.SipUtils;

import javax.sip.InvalidArgumentException;
import javax.sip.PeerUnavailableException;
import javax.sip.SipFactory;
import javax.sip.address.Address;
import javax.sip.address.SipURI;
import javax.sip.header.*;
import javax.sip.message.Request;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Date:2024/12/10 09:36:49
 * Author：Tobin
 * Description: 生成sip request
 */

@Slf4j
@Service
public class SipRequestService {


    @Autowired
    private SipCacheService sipCacheService;

    @Autowired
    private SipUtils sipUtils;

    @Autowired
    private SipConfUtils sipConfUtils;


    private final String catalogTemplate = """
            <?xml version="1.0"?>
            <Query>
                <CmdType>Catalog</CmdType>
                <SN>{}</SN>
                <DeviceID>{}</DeviceID>
            </Query>
            """;

    private final String ptzTemplate = """
            <?xml version="1.0"?>
            <Control>
                 <CmdType>DeviceControl</CmdType>
                  <SN>{}</SN>
                  <DeviceID>{}</DeviceID>
                  <PTZCmd>{}</PTZCmd>
            </Control>
             """;

    public Request createCatalog(DeviceBean device, CallIdHeader callIdHeader) throws ParseException, InvalidArgumentException, PeerUnavailableException {


        String content = StrUtil.format(catalogTemplate, this.sipCacheService.getNextCSeqFromRedis(device.getDeviceId()), device.getDeviceId());


        return this.createBase(device, Request.MESSAGE, content, this.sipUtils.getNewTag(), this.sipUtils.getNewTag(), callIdHeader);
    }

    public Request createDeviceInfo(DeviceBean device, CallIdHeader callIdHeader) throws ParseException, InvalidArgumentException, PeerUnavailableException {


        String xmlContent = """
                <?xml version="1.0"?>
                <Query>
                    <CmdType>DeviceInfo</CmdType>
                    <SN>{}</SN>
                    <DeviceID>{}</DeviceID>
                </Query>
                """;
        String content = StrUtil.format(xmlContent, this.sipCacheService.getNextCSeqFromRedis(device.getDeviceId()), device.getDeviceId());


        return this.createBase(device, Request.MESSAGE, content, this.sipUtils.getNewTag(), this.sipUtils.getNewTag(), callIdHeader);
    }


    /**
     * 发送订阅消息时，to header的tag无需设置
     *
     * @param device
     * @param callIdHeader
     * @return
     */
    @SneakyThrows
    public Request createCatalogSubscribe(DeviceBean device, CallIdHeader callIdHeader) {


        String content = StrUtil.format(catalogTemplate, this.sipCacheService.getNextCSeqFromRedis(device.getDeviceId()), device.getDeviceId());

        Request request = this.createBase(device, Request.SUBSCRIBE, content, this.sipUtils.getNewTag(), null, callIdHeader);

        // 设置 Expires
        ExpiresHeader expiresHeader = SipFactory.getInstance().createHeaderFactory().createExpiresHeader(3600);
        request.addHeader(expiresHeader);


        // Event
        EventHeader eventHeader = SipFactory.getInstance().createHeaderFactory().createEventHeader("Catalog");


        request.addHeader(eventHeader);


        return request;


    }


    @SneakyThrows
    public Request createPTZ(DeviceBean device, CallIdHeader callIdHeader,String ptzCmd) {


        String content = StrUtil.format(ptzTemplate
                , this.sipCacheService.getNextCSeqFromRedis(device.getDeviceId())
                , device.getDeviceId()
                ,ptzCmd
        );

        return this.createBase(device, Request.MESSAGE, content, this.sipUtils.getNewTag(), null, callIdHeader);


    }

    @SneakyThrows
    public Request cancelCatalogSubscribe(DeviceBean device, CallIdHeader callIdHeader) {

        Request request = this.createCatalogSubscribe(device, callIdHeader);

        request.removeHeader(ExpiresHeader.NAME);
        // 设置 Expires
        ExpiresHeader expiresHeader = SipFactory.getInstance().createHeaderFactory().createExpiresHeader(0);
        request.addHeader(expiresHeader);


        return request;


    }

//    @SneakyThrows
//    public Request createBye(InviteSessionBean inviteSessionBean) {
//
//
//        DeviceBean deviceBean = new DeviceBean();
//
//        deviceBean.setHost(inviteSessionBean.getDeviceHost());
//        deviceBean.setPort(inviteSessionBean.getDevicePort());
//        deviceBean.setDeviceId(inviteSessionBean.getChannelId());
//        deviceBean.setTransport(inviteSessionBean.getChannelId());
//
//        CallIdHeader callIdHeader = SipFactory.getInstance().createHeaderFactory().createCallIdHeader(inviteSessionBean.getCallId());
//        ArrayList<ViaHeader> viaHeaders = createViaHeaders(deviceBean, false);
//
//        SIPRequest request = (SIPRequest) this.createBase(deviceBean, Request.BYE, null, inviteSessionBean.getFromTag(), inviteSessionBean.getToTag(), callIdHeader);
//
//        request.setVia(viaHeaders);
//
//        return request;
//    }


    @SneakyThrows
    public Request createInvite(DeviceBean device, ChannelBean channelBean, CallIdHeader callIdHeader, String ssrc) {

        try {


            device.setDeviceId(channelBean.getChannelCode()); // 此处把通道id当成设备id使用

            Request request = this.createBase(device, Request.INVITE, null, this.sipUtils.getNewTag(), null, callIdHeader);


            // subject格式 媒体流发送者ID（通道id）:发送方媒体流序列号,媒体流接收者ID（sip域）:接收方媒体流序列号
            //  String ssrc = ssrcManager.createPlaySsrc(ssrcInfoBean);
            String subject = StrUtil.format("{}:{},{}:{}", channelBean.getChannelCode(), ssrc, this.sipConfUtils.getId(), 0);
            SubjectHeader subjectHeader = SipFactory.getInstance().createHeaderFactory().createSubjectHeader(subject);
            request.addHeader(subjectHeader);

            String sdp = StrUtil.format("""
                    v=0
                    o={} 0 0 IN IP4 {}
                    s=Play
                    u={}:0
                    c=IN IP4 {}
                    t=0 0
                    m=video {} RTP/AVP 96 97 98 99
                    a=recvonly
                    a=rtpmap:96 PS/90000
                    a=rtpmap:98 H264/90000
                    a=rtpmap:97 MPEG4/90000
                    a=rtpmap:99 H265/90000
                    y={}
                    """, this.sipConfUtils.getId(), device.getNodeHost(), channelBean.getChannelCode(), device.getNodeHost(), device.getNodePort(), ssrc);

            ContentTypeHeader contentTypeHeader = SipFactory.getInstance().createHeaderFactory().createContentTypeHeader("APPLICATION", "SDP");
            request.setContent(sdp, contentTypeHeader);


            return request;

        } catch (Exception e) {
            return null;
        }


    }


    /**
     * 创建基本消息的request
     *
     * @param device
     * @param method
     * @param content
     * @param callIdHeader
     * @return
     * @throws PeerUnavailableException
     * @throws ParseException
     * @throws InvalidArgumentException
     */
    private Request createBase(DeviceBean device, String method, String content, String fromTag, String toTag, CallIdHeader callIdHeader) throws PeerUnavailableException, ParseException, InvalidArgumentException {
        SipURI requestURI = createRequestURI(device);
        ArrayList<ViaHeader> viaHeaders = createViaHeaders(device, true);
        FromHeader fromHeader = createFromHeader(fromTag);
        ContactHeader contact = createContact(fromHeader.getAddress());

        ToHeader toHeader = createToHeader(device, toTag);
        MaxForwardsHeader maxForwards = createMaxForwardsHeader();
        CSeqHeader cSeqHeader = this.createCSeqHeader(device.getDeviceId(), method);
        UserAgentHeader userAgent = this.createUserAgent();

        Request request = SipFactory.getInstance().createMessageFactory().createRequest(requestURI, method, callIdHeader, cSeqHeader, fromHeader, toHeader, viaHeaders, maxForwards);


        request.setHeader(contact);


        if (StrUtil.isNotBlank(content)) {
            ContentTypeHeader contentTypeHeader = SipFactory.getInstance().createHeaderFactory().createContentTypeHeader("Application", "MANSCDP+xml");
            request.setContent(content, contentTypeHeader);
        }

        request.addHeader(userAgent);

        return request;

    }

    private SipURI createRequestURI(DeviceBean device) throws ParseException, PeerUnavailableException {
        return SipFactory.getInstance().createAddressFactory().createSipURI(device.getDeviceId(), device.getHost() + ":" + device.getPort());
    }

    private ArrayList<ViaHeader> createViaHeaders(DeviceBean device, boolean rPort) throws ParseException, InvalidArgumentException, PeerUnavailableException {
        ArrayList<ViaHeader> viaHeaders = new ArrayList<>();


        String branch = this.sipUtils.getNewViaBranch();
        ViaHeader viaHeader = SipFactory.getInstance().createHeaderFactory().createViaHeader(sipConfUtils.getIp(), sipConfUtils.getPort(), device.getTransport(), branch);
        if (rPort) {
            viaHeader.setRPort();
        }
        viaHeaders.add(viaHeader);
        return viaHeaders;
    }

    private FromHeader createFromHeader(String fromTag) throws ParseException, PeerUnavailableException {
        SipURI fromSipURI = SipFactory.getInstance().createAddressFactory().createSipURI(this.sipConfUtils.getId(), sipConfUtils.getIp() + ":" + sipConfUtils.getPort());
        Address fromAddress = SipFactory.getInstance().createAddressFactory().createAddress(fromSipURI);

        return SipFactory.getInstance().createHeaderFactory().createFromHeader(fromAddress, fromTag);
    }


    private ContactHeader createContact(Address address) throws PeerUnavailableException {
        return SipFactory.getInstance().createHeaderFactory().createContactHeader(address);

    }

    private ToHeader createToHeader(DeviceBean device, String toTag) throws ParseException, PeerUnavailableException {
        SipURI toSipURI = SipFactory.getInstance().createAddressFactory().createSipURI(device.getDeviceId(), device.getHost() + ":" + device.getPort());
        Address toAddress = SipFactory.getInstance().createAddressFactory().createAddress(toSipURI);
        return SipFactory.getInstance().createHeaderFactory().createToHeader(toAddress, toTag);
    }

    private MaxForwardsHeader createMaxForwardsHeader() throws InvalidArgumentException, PeerUnavailableException {
        return SipFactory.getInstance().createHeaderFactory().createMaxForwardsHeader(70);
    }


    private UserAgentHeader createUserAgent() throws PeerUnavailableException, ParseException {
        List<String> objects = new ArrayList<>();

        objects.add("sugar_media");
        HeaderFactory headerFactory = SipFactory.getInstance().createHeaderFactory();

        return headerFactory.createUserAgentHeader(objects);
    }

    /**
     * 设备国标id
     *
     * @param deviceId
     * @return
     * @throws InvalidArgumentException
     * @throws ParseException
     * @throws PeerUnavailableException
     */
    private CSeqHeader createCSeqHeader(String deviceId, String method) throws InvalidArgumentException, ParseException, PeerUnavailableException {
        return SipFactory.getInstance().createHeaderFactory().createCSeqHeader(this.sipCacheService.getNextCSeqFromRedis(deviceId), method);
    }

}
