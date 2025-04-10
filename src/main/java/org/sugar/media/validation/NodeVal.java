package org.sugar.media.validation;


import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.groups.Default;
import lombok.Data;
import org.sugar.media.enums.MediaServerEnum;
import org.sugar.media.validation.validator.EnumValidatorInterface;

@Data
public class NodeVal {


    @NotNull(message = "id can not be null", groups = Update.class)
    private Long id;


//    @NotNull(message = "Media server type is required")
//    @EnumValidatorInterface(enumClass = MediaServerEnum.class, message = "Invalid media server type")
//    private String types;// 状态


    private String name;

    @NotBlank(message = "ip can not be null")
    private String ip;

    @NotBlank(message = "remote ip can not be null")
    private String remoteIp;

    @NotNull(message = "httpPort can not be null")
    private Integer httpPort;

    @NotNull(message = "httpsPort can not be null")
    private Integer httpsPort;


    @NotNull(message = "remoteRtpPort can not be null")
    private Integer remoteRtpPort;

    @NotBlank(message = "secret can not be null")
    private String secret;



    public interface Create extends Default {

    }

    public interface Update extends Default {

    }

}
