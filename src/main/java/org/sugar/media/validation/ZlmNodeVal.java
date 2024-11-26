package org.sugar.media.validation;


import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.groups.Default;
import lombok.Data;

@Data
public class ZlmNodeVal {


    @NotNull(message = "id can not be null")
    private Long id;



    private Float aliveInterval ;

    // hook api最大等待回复时间，单位秒
    private Integer timeoutSec;


    // rtmp port
    private Integer rtmpPort;

    // rtsp port
    private Integer rtspPort;

    public interface Create extends Default {

    }

    public interface Update extends Default {

    }

}
