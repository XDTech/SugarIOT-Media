package org.sugar.media.beans.stream;

import lombok.Data;
import org.sugar.media.enums.StatusEnum;

import java.util.Date;

/**
 * (MStreamPull)表实体类
 *
 * @author Tobin
 * @since 2024-11-27 12:42:56
 */

@Data
public class StreamPullBean  {


    private Long id;

    private String name;

    private Boolean addMuteAudio;

    private String app;


    // 0表示正常 1表示正在尝试拉流
    private String status;

    private String streamKey;

    private Boolean enableHls;


    private Boolean enableAudio;

    private Boolean enableFmp4;

    private Boolean enableMp4;

    private Boolean enableRtmp;

    private Boolean enableRtsp;

    private Boolean enableTs;

    private Long mp4maxSecond;

    private Long nodeId;

    private String stream;

    private Float timeoutSec;

    private String url;

    private Date createdAt;
    private  String playerType;


    private boolean enablePull;


    private String autoClose;

    private String nodeName;


}

