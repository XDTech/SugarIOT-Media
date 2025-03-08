package org.sugar.media.model.system;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Table;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.*;
import org.sugar.media.enums.StatusEnum;
import org.sugar.media.utils.SnowflakeId;


import java.util.Date;

//路由 权限表

@Data
@Entity
@Table(name = "department", schema = "public")
@DynamicInsert // 不然@Column 不起作用
@JsonIgnoreProperties(value = {"hibernateLazyInitializer", "handler"})
// @see// https://blog.csdn.net/weixin_43272781/article/details/105246784
@SQLDelete(sql = "update \"department\"  set deleted = true where id = ?")
public class DepartmentModel {


    @Id
    @GeneratedValue(generator = "snowFlakeId")
    @GenericGenerator(name = "snowFlakeId", type = SnowflakeId.class)
    @Column(name = "id")
    private long id;

    // 权限名称
    private String name;

    private long parentId;



    private String charge;// 负责人

    private String phone;// 电话

    private String email;// 邮箱

    private int sort;

    @Column(columnDefinition = "text default ''")
    private String remarks;

    @CreationTimestamp
    private Date createdAt;

    @UpdateTimestamp
    private Date updatedAt;

    @Column(columnDefinition = "bool default false")
    private boolean deleted = false;

    @Column(columnDefinition = "varchar(20) default 'normal'")
    private String status; // 状态


}