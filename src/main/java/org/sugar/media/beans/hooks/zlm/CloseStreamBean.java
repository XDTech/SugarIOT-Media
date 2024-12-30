package org.sugar.media.beans.hooks.zlm;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Date:2024/12/29 17:15:44
 * Author：Tobin
 * Description:
 */

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CloseStreamBean {

    @JsonProperty("code")
    private Integer code;

    @JsonProperty("count_hit")
    private Integer countHit;

    @JsonProperty("count_closed")
    private Integer countClosed;
}
