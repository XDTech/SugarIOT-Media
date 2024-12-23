package org.sugar.media.model.node;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.UpdateTimestamp;
import org.sugar.media.enums.MediaServerEnum;

import java.util.Date;


@Data
@Entity
@Table(name = "m_node", schema = "public") // 1.表名 2.模式
@DynamicInsert
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"}) // 忽略  lazy 层级/为空 时候的引用
public class NodeModel {

    // ==========基础字段========
    @Id
    @GeneratedValue(generator = "snowFlakeId")
    @GenericGenerator(name = "snowFlakeId", strategy = "org.sugar.media.utils.SnowflakeId")
    @Column(name = "id")
    private Long id;


    @CreationTimestamp
    private Date createdAt;

    @UpdateTimestamp
    private Date updatedAt;




    // ===========end===========


    // 配置文件同步时间
    private Date syncConfigTime;

    // 心跳检测同步时间
    private Date syncHeartbeatTime;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "varchar(20) default 'zlm'")
    private MediaServerEnum types;// 状态



    // 默认节点


    private String name;

    @NotBlank
    private String ip;

    @NotNull
    @Column(columnDefinition = "int4 default 80")
    private Integer httpPort;

    @NotNull
    @Column(columnDefinition = "int4 default 443")
    private Integer httpsPort;

    @NotBlank
    private String secret;

    // =====ZLM配置 可跟后续平台公用

    // ==webhook配置

    // keepalive hook触发间隔,单位秒，float类型
    private Float aliveInterval ;

    // hook api最大等待回复时间，单位秒
    private Integer timeoutSec;


    // rtmp port
    @Column(columnDefinition = "int4 default 1935")
    private Integer rtmpPort;

    // rtsp port
    @Column(columnDefinition = "int4 default 554")
    private Integer rtspPort;


    // rtp
    @Column(columnDefinition = "int4 default 10000")
    private Integer rtpPort;
}
