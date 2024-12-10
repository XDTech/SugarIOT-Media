package org.sugar.media.sipserver.request;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Date:2024/12/10 09:36:49
 * Author：Tobin
 * Description: 创建sip request
 */

@Slf4j
@Service
public class SipRequestService {


//
//
//    public Request createDeviceInfo(Device device, String content, String viaTag, String fromTag, String toTag, CallIdHeader callIdHeader)
//            throws ParseException, InvalidArgumentException, PeerUnavailableException {
//        SipURI requestURI = createRequestURI(device);
//        ArrayList<ViaHeader> viaHeaders = createViaHeaders(device, viaTag);
//        FromHeader fromHeader = createFromHeader(fromTag);
//        ToHeader toHeader = createToHeader(device, toTag);
//        MaxForwardsHeader maxForwards = createMaxForwardsHeader();
//        CSeqHeader cSeqHeader = createCSeqHeader();
//
//        Request request = SipFactory.getInstance()
//                .createMessageFactory()
//                .createRequest(requestURI, Request.MESSAGE, callIdHeader, cSeqHeader, fromHeader, toHeader, viaHeaders, maxForwards);
//
//        ContentTypeHeader contentTypeHeader = SipFactory.getInstance()
//                .createHeaderFactory()
//                .createContentTypeHeader("Application", "MANSCDP+xml");
//        request.setContent(content, contentTypeHeader);
//
//        return request;
//    }
//
//    private SipURI createRequestURI(Device device) throws ParseException, PeerUnavailableException {
//        return SipFactory.getInstance()
//                .createAddressFactory()
//                .createSipURI(device.getDeviceId(), device.getHostAddress());
//    }
//
//    private ArrayList<ViaHeader> createViaHeaders(Device device, String viaTag) throws ParseException, InvalidArgumentException, PeerUnavailableException {
//        ArrayList<ViaHeader> viaHeaders = new ArrayList<>();
//        ViaHeader viaHeader = SipFactory.getInstance()
//                .createHeaderFactory()
//                .createViaHeader(sipLayer.getLocalIp(device.getLocalIp()), sipConfig.getPort(), device.getTransport(), viaTag);
//        viaHeader.setRPort();
//        viaHeaders.add(viaHeader);
//        return viaHeaders;
//    }
//
//    private FromHeader createFromHeader(String fromTag) throws ParseException, PeerUnavailableException {
//        SipURI fromSipURI = SipFactory.getInstance()
//                .createAddressFactory()
//                .createSipURI(sipConfig.getId(), sipConfig.getDomain());
//        Address fromAddress = SipFactory.getInstance()
//                .createAddressFactory()
//                .createAddress(fromSipURI);
//        return SipFactory.getInstance()
//                .createHeaderFactory()
//                .createFromHeader(fromAddress, fromTag);
//    }
//
//    private ToHeader createToHeader(Device device, String toTag) throws ParseException, PeerUnavailableException {
//        SipURI toSipURI = SipFactory.getInstance()
//                .createAddressFactory()
//                .createSipURI(device.getDeviceId(), device.getHostAddress());
//        Address toAddress = SipFactory.getInstance()
//                .createAddressFactory()
//                .createAddress(toSipURI);
//        return SipFactory.getInstance()
//                .createHeaderFactory()
//                .createToHeader(toAddress, toTag);
//    }
//
//    private MaxForwardsHeader createMaxForwardsHeader() throws InvalidArgumentException, PeerUnavailableException {
//        return SipFactory.getInstance()
//                .createHeaderFactory()
//                .createMaxForwardsHeader(70);
//    }
//
//    private CSeqHeader createCSeqHeader() throws InvalidArgumentException, ParseException, PeerUnavailableException {
//        return SipFactory.getInstance()
//                .createHeaderFactory()
//                .createCSeqHeader(redisCatchStorage.getCSEQ(), Request.MESSAGE);
//    }

}
