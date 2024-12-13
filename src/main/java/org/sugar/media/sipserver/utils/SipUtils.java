package org.sugar.media.sipserver.utils;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.lang.Console;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.XmlUtil;
import gov.nist.javax.sip.address.AddressImpl;
import gov.nist.javax.sip.address.SipUri;
import gov.nist.javax.sip.header.ViaList;
import gov.nist.javax.sip.message.SIPRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.sugar.media.beans.hooks.zlm.AddressBean;
import org.sugar.media.model.gb.DeviceModel;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.sip.header.FromHeader;
import javax.sip.header.ViaHeader;
import javax.xml.xpath.XPathConstants;
import java.util.List;

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

    public DeviceModel getDeviceInfo(String xmlContent, DeviceModel deviceModel) {


        Document document = XmlUtil.parseXml(xmlContent);


        // 获取device name
        Object deviceName = XmlUtil.getByXPath("//Response/DeviceName", document, XPathConstants.STRING);
        Object model = XmlUtil.getByXPath("//Response/Model", document, XPathConstants.STRING);
        Object channel = XmlUtil.getByXPath("//Response/Channel", document, XPathConstants.STRING);
        Object manufacturer = XmlUtil.getByXPath("//Response/Manufacturer", document, XPathConstants.STRING);
        Object firmware = XmlUtil.getByXPath("//Response/Firmware", document, XPathConstants.STRING);

        deviceModel.setDeviceName(Convert.toStr(deviceName));
        deviceModel.setModel(Convert.toStr(model));
        deviceModel.setChannel(Convert.toInt(channel));
        deviceModel.setManufacturer(Convert.toStr(manufacturer));
        deviceModel.setFirmware(Convert.toStr(firmware));


        return deviceModel;
    }

    public String getNewTag() {
        return String.valueOf(System.currentTimeMillis()+RandomUtil.randomNumbers(6));
    }


    // 生成via 事务id
    public String getNewViaBranch() {
        return "z9hG4bK" + UUID.randomUUID().toString().replace("-", "");
    }


    // 获取传输协议
    public String getTransportProtocol(SIPRequest request) {
        try {
            // 获取 Via 头部
            ViaList viaHeaders = request.getViaHeaders();


            if (viaHeaders != null && !viaHeaders.isEmpty()) {
                // 读取首个 ViaHeader 的 Transport 参数
                String transport = viaHeaders.get(0).getTransport().toUpperCase();
                return transport; // 返回 UDP 或 TCP
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "UNKNOWN"; // 如果无法解析
    }

}
