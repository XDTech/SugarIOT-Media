package org.sugar.media.validation.stream;


import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.groups.Default;
import lombok.Data;
import org.sugar.media.enums.AutoCloseEnum;
import org.sugar.media.enums.PlayerTypeEnum;
import org.sugar.media.validation.validator.EnumValidatorInterface;

@Data
public class StreamPullVal {


    @NotNull(message = "id can not be null", groups = Update.class)
    private Long id;

    @NotBlank(message = "url can not be null")
    private String url;// 拉流地址


    @NotBlank(message = "name can not be null")
    private String name;

    @NotBlank(message = "app can not be null")
    private String app;

    @NotBlank(message = "stream can not be null")
    private String stream;


    private boolean enableHls;


    private boolean enableAudio;

    // 拉流超时时间 默认10s
    @NotNull(message = "timout not null")
    private Float timeoutSec;


    // enable_mp4
    @NotNull(message = "enable Mp4 not null")
    private Boolean enableMp4;


    @NotBlank(message = "player type is required")
    @EnumValidatorInterface(enumClass = PlayerTypeEnum.class, message = "Invalid player type")
    private  String playerType;

    private Long nodeId; //播放使用的节点

    private boolean enablePull;


    @EnumValidatorInterface(enumClass = AutoCloseEnum.class, message = "Invalid autoClose")
    private String autoClose;

    public interface Create extends Default {

    }

    public interface Update extends Default {

    }

}
