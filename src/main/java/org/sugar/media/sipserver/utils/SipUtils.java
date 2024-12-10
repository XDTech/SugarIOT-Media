package org.sugar.media.sipserver.utils;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.lang.Console;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.XmlUtil;
import gov.nist.javax.sip.address.AddressImpl;
import gov.nist.javax.sip.address.SipUri;
import gov.nist.javax.sip.message.SIPRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.sugar.media.beans.hooks.zlm.AddressBean;
import org.w3c.dom.Document;

import javax.sip.header.FromHeader;
import javax.xml.xpath.XPathConstants;

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


    public String getCmdType(String xmlContent) {


        Document document = XmlUtil.parseXml(xmlContent);

        String cmdType = "";

        // 保活消息  [Keepalive]
        Object keepalive = XmlUtil.getByXPath("//Notify/CmdType", document, XPathConstants.STRING);

        if (ObjectUtil.isNotEmpty(keepalive)) return Convert.toStr(keepalive);

        // 事件消息 比如 [catalog]
        Object Catalog = XmlUtil.getByXPath("//Response/CmdType", document, XPathConstants.STRING);

        if (ObjectUtil.isNotEmpty(Catalog)) return Convert.toStr(Catalog);

        return cmdType;
    }

    public  String getNewTag(){
        return String.valueOf(System.currentTimeMillis());
    }


//    public static  String getNewViaTag() {
//        return "z9hG4bK" + RandomStringUtils.randomNumeric(10);
//    }


}
