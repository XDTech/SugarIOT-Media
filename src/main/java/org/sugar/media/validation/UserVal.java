package org.sugar.media.validation;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.groups.Default;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserVal {


    @NotNull(message = "id can not be null", groups = Update.class)
    private Long id;

    @NotBlank(message = "username can not be null",groups = Create.class)
    private String username;


    @NotBlank(message = "password can not be null",groups = Create.class)
    private String password;


    private String name;// 姓名

    private String avatar; // 头像

    private String email;// 邮箱

    private String phone;// 电话

    private String postName;// 岗位

    private String status;

    public interface Create extends Default {

    }

    public interface Update extends Default {

    }

}
