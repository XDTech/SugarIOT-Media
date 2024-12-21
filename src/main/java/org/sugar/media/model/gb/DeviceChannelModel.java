package org.sugar.media.model.gb;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.UpdateTimestamp;
import org.sugar.media.enums.StatusEnum;

import java.util.Date;

/**
 * Date:2024/12/13 11:55:25
 * Author：Tobin
 * Description: 设备通道表
 */

@Data
@Entity
@Table(name = "m_gb_device_channel", schema = "public", indexes = {@Index(name = "idx_device_tenantId", columnList = "tenantId"),
        @Index(name = "idx_device_Id", columnList = "deviceId")})
// 1.表名 2.模式
@DynamicInsert
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"}) // 忽略  lazy 层级/为空 时候的引用
public class DeviceChannelModel {

    @Id
    @GeneratedValue(generator = "snowFlakeId")
    @GenericGenerator(name = "snowFlakeId", strategy = "org.sugar.media.utils.SnowflakeId")
    @Column(name = "id")
    private Long id;


    @NotNull
    private Long deviceId;// 设备id

    @NotNull
    private Long tenantId;// 租户id

    @CreationTimestamp
    private Date createdAt;

    @UpdateTimestamp
    private Date updatedAt;

    @NotBlank
    private String channelCode;

    private String channelName;           // 通道名称
    private String manufacturer;   // 设备制造商
    private String model;          // 设备类型
    private String owner;          // 设备所有者
    private String civilCode;      // 行政区域编码
    private String address;        // 设备地址
    private Integer parental;          // 是否为子设备 (0: 无父设备, 1: 有父设备)
    private String parentId;       // 父设备 ID 此处应该是sip 服务的id eg:34020000002000000001
    private Integer safetyWay;         // 安全传输方式 (0: 无安全措施, 1: S/MIME 签名, 2: S/MIME 加密, 3: 签名+加密)
    private Integer registerWay;       // 注册方式 (1: 符合 GB/T 28181)
    private Integer secrecy;           // 是否涉密 (0: 非涉密, 1: 涉密)

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "varchar(20) default 'offline'")
    private StatusEnum status = StatusEnum.offline;// 状态
}
