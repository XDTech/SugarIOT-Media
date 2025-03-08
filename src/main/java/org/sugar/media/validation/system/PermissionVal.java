package org.sugar.media.validation.system;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.groups.Default;
import lombok.Data;

/**
 * (Permission)表验证类
 *
 * @author Tobin
 * @since 2025-01-20 09:53:44
 */
@Data
public class PermissionVal {

    //@NotBlank(message = "username can not be null")
    //private String username;

    @NotNull(message = "id can not be null", groups = Update.class)
    private Long id;

    private String description;


    @NotBlank(message = "identity can not be null")
    private String identity;


    @NotBlank(message = "identity can not be null")
    private String name;

    private Long parentId;

    private String remarks;

    private Integer sort=0;


    public interface Create extends Default {

    }

    public interface Update extends Default {

    }

}

