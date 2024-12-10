package org.sugar.media.sipserver.utils;

import gov.nist.javax.sip.address.AddressImpl;
import gov.nist.javax.sip.address.SipUri;
import gov.nist.javax.sip.message.SIPRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.sugar.media.beans.hooks.zlm.AddressBean;

import javax.sip.header.FromHeader;

/**
 * Date:2024/12/09 20:23:47
 * Author：Tobin
 * Description:
 */

@Slf4j
@Service
public class SipUtils {


    /**
     * 获取远端地址
     *
     * @param sipRequest
     * @return
     */
    public AddressBean getAddress(SIPRequest sipRequest) {
        AddressBean addressBean = new AddressBean();
        addressBean.setHost(sipRequest.getViaHost());
        addressBean.setPort(sipRequest.getViaPort());

        return addressBean;

    }


    /**
     * 获取设备id
     *
     * @param request
     * @return
     */
    public String getDeviceId(SIPRequest request) {
        FromHeader fromHeader = (FromHeader) request.getHeader(FromHeader.NAME);
        AddressImpl address = (AddressImpl) fromHeader.getAddress();
        SipUri uri = (SipUri) address.getURI();


        return uri.getUser();
    }

}
