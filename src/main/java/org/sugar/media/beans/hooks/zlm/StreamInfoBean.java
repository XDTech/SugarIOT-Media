/**
  * Copyright 2025 bejson.com 
  */
package org.sugar.media.beans.hooks.zlm;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;



@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class StreamInfoBean {

    private Long nodeId;
    private Long aliveSecond;
    private String app;
    private int bytesSpeed;
    private long createStamp;
    private boolean isRecordingHLS;
    private boolean isRecordingMP4;
    private OriginSock originSock;
    private int originType;
    private String originTypeStr;
    private String originUrl;
    private String params;
    private int readerCount;
    private String schema;
    private String stream;
    private int totalReaderCount;
    private List<Tracks> tracks;
    private String vhost;

}