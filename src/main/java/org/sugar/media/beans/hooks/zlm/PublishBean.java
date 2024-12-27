package org.sugar.media.beans.hooks.zlm;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

/**
 * Date:2024/12/27 10:41:16
 * Author：Tobin
 * Description:
 */

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PublishBean {
    private String app;
    private int hookIndex;
    private String id;
    private String ip;
    private String mediaServerId;
    private int originType;
    private String originTypeStr;
    private String params;
    private int port;
    private String schema;
    private String stream;
    private String vhost;


}
