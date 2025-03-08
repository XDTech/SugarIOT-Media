package org.sugar.media.model.system;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.UpdateTimestamp;
import org.sugar.media.utils.SnowflakeId;

import java.util.Date;

//路由 权限表

@Data
@Entity
@Table(name = "permission", schema = "public")
@DynamicInsert // 不然@Column 不起作用
@JsonIgnoreProperties(value = {"hibernateLazyInitializer", "handler"})
// @see// https://blog.csdn.net/weixin_43272781/article/details/105246784
//@Where(clause = "deleted != true")
public class PermissionModel {


    @Id
    @GeneratedValue(generator = "snowFlakeId")
    @GenericGenerator(name = "snowFlakeId", type = SnowflakeId.class)
    @Column(name = "id")
    private Long id;

    // 权限名称
    private String name;

    private long parentId;

    @Column(unique = true)
    @NotBlank
    private String identity;

    // 权限描述
    private String description;


    private int sort;

    @Column(columnDefinition = "text default ''")
    private String remarks;

    @CreationTimestamp
    private Date createdAt;

    @UpdateTimestamp
    private Date updatedAt;

    @Column(columnDefinition = "bool default false")
    private boolean deleted = false;


}