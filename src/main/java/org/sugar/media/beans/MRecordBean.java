package org.sugar.media.beans;

import java.util.Date;

import lombok.Data;

/**
 * (MRecord)表实体类
 *
 * @author Tobin
 * @since 2025-03-24 17:33:27
 */
@Data
public class MRecordBean {


    private Long id;

    private String app;

    private String fileName;

    private String filePath;

    private Long fileSize;

    private String folder;

    private Long hookIndex;

    private String mediaServerId;

    private String params;

    private Long startTime;

    private String stream;

    private Long tenantId;

    private String timeLen;

    private String url;

    private String vhost;

    private String playUrl;


}

