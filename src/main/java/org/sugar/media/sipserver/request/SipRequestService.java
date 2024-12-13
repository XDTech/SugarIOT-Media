package org.sugar.media.sipserver.request;

import cn.hutool.core.util.StrUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.sugar.media.beans.gb.DeviceBean;
import org.sugar.media.sipserver.utils.SipCacheService;
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

    private String catalogTemplate = """
            <?xml version="1.0"?>
            <Query>
                <CmdType>Catalog</CmdType>
                <SN>{}</SN>
                <DeviceID>{}</DeviceID>
            </Query>
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
    public Request cancelCatalogSubscribe(DeviceBean device, CallIdHeader callIdHeader) {

        Request request = this.createCatalogSubscribe(device, callIdHeader);

        request.removeHeader(ExpiresHeader.NAME);
        // 设置 Expires
        ExpiresHeader expiresHeader = SipFactory.getInstance().createHeaderFactory().createExpiresHeader(0);
        request.addHeader(expiresHeader);



        return request;


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
        ArrayList<ViaHeader> viaHeaders = createViaHeaders(device);
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

    private ArrayList<ViaHeader> createViaHeaders(DeviceBean device) throws ParseException, InvalidArgumentException, PeerUnavailableException {
        ArrayList<ViaHeader> viaHeaders = new ArrayList<>();


        String branch = this.sipUtils.getNewViaBranch();
        ViaHeader viaHeader = SipFactory.getInstance().createHeaderFactory().createViaHeader(sipConfUtils.getIp(), sipConfUtils.getPort(), device.getTransport(), branch);
        viaHeader.setRPort();
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
