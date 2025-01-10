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
import org.sugar.media.enums.AutoCloseEnum;
import org.sugar.media.enums.DeviceTypeEnum;
import org.sugar.media.enums.MediaServerEnum;
import org.sugar.media.enums.StatusEnum;

import java.util.Date;

/**
 * Date:2024/12/10 11:37:54
 * Author：Tobin
 * Description: 存储国标设备信息
 */
@Data
@Entity
@Table(name = "m_gb_device", schema = "public", indexes = {@Index(name = "idx_device_tenantId", columnList = "tenantId")})
// 1.表名 2.模式
@DynamicInsert
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"}) // 忽略  lazy 层级/为空 时候的引用
public class DeviceModel {

    // ==========基础字段========
    @Id
    @GeneratedValue(generator = "snowFlakeId")
    @GenericGenerator(name = "snowFlakeId", strategy = "org.sugar.media.utils.SnowflakeId")
    @Column(name = "id")
    private Long id;

    @NotNull
    private Long tenantId;// 租户id

    @CreationTimestamp
    private Date createdAt;

    @UpdateTimestamp
    private Date updatedAt;


    // ===========end===========


    @NotBlank
    private String name; // 名称

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "varchar(20)")
    private DeviceTypeEnum deviceType; // 设备类型


    @NotBlank
    private String deviceId;// 设备id 规则:[租户编码]+0000+7位流水号生成


    private String transport;   // UDP TCP

    private String pwd; //国标设备验证id 为空采用系统密码


    private String host;

    private Integer port;

    private String deviceName; //设备名称

    private String manufacturer;// 厂商

    private String firmware;

    private String model;// 设备型号

    private Integer channel;//通道数

    private Date syncTime;


//    @NotNull
//    @Enumerated(EnumType.STRING)
//    @Column(columnDefinition = "varchar(20) default 'offline'")
//    private StatusEnum status = StatusEnum.offline;// 状态


    //


    // 无人观看时，是否直接关闭(而不是通过on_none_reader hook返回close)
    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "varchar(255) default 'no'")
    private AutoCloseEnum autoClose = AutoCloseEnum.no;


    @Column(columnDefinition = "bool default false")
    private boolean enablePull = false; //是否开启拉流 默认关闭

    // enable_mp4 录制
    @Column(columnDefinition = "bool default false")
    private boolean enableMp4 = false;
}
