package org.sugar.media.beans.stream;

import lombok.Data;

import java.io.Serializable;
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

    private Boolean autoClose;

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

}

