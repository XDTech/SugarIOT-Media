package org.sugar.media.beans.node;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.sugar.media.enums.MediaServerEnum;

import java.util.Date;

/**
 * Date:2024/11/18 09:51:36
 * Author：Tobin
 * Description:
 */

@Data
public class NodeBean {


    private Long id;


    private boolean online;

    private String types;// 状态


    private String name;

    private String ip;


    private Integer httpPort;


    private Integer httpsPort;

    private String secret;


    private Date syncConfigTime;

    // 心跳检测同步时间
    private Date syncHeartbeatTime;

    private Float aliveInterval ;

    // hook api最大等待回复时间，单位秒
    private Integer timeoutSec;


    // rtmp port
    private Integer rtmpPort;

    // rtsp port
    private Integer rtspPort;
}
