package org.sugar.media.beans.gb;

import lombok.Data;

import javax.sip.Dialog;

/**
 * Date:2024/12/23 15:06:44
 * Author：Tobin
 * Description:
 */

@Data
public class SsrcInfoBean {

    private String deviceCode;

    private String channelCode;

    private String deviceHost;

    private Integer devicePort;

    private String transport;

    private Long nodeId;

    private String callId;
    private String ssrc;
    private String fromAddr;
    private String fromTag;
    private String toAddr;
    private String toTag;

    private String streamId;

    private Dialog dialog;
}
