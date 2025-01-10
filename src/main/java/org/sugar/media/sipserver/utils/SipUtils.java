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

    public List<DeviceChannelModel> parseCatalog(String xml, DeviceModel deviceModel, Long tenantId, String tenantCode, List<DeviceChannelModel> modelList) {

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

            // ====要用通道自定义配置====
            deviceChannelModel.setEnablePull(deviceModel.isEnablePull());
            deviceChannelModel.setAutoClose(deviceModel.getAutoClose());
            deviceChannelModel.setEnableMp4(deviceModel.isEnableMp4());

            //==========
            // 查询是否存在
            Optional<DeviceChannelModel> result = modelList.stream().filter(device -> device.getChannelCode().equals(channelCode)).findFirst();

            if (result.isPresent()) {
                deviceChannelModel = result.get();
            }

            {

                deviceChannelModel.setDeviceId(deviceModel.getId());
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

    // 生成ptz的控制命令
    public byte[] genPtzCommand(List<String> directions, int speed) {
        byte[] command = new byte[8];
        // 字节1：指令的首字节 (A5H)
        command[0] = (byte) 0xA5;

        // 字节2：组合码1，版本0H，校验位
        // 字节2: 组合码1, 高4 位是版本信息, 低4 位是校验位。 本标准的版本号是1.0, 版本信息为0H。
        // 校验位= (字节1 的高4 位+ 字节1 的低4 位+ 字节2 的高4 位) %16。
        int version = 0x00;
        int byte1High4Bits = (command[0] >> 4) & 0x0F; // 获取字节1的高4位
        int byte1Low4Bits = command[0] & 0x0F; // 获取字节1的低4位
        int versionHigh4Bits = (version >> 4) & 0x0F;
        int checksum = (byte1High4Bits + byte1Low4Bits + versionHigh4Bits) % 16;
        command[1] = (byte) ((version << 4) | checksum);
        byte address = 0x01;
        // 字节3：设备地址的低8位
        command[2] = (byte) (address & 0xFF);

        // 字节4 ptz command

        byte result = generateControlByte(directions);

        command[3] = result;

        // 水平速度
        command[4] = (byte) speed;

        // 垂直速度
        command[5] = (byte) speed;

        // zoom out in 速度
        command[6] = (byte) ((speed << 4) | ((address >> 4) & 0x0F));


        byte checksum8 = 0;
        for (int i = 0; i < 7; i++) {
            checksum8 += command[i];
        }
        command[7] = (byte) ((checksum8 & 0xFF) % 256);

        return command;
    }


    private byte generateControlByte(List<String> directions) {
        byte result = 0;

        // 遍历指令列表，设置相应的位
        for (String direction : directions) {
            switch (direction) {
                case "zoomOut":
                    result = setBit(result, 5, true); // 镜头缩小（OUT），Bit5
                    break;
                case "zoomIn":
                    result = setBit(result, 4, true);  // 镜头放大（IN），Bit4
                    break;
                case "tiltUp":
                    result = setBit(result, 3, true);  // 向上（Up），Bit3
                    break;
                case "tiltDown":
                    result = setBit(result, 2, true);  // 向下（Down），Bit2
                    break;
                case "panLeft":
                    result = setBit(result, 1, true);  // 向左（Left），Bit1
                    break;
                case "panRight":
                    result = setBit(result, 0, true);  // 向右（Right），Bit0
                    break;

                case "stop":
                    break;
                default:
                    // 如果是未知指令，可以忽略或抛出异常
                    System.out.println("未知指令: " + direction);
                    break;
            }
        }

        return result;
    }

    /**
     * 设置字节的指定位
     *
     * @param byteValue   当前字节值
     * @param bitPosition 位的位置（0 到 5）
     * @param setBit      是否设置该位为 1（true）或 0（false）
     * @return 修改后的字节值
     */
    private byte setBit(byte byteValue, int bitPosition, boolean setBit) {
        if (setBit) {
            byteValue |= (byte) (1 << bitPosition);  // 将指定的位设置为 1
        }
        return byteValue;
    }

    public String byteArrayToHexString(byte[] byteArray) {
        StringBuilder hexString = new StringBuilder();

        for (byte b : byteArray) {
            // 使用 Integer.toHexString 方法将每个字节转换为16进制
            hexString.append(String.format("%02X", b));  // %02X 确保每个字节都以两位16进制形式表示
        }

        return hexString.toString();
    }
}
