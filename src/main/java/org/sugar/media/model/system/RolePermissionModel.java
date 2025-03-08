package org.sugar.media.model.system;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.UpdateTimestamp;
import org.sugar.media.utils.SnowflakeId;

import java.util.Date;

/**
 * Date:2025/01/17 20:22:32
 * Author：Tobin
 * Description:
 */

@Data
@Entity
@Table(name = "role_permission", schema = "public")
@DynamicInsert // 不然@Column 不起作用
public class RolePermissionModel {


    @Id
    @GeneratedValue(generator = "snowFlakeId")
    @GenericGenerator(name = "snowFlakeId", type = SnowflakeId.class)
    @Column(name = "id")
    private long id;

    @CreationTimestamp
    private Date createdAt;

    @UpdateTimestamp
    private Date updatedAt;

    private Long roleId;

    private Long permissionId;
}
