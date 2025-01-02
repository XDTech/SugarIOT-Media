
package org.sugar.media.beans.hooks.zlm;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class OriginSock {

    private String identifier;

    @JsonProperty("local_ip")
    private String localIp;

    @JsonProperty("local_port")
    private int localPort;

    @JsonProperty("peer_ip")
    private String peerIp;

    @JsonProperty("peer_port")
    private int peerPort;

}