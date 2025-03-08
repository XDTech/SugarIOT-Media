package org.sugar.media.beans.system;

import lombok.Data;
import org.sugar.media.enums.StatusEnum;

/**
 * (Role)表实体类
 *
 * @author Tobin
 * @since 2025-01-19 11:30:15
 */
@Data
public class RoleBean {


    private Long id;

    private String identity;

    private String name;

    private Integer sort;

    private Long[] permissions;

    private String remarks;

    private StatusEnum status;


}

