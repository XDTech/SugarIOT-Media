package org.sugar.media.model.record;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.UpdateTimestamp;

import java.util.Date;

/**
 * Date:2024/12/27 22:09:15
 * Author：Tobin
 * Description:
 */

@Data
@Entity
@Table(name = "m_record", schema = "public", indexes = {@Index(name = "idx_pull_zid", columnList = "tenantId")})
// 1.表名 2.模式
@DynamicInsert
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"}) // 忽略  lazy 层级/为空 时候的引用
public class RecordModel {

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


    private String app;
    private String fileName;
    private String filePath;
    private Long fileSize;
    private String folder;
    private Long hookIndex;
    private String mediaServerId;
    private String params;
    private Long startTime;
    private String stream;
    private Double timeLen;
    private String url;
    private String vhost;
}
