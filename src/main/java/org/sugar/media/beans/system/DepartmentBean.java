package org.sugar.media.beans.system;

import lombok.Data;

/**
 * (Department)表实体类
 *
 * @author Tobin
 * @since 2025-01-22 21:32:55
 */
@Data
public class DepartmentBean {


    private Long id;

    private String charge;

    private String email;

    private String name;

    private Long parentId;

    private String phone;

    private String remarks;

    private Integer sort;

    private String status;


}

