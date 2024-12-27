package org.sugar.media.beans.hooks.zlm;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Date:2024/12/27 22:28:50
 * Author：Tobin
 * Description:
 */

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class HookRecordBean {

    @JsonProperty("app")
    private String app;

    @JsonProperty("file_name")
    private String fileName;

    @JsonProperty("file_path")
    private String filePath;

    @JsonProperty("file_size")
    private long fileSize;

    @JsonProperty("folder")
    private String folder;

    @JsonProperty("hook_index")
    private int hookIndex;

    @JsonProperty("mediaServerId")
    private String mediaServerId;

    @JsonProperty("params")
    private String params;

    @JsonProperty("start_time")
    private long startTime;

    @JsonProperty("stream")
    private String stream;

    @JsonProperty("time_len")
    private double timeLen;

    @JsonProperty("url")
    private String url;

    @JsonProperty("vhost")
    private String vhost;
}
