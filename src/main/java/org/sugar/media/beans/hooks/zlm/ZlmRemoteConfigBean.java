package org.sugar.media.beans.hooks.zlm;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * Date:2024/11/17 10:53:12
 * Author：Tobin
 * Description:
 */

@Data
public class ZlmRemoteConfigBean {

    private Integer code;

    private List<Map<String,String>> data;
}
