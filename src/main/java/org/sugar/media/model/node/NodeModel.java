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
@Table(name = "m_node", schema = "public", indexes = {
        @Index(name = "idx_zid", columnList = "zid")
   }) // 1.表名 2.模式
@DynamicInsert
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"}) // 忽略  lazy 层级/为空 时候的引用
public class NodeModel {

    // ==========基础字段========
    @Id
    @GeneratedValue(generator = "snowFlakeId")
    @GenericGenerator(name = "snowFlakeId", strategy = "org.sugar.media.utils.SnowflakeId")
    @Column(name = "id")
    private Long id;

    @NotNull
    private Long zid;// 租户id

    @CreationTimestamp
    private Date createdAt;

    @UpdateTimestamp
    private Date updatedAt;

    // ===========end===========

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "varchar(20) default 'zlm'")
    private MediaServerEnum types;// 状态


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

}
