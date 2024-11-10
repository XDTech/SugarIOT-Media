package org.sugar.media.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.UpdateTimestamp;


import java.util.Date;

@Data
@Entity
@Table(name = "m_user", schema = "public") // 1.表名 2.模式
@DynamicInsert
@JsonIgnoreProperties({"hibernateLazyInitializer","handler"}) // 忽略  lazy 层级/为空 时候的引用
public class UserModel {

    @Id
    @GeneratedValue(generator = "snowFlakeId")
    @GenericGenerator(name = "snowFlakeId", strategy = "org.sugar.media.utils.SnowflakeId")
    @Column(name = "id")
    private Long id;

    @NotNull
    @Column(unique = true, nullable = false)
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


    @CreationTimestamp
    private Date createdAt;

    @UpdateTimestamp
    private Date updatedAt;

}