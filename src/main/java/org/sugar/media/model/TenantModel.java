package org.sugar.media.model;

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
 * Date:2024/12/11 15:53:49
 * Author：Tobin
 * Description: 租户信息表
 */

@Data
@Entity
@Table(name = "m_tenant", schema = "public", indexes = {@Index(name = "idx_code", columnList = "code")}) // 1.表名 2.模式
@DynamicInsert
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"}) // 忽略  lazy 层级/为空 时候的引用
public class TenantModel {

    @Id
    @GeneratedValue(generator = "snowFlakeId")
    @GenericGenerator(name = "snowFlakeId", strategy = "org.sugar.media.utils.SnowflakeId")
    @Column(name = "id")
    private Long id;


    @CreationTimestamp
    private Date createdAt;

    @UpdateTimestamp
    private Date updatedAt;


    /**
     * 租户编码 6位识别码 顺序码
     * eg:100000,100001
     */
    @Column(unique = true)
    private Integer code;


}
