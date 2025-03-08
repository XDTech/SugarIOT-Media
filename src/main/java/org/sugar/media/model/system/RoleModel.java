package org.sugar.media.model.system;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Table;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.hibernate.annotations.*;
import org.sugar.media.enums.StatusEnum;
import org.sugar.media.utils.SnowflakeId;


import java.io.Serializable;
import java.util.Date;

//角色 组表
// 内置一个root角色账户
@Data
@Entity
@Table(name = "role", schema = "public")
@DynamicInsert // 不然@Column 不起作用
@JsonIgnoreProperties(value = {"hibernateLazyInitializer"})
@SQLDelete(sql = "update \"role\"  set deleted = true where id = ?")
//@Where(clause = "deleted != true")
public class RoleModel implements Serializable {


    @Id
    @GeneratedValue(generator = "snowFlakeId")
    @GenericGenerator(name = "snowFlakeId", type = SnowflakeId.class)
    private Long id;

    @NotBlank
    private String name;

    private int sort;


    @Column(unique = true)
    @NotBlank
    private String identity; // hasRole 标识


    @Column(columnDefinition = "bool default false")
    private boolean deleted = false;

    @CreationTimestamp
    private Date createdAt;

    @UpdateTimestamp
    private Date updatedAt;

    @Column(columnDefinition = "text default ''")
    private String remarks;


    @Column(columnDefinition = "varchar(20) default 'normal'")
    private String status ;// 状态

}