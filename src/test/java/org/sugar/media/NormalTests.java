package org.sugar.media;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Console;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import cn.hutool.core.util.XmlUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.jwt.JWT;
import cn.hutool.jwt.signers.JWTSigner;
import cn.hutool.jwt.signers.JWTSignerUtil;
import cn.hutool.log.StaticLog;
import org.junit.jupiter.api.Test;
import org.springframework.web.util.UriComponentsBuilder;
import org.sugar.media.enums.AppEnum;
import org.sugar.media.utils.BaseUtil;
import org.sugar.media.utils.MonitorUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import oshi.SystemInfo;
import oshi.software.os.FileSystem;
import oshi.software.os.OSFileStore;
import oshi.software.os.OperatingSystem;

import java.net.URL;
import java.text.DecimalFormat;
import java.util.*;


/**
 * Date:2024/11/26 15:49:49
 * Author：Tobin
 * Description:
 */

public class NormalTests {


    private static final String[] UNITS = {"B", "KB", "MB", "GB", "TB", "PB"};
    private static final int UNIT_THRESHOLD = 1024; // 换算单位为1024

    public String formatByte(long bytes) {
        if (bytes < UNIT_THRESHOLD) {
            return bytes + " B";
        }

        int unitIndex = 0;
        double size = bytes;
        while (size >= UNIT_THRESHOLD && unitIndex < UNITS.length - 1) {
            size /= UNIT_THRESHOLD;
            unitIndex++;
        }

        // 保留两位小数
        DecimalFormat decimalFormat = new DecimalFormat("#.##");
        return decimalFormat.format(size) + " " + UNITS[unitIndex];
    }
    @Test
    void test1(){


        Console.log(AppEnum.rtp.toString());

        String s = this.formatByte(30000000);

        Console.log(s);


    }

    @Test
    void testXml() {
        String s = """
                <?xml version="1.0" encoding="gb2312"?>
                <Response>
                <CmdType>Catalog</CmdType>
                <SN>4</SN>
                <DeviceID>10000100001111111111</DeviceID>
                <SumNum>2</SumNum>
                <DeviceList Num="1">
                <Item>
                <DeviceID>10000100001320000002</DeviceID>
                <Name>IPdome</Name>
                <Manufacturer>Manufacturer</Manufacturer>
                <Model>Camera</Model>
                <Owner>Owner</Owner>
                <CivilCode>CivilCode</CivilCode>
                <Address>192.168.31.143</Address>
                <Parental>0</Parental>
                <ParentID>10000100001111111111</ParentID>
                <SafetyWay>0</SafetyWay>
                <RegisterWay>1</RegisterWay>
                <Secrecy>0</Secrecy>
                <Status>ON</Status>
                <Longitude>0.000</Longitude>
                <Latitude>0.000</Latitude>
                <Info>
                <PTZType>2</PTZType>
                <a>4</a>
                </Info>
                </Item>
                </DeviceList>
                </Response>
                """;

        // 使用 Hutool 解析 XML
        Document document = XmlUtil.parseXml(s);
        Element root = XmlUtil.getRootElement(document);
        // 解析 DeviceList 中的 Item 节点
        Element deviceList = XmlUtil.getElement(root, "DeviceList");
        List<Element> item = XmlUtil.getElements(deviceList, "Item");
        String cmdType = XmlUtil.elementText(root, "CmdType");

        for (Element element : item) {


            Element info = XmlUtil.getElement(element, "Info");

            if (ObjectUtil.isNotEmpty(info)) {
                Console.log(XmlUtil.elementText(info, "a"),"22");
            }
        }
    }

    @Test
    void testUrl() {
        Integer anInt = Convert.toInt("");
        Console.log(anInt);

        String s = "/root/zlm_v2/release/linux/Debug/www/100001/record/live/test/2024-12-28/00-33-51-0.mp4";
        Console.log(s);


        String a = "record/live/test/2024-12-28/00-33-51-0.mp4";

        String substring = s.substring(a.length() - 5).substring(0, 6);

        Console.log(substring, a.length(), a.length() - 5, s.length());


    }

    @Test
    void test() {

        String s = BaseUtil.ssrc2hex("200008772");
        Console.log(s);

        s = BaseUtil.hex2ssrc("0BEBE444");

        Console.log(s);

    }

    @Test
    void normalTest() {
        String xml = """
                <Response>
                    <CmdType>Catalog</CmdType>
                    <SN>4</SN>
                    <DeviceID>10000100001111111</DeviceID>
                    <SumNum>1</SumNum>
                    <DeviceList Num="1">
                        <Item>
                            <DeviceID>34020000001320000009</DeviceID>
                            <Name>Camera 01</Name>
                            <Manufacturer>Hikvision</Manufacturer>
                            <Model>IP Camera</Model>
                            <Owner>Owner</Owner>
                            <CivilCode>34020000000</CivilCode>
                            <Address>Address</Address>
                            <Parental>0</Parental>
                            <ParentID>34020000002000000001</ParentID>
                            <SafetyWay>0</SafetyWay>
                            <RegisterWay>1</RegisterWay>
                            <Secrecy>0</Secrecy>
                            <Status>ON</Status>
                        </Item>
                    </DeviceList>
                </Response>
                """;

        Document document = XmlUtil.parseXml(xml);

        // 获取根节点
        Element root = XmlUtil.getRootElement(document);

        // 获取 CmdType
        String cmdType = XmlUtil.elementText(root, "CmdType");
        System.out.println("CmdType: " + cmdType);

        // 获取 DeviceID
        String deviceId = XmlUtil.elementText(root, "DeviceID");
        System.out.println("DeviceID: " + deviceId);

        // 获取 SumNum
        String sumNum = XmlUtil.elementText(root, "SumNum");
        System.out.println("SumNum: " + sumNum);

        // 解析 DeviceList 中的 Item 节点
        Element deviceList = XmlUtil.getElement(root, "DeviceList");
        List<Element> item = XmlUtil.getElements(deviceList, "Item");


        for (Element element : item) {
//            // 获取 Item 的详细信息
            Map<String, String> itemInfo = new HashMap<>();
            itemInfo.put("DeviceID", XmlUtil.elementText(element, "DeviceID"));
            itemInfo.put("Name", XmlUtil.elementText(element, "Name"));
            itemInfo.put("Manufacturer", XmlUtil.elementText(element, "Manufacturer"));
            itemInfo.put("Model", XmlUtil.elementText(element, "Model"));
            itemInfo.put("Owner", XmlUtil.elementText(element, "Owner"));
            itemInfo.put("Status", XmlUtil.elementText(element, "Status"));

            System.out.println("Item Info: " + itemInfo);
        }


//        String format = StrUtil.format("{}0000{}", 1, 1);
//        Console.log(format);

//        final JWTSigner signer = JWTSignerUtil.hs256("13".getBytes());
//        JWT jwt = JWT.create().setSigner(signer);


    }
}
