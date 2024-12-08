package org.sugar.media.beans.hooks.zlm;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

/**
 * Date:2024/12/08 20:56:17
 * Author：Tobin
 * Description:
 */

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class FlowReportBean {

        private String app;
        private int duration;
        private int hook_index;
        private String id;
        private String ip;
        private String  mediaServerId;
        private String params;
        private boolean player;
        private int port;
        private String schema;
        private String stream;
        private Long totalBytes;
        private String vhost;


}
