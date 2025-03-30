package org.sugar.media.model.stream;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.UpdateTimestamp;
import org.sugar.media.enums.AppEnum;
import org.sugar.media.enums.AutoCloseEnum;

import java.util.Date;

/**
 * Date:2024/12/30 14:45:14
 * Author：Tobin
 * Description:
 */

@Data
@Entity
@Table(name = "m_stream_push", schema = "public", indexes = {@Index(name = "idx_push_zid", columnList = "tenantId")})
// 1.表名 2.模式
@DynamicInsert
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"}) // 忽略  lazy 层级/为空 时候的引用
public class StreamPushModel {

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


    private String name;


    private String app;

    private String stream;

    @Column(columnDefinition = "text")
    private String params;


    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "varchar(255)")
    private AppEnum types;


    private Long nodeId;

    @Column(unique = true)
    private Long relevanceId; // 关联的 通道id

    private String originTypeStr;

    private Integer originType;

    private String schema;

    private Date pushAt;
}
