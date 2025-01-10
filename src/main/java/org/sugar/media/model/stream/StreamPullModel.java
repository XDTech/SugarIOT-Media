package org.sugar.media.model.stream;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.validator.constraints.UniqueElements;
import org.sugar.media.enums.AutoCloseEnum;
import org.sugar.media.enums.PlayerTypeEnum;

import java.util.Date;

/**
 * Date:2024/11/27 11:51:44
 * Author：Tobin
 * Description:
 */

@Data
@Entity
@Table(name = "m_stream_pull", schema = "public", indexes = {@Index(name = "idx_pull_zid", columnList = "tenantId")})
// 1.表名 2.模式
@DynamicInsert
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"}) // 忽略  lazy 层级/为空 时候的引用
public class StreamPullModel {
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

    //===

    @NotBlank
    private String name;

    @NotBlank
    private String url;// 拉流地址

    @NotBlank
    private String app;

    @NotBlank
    private String stream;


    // 播放方式
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "varchar(20) default 'balance'")
    private PlayerTypeEnum playerType = PlayerTypeEnum.balance;

    private Long nodeId; //播放使用的节点


    //  流的唯一标识
    private String streamKey;


    @Column(columnDefinition = "varchar(255) default '__defaultVhost__'")
    private String vhost = "__defaultVhost__";


    @Column(columnDefinition = "bool default false")
    private boolean enablePull = false; //是否开启拉流 默认关闭


    // 拉流超时时间 默认10s
    @Column(columnDefinition = "float4 default 10.0")
    private Float timeoutSec;


    // enable_mp4 录制
    @Column(columnDefinition = "bool default false")
    private boolean enableMp4 = false;


    // hls 录制
    @Column(columnDefinition = "bool default false")
    private boolean enableHls = false;


    // 是否转协议为rtsp/webrtc
    @Column(columnDefinition = "bool default true")
    private boolean enableRtsp = true;


    // 是否转协议为rtmp/flv
    @Column(columnDefinition = "bool default true")
    private boolean enableRtmp = true;


    // enable_ts
    @Column(columnDefinition = "bool default true")
    private boolean enableTs = true;


    // 是否转协议为http-fmp4/ws-fmp4
    @Column(columnDefinition = "bool default true")
    private boolean enableFmp4 = true;

    // 转协议是否开启音频
    @Column(columnDefinition = "bool default true")
    private boolean enableAudio = true;


    // 转协议无音频时，是否添加静音aac音频
    @Column(columnDefinition = "bool default true")
    private boolean addMuteAudio = true;


    // mp4录制切片大小，单位秒
    @Column(columnDefinition = "int8 default 3600")
    private Long mp4MaxSecond = 3600L;

    // 无人观看时，是否直接关闭(而不是通过on_none_reader hook返回close)
    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "varchar(255) default 'ignore'")
    private AutoCloseEnum autoClose = AutoCloseEnum.ignore;

}
