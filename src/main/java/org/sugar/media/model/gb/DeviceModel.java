package org.sugar.media.model.gb;

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
 * Date:2024/12/10 11:37:54
 * Author：Tobin
 * Description: 存储国标设备信息
 */
@Data
@Entity
@Table(name = "m_gb_device", schema = "public", indexes = {@Index(name = "idx_device_zid", columnList = "zid")}) // 1.表名 2.模式
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
    private Long zid;// 租户id

    @CreationTimestamp
    private Date createdAt;

    @UpdateTimestamp
    private Date updatedAt;


    // ===========end===========
}
