package org.sugar.media.beans.gb;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.sugar.media.enums.DeviceTypeEnum;

import java.util.Date;

/**
 * Date:2024/12/12 11:05:14
 * Author：Tobin
 * Description:
 */


@Data
public class DeviceBean {
    private Long id;

    @JsonIgnore
    private Long tenantId;// 租户id

    @JsonIgnore
    private String tenantCode;

    private String name; // 设备名称
    private String deviceId;// 设备id 规则:[租户编码]+0000+7位流水号生成
    private String pwd; //国标设备验证id 为空采用系统密码


    private String deviceType; // 设备类型
    private Long nodeId;// 采用哪个节点播放

    private String nodeHost;

    private Integer nodePort;

    private String host;// 设备ip

    private Integer port;// 设备端口

    // UDP TCP-PASSIVE  TCP-ACTIVE
    private String transport;

    private String status;

    private String deviceName; //设备名称

    private String manufacturer;// 厂商

    private String firmware;

    private String model;// 设备型号

    private Integer channel;//通道数

    private Date syncTime;
}
