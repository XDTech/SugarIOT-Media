package org.sugar.media.beans.gb;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.UpdateTimestamp;
import org.sugar.media.enums.AutoCloseEnum;
import org.sugar.media.enums.StatusEnum;

import java.util.Date;

/**
 * Date:2024/12/12 11:05:14
 * Author：Tobin
 * Description:
 */


@Data
public class ChannelBean {


    private Long id;


    private Long deviceId;// 设备id

    private String deviceCode;

    private String deviceName;

    private Long tenantId;// 租户id

    private Date createdAt;

    private Date updatedAt;

    private String channelCode;

    private String channelName;     // 通道名称
    private String manufacturer;   // 设备制造商
    private String model;          // 设备类型
    private String owner;          // 设备所有者
    private String civilCode;      // 行政区域编码 SIP服务器域
    private String address;        // 设备地址
    private Integer parental;          // 是否为子设备 (0: 无父设备, 1: 有父设备)
    private String parentId;       // 父设备 ID 此处应该是sip 服务的id eg:34020000002000000001
    private Integer safetyWay;         // 安全传输方式 (0: 无安全措施, 1: S/MIME 签名, 2: S/MIME 加密, 3: 签名+加密)
    private Integer registerWay;       // 注册方式 (1: 符合 GB/T 28181)
    private Integer secrecy;           // 是否涉密 (0: 非涉密, 1: 涉密)


    private Date syncTime; // 通道同步时间


    private StatusEnum status;// 状态

    private StatusEnum playStatus;// 播放状态

    private String lng;

    private String lat;


    // 1-球机； 2-半球； 3-固定枪机；4-遥控枪机。
    private Integer ptzType;//


    private AutoCloseEnum autoClose;


    private boolean enablePull;

    // enable_mp4 录制
    private boolean enableMp4;


    private String secret;


}
