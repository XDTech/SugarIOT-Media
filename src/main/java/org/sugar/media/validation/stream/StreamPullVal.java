package org.sugar.media.validation.stream;


import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.groups.Default;
import lombok.Data;

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




    // 拉流超时时间 默认10s
    @NotNull(message = "timout not null")
    private Float timeoutSec;


    // enable_mp4
    @NotNull(message = "enable Mp4 not null")
    private Boolean enableMp4;

    public interface Create extends Default {

    }

    public interface Update extends Default {

    }

}
