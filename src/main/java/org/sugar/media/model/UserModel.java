package org.sugar.media.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.*;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.annotations.*;
import org.sugar.media.enums.UserStatusEnum;


import java.util.Date;

@Data
@Entity
@Table(name = "m_user", schema = "public", uniqueConstraints = @UniqueConstraint(name = "unique_username_zid_status", columnNames = {"username", "zid","status"}))
// 1.表名 2.模式
@SQLDelete(sql = "update \"m_user\"  set status = 'deleted' where id = ?")
@Where(clause = "status != 'deleted'")
@DynamicInsert
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"}) // 忽略  lazy 层级/为空 时候的引用
public class UserModel {

    @Id
    @GeneratedValue(generator = "snowFlakeId")
    @GenericGenerator(name = "snowFlakeId", strategy = "org.sugar.media.utils.SnowflakeId")
    @Column(name = "id")
    private Long id;

    @NotNull
    private String username;

    @JsonIgnore
    @NotNull
    @Column(nullable = false)
    private String password;


    @JsonIgnore
    @NotNull
    @Column(nullable = false)
    private String salt;// 加密盐

    private String name;// 姓名

    private String avatar; // 头像

    private String email;// 邮箱

    private String phone;// 电话


    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "varchar(20) default 'normal'")
    private UserStatusEnum status = UserStatusEnum.normal;// 状态

    @NotNull
    private Long zid;


    @CreationTimestamp
    private Date createdAt;

    @UpdateTimestamp
    private Date updatedAt;

}