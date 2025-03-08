package org.sugar.media.validation.system;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.groups.Default;
import lombok.Data;

/**
 * (Department)表验证类
 *
 * @author Tobin
 * @since 2025-01-22 21:32:55
 */
@Data
public class DepartmentVal {

    //@NotBlank(message = "username can not be null")
    //private String username;

    @NotNull(message = "id can not be null", groups = Update.class)
    private Long id;

    private String charge;

    private String email;

    private String name;

    private Long parentId;

    private String phone;

    private String remarks;

    private Integer sort;

    private String status;


    public interface Create extends Default {

    }

    public interface Update extends Default {

    }

}

