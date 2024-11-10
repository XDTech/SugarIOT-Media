package org.sugar.media.validation;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.groups.Default;
import lombok.Data;

@Data
public class UserVal {


    @NotNull(message = "id can not be null", groups = Update.class)
    private Long id;

    @NotBlank(message = "username can not be null")
    private String username;


    @NotBlank(message = "pwd can not be null")
    private String pwd;


    public interface Create extends Default {

    }

    public interface Update extends Default {

    }

}
