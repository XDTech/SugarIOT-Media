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
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.sugar.media.beans.hooks.zlm.AddressBean;
import org.sugar.media.enums.StatusEnum;
import org.sugar.media.model.gb.DeviceChannelModel;
import org.sugar.media.model.gb.DeviceModel;
import org.sugar.media.service.gb.ChannelService;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.sip.header.FromHeader;
import javax.sip.header.ViaHeader;
import javax.xml.xpath.XPathConstants;
import java.nio.charset.Charset;
import java.util.*;

/**
 * Date:2024/12/09 20:23:47
 * Author：Tobin
 * Description:
 */

@Slf4j
@Service
public class SipUtils {


    public String channelCodeMid = "0000132";


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
        return String.valueOf(System.currentTimeMillis() + RandomUtil.randomNumbers(6));
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

    // 解析catalog

    public List<DeviceChannelModel> parseCatalog(String xml, Long deviceId, Long tenantId, String tenantCode, List<DeviceChannelModel> modelList) {

        List<DeviceChannelModel> channelModelList = new ArrayList<>();
        // 使用 Hutool 解析 XML
        Document document = XmlUtil.parseXml(xml);

        // 获取根节点
        Element root = XmlUtil.getRootElement(document);

        // 获取 CmdType
        String cmdType = XmlUtil.elementText(root, "CmdType");


        // 获取 SumNum
        String sumNum = XmlUtil.elementText(root, "SumNum");


        // 解析 DeviceList 中的 Item 节点
        Element deviceList = XmlUtil.getElement(root, "DeviceList");
        List<Element> item = XmlUtil.getElements(deviceList, "Item");


        for (Element element : item) {


            String channelCode = XmlUtil.elementText(element, "DeviceID");
            // 判断通道号是否合法
            boolean checked = this.checkChannelCode(tenantCode, channelCode);


            if (!checked) {
                log.warn("[通道ID不合法]:{},租户id：{}", channelCode, tenantCode);
                continue;
            }

            DeviceChannelModel deviceChannelModel = new DeviceChannelModel();
            // 查询是否存在
            Optional<DeviceChannelModel> result = modelList.stream().filter(device -> device.getChannelCode().equals(channelCode)).findFirst();

            if (result.isPresent()) {
                deviceChannelModel = result.get();
            }


            deviceChannelModel.setDeviceId(deviceId);
            deviceChannelModel.setTenantId(tenantId);

            deviceChannelModel.setChannelCode(XmlUtil.elementText(element, "DeviceID"));
            deviceChannelModel.setChannelName(XmlUtil.elementText(element, "Name"));
            deviceChannelModel.setManufacturer(XmlUtil.elementText(element, "Manufacturer"));
            deviceChannelModel.setModel(XmlUtil.elementText(element, "Model"));
            deviceChannelModel.setOwner(XmlUtil.elementText(element, "Owner"));
            deviceChannelModel.setCivilCode(XmlUtil.elementText(element, "CivilCode"));
            deviceChannelModel.setAddress(XmlUtil.elementText(element, "Address"));
            deviceChannelModel.setParental(Convert.toInt(XmlUtil.elementText(element, "Parental")));
            deviceChannelModel.setParentId(XmlUtil.elementText(element, "ParentID"));
            deviceChannelModel.setSafetyWay(Convert.toInt(XmlUtil.elementText(element, "SafetyWay")));
            deviceChannelModel.setRegisterWay(Convert.toInt(XmlUtil.elementText(element, "RegisterWay")));
            deviceChannelModel.setSecrecy(Convert.toInt(XmlUtil.elementText(element, "Secrecy")));
            deviceChannelModel.setStatus(XmlUtil.elementText(element, "Status").equalsIgnoreCase("ON") ? StatusEnum.online : StatusEnum.offline);
            deviceChannelModel.setLng(XmlUtil.elementText(element, "Longitude"));
            deviceChannelModel.setLat(XmlUtil.elementText(element, "Latitude"));

            deviceChannelModel.setSyncTime(new Date());
            // 获取info的ptz type
            Element info = XmlUtil.getElement(element, "Info");
            if (ObjectUtil.isNotEmpty(info)) {

                String ptzType = XmlUtil.elementText(info, "PTZType");


                deviceChannelModel.setPtzType(Convert.toInt(ptzType));


            }

            channelModelList.add(deviceChannelModel);

        }
        return channelModelList;

    }


    public String getXmlContent(SIPRequest request) {
        return new String(request.getRawContent(), Charset.forName("GB2312"));

    }

    public String getXmlContent(byte[] rawContent) {
        return new String(rawContent, Charset.forName("GB2312"));

    }

    /**
     * @param tenantCode
     * @param channelCode 设备传过来的20位编码
     * @return 000 0132
     */
    public boolean checkChannelCode(String tenantCode, String channelCode) {

        // 判断长度是否20位
        if (channelCode.length() != 20) {
            return false;
        }
        // 判断前6位租户编码是否相同
        String tenant = channelCode.substring(0, 6);
        if (!tenantCode.equals(tenant)) {
            return false;
        }

        String check = channelCode.substring(6, 13);

        if (!check.equals(this.channelCodeMid)) {
            return false;
        }

        return true;

    }
}
