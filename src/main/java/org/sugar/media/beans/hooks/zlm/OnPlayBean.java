package org.sugar.media.beans.hooks.zlm;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

/**
 * Date:2024/12/07 21:34:49
 * Author：Tobin
 * Description:
 */

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class OnPlayBean {

    private String mediaServerId;
    private String app;
    private String id;
    private String ip;
    private String params;
    private int port;
    private String schema;
    private String stream;
    private String vhost;

    private String hook_index;
}
