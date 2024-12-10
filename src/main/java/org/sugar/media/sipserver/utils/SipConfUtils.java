package org.sugar.media.sipserver.utils;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Date:2024/12/10 11:21:11
 * Author：Tobin
 * Description:
 */

@Data
@Component
@ConfigurationProperties(prefix = "sip")
public class SipConfUtils {

    private String domain;
    private int port;
    private String pwd;
    private String id;
    private String ip;
}
