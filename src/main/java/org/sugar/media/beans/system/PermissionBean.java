package org.sugar.media.beans.system;

import lombok.Data;

/**
 * (Permission)表实体类
 *
 * @author Tobin
 * @since 2025-01-20 09:53:44
 */
@Data
public class PermissionBean {


    private Long id;

    private String description;

    private String identity;

    private String name;

    private Long parentId;

    private String remarks;

    private Integer sort;


}

