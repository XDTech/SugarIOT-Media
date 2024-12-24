package org.sugar.media;

import cn.hutool.core.lang.Console;
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
import org.sugar.media.utils.BaseUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Date:2024/11/26 15:49:49
 * Author：Tobin
 * Description:
 */

public class NormalTests {


    @Test
    void test(){

        String s = BaseUtil.ssrc2hex("200008772");
        Console.log(s);

         s = BaseUtil.hex2ssrc("0BEBE444");

        Console.log(s);

    }
    @Test
    void normalTest(){
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
